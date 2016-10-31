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
import com.thinkware.florida.external.service.data.VacancyLightData;
import com.thinkware.florida.external.service.vacancylight.VacancyLightDataParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 빈차등과 연동하기 위한 서비스 클래스이다.
 * 이 서비스는 두 가지 IBinder를 제공한다. 외부 기기 오픈 등 서비스 로직을 실행시키기 위한 LocalBinder 를
 * 제공하며 외부 기기의 상태를 콜백으로 전달받기 위한 AIDL을 제공한다.
 * 애플리케이션 전용의 LocalBinder를 사용하기 위해서는 아래와 같이 binding하여야 한다.<p>
 * <pre>{@code
 * Intent intent = new Intent(context, VacancyLightService.class);
 * intent.putExtra("local.bind", 1);
 * bindService(intent, connection, Context.BIND_AUTO_CREATE);
 * }
 * </pre>
 */
public class VacancyLightService extends Service {

    public static final String INTENT_ACTION = "com.thinkware.florida.external.service.VacancyLightService";

    private static final String PORT_ROOT = "/dev";
    private static final String DEFAULT_PORT = "/dev/ttyUSB1";
    // what type of handler
    private static final int WHAT_BROADCAST_DATA = 2;
    private static final int WHAT_BROADCAST_SERVICE_STATUS = 3;
    private static final int WHAT_TIMER = 10;

    // lock object
    private final Object lock = new Object();
    // callback for service
    private RemoteCallbackList<IVacancyLightCallback> callbacks = new RemoteCallbackList<>();
    // 빈차등에 데이터를 쓰기 위한 Queue
    private BlockingQueue<byte[]> wQueue = new LinkedBlockingQueue<>();
    // 빈차등에서 수신한 데이터
    private VacancyLightData currentData;

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
        public VacancyLightService getService() {
            return VacancyLightService.this;
        }
    }

    // aidl binder
    private final IVacancyLight.Stub binder = new IVacancyLight.Stub() {

        @Override
        public boolean registerCallback(IVacancyLightCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.register(callback);
            }
            return flag;
        }

        @Override
        public boolean unregisterCallback(IVacancyLightCallback callback) throws RemoteException {
            boolean flag = false;
            if (callback != null) {
                flag = callbacks.unregister(callback);
            }
            return flag;
        }

        @Override
        public VacancyLightData getCurrentData() throws RemoteException {
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
                    broadcastData((VacancyLightData) msg.obj);
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
     * Serial Port에 연결하여 빈차등과 연동하는 로직을 수행한다.
     * 해당 메소드는 Local Bind를 하여 호출해준다.
     */
    public synchronized void launchService() {
        close();

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

    /**
     * 현재 서비스의 기능 동작 상태를 반환한다.
     *
     * @return int {@link com.thinkware.florida.external.service.data.ServiceStatus}
     */
    public synchronized int getServiceStatus() {
        return serviceStatus;
    }

    /*
     * 예약으로 설정한다.
     */
    public synchronized void setReservation() {
        try {
            wQueue.add(new byte[]{(byte) 0x90, 0x04, (byte) 0x90 | 0x04});
        } catch (IllegalStateException e) {
            serviceStatus = ServiceStatus.FAILED_SET_QUEUE;
            e.printStackTrace();
        }
    }

    /**
     * 빈차로 설정한다.
     */
    public synchronized void setVacancy() {
        try {
            wQueue.add(new byte[]{(byte) 0x90, 0x01, (byte) 0x90 | 0x01});
        } catch (IllegalStateException e) {
            serviceStatus = ServiceStatus.FAILED_SET_QUEUE;
            e.printStackTrace();
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
        handlerThread = new HandlerThread(VacancyLightService.class.getSimpleName() + "Thread");
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

    /*
     * Port를 open한다.
     */
    private void openPort() throws Exception {
        // 단말 재부팅 등의 이유로 Port의 순서가 달라질 수 있는데,
        // 두번째 Port를 빈차등이라고 간주하고 연결한다.
        File file = new File(PORT_ROOT);
        if (file != null && file.exists()) {
            String[] fileNames = file.list(new PortFilter());
            if (fileNames != null && fileNames.length > 1) {
                Arrays.sort(fileNames);
                portPath = PORT_ROOT + File.separator + fileNames[1];
            }
        }

        serialPort = new SerialPort(new File(portPath), 2400, SerialPort.O_RDWR | SerialPort.O_APPEND);
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
        return (isStartedHandlerThread() && serialPort != null && inputStream != null && outputStream != null);
    }

    /*
     * 데이터를 파싱하기 위한 클래스를 설정한다.
     */
    private void makeDataParser() throws Exception {
        if (dataParser != null) {
            dataParser = null;
        }

        // 쓰기 Queue 초기화
        wQueue.clear();

        dataParser = new VacancyLightDataParser();
        if (dataParser == null) {
            throw new NullPointerException();
        }

        dataParser.setListener(new DataParseCallback<VacancyLightData>() {
            @Override
            public void onParse(final VacancyLightData data) {
                synchronized (lock) {
                    if (data != null) {
                        // 빈차등에서 신호를 받으면 현재 상태와 비교하여 다르면 송출한다.
                        if (currentData == null || currentData.getStatus() != data.getStatus()) {
                            currentData = data;
                            Message message = handler.obtainMessage(WHAT_BROADCAST_DATA);
                            message.obj = data;
                            message.sendToTarget();
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
    private void broadcastData(VacancyLightData data) {
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
