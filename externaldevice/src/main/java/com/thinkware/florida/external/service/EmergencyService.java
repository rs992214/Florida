package com.thinkware.florida.external.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.thinkware.florida.utility.log.LogHelper;
import com.thinkware.florida.external.service.data.EmergencyData;
import com.thinkware.florida.external.service.data.ServiceStatus;
import com.thinkware.florida.external.service.emergency.EmergencyDataParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * 긴급버튼과 연동하기 위한 서비스 클래스이다.
 * 이 서비스는 두 가지 IBinder를 제공한다. 외부 기기 오픈 등 서비스 로직을 실행시키기 위한 LocalBinder 를
 * 제공하며 외부 기기의 상태를 콜백으로 전달받기 위한 AIDL을 제공한다.
 * 애플리케이션 전용의 LocalBinder를 사용하기 위해서는 아래와 같이 binding하여야 한다.<p>
 * <pre>{@code
 * Intent intent = new Intent(context, EmergencyService.class);
 * intent.putExtra("local.bind", 1);
 * bindService(intent, connection, Context.BIND_AUTO_CREATE);
 * }
 * </pre>
 */
public class EmergencyService extends Service {

    public static final String INTENT_ACTION = "com.thinkware.florida.external.service.EmergencyService";

    private static final String PORT = "/sys/class/switch/emergency/emergency_on";
    // what type of handler
    private static final int WHAT_EMERGENCY_ON = 1;
    private static final int WHAT_BROADCAST_DATA = 2;
    private static final int WHAT_BROADCAST_SERVICE_STATUS = 3;
    private static final int WHAT_TIMER = 10;

    // lock object
    private final Object lock = new Object();
    // callback for service
    private RemoteCallbackList<IEmergencyCallback> callbacks = new RemoteCallbackList<>();
    private boolean emergencyOn = false;
    // 긴급상태를 수신한 데이터
    private EmergencyData currentData = new EmergencyData();

    private int serviceStatus = ServiceStatus.SERVICE_NOT_LAUNCHED;
    private HandlerThread handlerThread;
    private Handler handler;
    private File file;
    private InputStream inputStream;
    private FileChannel readChannel;
    // 데이터 수신 Thread
    private ReadFileThread readThread;
    // 데이터를 파싱하기 위한 object
    private DataParser dataParser;
    private long testPressedTime;

    private final IBinder localBinder = new LocalBinder();

    /**
     * local binder
     */
    public class LocalBinder extends Binder {
        public EmergencyService getService() {
            return EmergencyService.this;
        }
    }

    private final IEmergency.Stub binder = new IEmergency.Stub() {

        @Override
        public boolean registerCallback(IEmergencyCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.register(callback);
            }
            return flag;
        }

