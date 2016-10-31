package com.thinkware.florida.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.thinkware.florida.R;

/**
 * Created by Mihoe on 2016-09-07.
 */
public class SingleLineDialog extends Dialog {
    String title, message;
    TextView txtTitle, txtMessage;

    public SingleLineDialog(Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

    public SingleLineDialog(Context context, String title, String message) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.title = title;
        this.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                        |WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_single_line);

        txtTitle = (TextView) findViewById(R.id.txt_title);
        txtMessage = (TextView) findViewById(R.id.txt_message);

        // 제목과 내용을 생성자에서 셋팅한다.
        txtTitle.setText(title);
        txtMessage.setText(message);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return super.onTouchEvent(event);
    }
}
