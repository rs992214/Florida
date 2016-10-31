package com.thinkware.florida.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkware.florida.R;
import com.thinkware.florida.media.WavResourcePlayer;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.network.packets.server2mdt.WaitPlaceInfoPacket;
import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.adapter.WaitLocationListAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaitCallFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    View root;
    TextView textStatus;
    ListView listWaitLocation;
    WaitLocationListAdapter adapter;
    ArrayList<WaitPlaceInfoPacket> waitPlaces;

    public WaitCallFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_wait_call, container, false);

        textStatus = (TextView) root.findViewById(R.id.textView_status);
        listWaitLocation = (ListView) root.findViewById(R.id.list_waitlocation);
        adapter = new WaitLocationListAdapter(getContext());
        listWaitLocation.setAdapter(adapter);
        listWaitLocation.setOnItemClickListener(this);

        initialize();
        return root;
    }

    public void apply(WaitPlaceInfoPacket packet) {
        if (waitPlaces == null) {
            waitPlaces = new ArrayList<>();
        }
        textStatus.setText(": " + getString(R.string.success_request_wait_area));
        waitPlaces.add(packet);
        adapter.addItem(packet.getWaitPlaceName());
        adapter.notifyDataSetChanged();
    }

    public void successWait() {
        WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_135);
        textStatus.setText(": " + getString(R.string.success_request_wait));
        INaviExecutor.run(getActivity());
    }

    public void failWait() {
        WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_137);
        textStatus.setText(": " + getString(R.string.fail_request_wait));
    }

    public void cancelWait() {
        WavResourcePlayer.getInstance(getActivity()).play(R.raw.voice_139);
        textStatus.setText(": " + getString(R.string.success_request_cancel_wait));
        INaviExecutor.run(getActivity());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (waitPlaces == null || waitPlaces.size() <= 0) {
            return ;
        }

        textStatus.setText(": " + getString(R.string.request_wait));
        getScenarioService().requestWait(waitPlaces.get(i).getWaitPlaceCode());
    }

    private void initialize() {
        ResponseWaitDecisionPacket waitInfo = PreferenceUtil.getWaitArea(getActivity());
        if (waitInfo == null) {
            // 저장된 대기정보가 없으면 새로운 대기지역을 요청 한다.
            textStatus.setText(": " + getString(R.string.request_wait_area));
            getScenarioService().requestWaitAreas();
        } else {
            textStatus.setText(": " + getString(R.string.request_cancel_wait));
            getScenarioService().requestWaitCancel(waitInfo.getWaitPlaceCode());
        }
    }
}


