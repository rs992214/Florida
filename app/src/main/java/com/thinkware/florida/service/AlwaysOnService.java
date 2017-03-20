package com.thinkware.florida.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.thinkware.florida.external.service.EmergencyService;
import com.thinkware.florida.external.service.IEmergency;
import com.thinkware.florida.external.service.IEmergencyCallback;
import com.thinkware.florida.external.service.ITachoMeter;
import com.thinkware.florida.external.service.ITachoMeterCallback;
import com.thinkware.florida.external.service.IVacancyLight;
import com.thinkware.florida.external.service.IVacancyLightCallback;
import com.thinkware.florida.external.service.TachoMeterService;
import com.thinkware.florida.external.service.VacancyLightService;
import com.thinkware.florida.external.service.data.EmergencyData;
import com.thinkware.florida.external.service.data.TachoMeterData;
import com.thinkware.florida.external.service.data.VacancyLightData;
import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.server2mdt.ResponseWaitDecisionPacket;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.scenario.PreferenceUtil;
import com.thinkware.florida.ui.MainActivity;
import com.thinkware.florida.ui.MainApplication;
import com.thinkware.florida.ui.view.CallStatusView;
import com.thinkware.florida.utility.log.LogHelper;

import java.util.List;


/**
 * Created by Mihoe on 2016-09-08.
 */
public class AlwaysOnService extends Service implements View.OnTouchListener, View.OnClickListener {
    WindowManager windowManager;
    CallStatusView statusView;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private View topLeftView;
    private final int MSG_TACHOMETER = 0;
    private final int MSG_VACANCYLIGHT = 1;
    private final int MSG_EMERGENCY = 2;
    private final int MSG_WATCH_PROCESS = 3;
    private boolean passengerAboard = false;
    private boolean emergency = false;
    private final int touchSlop = 10;
    private ConfigurationLoader cfgLoader;
    private ScenarioService scenarioService;
    private Rect windowVisibleDisplayFrame;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_EMERGENCY:
                    statusView.setEmergency(emergency);
                    break;
                case MSG_TACHOMETER:
                case MSG_VACANCYLIGHT:
                    statusView.setPassengerAboard(passengerAboard);
                    break;
                case MSG_WATCH_PROCESS:
                    // 지도뷰 위에서만 아이콘이 보여지도록 처리
                    // 아이나비 Map Activity : com.thinkware.sundo.inavi3d.INavi3DActivity
                    // status bar 없는 화면에서만 보여지도록 조건 추가.
                    // --> Screen Size는 800x480으로 가정한다.
                    if (statusView != null) {


                        if (((MainApplication) getApplication()).isForegroundActivity("INavi3DActivity")){
                            if(windowVisibleDisplayFrame == null) {
                                windowVisibleDisplayFrame = new Rect();
                            }

                            if(statusView.getVisibility() == View.GONE) {
                                statusView.setVisibility(View.INVISIBLE);
                                statusView.invalidate();
                                sendEmptyMessageDelayed(MSG_WATCH_PROCESS, 500);
                                break;
                            }

                            statusView.getWindowVisibleDisplayFrame(windowVisibleDisplayFrame);
                            int h = windowVisibleDisplayFrame.height();
                            if(h == 480) {
                                //480이 아니면 status bar가 보이는 상태라고 판단한다.
                                if (statusView.getVisibility() != View.VISIBLE) {
                                    statusView.setVisibility(View.VISIBLE);
                                }
                            } else {
                                if (statusView.getVisibility() != View.GONE) {
                                    statusView.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            if (statusView.getVisibility() != View.GONE) {
                                statusView.setVisibility(View.GONE);
                            }
                        }
                    }
                    sendEmptyMessageDelayed(MSG_WATCH_PROCESS, 500);
                    break;
            }
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    LogHelper.d("onPreferenceChanged ", key);
                    if (key.equals("WaitAreaInfo")) {
                        setWaitingStatus();
                    }
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        cfgLoader = ConfigurationLoader.getInstance();
        scenarioService = ((MainApplication) getApplication()).getScenarioService();
        bindCallbackServices();

        statusView = new CallStatusView(this);
        statusView.setOnClickListener(this);
        statusView.setOnTouchListener(this);

        ScenarioService scenarioService = ((MainApplication) getApplication()).getScenarioService();
        if (scenarioService != null) {
            // 인증되었는지 여부
            statusView.setCertification(scenarioService.hasCertification());
            // 비상 상태
            statusView.setEmergency(scenarioService.getEmergencyType() == Packets.EmergencyType.Begin);
            // 승차 상태
            statusView.setPassengerAboard(scenarioService.getBoardType() == Packets.BoardType.Boarding);
        }
        // Car ID
        ConfigurationLoader cfgLoader = ConfigurationLoader.getInstance();
        if (cfgLoader != null) {
            statusView.setCarID(cfgLoader.getCarId() + "");
        }
        // 대기 중 여부
        setWaitingStatus();
        SharedPreferences preferences = getSharedPreferences("FloridaPreference", MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);

        addStatusView();

        handler.sendEmptyMessage(MSG_WATCH_PROCESS);
    }

    private void setWaitingStatus() {
        ResponseWaitDecisionPacket waitOrder = PreferenceUtil.getWaitArea(AlwaysOnService.this);
        if (waitOrder != null) {
            statusView.setWaiting(true);
        } else {
            statusView.setWaiting(false);
        }
    }

    private void addStatusView() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        SharedPreferences test = getSharedPreferences("last_position", MODE_PRIVATE);
        params.x = (int) test.getFloat("lastX", 350);
        params.y = (int) test.getFloat("lastY", 0);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(statusView, params);

