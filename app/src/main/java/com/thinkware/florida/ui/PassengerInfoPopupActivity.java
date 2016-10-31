package com.thinkware.florida.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.fragment.BaseFragment;
import com.thinkware.florida.ui.fragment.FragmentUtil;
import com.thinkware.florida.ui.fragment.PassengerInfoFragment;

public class PassengerInfoPopupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        PassengerInfoFragment fragment = new PassengerInfoFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean("full_screen", true);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finishWithINavi();
        return super.onTouchEvent(event);
    }
}
