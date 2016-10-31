package com.thinkware.florida.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.thinkware.florida.scenario.INaviExecutor;
import com.thinkware.florida.service.AlwaysOnService;
import com.thinkware.florida.service.ScenarioService;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mihoe on 2016-09-09.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        removeStatusBar();
        stopService(new Intent(this, AlwaysOnService.class));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            removeStatusBar();
        }
    }

    public void removeStatusBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void finishWithINavi() {
        ScenarioService service = ((MainApplication)getApplication()).getScenarioService();
        if (service != null && service.isPrevStatusBackground()) {
            service.setPrevStatusBackground(false);
            INaviExecutor.run(this);
        }

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

}
