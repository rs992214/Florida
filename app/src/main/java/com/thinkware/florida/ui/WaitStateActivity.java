package com.thinkware.florida.ui;

import android.os.Bundle;
import android.view.View;

import com.thinkware.florida.R;

/**
 * Created by hoonlee on 2017. 5. 30..
 */

public class WaitStateActivity extends BaseActivity {

    View btnCloseX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_state);

        btnCloseX = (View) findViewById(R.id.btn_close_x);
        btnCloseX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }
}
