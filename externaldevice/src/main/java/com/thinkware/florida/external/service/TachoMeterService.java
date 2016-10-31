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

import com.thinkware.florida.external.serial.SerialPort;
import com.thinkware.florida.external.service.data.ServiceStatus;
import com.thinkware.florida.external.service.data.TachoMeterData;
import com.thinkware.florida.external.service.tachometer.TachoMeterDataParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 미터기와 연동하기 위한 서비스 클래스이다.
 * 이 서비스는 두 가지 IBinder를 제공한다. 외부 기기 오픈 등 서비스 로직을 실행시키기 위한 LocalBinder 를
 * 제공하며 외부 기기의 상태를 콜백으로 전달받기 위한 AIDL을 제공한다.
 * 애플리케이션 전용의 LocalBinder를 사용하기 위해서는 아래와 같이 binding하여야 한다.<p>
 * <pre>{@code
 * Intent intent = new Intent(context, TachoMeterService.class);
 * intent.putExtra("local.bind", 1);
 * bindService(intent, connection, Context.BIND_AUTO_CREATE);
 * }
 * </pre>
 */
public class TachoMeterService extends Service {

    public static final String INTENT_ACTION = "com.thinkware.florida.external.service.TachoMeterService";

    private static final String PORT_ROOT = "/dev";
    private static final String DEFAULT_PORT = "/dev/ttyUSB0";
    // what type of handler
    private static final int WHAT_BROADCAST_DATA = 2;
    private static final int WHAT_BROADCAST_SERVICE_STATUS = 3;
    private static final int WHAT_TIMER = 10;

    // lock object
    private final Object lock = new Object();
    // callback for service
    private RemoteCallbackList<ITachoMeterCallback> callbacks = new RemoteCallbackList<>();
    // 미터기에 데이터를 쓰기 위한 Queue
    private BlockingQueue<byte[]> wQueue = new LinkedBlockingQueue<>();
    // 미터기에서 수신한 데이터
    private TachoMeterData currentData;

    // 미터기 종류
    private int type = -1;
    private int serviceStatus = ServiceStatus.SERVICE_NOT_LAUNCHED;
    private HandlerThread handlerThread;
    private Handler handler;
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    // 데이터 수신 Thread
    private ReadSerialPortThread readSerialPortThread;
    // 데이터 송신 Thread
    private WriteThread writeThread;
    // 데이터를 파싱하기 위한 object
    private DataParser dataParser;
    private String portPath = DEFAULT_PORT;
    private final IBinder localBinder = new LocalBinder();

    /**
     * local binder
     */
    public class LocalBinder extends Binder {
        public TachoMeterService getService() {
            return TachoMeterService.this;
        }
    }

    /*
     * aidl binder
     */
    private final ITachoMeter.Stub binder = new ITachoMeter.Stub() {

        @Override
        public boolean registerCallback(ITachoMeterCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.register(callback);
            }
            return flag;
        }

