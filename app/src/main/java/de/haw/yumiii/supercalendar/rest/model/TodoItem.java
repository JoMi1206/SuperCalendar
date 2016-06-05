package de.haw.yumiii.supercalendar.rest.model;

/**
 * Created by Yumiii on 22.05.16.
 */
public class TodoItem {

    private String _id;
    private String name;
    private boolean completed;
    private String note;

    //TODO add to Server
    // private Date day;


    public TodoItem() {
    }

    public TodoItem(String name, boolean completed, String note) {
        this.name = name;
        this.completed = completed;
        this.note = note;
    }

    public String get_id() {
        return _id;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String toString() {
        return "TodoItem{" +
                "name='" + name + '\'' +
                ", completed=" + completed +
                '}';
    }
}
