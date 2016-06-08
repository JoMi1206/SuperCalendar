package de.haw.yumiii.supercalendar.rest.model;

import java.util.Date;

/**
 * Created by Yumiii on 05.06.16.
 */
public class UserItem {

    protected String _id;
    protected String note;
    protected Date date;

    public UserItem() {
    }

    public UserItem(String note, Date date) {
        this.note = note;
        this.date = date;
    }

    public String get_id() {
        return _id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
