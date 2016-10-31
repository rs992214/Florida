package com.thinkware.florida.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;

import com.thinkware.florida.R;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.server2mdt.OrderInfoPacket;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.ui.fragment.BaseFragment;
import com.thinkware.florida.ui.fragment.FragmentUtil;
import com.thinkware.florida.ui.fragment.RequestOrderFragment;

public class RequestOrderPopupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        RequestOrderFragment fragment = new RequestOrderFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean("full_screen", true);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Fragment f = FragmentUtil.getTopFragment(getSupportFragmentManager());
        if (f != null && f instanceof RequestOrderFragment) {
            OrderInfoPacket tempPacket = PreferenceUtil.getTempCallInfo(this);
            if (tempPacket != null) {
                ScenarioService scenarioService = ((MainApplication) getApplication()).getScenarioService();
                scenarioService.requestOrderRealtime(Packets.OrderDecisionType.Reject, tempPacket);
            }
            finishWithINavi();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        Fragment f = FragmentUtil.getTopFragment(getSupportFragmentManager());
        if (f != null && f instanceof BaseFragment) {
            ((BaseFragment) f).onBackPressed();
        } else {
            super.onBackPressed();
        }
    }
}
