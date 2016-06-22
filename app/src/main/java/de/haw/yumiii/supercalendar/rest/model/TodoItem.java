package de.haw.yumiii.supercalendar.rest.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.haw.yumiii.supercalendar.utils.Settings;

/**
 * Created by Yumiii on 22.05.16.
 */
public class TodoItem extends UserItem {


    private String name;
    private boolean completed;

    public TodoItem() {
        super();
    }

    public TodoItem(String name, boolean completed, String note, Date date) {
        super(note, date);
        this.name = name;
        this.completed = completed;
    }

    public TodoItem(String id, String name, boolean completed, String note, Date date) {
        super(id, note, date);
        this.name = name;
        this.completed = completed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }



    @Override
    public String toString() {
        return "TodoItem{" +
                "name='" + name + '\n' +
                "description='" + description + '\n' +
                "date='" + new SimpleDateFormat(Settings.DATE_FORMAT).format(date) + '\n' +
                ", completed=" + completed +
                '}';
    }

}
