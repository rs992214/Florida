package com.thinkware.florida.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.fragment.ConfigFragment;
import com.thinkware.florida.ui.fragment.ConfigPasswordFragment;
import com.thinkware.florida.ui.fragment.PassengerInfoFragment;

/**
 * Created by Mihoe on 2016-09-12.
 */
public class ConfigActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        ConfigPasswordFragment fragment = new ConfigPasswordFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, fragment);
        transaction.commit();

    }
}
