package com.thinkware.florida.ui;

import android.content.DialogInterface;
import android.os.Bundle;

import com.thinkware.florida.R;
import com.thinkware.florida.ui.dialog.SingleLineDialog;

/**
 * Created by zic325 on 2016. 10. 6..
 */

public class PopupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String message = getIntent().getStringExtra("MSG");

        SingleLineDialog dialog = new SingleLineDialog(
                this, getString(R.string.done), message);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finishWithINavi();
            }
        });
        dialog.show();
    }
}
