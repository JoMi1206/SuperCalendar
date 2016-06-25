package de.haw.yumiii.supercalendar.addupdate;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseFile;
import com.parse.ParseImageView;
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

import de.haw.yumiii.supercalendar.utils.DatePickerFragment;
import de.haw.yumiii.supercalendar.R;
import de.haw.yumiii.supercalendar.utils.Settings;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.utils.Utility;

public class AddImageActivity extends AppCompatActivity implements DatePickerFragment.OnFragmentDateSetListener {

    public static final String PARAM_IS_MODE_ADD = "mode_add";
    public static final String PARAM_ID = "id";
    public static final String PARAM_NOTE = "description";
    public static final String PARAM_DATE = "date";

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
    private ParseImageView mImageView;

    private Button mSaveButton;

    private ImageItem mImageItemToUpdate;

    private Date mDate = null;
    SimpleDateFormat sdf = new SimpleDateFormat(Settings.DATE_FORMAT);
    ParseFile mImageFile;

    private Mode mode = Mode.ADD;

    private String imageId = "-1";
    private byte[] mImageBytes;

    public enum Mode {ADD, UPDATE};

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_image);


        mDescriptionNote = (EditText) findViewById(R.id.description_edit_text);
        mDateButton = (Button) findViewById(R.id.date_button);
        mImageView = (ParseImageView) findViewById(R.id.add_imageview);
        mChooseImageButton = (Button) findViewById(R.id.select_image_button);

        mSaveButton = (Button) findViewById(R.id.save_button);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

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

            // show a progress dialog
            mProgressDialog.setTitle(R.string.progress_title_save);
            mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_save));
            mProgressDialog.show();

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");

            // Retrieve the object by id
            query.getInBackground(imageId, new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject imageobject, com.parse.ParseException e) {
                    mProgressDialog.dismiss();

                    if (e == null) {
                        //TODO load imageFile and show
                        mImageItemToUpdate = new ImageItem(imageobject.getObjectId(), imageobject.getParseFile("image"), imageobject.getString("description"), imageobject.getDate("date"));
                        mImageFile = mImageItemToUpdate.getImageFile();
                        if(mImageFile != null) {
                            mImageView.setParseFile(mImageFile);
                            mImageView.loadInBackground(null);
                        }
                    }
                }
            });
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
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
        mProgressDialog.setTitle(R.string.progress_title_save);
        mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_save));
        mProgressDialog.show();

        // Save image as file and append it to imageItem
        String fileName = "image_" + (new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())) + ".jpg";
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

                            mProgressDialog.dismiss();

                            if (e == null) {
                                // after the imageItem is saved it has a ObjectID
                                Toast.makeText(AddImageActivity.this, R.string.toast_image_added, Toast.LENGTH_SHORT).show();

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
                    mProgressDialog.dismiss();

                    // The save failed.
                    Toast.makeText(AddImageActivity.this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                    Log.d("MyApp", "Image post error: " + e);
                }
            }
        });


    }

    private void updateImageItem(final String description, final byte[] image) {

        // show a progress dialog
        mProgressDialog.setTitle(R.string.progress_title_save);
        mProgressDialog.setMessage(getApplicationContext().getResources().getString(R.string.progress_message_save));
        mProgressDialog.show();

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Image");
        // Retrieve the object by id
        query.getInBackground(imageId, new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject image, com.parse.ParseException e) {
                if (e == null) {

                    // Update imageFile?
                    if(mImageBytes != null && mImageBytes.length > 0) {
                        String fileName = "image_" + (new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())) + ".jpg";
                        mImageFile = new ParseFile(fileName, mImageBytes);
                    }

                    // Now let's update it with some new data.
                    image.put("description", description);
                    image.put("date", mDate);

                    mImageFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            if(e == null) {
                                mProgressDialog.dismiss();
                                // if the image is stored successfully -> add it to the imageItem and save the imageItem
                                image.put("image", mImageFile);

                                image.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(com.parse.ParseException e) {
                                        if (e == null) {
                                            // Saved successfully.
                                            Toast.makeText(AddImageActivity.this, R.string.toast_image_updated, Toast.LENGTH_SHORT).show();
                                            Intent result = new Intent();
                                            setResult(RESULT_OK, result);
                                            finish();
                                        } else {
                                            mProgressDialog.dismiss();
                                            // The save failed.
                                            Toast.makeText(AddImageActivity.this, R.string.toast_save_failed, Toast.LENGTH_SHORT).show();
                                            Log.d("MyApp", "Image update error: " + e);
                                        }
                                    }
                                });
                            } else {
                                mProgressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    mProgressDialog.dismiss();
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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

//    Uri mPhotoURI;
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
                Uri photoURI = FileProvider.getUriForFile(this, "de.haw.yumiii.fileprovider", photoFile);
                Log.d("MyApp", "photoURI: " + photoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
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

    private void setImage() {

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Log.d("MyApp", "Bitmap size: " + bitmap.getWidth() + "|" + bitmap.getHeight());
        Bitmap imageScaled = Bitmap.createScaledBitmap(bitmap, 1000, 1000 * bitmap.getHeight() / bitmap.getWidth(), false);
        Log.d("MyApp", "Scaled image size: " + imageScaled.getWidth() + "|" + imageScaled.getHeight());
        mImageView.setImageBitmap(imageScaled);

        // Save the byte array for storing to the server
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageScaled.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        mImageBytes = bytes.toByteArray();
    }


}
