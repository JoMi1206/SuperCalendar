package de.haw.yumiii.supercalendar.dayoverview;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.joda.time.DateTimeComparator;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.haw.yumiii.supercalendar.LoginDispatchActivity;
import de.haw.yumiii.supercalendar.R;
import de.haw.yumiii.supercalendar.addupdate.ImageActivity;
import de.haw.yumiii.supercalendar.addupdate.TodoActivity;
import de.haw.yumiii.supercalendar.monthoverview.MonthActivity;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import de.haw.yumiii.supercalendar.utils.DatePickerFragment;
import de.haw.yumiii.supercalendar.utils.Settings;

public class DayActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentDateSetListener {

    private LocalDate mCurrentDate = new LocalDate();
    DateTimeFormatter sdf = DateTimeFormat.forPattern(Settings.DATE_FORMAT);

    private ListView mListView;

    private final int ADD_TODO_REQUEST = 1;
    private final int UPDATE_TODO_REQUEST = 2;
    private final int ADD_IMAGE_REQUEST = 3;
    private final int UPDATE_IMAGE_REQUEST = 4;
    private final int CHOOSE_ADD_TYPE_REQUEST = 5;
    private final int SHOW_MONTH_REQUEST = 6;

    private List<TodoItem> mTodoItemListAll = new ArrayList<>();
    private List<ImageItem> mImageItemListAll = new ArrayList<>();

    private List<Object> mUserItemListCurrentDay = new ArrayList<>();

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

        setTitle(sdf.print(mCurrentDate));

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

        mListView = (ListView) findViewById(R.id.day_list_view);

        // Custom Adapter to show the items in a nice way
        UserItemAdapter adapter = new UserItemAdapter(this, new ArrayList<Object>());
        mListView.setAdapter(adapter);

        // Open the edit Activity to edit the selected Item
        final Context context = this;
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // open TodoItem
                if(mUserItemListCurrentDay.get(position) instanceof TodoItem) {
                    TodoItem selectedTodo = (TodoItem) mUserItemListCurrentDay.get(position);

                    Intent detailIntent = new Intent(context, TodoActivity.class);
                    detailIntent.putExtra(TodoActivity.PARAM_IS_MODE_ADD, false);

                    detailIntent.putExtra(TodoActivity.PARAM_ID, selectedTodo.get_id());
                    detailIntent.putExtra(TodoActivity.PARAM_NAME, selectedTodo.getName());
                    detailIntent.putExtra(TodoActivity.PARAM_DESCRIPTION, selectedTodo.getDescription());

                    detailIntent.putExtra(TodoActivity.PARAM_DATE, sdf.print(selectedTodo.getDate()));
                    detailIntent.putExtra(TodoActivity.PARAM_COMPLETED, selectedTodo.isCompleted());

                    startActivityForResult(detailIntent, UPDATE_TODO_REQUEST);
                } else if(mUserItemListCurrentDay.get(position) instanceof ImageItem) {
                    // open ImageItem
                    ImageItem selectedImage = (ImageItem) mUserItemListCurrentDay.get(position);

                    Intent detailIntent = new Intent(context, ImageActivity.class);
                    detailIntent.putExtra(ImageActivity.PARAM_IS_MODE_ADD, false);

                    detailIntent.putExtra(ImageActivity.PARAM_ID, selectedImage.get_id());
                    detailIntent.putExtra(ImageActivity.PARAM_NOTE, selectedImage.getDescription());
                    detailIntent.putExtra(ImageActivity.PARAM_DATE, sdf.print(selectedImage.getDate()));

                    startActivityForResult(detailIntent, UPDATE_IMAGE_REQUEST);
                }
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // open TodoItem
                if(mUserItemListCurrentDay.get(position) instanceof TodoItem) {
                    TodoItem selectedTodo = (TodoItem) mUserItemListCurrentDay.get(position);
                    removeTodoItem(selectedTodo);
                } else if(mUserItemListCurrentDay.get(position) instanceof ImageItem) {
                    // open ImageItem
                    ImageItem selectedImage = (ImageItem) mUserItemListCurrentDay.get(position);
                    removeImageItem(selectedImage);
                }
                return true;
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
                        ImageItem imageItem = new ImageItem(image.getObjectId(), image.getParseFile("image"),
                                                            image.getInt("imageWidth"), image.getInt("imageHeight"),
                                                            image.getString("description"), image.getDate("date"));
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
        if (id == R.id.action_showmonth) {
            Intent monthIntent = new Intent(this, MonthActivity.class);
            Log.d("MyApp", "Call Intent with Month: " + mCurrentDate.getMonthOfYear() + " Year: " + mCurrentDate.getYear());
            monthIntent.putExtra(MonthActivity.PARAM_MONTH, mCurrentDate.getMonthOfYear());
            monthIntent.putExtra(MonthActivity.PARAM_YEAR, mCurrentDate.getYear());

            startActivityForResult(monthIntent, SHOW_MONTH_REQUEST);
            return true;
        }

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

