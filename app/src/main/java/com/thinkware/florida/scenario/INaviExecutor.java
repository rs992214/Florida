package com.thinkware.florida.scenario;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.thinkware.florida.service.AlwaysOnService;

import java.util.List;

/**
 * Created by Mihoe on 2016-09-20.
 */
public class INaviExecutor {
    public static final String INAVI_REQUEST_RUNNAVI = "com.thinkware.externalservice.request.runnavi";
    public static final String INAVI_REQUEST_CURRENTON = "com.thinkware.externalservice.request.currenton";
    public static final String INAVI_REQUEST_RUNROUTENOW = "com.thinkware.externalservice.request.runroutepassui";
    public static final String INAVI_REQUEST_ROUTECANCEL = "com.thinkware.externalservice.request.routecancle";

    private static final String InaviPackageName="com.thinkware.inavi3ds";
    private static final String InaviActivityName="com.thinkware.sundo.inavi3d.INavi3DActivity";

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

    /*
    private static void executeInDirect(Context context) {
        ComponentName componentName = new ComponentName(InaviPackageName, InaviActivityName);
        Intent activityIntent = new Intent();
        activityIntent.setAction(Intent.ACTION_MAIN);
        activityIntent.setComponent(componentName);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(activityIntent);

        context.startService(new Intent(context, AlwaysOnService.class));
    }
    */

    private static void execute(Context context) {
        startInavi(context);

        context.startService(new Intent(context, AlwaysOnService.class));
    }

    // 아이나비 실행 함수 예제
    private static void startInavi(Context context) {
        if (isINaviRunning()) {
            // 아이나비 실행된 상태
            Intent intent = new Intent();
            intent.setAction("com.thinkware.sundo.inavi3d.action.ExtCommand");
            intent.putExtra("EXTTYPE", "ExtCurrentOn");
            context.sendBroadcast(intent);
        } else {
            // 아이나비 미실행 상태
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(InaviPackageName );
            context.startActivity(intent);
        }
    }

    // 아이나비 실행 여부 체크 함수 예제
    private static boolean isINaviRunning(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) { // 11
            final ActivityManager activityManager =(ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            final List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

            ActivityManager.RecentTaskInfo recentTaskInfo = null;
            int taskInfoCount = recentTasks.size();

            for (int i = taskInfoCount - 1; i >= 0; i--) {
                recentTaskInfo = recentTasks.get(i);

                if(recentTaskInfo != null && recentTaskInfo.id > -1) {
                    if (recentTaskInfo.baseIntent != null) {
                        String packageName = recentTaskInfo.baseIntent.getComponent().getPackageName();
                        if (packageName.equals(InaviPackageName) == true) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
