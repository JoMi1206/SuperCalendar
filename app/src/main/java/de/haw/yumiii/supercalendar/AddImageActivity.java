package de.haw.yumiii.supercalendar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.haw.yumiii.supercalendar.rest.api.RestAPI;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.utils.Utility;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddImageActivity extends AppCompatActivity implements Callback<ImageItem>, DatePickerFragment.OnFragmentDateSetListener {

    public static final String PARAM_IS_MODE_ADD = "mode_add";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NOTE = "description";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_DATA = "data";

    public static final int REQUEST_CAMERA = 1;
    public static final int SELECT_FILE = 2;

    //TODO add to strings.xml
    final String PARAM_CAMERA = "Take Photo";
    final String PARAM_LIBRARY = "Choose from Library";
    final String PARAM_CANCEL = "Cancel";

    private String userChoosenTask;

    private EditText mDescriptionNote;
    private Button mDateButton;
    private Button mChooseImageButton;
    private ImageView mImageView;

    private Date date = null;
    private Bitmap image = null;

    private Mode mode = Mode.ADD;

    private String imageId = "-1";

    public enum Mode {ADD, UPDATE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_image);

        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mDateButton = (Button) findViewById(R.id.date_button);
        mImageView = (ImageView) findViewById(R.id.add_imageview);
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

            String data = this.getIntent().getExtras().getString(PARAM_DATA);
            if(data != null) {
                mImageView.setImageBitmap(ImageItem.getBitmapFromByteArray(Base64.decode(data, Base64.DEFAULT)));
            }
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

        mChooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
    }


    private void saveItem() {

        String description = mDescriptionNote.getText().toString();
        ImageItem item = new ImageItem(image, description, date);
        Retrofit retrofit = new Retrofit.Builder().baseUrl(Settings.REST_API_BASEURL_EMULATOR).addConverterFactory(GsonConverterFactory.create()).build();
        RestAPI restAPI = retrofit.create(RestAPI.class);

        if(mode == Mode.ADD) {
            Call<ImageItem> call = restAPI.postImage(item);
            call.enqueue(this);
        } else {
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

    private void selectImage() {
        final CharSequence[] items = { PARAM_CAMERA, PARAM_LIBRARY, PARAM_CANCEL };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddImageActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(PARAM_CAMERA)) {
                    boolean result= Utility.checkPermission(AddImageActivity.this);
                    userChoosenTask=PARAM_CAMERA;
                    if(result)
                        cameraIntent();

                } else if (items[item].equals(PARAM_LIBRARY)) {
                    boolean result= Utility.checkPermission(AddImageActivity.this);
                    userChoosenTask=PARAM_LIBRARY;
                    if(result)
                        galleryIntent();

                } else if (items[item].equals(PARAM_CANCEL)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals(PARAM_CAMERA))
                        cameraIntent();
                    else if(userChoosenTask.equals(PARAM_LIBRARY))
                        galleryIntent();
                } else {
                    Toast.makeText(AddImageActivity.this, "Permission denied!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mImageView.setImageBitmap(bm);
        image = bm;
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("MyApp", "thumbnailsize: " + thumbnail.getHeight() + "|" + thumbnail.getWidth());

        //TODO get the fullsize image
        Bitmap img = BitmapFactory.decodeFile(destination.getPath());

        mImageView.setImageBitmap(img);
        image = img;
    }


}
