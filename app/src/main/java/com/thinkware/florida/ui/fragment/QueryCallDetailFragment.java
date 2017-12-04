package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.network.packets.server2mdt.ResponseAccountPacket;

/**
 * A simple {@link Fragment} subclass.
 */
public class QueryCallDetailFragment extends BaseFragment {
    View root, btReturn;
    TextView txtCallCount, txtCallNormal, txtCallApp, txtCallExtern, txtCallCheomdu, txtNote;

    public QueryCallDetailFragment() {
        // Required empty public constructor
    }

    public static QueryCallDetailFragment newInstance(String begin, String end) {
        Bundle args = new Bundle();
        args.putString("BEGIN", begin);
        args.putString("END", end);
        QueryCallDetailFragment f = new QueryCallDetailFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_query_call_detail, container, false);

        btReturn = root.findViewById(R.id.btn_return);
        btReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentUtil.remove(getFragmentManager(), QueryCallDetailFragment.this);
            }
        });

        txtCallCount = (TextView) root.findViewById(R.id.txt_call_count);
        txtCallNormal = (TextView) root.findViewById(R.id.txt_call_normal);
        txtCallApp = (TextView) root.findViewById(R.id.txt_call_app);
        txtCallExtern = (TextView) root.findViewById(R.id.txt_call_extern);
	    txtCallCheomdu = (TextView) root.findViewById(R.id.txt_call_cheomdu);
        txtNote = (TextView) root.findViewById(R.id.txt_note);

        String begin = getArguments().getString("BEGIN");
        String end = getArguments().getString("END");
        getScenarioService().requestAccount(begin, end);
        return root;
    }

    public void apply(ResponseAccountPacket p) {
        txtCallCount.setText(String.valueOf(p.getNormalCallNumber()));
        txtCallNormal.setText(String.valueOf(p.getNormalCallFee()));
        txtCallApp.setText(String.valueOf(p.getArrear()));
        txtCallExtern.setText(String.valueOf(p.getLastMonthOffsetting()));
	    txtCallCheomdu.setText(String.valueOf(p.getBusinessCallFee()));
        txtNote.setText(TextUtils.isEmpty(p.getMemo()) ? "" : p.getMemo());
    }
}
