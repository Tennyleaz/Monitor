package com.example.tenny.monitor;

/**
 * Created by Tenny on 2015/10/6.
 */
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MySimpleArrayAdapter extends ArrayAdapter<ListItem> {
    private final Context context;
    private final ListItem[] items;

    public MySimpleArrayAdapter(Context context, ListItem[] items) {
        super(context, R.layout.my_list_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        // reuse views
        if(rowView == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.my_list_item, parent, false);
            // configure view holder
            ItemHolder viewHolder = new ItemHolder();
            viewHolder.first = (TextView) rowView.findViewById(R.id.firstLine);
            viewHolder.second = (TextView) rowView.findViewById(R.id.secondLine);
            viewHolder.count = (TextView) rowView.findViewById(R.id.count);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(viewHolder);
        }

        // fill data
        ItemHolder holder = (ItemHolder) rowView.getTag();
        holder.first.setText(items[position].productName);
        holder.second.setText(items[position].productSerial);
        holder.count.setText(items[position].count);
        holder.image.setImageBitmap(items[position].barcode);
        return rowView;
    }

    static class ItemHolder {
        public TextView first, second, count;
        public ImageView image;
    }

}
