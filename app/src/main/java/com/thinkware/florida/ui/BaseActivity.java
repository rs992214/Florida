package com.thinkware.florida.ui;

import android.app.Activity;
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
        /*Activity belowAct = ((MainApplication) getApplication()).getBelowActivity();
        // 최상위 Activity는 지금 보여지고 있는 창(메시지, 공지사항, 콜방송, 고객정보) 이므로
        // 바로 아래 Activity가 MainActivity일 경우를 체크 한다.
        if (belowAct != null && belowAct.getClass().getSimpleName().contains("MainActivity")) {
            // ScenarioService.launchActivity()에서 설정한 FLAG 값에 따라 지도 실행 여부를 결정한다.
            ScenarioService service = ((MainApplication) getApplication()).getScenarioService();
            if (service != null && service.isPrevStatusBackground()) {
                service.setPrevStatusBackground(false);
                INaviExecutor.run(this);
            }
        }*/

        INaviExecutor.run(this);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                finish();
            }
        }, 100);
    }

}
