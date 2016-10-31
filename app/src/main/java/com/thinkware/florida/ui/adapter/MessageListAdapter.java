package com.thinkware.florida.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.fragment.MessageListFragment;
import com.thinkware.florida.ui.view.ViewHolder;

import java.util.ArrayList;

/**
 * Created by Mihoe on 2016-09-08.
 */
public class MessageListAdapter extends BaseAdapter {
    ArrayList<String> dataList = new ArrayList<>();
    Context context;

    public MessageListAdapter(Context context) {
        this.context = context;
    }

    public void clear() {
        dataList.clear();
    }

    public void addItem(int index, String location) {
        dataList.add(index, location);
        if (dataList.size() > MessageListFragment.MAX_MESSAGE_COUNT) {
            dataList.remove(dataList.size()-1);
        }
    }

    public void addItem(String location) {
        dataList.add(location);
        if (dataList.size() > MessageListFragment.MAX_MESSAGE_COUNT) {
            dataList.remove(dataList.size()-1);
        }
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
            convertView = LayoutInflater.from(this.context).inflate(R.layout.listview_message, parent, false);
        }

        final String item = (String)getItem(position);
        TextView location = ViewHolder.get(convertView, R.id.txt_message);
        location.setText(item);

        return convertView;
    }
}
