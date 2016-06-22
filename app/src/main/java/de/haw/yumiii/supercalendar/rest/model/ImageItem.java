package de.haw.yumiii.supercalendar.rest.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.parse.ParseFile;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by Yumiii on 05.06.16.
 */
public class ImageItem extends UserItem {


    private ParseFile imageFile;

    public ImageItem() {
        super();
    }


    public ImageItem(ParseFile imageFile, String note, Date date) {
        super(note, date);
        this.imageFile = imageFile;
        this.description = note;
    }

    public ImageItem(String objectId, ParseFile imageFile, String note, Date date) {
        super(objectId, note, date);
        this.imageFile = imageFile;
        this.description = note;
    }

    public ParseFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(ParseFile imageFile) {
        this.imageFile = imageFile;
    }

    @Override
    public String toString() {
        return "ImageItem{" +
                "description=" + description +
                '}';
    }

//    public Bitmap getBitmap() {
//        return getBitmapFromByteArray(Base64.decode(imageData.getBytes(), Base64.DEFAULT));
//    }
//
//    public void setBitmap(Bitmap img) {
//
//        byte[] byteArray = compressImage(img);
//        byte[] base64 = Base64.encode(byteArray, Base64.DEFAULT);
////        while(byteArray.length > 1000) {
////            Log.d("MyApp", "image size = " + byteArray.length);
////            Bitmap bmp = getBitmapFromByteArray(byteArray);
////            byteArray = compressImage(bmp);
////        }
//
//        imageData = new String(base64);
//    }
//
//    private byte[] compressImage(Bitmap img) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        img.compress(Bitmap.CompressFormat.JPEG, 50, stream);
//        byte[] byteArray = stream.toByteArray();
//
//        return byteArray;
//    }
//
//    public static Bitmap getBitmapFromByteArray(byte[] byteArray) {
//        Bitmap bmp;
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = true;
//        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
//
//        return bmp;
//    }
}
