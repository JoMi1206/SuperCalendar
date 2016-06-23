package de.haw.yumiii.supercalendar.dayoverview;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.joda.time.DateTimeComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.haw.yumiii.supercalendar.addupdate.AddImageActivity;
import de.haw.yumiii.supercalendar.addupdate.AddTodoActivity;
import de.haw.yumiii.supercalendar.utils.DatePickerFragment;
import de.haw.yumiii.supercalendar.LoginDispatchActivity;
import de.haw.yumiii.supercalendar.R;
import de.haw.yumiii.supercalendar.utils.Settings;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import de.haw.yumiii.supercalendar.rest.model.UserItem;

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

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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

                    startActivityForResult(detailIntent, UPDATE_IMAGE_REQUEST);
                }
            }
        });
    }

    private void loadItems() {
        // show a progress dialog
        mProgressDialog.setTitle(R.string.progress_title_loading);
        mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_loading));
        mProgressDialog.show();

        loadTodoItems();
        loadImageItems();
    }

    /**
     * Loads all To-Do-Items from the server and stores them in <i>mTodoItemListAll</i>.
     */
    private void loadTodoItems() {
        setProgressBarIndeterminateVisibility(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> todoObjects, ParseException e) {
                if (e == null) {
                    mProgressDialog.dismiss();
                    // If there are results, update the list of todos
                    mTodoItemListAll.clear();
                    for (ParseObject todo : todoObjects) {
                        TodoItem todoItem = new TodoItem(todo.getObjectId(), todo.getString("name"), todo.getBoolean("completed"), todo.getString("description"), todo.getDate("due_date"));
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
     * Loads all Image-Items from the server and stores them in <i>mImageItemListAll</i>.
     */
    private void loadImageItems() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> imageObject, ParseException e) {
                mProgressDialog.dismiss();
                if (e == null) {
                    // If there are results, update the list of images
                    mImageItemListAll.clear();
                    for (ParseObject image : imageObject) {
                        ImageItem imageItem = new ImageItem(image.getObjectId(), image.getParseFile("image"), image.getString("description"), image.getDate("date"));
                        mImageItemListAll.add(imageItem);
                    }
                    filterDailyList();
                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
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

    /**
     * Shows the datepickerdialog with which the user can determine the current day.
     * @param v
     */
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


    /**
     * Adds all todoItems and ImageItems which has the same date as the user selected date
     * to the daily list and adds this list to the adapter.
     */
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
