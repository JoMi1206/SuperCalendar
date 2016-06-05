package de.haw.yumiii.supercalendar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.haw.yumiii.supercalendar.rest.api.TodoAPI;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DayActivity extends AppCompatActivity implements Callback<List<TodoItem>> {

    private ListView mListView;

    private final int ADD_TASK_REQUEST = 1;

    private List<TodoItem> todoItemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DayActivity.this, AddTodoActivity.class);

                intent.putExtra("Mode_Add", true);

                startActivityForResult(intent, ADD_TASK_REQUEST);
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
                Log.d("MyApp", "Item clicked: " + todoItemList.get(position));

                TodoItem selectedTodo = todoItemList.get(position);

                Intent detailIntent = new Intent(context, AddTodoActivity.class);
                detailIntent.putExtra("Mode_Add", false);

                detailIntent.putExtra("id", selectedTodo.get_id());
                detailIntent.putExtra("name", selectedTodo.getName());
                detailIntent.putExtra("note", selectedTodo.getNote());
                detailIntent.putExtra("completed", selectedTodo.isCompleted());

                startActivity(detailIntent);
            }
        });
    }

    private void loadTodoItems() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

        TodoAPI todoAPI = retrofit.create(TodoAPI.class);

        Call<List<TodoItem>> call = todoAPI.receiveTodos();
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ADD_TASK_REQUEST) {
            if(resultCode == RESULT_OK) {
                loadTodoItems();
            }
        }
    }

    @Override
    public void onResponse(Call<List<TodoItem>> call, Response<List<TodoItem>> response) {
        Log.d("MyAPP", "onResponse called!");

        TodoAdapter adapter = (TodoAdapter) mListView.getAdapter();
        adapter.clear();
        todoItemList = response.body();
        adapter.addAll(todoItemList);

        Toast.makeText(DayActivity.this, "Todos loaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(Call<List<TodoItem>> call, Throwable t) {
        Log.d("MyApp", "onFailure called!");
        Log.d("MyApp", t.getLocalizedMessage());
        t.printStackTrace();

        Toast.makeText(DayActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    }
}
