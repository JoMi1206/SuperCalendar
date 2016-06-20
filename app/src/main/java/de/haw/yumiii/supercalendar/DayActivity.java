package de.haw.yumiii.supercalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.haw.yumiii.supercalendar.rest.api.RestAPI;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import de.haw.yumiii.supercalendar.rest.model.UserItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DayActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentDateSetListener {

    private Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);

    private ListView mListView;

    private final int ADD_TODO_REQUEST = 1;
    private final int UPDATE_TODO_REQUEST = 2;
    private final int ADD_IMAGE_REQUEST = 3;
    private final int UPDATE_IMAGE_REQUEST = 4;
    private final int CHOOSE_ADD_TYPE_REQUEST = 5;

    private List<TodoItem> mTodoItemListAll = new ArrayList<>();
    private List<ImageItem> mImageItemListAll = new ArrayList<>();

    private List<UserItem> mUserItemListCurrentDay = new ArrayList<>();

    private RestAPI restAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);


        setContentView(R.layout.activity_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(sdf.format(currentDate));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DayActivity.this, ChooseAddTypeActivity.class);
                startActivityForResult(intent, CHOOSE_ADD_TYPE_REQUEST);
                return;
            }
        });

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL_EMULATOR).addConverterFactory(GsonConverterFactory.create()).build();
        restAPI = retrofit.create(RestAPI.class);

        loadItems();

        mListView = (ListView) findViewById(R.id.todo_list_view);

        // Custom Adapter to show the items in a nice way
        TodoAdapter adapter = new TodoAdapter(this, new ArrayList<UserItem>());
        mListView.setAdapter(adapter);

        // Open the edit Activity to edit the selected Item
        final Context context = this;
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MyApp", "Item clicked: " + mUserItemListCurrentDay.get(position));

                if(mUserItemListCurrentDay.get(position) instanceof TodoItem) {
                    TodoItem selectedTodo = (TodoItem) mUserItemListCurrentDay.get(position);

                    Intent detailIntent = new Intent(context, AddTodoActivity.class);
                    detailIntent.putExtra(AddTodoActivity.PARAM_IS_MODE_ADD, false);

                    detailIntent.putExtra(AddTodoActivity.PARAM_ID, selectedTodo.get_id());
                    detailIntent.putExtra(AddTodoActivity.PARAM_NAME, selectedTodo.getName());
                    detailIntent.putExtra(AddTodoActivity.PARAM_DESCRIPTION, selectedTodo.getDescription());

                    detailIntent.putExtra(AddTodoActivity.PARAM_DATE, sdf.format(selectedTodo.getDate()));
                    detailIntent.putExtra(AddTodoActivity.PARAM_COMPLETED, selectedTodo.isCompleted());

                    startActivityForResult(detailIntent, UPDATE_TODO_REQUEST);
                } else if(mUserItemListCurrentDay.get(position) instanceof ImageItem) {
                    ImageItem selectedTodo = (ImageItem) mUserItemListCurrentDay.get(position);

                    Intent detailIntent = new Intent(context, AddImageActivity.class);
                    detailIntent.putExtra(AddImageActivity.PARAM_IS_MODE_ADD, false);

                    detailIntent.putExtra(AddImageActivity.PARAM_ID, selectedTodo.get_id());
                    detailIntent.putExtra(AddImageActivity.PARAM_NOTE, selectedTodo.getDescription());
                    detailIntent.putExtra(AddImageActivity.PARAM_DATE, sdf.format(selectedTodo.getDate()));
                    //TODO check how it is possible to get the image to the view (it is too large as String)
//                    detailIntent.putExtra(AddImageActivity.PARAM_DATA, selectedTodo.getImageData());

                    startActivityForResult(detailIntent, UPDATE_IMAGE_REQUEST);
                }
            }
        });
    }

    private void loadItems() {
        loadTodoItems();
        loadImageItems();
    }

    /**
     * Loads all To-Do-Items from the server and filters for the selected day.
     */
    private void loadTodoItems() {
        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> todoObjects, ParseException e) {
                if (e == null) {
                    // If there are results, update the list of posts
                    // and notify the adapter
                    mTodoItemListAll.clear();
                    for (ParseObject todo : todoObjects) {
                        TodoItem todoItem = new TodoItem(todo.getObjectId(), todo.getString("name"), todo.getBoolean("completed"), todo.getString("description"), todo.getDate("due_date"));
                        Log.d("MyApp", "Loaded Todo: " + todoItem);
                        mTodoItemListAll.add(todoItem);
                    }
                    filterDailyList();
                    setProgressBarIndeterminateVisibility(false);
                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
            }
        });
    }


    /**
     * Loads all Image-Items from the server and filters for the selected day.
     */
    private void loadImageItems() {
        Call<List<ImageItem>> call = restAPI.getImages();
        call.enqueue(new Callback<List<ImageItem>>() {
            @Override
            public void onResponse(Call<List<ImageItem>> call, Response<List<ImageItem>> response) {
                mImageItemListAll = response.body();
                filterDailyList();

                Toast.makeText(DayActivity.this, "Todos loaded", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<List<ImageItem>> call, Throwable t) {
                Log.d("MyApp", "onFailure called!");
                Log.d("MyApp", t.getLocalizedMessage());
                t.printStackTrace();

                Toast.makeText(DayActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_day, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reload) {
            loadItems();
            return true;
        }

        if (id == R.id.action_choosedate) {
            showDatePickerDialog(findViewById(android.R.id.content));
            return true;
        }

        if(id == R.id.action_logout) {
            ParseUser.logOut();

            // FLAG_ACTIVITY_CLEAR_TASK only works on API 11, so if the user
            // logs out on older devices, we'll just exit.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                Intent intent = new Intent(DayActivity.this, LoginDispatchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();

        Calendar cal = Calendar.getInstance();

        cal.setTime(currentDate);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        args.putInt(DatePickerFragment.ARG_YEAR, year);
        args.putInt(DatePickerFragment.ARG_MONTH, month);
        args.putInt(DatePickerFragment.ARG_DAY, day);

        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onFragmentDateSetListener(Date newDate) {
        currentDate = newDate;
        setTitle(sdf.format(currentDate));
        // show day specific items
        filterDailyList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TODO_REQUEST || requestCode == UPDATE_TODO_REQUEST) {
            if (resultCode == RESULT_OK) {
                loadTodoItems();
            }
            return;
        }

        if (requestCode == ADD_IMAGE_REQUEST || requestCode == UPDATE_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                loadImageItems();
            }
            return;
        }

        if(requestCode == CHOOSE_ADD_TYPE_REQUEST) {
            if(resultCode == RESULT_CANCELED) {
                return;
            } else if(resultCode == RESULT_OK) {

                if(data.getStringExtra(ChooseAddTypeActivity.PARA_TYPE).equals(ChooseAddTypeActivity.ADD_TODO)) {
                    Intent intent = new Intent(DayActivity.this, AddTodoActivity.class);
                    intent.putExtra(AddTodoActivity.PARAM_IS_MODE_ADD, true);
                    intent.putExtra(AddTodoActivity.PARAM_DATE, sdf.format(currentDate));
                    startActivityForResult(intent, ADD_TODO_REQUEST);
                    return;
                }

                if(data.getStringExtra(ChooseAddTypeActivity.PARA_TYPE).equals(ChooseAddTypeActivity.ADD_IMAGE)) {
                    Intent intent = new Intent(DayActivity.this, AddImageActivity.class);
                    intent.putExtra(AddImageActivity.PARAM_IS_MODE_ADD, true);
                    intent.putExtra(AddImageActivity.PARAM_DATE, sdf.format(currentDate));
                    startActivityForResult(intent, ADD_IMAGE_REQUEST);
                    return;
                }
            }
        }
    }


    private void filterDailyList() {
        TodoAdapter adapter = (TodoAdapter) mListView.getAdapter();
        adapter.clear();
        mUserItemListCurrentDay.clear();

        DateTimeComparator dtc = DateTimeComparator.getDateOnlyInstance();

        for(TodoItem item: mTodoItemListAll) {
            if(dtc.compare(item.getDate(), currentDate) == 0) {
                mUserItemListCurrentDay.add(item);
            }
        }

        for(ImageItem item: mImageItemListAll) {
            if(dtc.compare(item.getDate(), currentDate) == 0) {
                mUserItemListCurrentDay.add(item);
            }
        }

        adapter.addAll(mUserItemListCurrentDay);
    }

}