        @Override
        public boolean unregisterCallback(IEmergencyCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.unregister(callback);
            }
            return flag;
        }

        @Override
        public EmergencyData getCurrentData() throws RemoteException {
            synchronized (lock) {
                return currentData;
            }
        }
    };

    /*
     * 비상 버튼 pressed를 판별하기 위한 handler callback
     */
    private Handler.Callback handlerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_EMERGENCY_ON:
                    synchronized (lock) {
                        // 4초이상 비상 버튼을 누를 경우
                        if (currentData.getStatus() == EmergencyData.EMERGENCY_ON && !emergencyOn) {
                            readThread.pause();
                            emergencyOn = true;
                            LogHelper.d("emergency: %d ms", System.currentTimeMillis() - testPressedTime);
                            Message message = handler.obtainMessage(WHAT_BROADCAST_DATA);
                            message.obj = currentData;
                            message.sendToTarget();
                        }
                    }
                    break;
                case WHAT_BROADCAST_DATA:
                    broadcastData((EmergencyData) msg.obj);
                    break;
                case WHAT_BROADCAST_SERVICE_STATUS:
                    broadcastServiceStatus(msg.arg1);
                    break;
                case WHAT_TIMER:
                    // Port node가 사라지는지 감시한다. 비상버튼은 이럴 일이 없지만...
                    File file = new File(PORT);
                    if (!file.exists()) {
                        broadcastServiceStatus(ServiceStatus.FAILED_USE_NOT_CONNECTED);
                        close();
                        handler.removeMessages(WHAT_TIMER);
                    } else {
                        handler.sendEmptyMessageDelayed(WHAT_TIMER, 5000);
                    }
                    break;
            }
            return false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            startHandlerThread();
        } catch (Exception e) {
            serviceStatus = ServiceStatus.FAILED_SET_HANDLER;
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        terminate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        int localBind = intent.getIntExtra("local.bind", 0);
        if (localBind == 1) {
            // 환경 설정을 위한 local binder
            return localBinder;
        }

        return binder;
    }

    /**
     * 파일에 연결하여 비상 버튼에 연동하는 로직을 수행한다.
     * 해당 메소드는 Local Bind를 하여 호출해준다.
     */
    public synchronized void launchService() {
        close();

        if (!isStartedHandlerThread()) {
            try {
                startHandlerThread();
            } catch (Exception e) {
                serviceStatus = ServiceStatus.FAILED_SET_HANDLER;
                e.printStackTrace();
                return;
            }
        }

        try {
            openPort();
        } catch (Exception e) {
            setServiceStatus(ServiceStatus.FAILED_PORT_OPENED);
            e.printStackTrace();
            return;
        }

        try {
            makeDataParser();
        } catch (Exception e) {
            setServiceStatus(ServiceStatus.FAILED_SET_PARSER);
            e.printStackTrace();
            return;
        }

        try {
            startThread();
        } catch (Exception e) {
            setServiceStatus(ServiceStatus.FAILED_SET_THREAD);
            e.printStackTrace();
            return;
        }

        setServiceStatus(ServiceStatus.SERVICE_LAUNCHED);
        handler.sendEmptyMessageDelayed(WHAT_TIMER, 5000);
    }

    /**
     * 기능이 동작 중인지 반환한다.
     *
     * @return boolean
     */
    public synchronized boolean isLaunched() {
        return (isStartedHandlerThread() && isOpened() && isStartedThread() && isSetDataParser());
    }

    public synchronized int getServiceStatus() {
        return serviceStatus;
    }

    /*
     * 비상상태 OFF
     */
    public synchronized void setEmergencyOff() {
        synchronized (lock) {
            if (emergencyOn && currentData != null) {
                currentData.setStatus(0);
                emergencyOn = false;
                readThread.setSleep(500);
                readThread.unpause();
                Message message = handler.obtainMessage(WHAT_BROADCAST_DATA);
                message.obj = currentData;
                message.sendToTarget();
            }
        }
    }

    /*
     * 비상상태 ON
     */
    public synchronized void setEmergencyOn() {
        synchronized (lock) {
            if (!emergencyOn) {
                currentData.setStatus(EmergencyData.EMERGENCY_ON);
                readThread.pause();
                emergencyOn = true;
                Message message = handler.obtainMessage(WHAT_BROADCAST_DATA);
                message.obj = currentData;
                message.sendToTarget();
            }
        }
    }

    private void terminate() {
        stopHandlerThread();
        close();
    }

    private void close() {
        stopThread();
        closePort();
    }

    /**
     * 내부적으로 사용하는 Handler Thread를 생성한다.
     *
     * @throws Exception
     */
    private void startHandlerThread() throws Exception {
        handlerThread = new HandlerThread(EmergencyService.class.getSimpleName() + "Thread");
        if (handlerThread == null) {
            throw new NullPointerException();
        }

        try {
            handlerThread.start();
        } catch (Exception e) {
            throw e;
        }

        handler = new Handler(handlerThread.getLooper(), handlerCallback);
        if (handlerThread == null || handler == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Handler Thread를 종료한다.
     */
    private void stopHandlerThread() {
        if (handlerThread != null) {
            handlerThread.quit();
            handler = null;
        }
    }

    private boolean isStartedHandlerThread() {
        return (handlerThread != null && handlerThread.isAlive() && handler != null);
    }

    /**
     * 파일에 연결한다
     *
     * @throws Exception
     */
    private void openPort() throws Exception {
        file = new File(PORT);
        inputStream = new FileInputStream(file);
        readChannel = ((FileInputStream) inputStream).getChannel();
        if (file == null || readChannel == null) {
            throw new NullPointerException();
        }
    }

    /**
     * 파일을 닫는다.
     */
    private void closePort() {
        try {
            if (readChannel != null) {
                readChannel.close();
                readChannel = null;
            }
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        file = null;
    }

    private boolean isOpened() {
        return (isStartedHandlerThread() && file != null && inputStream != null && readChannel != null);
    }

    /*
     * 데이터를 파싱하기 위한 클래스를 설정한다.
     */
    private void makeDataParser() throws Exception {
        if (dataParser != null) {
            dataParser = null;
        }

        dataParser = new EmergencyDataParser();
        if (dataParser == null) {
            throw new NullPointerException();
        }

        dataParser.setListener(new DataParseCallback<Integer>() {
            @Override
            public void onParse(Integer status) {
                synchronized (lock) {
                    if (!emergencyOn && currentData.getStatus() != status) {
                        currentData.setStatus(status);
                        if (status == EmergencyData.EMERGENCY_ON) {
                            // 비상 버튼을 누르고 있을 때는 빠르게 읽는다.
                            readThread.setSleep(50);
                            testPressedTime = System.currentTimeMillis();
                            // 4초 체크
                            handler.sendEmptyMessageDelayed(WHAT_EMERGENCY_ON, 4000);
                        } else {
                            // 비상 버튼을 누르고 있지 않을 때는 느리게 읽는다.
                            readThread.setSleep(500);
                            handler.removeMessages(WHAT_EMERGENCY_ON);
                        }
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                close();
                serviceStatus = ServiceStatus.FAILED_READ_DATA;
                setServiceStatus(ServiceStatus.FAILED_READ_DATA);
            }
        });
    }

    private boolean isSetDataParser() {
        return (dataParser != null);
    }

    /*
     * start thread
     */
    private void startThread() throws Exception {
        try {
            readThread = new ReadFileThread(readChannel, dataParser);
            readThread.setSleep(500);
            readThread.start();
        } catch (Exception e) {
            throw e;
        }
    }

    /*
     * stop thread
     */
    private void stopThread() {
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
    }

    private boolean isStartedThread() {
        return (readThread != null && readThread.isAlive());
    }

    /*
    * broadcast
    */
    private void broadcastData(EmergencyData data) {
        if (data == null) return;
        int n = callbacks.beginBroadcast();

        for (int i = 0; i < n; i++) {
            try {
                callbacks.getBroadcastItem(i).onReceive(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        callbacks.finishBroadcast();
    }

    private void setServiceStatus(int status) {
        this.serviceStatus = status;

        if (handler == null) return;

        Message message = handler.obtainMessage(WHAT_BROADCAST_SERVICE_STATUS);
        message.arg1 = status;
        message.sendToTarget();
    }

    private void broadcastServiceStatus(int status) {
        int n = callbacks.beginBroadcast();

        for (int i = 0; i < n; i++) {
            try {
                callbacks.getBroadcastItem(i).onServiceStatus(status);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        callbacks.finishBroadcast();
    }
}
