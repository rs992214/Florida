package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.scenario.PreferenceUtil;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends BaseFragment {
    View btnPrev, btnNext, btnList;
    TextView txtContent;
    boolean isFullScreen = false;

    private int currentIndex;
    private List<String> messages;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment newInstance(int index) {
        Bundle args = new Bundle();
        args.putInt("INDEX", index);

        MessageFragment f = new MessageFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            isFullScreen = getArguments().getBoolean("full_screen", false);
            currentIndex = getArguments().getInt("INDEX");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root;

        if (isFullScreen) {
            root = inflater.inflate(R.layout.fragment_message2, container, false);
        } else {
            root = inflater.inflate(R.layout.fragment_message, container, false);
        }

        btnList = root.findViewById(R.id.btn_list);
        btnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentUtil.replace(getFragmentManager(), new MessageListFragment());
            }
        });

        btnNext = root.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex++;
                if (currentIndex >= messages.size()) {
                    currentIndex = 0;
                }
                if (messages != null && messages.size() > currentIndex) {
                    txtContent.setText(messages.get(currentIndex));
                }
            }
        });

        btnPrev = root.findViewById(R.id.btn_prev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = messages.size() - 1;
                }
                if (messages != null && messages.size() > currentIndex) {
                    txtContent.setText(messages.get(currentIndex));
                }
            }
        });

        txtContent = (TextView) root.findViewById(R.id.txt_content);

        messages = PreferenceUtil.getMessageList(getActivity());

        if (isFullScreen) {
            currentIndex = 0;
        }

        if (messages != null && messages.size() > currentIndex) {
            txtContent.setText(messages.get(currentIndex));
        }

        return root;
    }

}