        @Override
        public boolean unregisterCallback(ITachoMeterCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.unregister(callback);
            }
            return flag;
        }

        @Override
        public TachoMeterData getCurrentData() throws RemoteException {
            synchronized (lock) {
                return currentData;
            }
        }
    };

    private Handler.Callback handlerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_BROADCAST_DATA:
                    broadcastData((TachoMeterData) msg.obj);
                    break;
                case WHAT_BROADCAST_SERVICE_STATUS:
                    broadcastServiceStatus(msg.arg1);
                    break;
                case WHAT_TIMER:
                    // Port node가 사라지는지 감시한다.
                    File file = new File(portPath);
                    if (!file.exists()) {
                        handler.removeMessages(WHAT_TIMER);
                        close();
                        broadcastServiceStatus(ServiceStatus.FAILED_USE_NOT_CONNECTED);
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
     * 미터기 종류를 설정한다.
     *
     * @param type 미터기 종류
     */
    public synchronized void setTachoMeterType(int type) {
        this.type = type;
    }

    /**
     * 미터기 종류를 변경하고 재시작한다.
     *
     * @param type 미터기 종류
     */
    public synchronized void changeTachoMeterType(int type) {
        this.type = type;
        launchService();
    }

    /**
     * Serial Port에 연결하여 미터기와 연동하는 로직을 수행한다.
     * 해당 메소드는 Local Bind를 하여 호출해준다.
     */
    public synchronized void launchService() {
        close();
        tryToLaunchService();
    }

    public synchronized boolean isLaunched() {
        return (isStartedHandlerThread() && isOpened() && isStartedThread() && isSetDataParser());
    }

    /**
     * 현재 서비스의 기능 동작 상태를 반환한다.
     *
     * @return int {@link com.thinkware.florida.external.service.data.ServiceStatus}
     */
    public synchronized int getServiceStatus() {
        return serviceStatus;
    }

    private void tryToLaunchService() {
        // handler thread는 생성되어 있을테지만 혹시라도 Service create시에 생성되어 있지 않을 때를
        // 대비하여 검사한다.
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

        loadTachoMeter();
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
        handlerThread = new HandlerThread(TachoMeterService.class.getSimpleName() + "Thread");
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
     * 현재 설정되어 있는 미터기 종류를 반환한다.
     *
     * @return 미터기 종류 인덱스
     */
    public synchronized int getTachoMeterType() {
        return this.type;
    }

    /**
     * Port Open 이외의 로직을 수행한다.
     */
    private void loadTachoMeter() {
        if (isOpened()) {
            try {
                makeDataParser(type);
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

            try {
                wQueue.add(dataParser.getInitCommandData());
            } catch (IllegalStateException e) {
                setServiceStatus(ServiceStatus.FAILED_SET_QUEUE);
                e.printStackTrace();
                return;
            }

            setServiceStatus(ServiceStatus.SERVICE_LAUNCHED);
            handler.sendEmptyMessageDelayed(WHAT_TIMER, 5000);
        }
    }

    private void openPort() throws Exception {
        // 단말 재부팅 등의 이유로 Port의 순서가 달라질 수 있는데,
        // 첫번째 Port를 미터기라고 간주하고 연결한다.
        File file = new File(PORT_ROOT);
        if (file != null && file.exists()) {
            String[] fileNames = file.list(new PortFilter());
            if (fileNames != null && fileNames.length > 0) {
                Arrays.sort(fileNames);
                portPath = PORT_ROOT + File.separator + fileNames[0];
            }
        }

        serialPort = new SerialPort(new File(portPath), 19200, SerialPort.O_RDWR | SerialPort.O_APPEND);
        inputStream = serialPort.getInputStream();
        outputStream = serialPort.getOutputStream();
        if (serialPort == null || inputStream == null || outputStream == null) {
            throw new Exception("Can't open port.");
        }
    }

    /**
     * Port를 닫는다.
     */
    private void closePort() {
        try {
            if (inputStream != null) {
                inputStream.close();
                inputStream = null;
            }

            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
    }

    private boolean isOpened() {
        return (serialPort != null && inputStream != null && outputStream != null);
    }

    /*
     * 데이터를 파싱하기 위한 클래스를 설정한다.
     */
    private void makeDataParser(int type) throws Exception {
        if (dataParser != null) {
            dataParser = null;
        }

        wQueue.clear();

        dataParser = TachoMeterDataParserFactory.getDataParser(type);
        if (dataParser == null) {
            throw new NullPointerException();
        }

        dataParser.setListener(new DataParseCallback<TachoMeterData>() {
            @Override
            public void onParse(TachoMeterData data) {
                synchronized (lock) {
                    currentData = data;
                    Message message = handler.obtainMessage(WHAT_BROADCAST_DATA);
                    message.obj = data;
                    message.sendToTarget();
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
     * start threads
     */
    private void startThread() throws Exception {
        try {
            readSerialPortThread = new ReadSerialPortThread(inputStream, dataParser);
            writeThread = new WriteThread(outputStream, wQueue, new DataParseCallback() {
                @Override
                public void onError(Exception e) {
                    close();
                    serviceStatus = ServiceStatus.FAILED_WRITE_DATA;
                    setServiceStatus(ServiceStatus.FAILED_WRITE_DATA);
                }
            });

            readSerialPortThread.start();
            writeThread.start();
        } catch (Exception e) {
            throw e;
        }
    }

    /*
     * stop threads
     */
    private void stopThread() {
        if (readSerialPortThread != null) {
            readSerialPortThread.interrupt();
            readSerialPortThread = null;
        }

        if (writeThread != null) {
            writeThread.interrupt();
            writeThread = null;
        }
    }

    private boolean isStartedThread() {
        return (readSerialPortThread != null && readSerialPortThread.isAlive()
                && writeThread != null && writeThread.isAlive());
    }

    /*
    * broadcast
    */
    private void broadcastData(TachoMeterData data) {
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
