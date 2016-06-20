package de.haw.yumiii.supercalendar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.utils.Utility;

public class AddImageActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentDateSetListener {

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

    private Button mSaveButton;

    private Date mDate = null;
    SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
    private Bitmap mImage = null;
    ParseFile mImageFile;

    private Mode mode = Mode.ADD;

    private String imageId = "-1";
    private byte[] mImageBytes;

    public enum Mode {ADD, UPDATE}

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_image);


        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mDateButton = (Button) findViewById(R.id.date_button);
        mImageView = (ImageView) findViewById(R.id.add_imageview);
        mChooseImageButton = (Button) findViewById(R.id.select_image_button);

        mSaveButton = (Button) findViewById(R.id.save_button);

        boolean mode_add = this.getIntent().getExtras().getBoolean(PARAM_IS_MODE_ADD);
        if (mode_add) {
            mode = Mode.ADD;
        } else {
            mode = Mode.UPDATE;
        }


        String dateStr = this.getIntent().getExtras().getString(PARAM_DATE);
        if (dateStr != null) {
            try {
                mDate = sdf.parse(dateStr);
                mDateButton.setText(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (mode == Mode.UPDATE) {
            imageId = this.getIntent().getExtras().getString(PARAM_ID);
            String note = this.getIntent().getExtras().getString(PARAM_NOTE);
            mDescriptionNote.setText(note);

            String data = this.getIntent().getExtras().getString(PARAM_DATA);
            if (data != null) {
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

        final String description = mDescriptionNote.getText().toString();

        //TODO: adjust null image
        if(mode == Mode.ADD) {
            addImageItem(description, null);
        } else {
            updateImageItem(description, null);
        }
    }

    private void addImageItem(String description, byte[] image) {
        // With Parse
        final ParseObject newImage = new ParseObject("Image");
        newImage.put("description", description);
        newImage.put("date", mDate);
        newImage.put("owner", ParseUser.getCurrentUser());

        if(mImageBytes == null) {
            Toast.makeText(getApplicationContext(), R.string.add_image_image_missing, Toast.LENGTH_SHORT);
            return;
        }

        // show a progress dialog
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(R.string.progress_title);
        progress.setMessage(getApplicationContext().getResources().getString(R.string.progress_message));
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.show();

//        mProgressBar.setVisibility(View.VISIBLE);
//        mSaveButton.setEnabled(false);


        // Save image as file and append it to imageItem
        String fileName = "image_" + (new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
        mImageFile = new ParseFile(fileName, mImageBytes);

        mImageFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if(e == null) {
                    // if the image is stored successfully -> add it to the imageItem and save the imageItem
                    newImage.put("image", mImageFile);

                    newImage.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {

//                            mProgressBar.setVisibility(View.INVISIBLE);
//                            mSaveButton.setEnabled(true);
                            progress.dismiss();

                            if (e == null) {
                                // after the imageItem is saved it has a ObjectID
                                Toast.makeText(AddImageActivity.this, R.string.toast_todo_updated, Toast.LENGTH_SHORT).show();

                                Intent result = new Intent();
                                setResult(RESULT_OK, result);

                                finish();
                            } else {
                                // The save failed.
                                Toast.makeText(AddImageActivity.this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                                Log.d("MyApp", "Image post error: " + e);
                            }
                        }
                    });
                } else {
//                    mProgressBar.setVisibility(View.INVISIBLE);
//                    mSaveButton.setEnabled(true);
                    progress.dismiss();

                    // The save failed.
                    Toast.makeText(AddImageActivity.this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                    Log.d("MyApp", "Image post error: " + e);
                }
            }
        });


    }

    private void updateImageItem(final String description, final byte[] image) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");

        // Retrieve the object by id
        query.getInBackground(imageId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject image, com.parse.ParseException e) {
                if (e == null) {
                    // Now let's update it with some new data.
                    image.put("description", description);
                    image.put("date", mDate);

                    image.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if (e == null) {
                                // Saved successfully.
                                Toast.makeText(AddImageActivity.this, R.string.toast_todo_updated, Toast.LENGTH_SHORT).show();
                                Intent result = new Intent();
                                setResult(RESULT_OK, result);
                                finish();
                            } else {
                                // The save failed.
                                Toast.makeText(AddImageActivity.this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                                Log.d("MyApp", "Image update error: " + e);
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to find the current Image-Item", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();

        Calendar cal = Calendar.getInstance();

        if (mDate != null) {
            cal.setTime(mDate);
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
        mDate = newDate;

        Log.d("MyApp", "onFragmentDateSetListener called, Date: " + sdf.format(mDate));
        mDateButton.setText(sdf.format(mDate));
    }

    private void selectImage() {
        final CharSequence[] items = {PARAM_CAMERA, PARAM_LIBRARY, PARAM_CANCEL};

        AlertDialog.Builder builder = new AlertDialog.Builder(AddImageActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(PARAM_CAMERA)) {
                    boolean result = Utility.checkPermission(AddImageActivity.this);
                    userChoosenTask = PARAM_CAMERA;
                    if (result)
                        dispatchTakePictureIntent();

                } else if (items[item].equals(PARAM_LIBRARY)) {
                    boolean result = Utility.checkPermission(AddImageActivity.this);
                    userChoosenTask = PARAM_LIBRARY;
                    if (result)
                        galleryIntent();

                } else if (items[item].equals(PARAM_CANCEL)) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    Uri mPhotoURI;
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getApplicationContext(), "Failed to create image File", Toast.LENGTH_SHORT);
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mPhotoURI = FileProvider.getUriForFile(this, "de.haw.yumiii.fileprovider", photoFile);
                Log.d("MyApp", mPhotoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }

//    private void cameraIntent() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, REQUEST_CAMERA);
//    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals(PARAM_CAMERA))
                        dispatchTakePictureIntent();
                    else if (userChoosenTask.equals(PARAM_LIBRARY))
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
                setImage();
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap imageScaled = Bitmap.createScaledBitmap(bm, 1000, 1000
                * bm.getHeight() / bm.getWidth(), false);
        mImageView.setImageBitmap(imageScaled);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bos);

        byte[] scaledData = bos.toByteArray();
        mImageBytes = scaledData;
    }

//    private void onCaptureImageResult(Intent data) {
//        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//
//        byte[] bytearray = bytes.toByteArray();
//
//        setImage(bytearray);

//        File destination = new File(Environment.getExternalStorageDirectory(),
//                System.currentTimeMillis() + ".jpg");
//
//        FileOutputStream fo;
//        try {
//            destination.createNewFile();
//            fo = new FileOutputStream(destination);
//            fo.write(bytes.toByteArray());
//            fo.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        Log.d("MyApp", "thumbnailsize: " + thumbnail.getHeight() + "|" + thumbnail.getWidth());
//
//        //TODO get the fullsize image
//        Bitmap img = BitmapFactory.decodeFile(destination.getPath());
//
//        mImageView.setImageBitmap(img);
//        image = img;
//    }

    private void setImage() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoURI.getPath(), bmOptions);
        Log.d("MyApp", "Bitmap size: " + bitmap.getWidth() + "|" + bitmap.getHeight());
        mImageView.setImageBitmap(bitmap);
        mImage = bitmap;
    }


}
