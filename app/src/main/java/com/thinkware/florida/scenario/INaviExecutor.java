package com.thinkware.florida.scenario;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.thinkware.florida.service.AlwaysOnService;

/**
 * Created by Mihoe on 2016-09-20.
 */
public class INaviExecutor {
    public static final String INAVI_REQUEST_RUNNAVI = "com.thinkware.externalservice.request.runnavi";
    public static final String INAVI_REQUEST_CURRENTON = "com.thinkware.externalservice.request.currenton";
    public static final String INAVI_REQUEST_RUNROUTENOW = "com.thinkware.externalservice.request.runroutepassui";
    public static final String INAVI_REQUEST_ROUTECANCEL = "com.thinkware.externalservice.request.routecancle";
    private static boolean isNavigating = false; // 승차 신호가 올라올 경우 경로 취소를 하기 위함.

    public static void startNavigationNow(Context context,String destination, double latitude, double longidute) {
        Intent intent = new Intent();
        intent.setAction(INAVI_REQUEST_RUNROUTENOW);
        intent.putExtra("GOAL1_NAME", destination);
        intent.putExtra("GOAL1_X", longidute);
        intent.putExtra("GOAL1_Y", latitude);
        context.sendBroadcast(intent);

        context.startService(new Intent(context, AlwaysOnService.class));

        isNavigating = true;
    }

    public static void cancelNavigation(Context context) {
        if (!isNavigating) {
            run(context);
            return ;
        }

        Intent intent = new Intent();
        intent.setAction(INAVI_REQUEST_ROUTECANCEL);
        context.sendBroadcast(intent);

        execute(context);

        isNavigating = false;
    }

    public static void run(Context context) {
        Intent intent = new Intent();
        intent.setAction(INAVI_REQUEST_RUNNAVI);
        context.sendBroadcast(intent);

        execute(context);
    }

    public static void currentOn(Context context) {
        Intent intent = new Intent();
        intent.setAction(INAVI_REQUEST_CURRENTON);
        context.sendBroadcast(intent);

        execute(context);
    }

    private static void execute(Context context) {
        ComponentName componentName = new ComponentName("com.thinkware.inavi3ds", "com.thinkware.sundo.inavi3d.INavi3DActivity");
        Intent activityIntent = new Intent();
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.setComponent(componentName);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);

        context.startService(new Intent(context, AlwaysOnService.class));
    }
}
