package com.thinkware.florida.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.thinkware.florida.R;

/**
 * Created by zic325 on 2016. 10. 7..
 */

public class CorporationDialog extends Dialog {

    private CheckBox cbIndivisual;
    private CheckBox cbCorporation;
	private CheckBox cbSunCheon, cbHanamIndividual, cbHanamCorporation;
    private ICorporationDialogListener listener;

    public interface ICorporationDialogListener {
        void onSelectedType(int type);
    }

    public CorporationDialog(Context context, ICorporationDialogListener l) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        listener = l;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lpWindow.dimAmount = 0.7f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_corporation_type);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbIndivisual.isChecked() || cbCorporation.isChecked() ||
		                cbSunCheon.isChecked() || cbHanamIndividual.isChecked() || cbHanamCorporation.isChecked()) {

                	int selectedType = 0;
                	if (cbIndivisual.isChecked()) {
                		selectedType = 1;
	                } else if (cbCorporation.isChecked()) {
		                selectedType = 2;
	                } else if (cbSunCheon.isChecked()) {
                		selectedType = 3;
	                } else if (cbHanamIndividual.isChecked()) {
		                selectedType = 4;
	                } else if (cbHanamCorporation.isChecked()) {
		                selectedType = 5;
	                }

                    listener.onSelectedType(selectedType);
                    dismiss();
                }
            }
        });

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        cbIndivisual = (CheckBox) findViewById(R.id.checkboxIndivisual);
        cbIndivisual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
	                cbCorporation.setChecked(false);
	                cbSunCheon.setChecked(false);
	                cbHanamIndividual.setChecked(false);
	                cbHanamCorporation.setChecked(false);
                }
            }
        });

        cbCorporation = (CheckBox) findViewById(R.id.checkboxCorporation);
        cbCorporation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
	                cbIndivisual.setChecked(false);
	                cbSunCheon.setChecked(false);
	                cbHanamIndividual.setChecked(false);
	                cbHanamCorporation.setChecked(false);
                }
            }
        });

	    cbSunCheon = (CheckBox) findViewById(R.id.checkboxSunCheon);
	    cbSunCheon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			    if (isChecked) {
				    cbIndivisual.setChecked(false);
				    cbCorporation.setChecked(false);
				    cbHanamIndividual.setChecked(false);
				    cbHanamCorporation.setChecked(false);
			    }
		    }
	    });

	    cbHanamIndividual = (CheckBox) findViewById(R.id.checkboxHanamIndividual);
	    cbHanamIndividual.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			    if (isChecked) {
				    cbIndivisual.setChecked(false);
				    cbCorporation.setChecked(false);
				    cbSunCheon.setChecked(false);
				    cbHanamCorporation.setChecked(false);
			    }
		    }
	    });

	    cbHanamCorporation = (CheckBox) findViewById(R.id.checkboxHanamCorporation);
	    cbHanamCorporation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			    if (isChecked) {
				    cbIndivisual.setChecked(false);
				    cbCorporation.setChecked(false);
				    cbSunCheon.setChecked(false);
				    cbHanamIndividual.setChecked(false);
			    }
		    }
	    });

	    cbIndivisual.setChecked(true);
    }
}
