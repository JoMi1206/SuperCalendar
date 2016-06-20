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

import com.parse.GetCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddTodoActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentDateSetListener {

    public static final String PARAM_IS_MODE_ADD = "mode_add";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NAME = "name";
    public static final String PARAM_DESCRIPTION = "description";
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


        SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
        String date = this.getIntent().getExtras().getString(PARAM_DATE);
        if(date != null) {
            try {
                dueDate = sdf.parse(date);
                mDateButton.setText(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        boolean mode_add = this.getIntent().getExtras().getBoolean(PARAM_IS_MODE_ADD);
        if(mode_add) {
            mode = Mode.ADD;
        } else {
            mode = Mode.UPDATE;

            todoId = this.getIntent().getExtras().getString(PARAM_ID);
            String name = this.getIntent().getExtras().getString(PARAM_NAME);
            String note = this.getIntent().getExtras().getString(PARAM_DESCRIPTION);
            boolean completed = this.getIntent().getExtras().getBoolean(PARAM_COMPLETED);

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

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
    }

    private void saveItem() {

        final String name = mNameEditText.getText().toString();
        final String description = mDescriptionNote.getText().toString();
        final Boolean completed = mCompletedCheckBox.isChecked();

        if(mode == Mode.ADD) {
            addTodo(name, description, completed);
        } else {
            updateTodo(name, description, completed);
        }
    }

    private void addTodo(String name, String description, Boolean completed) {
        // With Parse
        final ParseObject newTodo = new ParseObject("Todo");

        newTodo.put("name", name);
        newTodo.put("description", description);
        newTodo.put("completed", completed);
        newTodo.put("due_date", dueDate);
        newTodo.put("owner", ParseUser.getCurrentUser());
        newTodo.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {

                if (e == null) {
                    if (mode == Mode.ADD) {
                        Toast.makeText(AddTodoActivity.this, R.string.toast_todo_added, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AddTodoActivity.this, R.string.toast_todo_updated, Toast.LENGTH_SHORT).show();
                    }

                    Intent result = new Intent();
                    setResult(RESULT_OK, result);

                    finish();
                } else {
                    // The save failed.
                    Toast.makeText(getApplicationContext(), "Failed to Save", Toast.LENGTH_SHORT).show();
                    Log.d("MyApp", "User update error: " + e);
                }
            }
        });
    }

    private void updateTodo(final String name, final String description, final Boolean completed) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");

        // Retrieve the object by id
        query.getInBackground(todoId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject todo, com.parse.ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data.
                    todo.put("name", name);
                    todo.put("description", description);
                    todo.put("completed", completed);
                    todo.put("due_date", dueDate);
                    todo.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                // Saved successfully.
                                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                                Intent result = new Intent();
                                setResult(RESULT_OK, result);

                                finish();
                            } else {
                                // The save failed.
                                Toast.makeText(getApplicationContext(), "Failed to Save", Toast.LENGTH_SHORT).show();
                                Log.d("MyApp", "User update error: " + e);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to find the current Todo-Item", Toast.LENGTH_SHORT).show();
                }
            }

        });
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
