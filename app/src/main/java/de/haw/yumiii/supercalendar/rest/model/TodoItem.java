package de.haw.yumiii.supercalendar.rest.model;

import java.util.Date;

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
                "name='" + name + '\'' +
                ", completed=" + completed +
                '}';
    }
}