        topLeftView = new View(this);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;
        windowManager.addView(topLeftView, topLeftParams);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        saveLastPosition();
        windowManager.removeView(statusView);
        windowManager.removeView(topLeftView);
        unbindCallbackServices();

        handler.removeMessages(MSG_WATCH_PROCESS);
    }

    @Override
    public void onClick(View view) {
        LogHelper.d(">> Start MainActivity!");
        Intent intent = new Intent(AlwaysOnService.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            statusView.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            int[] topLeftLocationOnScreen = new int[2];
            topLeftView.getLocationOnScreen(topLeftLocationOnScreen);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) statusView.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 3 && Math.abs(newY - originalYPos) < 3 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            windowManager.updateViewLayout(statusView, params);
            moving = true;

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (moving && !isInTouchSlop(offsetX + event.getRawX(), offsetY + event.getRawY())) {
                saveLastPosition();
                return true;
            }
        }

        return false;
    }

    private boolean isInTouchSlop(float newX, float newY) {
        if (Math.abs(newX - originalXPos) > touchSlop || Math.abs(newY - originalYPos) > touchSlop) {
            return false;
        }
        return true;
    }

    private void saveLastPosition() {
        SharedPreferences test = getSharedPreferences("last_position", MODE_PRIVATE);

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) statusView.getLayoutParams();
        SharedPreferences.Editor editor = test.edit();
        editor.putFloat("lastX", params.x);
        editor.putFloat("lastY", params.y);
        editor.commit();
    }

    //----------------------------------------------------------------------------------------------
    // 외부 기기 Service Bind - Begin
    //----------------------------------------------------------------------------------------------

    // tachometer callback service
    private ITachoMeter tachoMeterService;
    private ServiceConnection tachoMeterConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                tachoMeterService = ITachoMeter.Stub.asInterface(service);
                if (tachoMeterService != null) {
                    try {
                        tachoMeterService.registerCallback(tachoMeterCallback);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ITachoMeterCallback tachoMeterCallback = new ITachoMeterCallback.Stub() {

        @Override
        public void onReceive(TachoMeterData data) throws RemoteException {
            if (!cfgLoader.isVacancyLight()
                    && scenarioService != null && scenarioService.hasCertification()) {
                if (data != null) {
//                    if (data.getStatus() == TachoMeterData.STATUS_DRIVING
//                            || data.getStatus() == TachoMeterData.STATUS_EXTRA_CHARGE) {
//                        passengerAboard = true;
//                    } else {
//                        passengerAboard = false;
//                    }
                    // 빈차 이외에는 다 승차로 처리 한다.
                    if ((data.getStatus() & TachoMeterData.STATUS_VACANCY) > 0) {
                        passengerAboard = false;
                    } else {
                        passengerAboard = true;
                    }
                    handler.sendEmptyMessage(MSG_TACHOMETER);
                }
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d("미터기 서비스 상태: %s", status);
        }
    };
    // Vacancy Light callback  service
    private IVacancyLight vacancyLightService;
    private ServiceConnection vacancyLightConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                vacancyLightService = IVacancyLight.Stub.asInterface(service);
                try {
                    vacancyLightService.registerCallback(vacancyLightCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private IVacancyLightCallback vacancyLightCallback = new IVacancyLightCallback.Stub() {
        @Override
        public void onReceive(VacancyLightData data) throws RemoteException {
            if (cfgLoader.isVacancyLight()
                    || scenarioService == null || !scenarioService.hasCertification()) {
                if (data != null) {
                    if (data.getStatus() == VacancyLightData.RIDDEN) {
                        passengerAboard = true;
                    } else {
                        passengerAboard = false;
                    }
                    handler.sendEmptyMessage(MSG_VACANCYLIGHT);
                }
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d("빈차등 서비스 상태: %s", status);
        }
    };


    // Emergency callback service
    private IEmergency emergencyService;
    private ServiceConnection emergencyConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service != null) {
                emergencyService = IEmergency.Stub.asInterface(service);
                try {
                    emergencyService.registerCallback(emergencyCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private IEmergencyCallback emergencyCallback = new IEmergencyCallback.Stub() {

        @Override
        public void onReceive(EmergencyData data) throws RemoteException {
            if (data != null) {
                if (data.getStatus() == EmergencyData.EMERGENCY_ON) {
                    emergency = true;
                } else {
                    emergency = false;
                }
                handler.sendEmptyMessage(MSG_EMERGENCY);
            }
        }

        @Override
        public void onServiceStatus(int status) throws RemoteException {
            LogHelper.d("긴급 서비스 상태: %s", status);
        }
    };

    private void bindCallbackServices() {
        if (tachoMeterService == null) {
            Intent intent = new Intent(TachoMeterService.INTENT_ACTION);
            bindService(intent, tachoMeterConnection, Context.BIND_AUTO_CREATE);
        }

        if (vacancyLightService == null) {
            Intent intent = new Intent(VacancyLightService.INTENT_ACTION);
            bindService(intent, vacancyLightConnection, Context.BIND_AUTO_CREATE);
        }

        if (emergencyService == null) {
            Intent intent = new Intent(EmergencyService.INTENT_ACTION);
            bindService(intent, emergencyConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindCallbackServices() {
        if (tachoMeterService != null) {
            try {
                tachoMeterService.unregisterCallback(tachoMeterCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(tachoMeterConnection);
            tachoMeterService = null;
        }

        if (vacancyLightService != null) {
            try {
                vacancyLightService.unregisterCallback(vacancyLightCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(vacancyLightConnection);
            vacancyLightService = null;
        }

        if (emergencyService != null) {
            try {
                emergencyService.unregisterCallback(emergencyCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            unbindService(emergencyConnection);
            emergencyService = null;
        }
    }

}
