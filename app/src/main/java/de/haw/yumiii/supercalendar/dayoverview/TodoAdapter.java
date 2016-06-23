package de.haw.yumiii.supercalendar.dayoverview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

import de.haw.yumiii.supercalendar.R;
import de.haw.yumiii.supercalendar.rest.model.ImageItem;
import de.haw.yumiii.supercalendar.rest.model.TodoItem;
import de.haw.yumiii.supercalendar.rest.model.UserItem;

/**
 * Created by Yumiii on 23.05.16.
 */
public class TodoAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<UserItem> mDataSource;

    ParseImageView mImageView;

    public TodoAdapter(Context context, ArrayList<UserItem> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setmDataSource(ArrayList<UserItem> mDataSource) {
        this.mDataSource = mDataSource;
    }

    public void clear() {
        mDataSource.clear();
    }

    public void addAll(Collection<UserItem> items) {
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
        // Get view for a todoItem
        if(getItem(position) instanceof TodoItem) {
            View rowView = mInflater.inflate(R.layout.list_item_todo, parent, false);

            TextView nameTextView = (TextView) rowView.findViewById(R.id.list_item_name);
            TextView noteTextView = (TextView) rowView.findViewById(R.id.list_item_note);
            CheckBox completedCheckBox = (CheckBox) rowView.findViewById(R.id.list_item_completed);

            TodoItem todo = (TodoItem) getItem(position);

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
//        nameTextView.setText(todo.getName() + " Fällig: " + sdf.format(todo.getDate()));
            nameTextView.setText(todo.getName());
            if (todo.isCompleted()) {
                nameTextView.setPaintFlags(nameTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }

            noteTextView.setText(todo.getDescription());
            completedCheckBox.setChecked(todo.isCompleted());

            return rowView;
        }

        // Get view for a imageItem
        if(getItem(position) instanceof ImageItem) {
            View rowView = mInflater.inflate(R.layout.list_item_image, parent, false);

            TextView noteTextView = (TextView) rowView.findViewById(R.id.list_item_image_note);
            mImageView = (ParseImageView) rowView.findViewById(R.id.list_item_image);

            ImageItem imageItem = (ImageItem) getItem(position);

            noteTextView.setText(imageItem.getDescription());
            ParseFile imageFile = imageItem.getImageFile();
            if(imageFile != null) {
                mImageView.setParseFile(imageFile);
                mImageView.loadInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        mImageView.getLayoutParams().height = getHeightOfBMPInImageView(mImageView);
                        mImageView.requestLayout();
                    }
                });
            }

            return rowView;
        }

        return null;
    }

    /**
     * Returns the height of the image according to the width of the ImageView.
     * If imageView.width >= image.width -> return imageHeight
     * else return imageHeight * (imageView.width/image.width)
     * @param imageView
     * @return
     */
    private int getHeightOfBMPInImageView(ParseImageView imageView) {

        if(imageView.getDrawable() == null) {
            return imageView.getHeight();
        }

        int imageViewWidth = imageView.getWidth();

        Rect bounds = imageView.getDrawable().getBounds();
        int imageWidth = bounds.width();
        int imageHeight = bounds.height();

        if(imageWidth <= imageViewWidth) {
            return imageHeight;
        }

        double ratio = ((double)imageViewWidth)/imageWidth;
        return (int) (imageHeight * ratio);
    }
}
