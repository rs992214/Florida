package com.thinkware.florida.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.thinkware.florida.BuildConfig;
import com.thinkware.florida.R;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.service.ScenarioService;
import com.thinkware.florida.utility.log.LogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Application
 */
public class MainApplication extends Application {

    // FATAL Exception으로 인한 App 종료시 앱을 로그를 저장하고 앱을 재시작하기 위한 Handler
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private AppStatus appStatus;
    private ArrayList<Activity> activities;
    // ScenarioService가 접근이 어려운 Class를 위해 MainApplication에 객체를 멤버로 둔다.
    private ScenarioService scenarioService;

    @Override
    public void onCreate() {
        uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandlerApplication());

        super.onCreate();
        LogHelper.setTag(getString(R.string.app_name_tag));
        LogHelper.enableDebug(BuildConfig.DEBUG);

        ConfigurationLoader.getInstance().initialize(getApplicationContext());

        activities = new ArrayList<>();
        registerActivityLifecycleCallbacks(new FloridaActivityLifecycleCallbacks());
    }

    public ScenarioService getScenarioService() {
        return scenarioService;
    }

    public void setScenarioService(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    // Get app is foreground
    public AppStatus getAppStatus() {
        return appStatus;
    }

    // check if app is return foreground
    public boolean isReturnedForground() {
        return appStatus.ordinal() == AppStatus.RETURNED_TO_FOREGROUND.ordinal();
    }

    public boolean isBackground() {
        return appStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    public boolean isLaunchedActivity(Class<?> cls) {
        String name = cls.getSimpleName();
        for (Activity act : activities) {
            if (act.getClass().getSimpleName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 최상위 Activity
     */
    public Activity getTopActivity() {
        if (activities == null) {
            return null;
        } else {
            return activities.get(activities.size() - 1);
        }
    }

    /**
     * @return 최상위 Activity의 바로 아래 Activity
     */
    public Activity getBelowActivity() {
        if (activities == null || activities.size() < 2) {
            return null;
        } else {
            return activities.get(activities.size() - 2);
        }
    }

    public Activity getActivity(Class<?> cls) {
        String name = cls.getSimpleName();
        for (Activity act : activities) {
            if (act.getClass().getSimpleName().equals(name)) {
                return act;
            }
        }
        return null;
    }

    public enum AppStatus {
        BACKGROUND,                // app is background
        RETURNED_TO_FOREGROUND,    // app returned to foreground(or first launch)
        FOREGROUND;                // app is foreground
    }

    public class FloridaActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            activities.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
                // running activity is 1,
                // app must be returned from background just now (or first launch)
                appStatus = AppStatus.RETURNED_TO_FOREGROUND;
            } else if (running > 1) {
                // 2 or more running activities,
                // should be foreground already.
                appStatus = AppStatus.FOREGROUND;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
                // no active activity
                // app goes to background
                appStatus = AppStatus.BACKGROUND;
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            activities.remove(activity);
        }
    }

    private class UncaughtExceptionHandlerApplication implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // FATAL Exception 발생시 로그를 저장하고 앱을 재실행 한다.
            if (uncaughtExceptionHandler != null && ex != null) {

                if (scenarioService != null) {
                    scenarioService.reset();
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String date = dateFormat.format(Calendar.getInstance().getTime());

                StringBuffer sb = new StringBuffer();

                Writer writer = new StringWriter();
                PrintWriter printWriter = new PrintWriter(writer);
                ex.printStackTrace(printWriter);

                Throwable cause = ex.getCause();
                while (cause != null) {
                    cause.printStackTrace(printWriter);
                    cause = cause.getCause();
                }

                printWriter.close();
                String result = writer.toString();
                sb.append(result);

                LogHelper.write("#### FATAL EXCEPTION");
                LogHelper.write(result);
                LogHelper.disableWriteLogFile();

                File directory = new File(getInternalDir());
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                try {
                    File file = new File(directory, "Fatal-" + date + "-log.txt");
                    FileOutputStream fos = new FileOutputStream(file);
                    try {
                        fos.write(sb.toString().getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //uncaughtExceptionHandler.uncaughtException(thread, ex);
            }

            restartApplication();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    private void restartApplication() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 1500,
                PendingIntent.getActivity(getApplicationContext(), 0, intent, 0));
    }

    public String getInternalDir() {
        return getExternalFilesDir(null) + File.separator + "log";
    }

    public String getExternalDir() {
        File[] files = getExternalFilesDirs(null);
        if (files == null || files.length <= 1) {
            return getExternalFilesDir(null).getAbsolutePath() + File.separator + "log";
        } else {
            return files[1].getAbsolutePath() + File.separator + "log";
        }
    }

    public boolean isForegroundActivity(String packageOrClassName) {
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
            if (tasks != null && tasks.size() > 0) {
                ActivityManager.RunningTaskInfo task = tasks.get(0);
                ComponentName cn = task.topActivity;
                String name = (cn == null) ? "" : cn.getClassName();
                return name.contains(packageOrClassName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
