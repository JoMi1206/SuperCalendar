package de.haw.yumiii.supercalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.haw.yumiii.supercalendar.rest.api.TodoAPI;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddTodoActivity extends AppCompatActivity implements Callback<TodoItem>, DatePickerFragment.OnFragmentDateSetListener {

    public static final String PARAM_IS_MODE_ADD = "mode_add";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_COMPLETED = "completed";

    private EditText mNameEditText;
    private EditText mDescriptionNote;
    private Button mDateButton;
    private CheckBox mCompletedCheckBox;

    private Date dueDate = null;

    private Mode mode = Mode.ADD;

    private String todoId = "-1";

    public enum Mode {ADD, UPDATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_todo);



        mNameEditText = (EditText) findViewById(R.id.name_edit_text);
        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mDateButton = (Button) findViewById(R.id.date_button);
        mCompletedCheckBox = (CheckBox) findViewById(R.id.completed_checkbox);

        boolean mode_add = this.getIntent().getExtras().getBoolean(PARAM_IS_MODE_ADD);
        if(mode_add) {
            mode = Mode.ADD;
//            mNameEditText.setEnabled(true);
//            mDescriptionNote.setEnabled(true);
        } else {
            mode = Mode.UPDATE;
//            mNameEditText.setEnabled(false);
//            mDescriptionNote.setEnabled(false);
        }

        if(mode == Mode.UPDATE) {
            todoId = this.getIntent().getExtras().getString(PARAM_ID);
            String name = this.getIntent().getExtras().getString(PARAM_NAME);
            String note = this.getIntent().getExtras().getString(PARAM_NOTE);
            String date = this.getIntent().getExtras().getString(PARAM_DATE);
            SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
            try {
                dueDate = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            boolean completed = this.getIntent().getExtras().getBoolean(PARAM_COMPLETED);

            mNameEditText.setText(name);
            mDescriptionNote.setText(note);
            mDateButton.setText(date);
            mCompletedCheckBox.setChecked(completed);
        }

        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveItem();
            }
        });

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
    }

    private void saveItem() {

        if(mode == Mode.ADD) {
            String name = mNameEditText.getText().toString();
            String description = mDescriptionNote.getText().toString();
            Boolean completed = mCompletedCheckBox.isChecked();

            TodoItem item = new TodoItem(name, completed, description, dueDate);

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

            TodoAPI todoAPI = retrofit.create(TodoAPI.class);

            Call<TodoItem> call = todoAPI.postTodo(item);

            call.enqueue(this);
        } else {

            String name = mNameEditText.getText().toString();
            String description = mDescriptionNote.getText().toString();
            Boolean completed = mCompletedCheckBox.isChecked();

            TodoItem item = new TodoItem(name, completed, description, dueDate);

            Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();

            TodoAPI todoAPI = retrofit.create(TodoAPI.class);

            Call<TodoItem> call = todoAPI.putTodo(todoId,item);

            call.enqueue(this);
        }

    }

    @Override
    public void onResponse(Call<TodoItem> call, Response<TodoItem> response) {
        if(mode == Mode.ADD) {
            Toast.makeText(AddTodoActivity.this, R.string.toast_todo_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddTodoActivity.this, R.string.toast_todo_updated, Toast.LENGTH_SHORT).show();
        }

        Intent result = new Intent();
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public void onFailure(Call<TodoItem> call, Throwable t) {
        Toast.makeText(AddTodoActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();

        Calendar cal = Calendar.getInstance();

        if(dueDate != null) {
            cal.setTime(dueDate);
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        args.putInt(DatePickerFragment.ARG_YEAR, year);
        args.putInt(DatePickerFragment.ARG_MONTH, month);
        args.putInt(DatePickerFragment.ARG_DAY, day);

        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void onFragmentDateSetListener(Date newDate) {
        dueDate = newDate;

        SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
        Log.d("MyApp", "onFragmentDateSetListener called, Date: " + sdf.format(dueDate));
        mDateButton.setText(sdf.format(dueDate));
    }

}
