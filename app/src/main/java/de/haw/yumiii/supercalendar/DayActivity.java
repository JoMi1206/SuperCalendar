package de.haw.yumiii.supercalendar;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTimeComparator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.haw.yumiii.supercalendar.rest.api.RestAPI;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DayActivity extends AppCompatActivity implements Callback<List<TodoItem>>, DatePickerFragment.OnFragmentDateSetListener {

    private Date currentDate = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);

    private ListView mListView;

    private final int ADD_TASK_REQUEST = 1;
    private final int UPDATE_TASK_REQUEST = 2;
    private final int CHOOSE_ADD_TYPE_REQUEST = 3;

    private List<TodoItem> mTodoItemListAll = new ArrayList<>();
    private List<TodoItem> mTodoItemListCurrentDay = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);

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

        loadTodoItems();

        mListView = (ListView) findViewById(R.id.todo_list_view);

        // Custom Adapter to show the items in a nice way
        TodoAdapter adapter = new TodoAdapter(this, new ArrayList<TodoItem>());
        mListView.setAdapter(adapter);

        // Open the edit Activity to edit the selected Item
        final Context context = this;
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MyApp", "Item clicked: " + mTodoItemListCurrentDay.get(position));

                TodoItem selectedTodo = mTodoItemListCurrentDay.get(position);

                Intent detailIntent = new Intent(context, AddTodoActivity.class);
                detailIntent.putExtra(AddTodoActivity.PARAM_IS_MODE_ADD, false);

                detailIntent.putExtra(AddTodoActivity.PARAM_ID, selectedTodo.get_id());
                detailIntent.putExtra(AddTodoActivity.PARAM_NAME, selectedTodo.getName());
                detailIntent.putExtra(AddTodoActivity.PARAM_NOTE, selectedTodo.getNote());

                detailIntent.putExtra(AddTodoActivity.PARAM_DATE, sdf.format(selectedTodo.getDate()));
                detailIntent.putExtra(AddTodoActivity.PARAM_COMPLETED, selectedTodo.isCompleted());

                startActivityForResult(detailIntent, UPDATE_TASK_REQUEST);
            }
        });
    }

    private void loadTodoItems() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

        RestAPI restAPI = retrofit.create(RestAPI.class);

        Call<List<TodoItem>> call = restAPI.getTodos();
        //TODO filter by date
        call.enqueue(this);
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
            loadTodoItems();
            return true;
        }

        if (id == R.id.action_choosedate) {
            showDatePickerDialog(findViewById(android.R.id.content));
            return true;
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
        filterTodoList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_TASK_REQUEST || requestCode == UPDATE_TASK_REQUEST) {
            if (resultCode == RESULT_OK) {
                loadTodoItems();
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
                    startActivityForResult(intent, ADD_TASK_REQUEST);
                    return;
                }

                if(data.getStringExtra(ChooseAddTypeActivity.PARA_TYPE).equals(ChooseAddTypeActivity.ADD_IMAGE)) {
                    Intent intent = new Intent(DayActivity.this, AddImageActivity.class);
                    intent.putExtra(AddImageActivity.PARAM_IS_MODE_ADD, true);
                    intent.putExtra(AddImageActivity.PARAM_DATE, sdf.format(currentDate));
                    startActivityForResult(intent, ADD_TASK_REQUEST);
                    return;
                }
            }
        }
    }

    @Override
    public void onResponse(Call<List<TodoItem>> call, Response<List<TodoItem>> response) {
        Log.d("MyApp", "onResponse called!");

        mTodoItemListAll = response.body();
        filterTodoList();

        Toast.makeText(DayActivity.this, "Todos loaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Call<List<TodoItem>> call, Throwable t) {
        Log.d("MyApp", "onFailure called!");
        Log.d("MyApp", t.getLocalizedMessage());
        t.printStackTrace();

        Toast.makeText(DayActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    private void filterTodoList() {
        TodoAdapter adapter = (TodoAdapter) mListView.getAdapter();
        adapter.clear();
        mTodoItemListCurrentDay.clear();
        //TODO filter List for current day

        DateTimeComparator dtc = DateTimeComparator.getDateOnlyInstance();

        for(TodoItem item: mTodoItemListAll) {
            if(dtc.compare(item.getDate(), currentDate) == 0) {
                mTodoItemListCurrentDay.add(item);
            }
        }

        adapter.addAll(mTodoItemListCurrentDay);
    }

}