        args.putInt(DatePickerFragment.ARG_YEAR, mCurrentDate.getYear());
        args.putInt(DatePickerFragment.ARG_MONTH, mCurrentDate.getMonthOfYear());
        args.putInt(DatePickerFragment.ARG_DAY, mCurrentDate.getDayOfMonth());

        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onFragmentDateSetListener(Date newDate) {
        mCurrentDate = new LocalDate(newDate);
        setTitle(sdf.print(mCurrentDate));
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
                    Intent intent = new Intent(DayActivity.this, TodoActivity.class);
                    intent.putExtra(TodoActivity.PARAM_IS_MODE_ADD, true);
                    intent.putExtra(TodoActivity.PARAM_DATE, sdf.print(mCurrentDate));
                    startActivityForResult(intent, ADD_TODO_REQUEST);
                    return;
                }

                if(data.getStringExtra(ChooseAddTypeActivity.PARA_TYPE).equals(ChooseAddTypeActivity.ADD_IMAGE)) {
                    Intent intent = new Intent(DayActivity.this, ImageActivity.class);
                    intent.putExtra(ImageActivity.PARAM_IS_MODE_ADD, true);
                    intent.putExtra(ImageActivity.PARAM_DATE, sdf.print(mCurrentDate));
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
        UserItemAdapter adapter = (UserItemAdapter) mListView.getAdapter();
        adapter.clear();
        mUserItemListCurrentDay.clear();

        DateTimeComparator dtc = DateTimeComparator.getDateOnlyInstance();

        for(TodoItem item: mTodoItemListAll) {
            if(item.getDate().equals(mCurrentDate)) {
                mUserItemListCurrentDay.add(item);
            }
        }

        for(ImageItem item: mImageItemListAll) {
            if(item.getDate().equals(mCurrentDate)) {
                mUserItemListCurrentDay.add(item);
            }
        }

        adapter.addAll(mUserItemListCurrentDay);
    }

    private void removeTodoItem(final TodoItem item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.dialog_delete_title);
        alertDialogBuilder.setMessage(R.string.dialog_delete_todo);
        alertDialogBuilder.setPositiveButton(R.string.dialog_delete_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show a progress dialog
                mProgressDialog.setTitle(R.string.progress_title_delete);
                mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_delete));
                mProgressDialog.show();

                ParseObject.createWithoutData("Todo", item.get_id()).deleteEventually(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        mProgressDialog.dismiss();
                        loadItems();
                    }
                });
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.dialog_delete_negative_button, null);
        alertDialogBuilder.setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void removeImageItem(final ImageItem item) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.dialog_delete_title);
        alertDialogBuilder.setMessage(R.string.dialog_delete_image);
        alertDialogBuilder.setPositiveButton(R.string.dialog_delete_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // show a progress dialog
                mProgressDialog.setTitle(R.string.progress_title_delete);
                mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_delete));
                mProgressDialog.show();

                ParseObject.createWithoutData("Image", item.get_id()).deleteEventually(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        mProgressDialog.dismiss();
                        loadItems();
                    }
                });
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.dialog_delete_negative_button, null);
        alertDialogBuilder.setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
