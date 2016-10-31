package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.thinkware.florida.R;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.adapter.MessageListAdapter;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageListFragment extends BaseFragment {

    public static final int MAX_MESSAGE_COUNT = 5;
    View root;
    ListView listMessage;
    MessageListAdapter adapter;
    List<String> messages;

    public MessageListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_message_list, container, false);

        listMessage = (ListView) root.findViewById(R.id.list_message);
        adapter = new MessageListAdapter(getContext());
        listMessage.setAdapter(adapter);
        listMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentUtil.replace(getFragmentManager(), MessageFragment.newInstance(i));
            }
        });

        refresh();
        return root;
    }

    public void refresh() {
        messages = PreferenceUtil.getMessageList(getActivity());
        adapter.clear();
        if (messages != null) {
            for (String msg : messages) {
                adapter.addItem(msg);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
