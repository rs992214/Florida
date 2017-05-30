package com.thinkware.florida.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thinkware.florida.R;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.ui.MainActivity;
import com.thinkware.florida.ui.WaitStateActivity;
import com.thinkware.florida.ui.dialog.SingleLineDialog;

/**
 * Created by hoonlee on 2017. 5. 30..
 */

public class ManageWaitCallFragment extends BaseFragment {

    View root;
    View btnReqWait;
    View btnWaitStatus;

    private ScenarioService scenarioService;
    private Context context;

    //---------------------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------------------
    public ManageWaitCallFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_wait_management, container, false);

        scenarioService = getScenarioService();
        context = getActivity();

        btnWaitStatus = root.findViewById(R.id.btn_waitstate);
        btnReqWait = root.findViewById(R.id.btn_reqwait);

        btnWaitStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, WaitStateActivity.class));
            }
        });

        btnReqWait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 승차중 또는 저장된 배차 정보가 있을 경우 대기 요청을 할 수 없다.
                if (scenarioService.getBoardType() == Packets.BoardType.Boarding
                        || PreferenceUtil.getWaitOrderInfo(context) != null
                        || PreferenceUtil.getNormalCallInfo(context) != null) {
                    SingleLineDialog dialog = new SingleLineDialog(context,
                            getString(R.string.done),
                            getString(R.string.cannot_query_wait));
                    dialog.show();
                } else {
                    FragmentUtil.replace(((MainActivity)context).getSupportFragmentManager(), new WaitCallFragment());
                }
            }
        });

        return root;

    }
}
