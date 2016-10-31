package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.network.packets.server2mdt.NoticesPacket;
import com.thinkware.florida.scenario.PreferenceUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoticeFragment extends BaseFragment {
    TextView txtTitle, txtContent;
    boolean isFullScreen = false;

    public NoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            isFullScreen = getArguments().getBoolean("full_screen", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root;

        if (isFullScreen) {
            root = inflater.inflate(R.layout.fragment_notice2, container, false);
        } else {
            root = inflater.inflate(R.layout.fragment_notice, container, false);
        }

        txtTitle = (TextView) root.findViewById(R.id.txt_title);
        txtContent = (TextView) root.findViewById(R.id.txt_content);

        NoticesPacket noticesPacket = PreferenceUtil.getNotice(getActivity());
        if (noticesPacket == null) {
            txtTitle.setText(R.string.empty_notice);
            txtContent.setText("");
        } else {
            txtTitle.setText(noticesPacket.getNoticeTitle());
            txtContent.setText(noticesPacket.getNotice());
        }
        return root;
    }
}
