package de.haw.yumiii.supercalendar;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import de.haw.yumiii.supercalendar.rest.model.TodoItem;

/**
 * Created by Yumiii on 23.05.16.
 */
public class TodoAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<TodoItem> mDataSource;

    public TodoAdapter(Context context, ArrayList<TodoItem> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setmDataSource(ArrayList<TodoItem> mDataSource) {
        this.mDataSource = mDataSource;
    }

    public void clear() {
        mDataSource.clear();
    }

    public void addAll(Collection<TodoItem> items) {
        mDataSource.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_todo, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.list_item_name);
        TextView noteTextView = (TextView) rowView.findViewById(R.id.list_item_note);
        CheckBox completedCheckBox = (CheckBox) rowView.findViewById(R.id.list_item_completed);

        TodoItem todo =(TodoItem) getItem(position);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        nameTextView.setText(todo.getName() + " Fällig: " + sdf.format(todo.getDate()));
        if(todo.isCompleted()) {
            nameTextView.setPaintFlags(nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        noteTextView.setText(todo.getNote());
        completedCheckBox.setChecked(todo.isCompleted());

        return rowView;
    }
}
