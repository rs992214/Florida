package com.thinkware.florida.network.manager;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.thinkware.florida.utility.log.LogHelper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zic325 on 2016. 9. 6..
 */
public class Connector extends Thread {

    //---------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------
    public static final int MSG_CONNECTED = 1; // socket이 연결 됨
    public static final int MSG_RECEIVED_DATA = 2; // 서버로 부터 데이터를 받음
    public static final int MSG_DISCONNECTED = 3; // socket이 끊어 짐
    public static final int MSG_CONNECT_FAILED = 4; // socket connect 중 Exception 발생
    public static final int MSG_SOCKET_CREATE_FAILED = 5; // socket 생성 실패
    public static final int MSG_STREAM_CREATE_FAILED = 6; // stream 생성 실패
    public static final int TIMEOUT = 20 * 1000;

    private String ip;
    private int port;
    private int timeOut;
    private Socket socket;
    private DataInputStream buffRecv;
    private DataOutputStream buffSend;
    // socket이 끊어 졌을 경우 connection을 새로 맺고 패킷을 전송해야 하므로 Queue를 둔다.
    private LinkedBlockingQueue<byte[]> requestPacketQ;

    private Handler handler;
    private boolean isConnected;

    //---------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------
    public Connector() {
       this(null);
    }

    public Connector(Handler handler) {
        this.handler = handler;
        isConnected = false;
        requestPacketQ = new LinkedBlockingQueue<>();
        timeOut = TIMEOUT;
    }

    //---------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------
    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected();
    }

    public void setTimeout(int timeOut) {
        this.timeOut = timeOut;
    }

    public void connect(String ip, int port) {
        LogHelper.d(">> Connecting... IP : " + ip + ", PORT : " + port);
        this.ip = ip;
        this.port = port;
        start();
    }

    public void disconnect() {
        LogHelper.d(">> Disconnecting...");
        this.interrupt();

        if (socket != null) {
            try {
                socket.close();
                socket = null;

                if (buffRecv != null) {
                    buffRecv.close();
                    buffRecv = null;
                }

                if (buffSend != null) {
                    buffSend.flush();
                    buffSend.close();
                    buffSend = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        requestPacketQ.clear();

        isConnected = false;
        LogHelper.d(">> Disconnected : " + ip + ", PORT : " + port);
        sendMessage(MSG_DISCONNECTED);
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void request(@NonNull byte[] bytes) {
        try {
            requestPacketQ.put(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------
    // Override
    //---------------------------------------------------------------------------------
    @Override
    public void run() {
        if (!connect()) {
            LogHelper.d(">> Failure connect : " + ip + ", PORT : " + port);
            sendMessage(MSG_CONNECT_FAILED);
            return;
        }

        if (socket == null) {
            LogHelper.d(">> Failure connect (Socket create fail) : " + ip + ", PORT : " + port);
            sendMessage(MSG_SOCKET_CREATE_FAILED);
            return;
        }

        try {
            buffRecv = new DataInputStream(socket.getInputStream());
            buffSend = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            LogHelper.d(">> Failure connect (Stream create fail) : " + ip + ", PORT : " + port);
            sendMessage(MSG_STREAM_CREATE_FAILED);
            return;
        }

        isConnected = true;
        LogHelper.d(">> Connected IP : " + ip + ", PORT : " + port);
        sendMessage(MSG_CONNECTED);

        while (!isInterrupted()) {
            if (!isConnected()) {
                isConnected = false;
                continue;
            }
            try {
                // 패킷 요청
                int queueSize = requestPacketQ.size();
                if (queueSize > 0) {
                    byte[] buffers = requestPacketQ.take();
                    LogHelper.d(">> Send (" + buffers.length + "): " + getPrettyByteArray(buffers));

                    synchronized (buffSend) {
                        buffSend.write(buffers, 0, buffers.length);
                        buffSend.flush();
                    }
                }

                if (buffRecv == null) {
                    continue;
                }

                // 패킷 응답
                synchronized (buffRecv) {
                    if (socket != null && !socket.isClosed()) {
                        int available = buffRecv.available();
                        if (available > 0) {
                            byte[] bytes = new byte[available];
                            buffRecv.read(bytes);
                            LogHelper.d(">> Receive (" + available + ") : " + getPrettyByteArray(bytes));

                            sendMessage(MSG_RECEIVED_DATA, bytes);
                        }
                    }
                }
            } catch (SocketException e2) {
                // 서버 소켓이 닫히거나 끊어질 경우
                e2.printStackTrace();
                isConnected = false;
                LogHelper.write("#### SocketException " + getStackTrace(e2));
            } catch (IOException e) {
                e.printStackTrace();
                isConnected = false;
                LogHelper.write("#### IOException " + getStackTrace(e));
            } catch (NullPointerException e) {
                e.printStackTrace();
                isConnected = false;
                LogHelper.write("#### NullPointerException " + getStackTrace(e));
            } catch (InterruptedException e) {
                e.printStackTrace();
                LogHelper.write("#### InterruptedException " + getStackTrace(e));
            }
        }
    }


    //---------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------
    private boolean connect() {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(ip), port);
            socket = new Socket();
            socket.setKeepAlive(true);
            socket.connect(socketAddress, timeOut);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void sendMessage(int what) {
        if (handler != null) {
            handler.sendEmptyMessage(what);
        }
    }

    private void sendMessage(int what, @NonNull byte[] bytes) {
        if (handler != null) {
            Message msg = Message.obtain();
            msg.what = what;
            msg.obj = bytes;
            handler.sendMessage(msg);
        }
    }

    private String getPrettyByteArray(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            sb.append(String.format("0x%02X ", b & 0xff));
        }
        return sb.toString();
    }

    private String getStackTrace(Throwable th) {
        if (th == null) {
            return "";
        }

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        Throwable cause = th;
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        final String stacktraceAsString = result.toString();
        printWriter.close();

        return stacktraceAsString;
    }
}