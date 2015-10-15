package com.example.tenny.monitor;

/**
 * Created by Tenny on 2015/10/6.
 */
import android.content.Context;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MySimpleArrayAdapter extends ArrayAdapter<ListItem> {

    public MySimpleArrayAdapter(Context context, ArrayList<ListItem> items) {
        super(context, R.layout.my_list_item, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ListItem i = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ItemHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ItemHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.my_list_item, parent, false);
            viewHolder.first = (TextView) convertView.findViewById(R.id.firstLine);
            viewHolder.second = (TextView) convertView.findViewById(R.id.secondLine);
            viewHolder.itemCount = (TextView) convertView.findViewById(R.id.itemCount);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.icon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ItemHolder) convertView.getTag();
        }
        // Populate the data into the template view using the data object
        Log.d("MySimpleArrayAdapter", "position=" + position + ", first=" + i.productName);
        Log.d("MySimpleArrayAdapter", "second=" + i.productSerial);
        Log.d("MySimpleArrayAdapter", "itemCount=" + i.itemCount);
        viewHolder.first.setText(i.productName);
        viewHolder.second.setText(i.productSerial);
        viewHolder.itemCount.setText(String.valueOf(i.itemCount));
        viewHolder.image.setImageBitmap(i.barcode);
        return convertView;
    }

    static class ItemHolder {
        public TextView first, second, itemCount;
        public ImageView image;
    }

    /*private ArrayList<DataSetObserver> observers = new ArrayList<DataSetObserver>();

    public void registerDataSetObserver(DataSetObserver observer) {
        observers.add(observer);
    }

    public void notifyDataSetChanged(){
        for (DataSetObserver observer: observers) {
            observer.onChanged();
            Log.d("Mylog", "Data onChanged");
        }
    }*/

}
