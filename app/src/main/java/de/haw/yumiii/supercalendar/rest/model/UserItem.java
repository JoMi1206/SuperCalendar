package de.haw.yumiii.supercalendar.rest.model;

import java.util.Date;

/**
 * Created by Yumiii on 05.06.16.
 */
public class UserItem {

    protected String _id;
    protected String description;
    protected Date date;

    public UserItem() {
    }

    public UserItem(String description, Date date) {
        this.description = description;
        this.date = date;
    }

    public UserItem(String id, String description, Date date) {
        this._id = id;
        this.description = description;
        this.date = date;
    }

    public String get_id() {
        return _id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
