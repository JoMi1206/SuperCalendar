package de.haw.yumiii.supercalendar.overview;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.haw.yumiii.supercalendar.R;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import de.haw.yumiii.supercalendar.rest.model.UserItem;

public class MonthActivity extends AppCompatActivity {

    public static final String PARAM_MONTH = "month";
    public static final String PARAM_YEAR = "year";

    private ListView mListView;

    private int mMonth;
    private int mYear;

    private ArrayList<Object> mItemList = new ArrayList<>();

    private List<TodoItem> mTodoItemListAll = new ArrayList<>();
    private List<ImageItem> mImageItemListAll = new ArrayList<>();

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_month);


        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mListView = (ListView) findViewById(R.id.month_list_view);
        // Custom Adapter to show the items in a nice way
        UserItemAdapter adapter = new UserItemAdapter(this, new ArrayList<Object>());
        mListView.setAdapter(adapter);

        mMonth = this.getIntent().getExtras().getInt(PARAM_MONTH);
        mYear = this.getIntent().getExtras().getInt(PARAM_YEAR);

        DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM");
        setTitle(dtf.print(new LocalDate(mYear, mMonth, 1)));
        Log.d("MyApp", "Month: " + mMonth + " Year: " + mYear);

        loadItemList();

    }

    private void loadItemList() {
        // show a progress dialog
        mProgressDialog.setTitle(R.string.progress_title_loading);
        mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_loading));
        mProgressDialog.show();
        // First load todos, this will call loadImages after it is finish
        loadTodoItems();
    }

    /**
     * Loads all To-Do-Items for the current month from the server
     * and stores them in <i>mTodoItemListAll</i>.
     */
    private void loadTodoItems() {
        // get first and last day of year
        LocalDate firstDay = new LocalDate(mYear, mMonth, 1);
        LocalDate lastDay = new LocalDate(mYear, mMonth, firstDay.dayOfMonth().getMaximumValue());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Todo");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        //TODO Einschränkung
        query.whereGreaterThanOrEqualTo("due_date", firstDay.toDate());
        query.whereLessThanOrEqualTo("due_date", lastDay.toDate());
//        Log.d("MyApp", "Todo greater date than: " + new SimpleDateFormat(Settings.DATE_FORMAT).format(firstDay.toDate()));
//        Log.d("MyApp", "Todo less date than: " + new SimpleDateFormat(Settings.DATE_FORMAT).format(lastDay.toDate()));

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> todoObjects, ParseException e) {
                if (e == null) {
                    // If there are results, update the list of todos
                    mTodoItemListAll.clear();
                    for (ParseObject todo : todoObjects) {
                        TodoItem todoItem = new TodoItem(todo.getObjectId(), todo.getString("name"), todo.getBoolean("completed"), todo.getString("description"), todo.getDate("due_date"));
                        mTodoItemListAll.add(todoItem);
                    }

                    // After the items are fetched: search for images
                    loadImageItems();
                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Loads all Image-Items for the current month from the server
     * and stores them in <i>mImageItemListAll</i>.
     */
    private void loadImageItems() {
        // get first and last day of year
        LocalDate firstDay = new LocalDate(mYear, mMonth, 1);
        LocalDate lastDay = new LocalDate(mYear, mMonth, firstDay.dayOfMonth().getMaximumValue());

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        query.whereEqualTo("owner", ParseUser.getCurrentUser());
        query.whereGreaterThanOrEqualTo("date", firstDay.toDate());
        query.whereLessThanOrEqualTo("date", lastDay.toDate());

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

                    // finally: sort the list
                    buildAdapterList();
                    mProgressDialog.dismiss();
                } else {
                    Log.d(getClass().getSimpleName(), "Error: " + e.getMessage());
                }
            }
        });
    }

    private void buildAdapterList() {
        mItemList.clear();

        // this maps hold per dayOfMonth the UserItems
        HashMap<Integer, ArrayList<UserItem>> itemsPerDay = new HashMap<>();

        // init empty Map
        LocalDate date = new LocalDate(mYear, mMonth, 1);
        for(int i=1; i <= date.dayOfMonth().getMaximumValue(); i++) {
            itemsPerDay.put(i, new ArrayList<UserItem>());
        }

        for(TodoItem todoItem: mTodoItemListAll) {
            itemsPerDay.get(todoItem.getDate().getDayOfMonth()).add(todoItem);
        }

        for(ImageItem imageItem: mImageItemListAll) {
            itemsPerDay.get(imageItem.getDate().getDayOfMonth()).add(imageItem);
        }

        // Build monthList
        DateTimeFormatter dtf = DateTimeFormat.forPattern("EEEE', 'dd.' 'MMMM");
        for(int i=1; i <= date.dayOfMonth().getMaximumValue(); i++) {
            if(itemsPerDay.get(i).isEmpty()) {
                continue;
            }

            LocalDate day = new LocalDate(mYear, mMonth, i);
            mItemList.add(new SectionItem(dtf.print(day)));
            mItemList.addAll(itemsPerDay.get(i));
        }

        UserItemAdapter adapter = (UserItemAdapter) mListView.getAdapter();
        adapter.addAll(mItemList);

    }

}
