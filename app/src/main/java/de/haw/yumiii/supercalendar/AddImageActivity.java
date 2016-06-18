package de.haw.yumiii.supercalendar;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.haw.yumiii.supercalendar.rest.api.RestAPI;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddImageActivity extends AppCompatActivity implements Callback<ImageItem>, DatePickerFragment.OnFragmentDateSetListener {

    public static final String PARAM_IS_MODE_ADD = "mode_add";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NOTE = "note";
    public static final String PARAM_DATE = "date";

    private EditText mDescriptionNote;
    private Button mDateButton;
    private Button mChooseImageButton;

    private Date date = null;

    private Mode mode = Mode.ADD;

    private String imageId = "-1";

    public enum Mode {ADD, UPDATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mDateButton = (Button) findViewById(R.id.date_button);
        mChooseImageButton = (Button) findViewById(R.id.select_image_button);

        boolean mode_add = this.getIntent().getExtras().getBoolean(PARAM_IS_MODE_ADD);
        if(mode_add) {
            mode = Mode.ADD;
        } else {
            mode = Mode.UPDATE;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
        String dateStr = this.getIntent().getExtras().getString(PARAM_DATE);
        if(dateStr != null) {
            try {
                date = sdf.parse(dateStr);
                mDateButton.setText(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if(mode == Mode.UPDATE) {
            imageId = this.getIntent().getExtras().getString(PARAM_ID);
            String note = this.getIntent().getExtras().getString(PARAM_NOTE);

            mDescriptionNote.setText(note);
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

        String description = mDescriptionNote.getText().toString();
        ImageItem item = new ImageItem(null, description, date);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL).addConverterFactory(GsonConverterFactory.create()).build();
        RestAPI restAPI = retrofit.create(RestAPI.class);

        if(mode == Mode.ADD) {
            //TODO change to image
            Call<ImageItem> call = restAPI.postImage(item);
            call.enqueue(this);
        } else {
            //TODO change to image
            Call<ImageItem> call = restAPI.putImage(imageId,item);
            call.enqueue(this);
        }
    }

    @Override
    public void onResponse(Call<ImageItem> call, Response<ImageItem> response) {
        if(mode == Mode.ADD) {
            Toast.makeText(AddImageActivity.this, R.string.toast_todo_added, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(AddImageActivity.this, R.string.toast_todo_updated, Toast.LENGTH_SHORT).show();
        }

        Intent result = new Intent();
        setResult(RESULT_OK, result);

        finish();
    }

    @Override
    public void onFailure(Call<ImageItem> call, Throwable t) {
        Toast.makeText(AddImageActivity.this, t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();

        Calendar cal = Calendar.getInstance();

        if(date != null) {
            cal.setTime(date);
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
        date = newDate;

        SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
        Log.d("MyApp", "onFragmentDateSetListener called, Date: " + sdf.format(date));
        mDateButton.setText(sdf.format(date));
    }

}
