package com.thinkware.florida.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.fragment.MessageFragment;

public class MessagePopupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        MessageFragment fragment = new MessageFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean("full_screen", true);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finishWithINavi();
        return super.onTouchEvent(event);
    }
}
