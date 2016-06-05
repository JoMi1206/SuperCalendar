package de.haw.yumiii.supercalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import de.haw.yumiii.supercalendar.rest.api.TodoAPI;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddTodoActivity extends AppCompatActivity implements Callback<TodoItem> {

    private EditText mNameEditText;
    private EditText mDescriptionNote;
    private CheckBox mCompletedCheckBox;

    private Mode mode = Mode.ADD;

    private String todoId = "-1";

    public enum Mode {ADD, UPDATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);



        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mCompletedCheckBox = (CheckBox) findViewById(R.id.completed_checkbox);

        boolean mode_add = this.getIntent().getExtras().getBoolean("Mode_Add");
        if(mode_add) {
            mode = Mode.ADD;
            mNameEditText.setEnabled(true);
            mDescriptionNote.setEnabled(true);
        } else {
            mode = Mode.UPDATE;
            mNameEditText.setEnabled(false);
            mDescriptionNote.setEnabled(false);
        }

        if(mode == Mode.UPDATE) {
            todoId = this.getIntent().getExtras().getString("id");
            String name = this.getIntent().getExtras().getString("name");
            String note = this.getIntent().getExtras().getString("note");
            boolean completed = this.getIntent().getExtras().getBoolean("completed");

            mNameEditText.setText(name);
            mDescriptionNote.setText(note);
            mCompletedCheckBox.setChecked(completed);
        }

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });
    }

    private void saveItem() {

        if(mode == Mode.ADD) {
            String name = mNameEditText.getText().toString();
            String description = mDescriptionNote.getText().toString();
            Boolean completed = mCompletedCheckBox.isChecked();

            TodoItem item = new TodoItem(name, completed, description);

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

            TodoAPI todoAPI = retrofit.create(TodoAPI.class);

            Call<TodoItem> call = todoAPI.postTodo(item);

            setProgressBarIndeterminateVisibility(true);
            setProgressBarVisibility(true);
            call.enqueue(this);
        } else {
            //TODO: Just update the Item with the saved id!!!

            String name = mNameEditText.getText().toString();
            String description = mDescriptionNote.getText().toString();
            Boolean completed = mCompletedCheckBox.isChecked();

            TodoItem item = new TodoItem(name, completed, description);

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

            TodoAPI todoAPI = retrofit.create(TodoAPI.class);

            Call<TodoItem> call = todoAPI.postTodo(item);

            setProgressBarIndeterminateVisibility(true);
            setProgressBarVisibility(true);
            call.enqueue(this);
        }

    }

    @Override
    public void onResponse(Call<TodoItem> call, Response<TodoItem> response) {
        setProgressBarIndeterminateVisibility(false);
        setProgressBarVisibility(false);
        Toast.makeText(AddTodoActivity.this, "Todo added", Toast.LENGTH_SHORT).show();

        Intent result = new Intent();
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public void onFailure(Call<TodoItem> call, Throwable t) {
        setProgressBarIndeterminateVisibility(false);
        setProgressBarVisibility(false);
        Toast.makeText(AddTodoActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }
}
