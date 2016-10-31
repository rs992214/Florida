package com.thinkware.florida.network.manager;

import android.os.Handler;
import android.os.Message;

import com.thinkware.florida.utility.log.LogHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zic325 on 2016. 9. 6..
 */
public class ATCommandManager {

    public interface IModemListener {
        void onModemResult(String result);
    }

    //---------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------
    public static final String IP = "192.168.39.1";
    public static final int PORT = 9999;

    public static final String CMD_MODEM_NO = "AT+CNUM";
    public static final String CMD_BOOT_LOG = "AT$$CELL_LOG=0";
    public static final String CMD_VERSION = "AT$$SVER";
    public static final String CMD_USIM_NO = "AT+ICCID";
    public static final String CMD_DEBUG_INFO = "AT$$DSCREEN?";
    public static final String CMD_USIM_STATE = "AT+CPIN?";

    private static ATCommandManager instance = new ATCommandManager();

    private Connector connector;
    private IModemListener listener;
    private Timer timerDisconnect;
    private String command;

    //---------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------
    private ATCommandManager() {
    }

    public static ATCommandManager getInstance() {
        return instance;
    }

    //---------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------
    public synchronized void request(String command, IModemListener listener) {
        LogHelper.write(">> REQ " + command);
        this.command = command;

        this.listener = listener;
        if (connector == null
                || !connector.isConnected()) {
            connector = new Connector();
            connector.connect(IP, PORT);
        }

        if (connector != null) {
            connector.setHandler(resultHandler);
            connector.request((command + "\r").getBytes());
        }

        if (timerDisconnect != null) {
            timerDisconnect.cancel();
            timerDisconnect = null;
        }

        timerDisconnect = new Timer();
        timerDisconnect.schedule(new TimerTask() {
            @Override
            public void run() {
                if (connector != null) {
                    connector.disconnect();
                    connector = null;
                }
            }
        }, 5000);

    }

    public String parseModemNumber(String result) {
        // result -> +CNUM: ,"01220185940",129
        // 쉼표를 기준으로 파싱하도록 한다.

        int startIdx = result.indexOf(",\"");
        int lastIdx = result.indexOf("\",");
        if (startIdx > 0 && lastIdx > 0) {
            return  result.substring(startIdx+2, lastIdx);
        }
        return "";
    }

    //---------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------
    private String parse(byte[] bytes) {
        if (bytes == null || bytes.length <= 0) {
            return "";
        }

        String str = "Encoding Fail(EUC-KR)";
        try {
            str = new String(bytes, 0, bytes.length, "EUC-KR");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // byte 중에 쓰레기값이 들어오는 케이스가 있어 예외처리 추가 한다.
        char szGarbage = 0xFFFD;
        str = str.replace('\r', ' ');
        str = str.replace(szGarbage, ' ');

        return str.trim();
    }

    private Handler resultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Connector.MSG_CONNECTED:
                    LogHelper.write("#### Modem connected.");
                    break;
                case Connector.MSG_CONNECT_FAILED:
                    LogHelper.write("#### Modem connect failed.");
                    break;
                case Connector.MSG_SOCKET_CREATE_FAILED:
                    LogHelper.write("#### Modem socket create failed.");
                    break;
                case Connector.MSG_STREAM_CREATE_FAILED:
                    LogHelper.write("#### Modem stream create failed.");
                    break;
                case Connector.MSG_DISCONNECTED:
                    LogHelper.write("#### Modem disconnected.");
                    break;
                case Connector.MSG_RECEIVED_DATA:
                    byte[] bytes = (byte[]) msg.obj;
                    String result = parse(bytes);
                    LogHelper.write(">> RES " + command + " = " + result.length());
                    if (listener != null) {
                        listener.onModemResult(result);
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
