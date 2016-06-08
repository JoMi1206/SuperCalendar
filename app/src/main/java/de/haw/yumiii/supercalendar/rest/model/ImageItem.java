package de.haw.yumiii.supercalendar.rest.model;

import android.media.Image;

import java.util.Date;

/**
 * Created by Yumiii on 05.06.16.
 */
public class ImageItem extends UserItem {


    private Image image;

    public ImageItem() {
        super();
    }

    public ImageItem(Image image, String note, Date date) {
        super(note, date);
        this.image = image;
        this.note = note;
    }


    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "note=" + note +
                '}';
    }
}
