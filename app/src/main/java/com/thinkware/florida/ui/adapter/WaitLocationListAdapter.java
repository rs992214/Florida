package com.thinkware.florida.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.view.ViewHolder;

import java.util.ArrayList;

/**
 * Created by Mihoe on 2016-09-08.
 */
public class WaitLocationListAdapter extends BaseAdapter {
    ArrayList<String> dataList = new ArrayList<>();
    Context context;

    public WaitLocationListAdapter(Context context) {
        this.context = context;
    }

    public void addItem(String location) {
        dataList.add(location);
    }

    @Override
    public int getCount() {
        if (dataList == null) {
            return 0;
        }
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        if (dataList == null) {
            return null;
        }
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.context).inflate(R.layout.listview_waitlocation, parent, false);
        }

        final String item = (String)getItem(position);
        TextView location = ViewHolder.get(convertView, R.id.txt_location);
        location.setText(item);

        return convertView;
    }
}
