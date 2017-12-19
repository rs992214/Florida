package com.thinkware.florida.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thinkware.florida.R;

/**
 * Created by Mihoe on 2016-09-12.
 */
public class CallStatusView extends LinearLayout {
    ImageView icRed, icGreen, icBlue, icYellow;
    TextView txtNumber;
    LinearLayout container;

    public CallStatusView(Context context) {
        super(context);
        initView();
    }

    public CallStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CallStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addView(inflater.inflate(R.layout.view_status, this, false));

        txtNumber = (TextView)findViewById(R.id.txt_number);
        icRed = (ImageView)findViewById(R.id.status_01);
        icGreen = (ImageView)findViewById(R.id.status_02);
        icBlue = (ImageView)findViewById(R.id.status_03);
        icYellow = (ImageView)findViewById(R.id.status_04);
	    container = (LinearLayout)findViewById(R.id.status_container);
    }

    public void setCarID(String carID) {
        txtNumber.setText(carID);
    }

    public void setCertification(boolean status) {
        if (status) {
            icRed.setImageResource(R.drawable.status_01);
        } else {
            icRed.setImageResource(R.drawable.disable);
        }
    }

    public void setPassengerAboard(boolean status) {
        if (status) {
            icGreen.setImageResource(R.drawable.status_02);
        } else {
            icGreen.setImageResource(R.drawable.disable);
        }
    }

    public void setWaiting(boolean status) {
        if (status) {
            icBlue.setImageResource(R.drawable.status_03);
	        container.setBackground(getResources().getDrawable(R.drawable.status_bg_ready));
        } else {
            icBlue.setImageResource(R.drawable.disable);
	        container.setBackground(getResources().getDrawable(R.drawable.status_bg));
        }
    }

    public void setEmergency(boolean status) {
        if (status) {
            icYellow.setImageResource(R.drawable.status_04);
        } else {
            icYellow.setImageResource(R.drawable.disable);
        }
    }
}
