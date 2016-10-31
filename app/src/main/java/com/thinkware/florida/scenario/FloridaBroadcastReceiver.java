package com.thinkware.florida.scenario;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.thinkware.florida.utility.log.LogHelper;

/**
 * Created by zic325 on 2016. 9. 23..
 */

public class FloridaBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        LogHelper.d(">> Received : " + (intent != null ? intent.getAction() : "intent is null"));
//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Intent i = new Intent(context, MainActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);
//        }
        LogHelper.write("#### 네비게이션 부팅 됨.");
    }
}
