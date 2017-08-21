package com.thinkware.florida.network.manager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;
import com.thinkware.florida.network.packets.ResponsePacket;
import com.thinkware.florida.network.packets.mdt2server.LivePacket;
import com.thinkware.florida.scenario.ConfigurationLoader;
import com.thinkware.florida.utility.log.LogHelper;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by zic325 on 2016. 9. 6..
 */
public class NetworkManager {

    //---------------------------------------------------------------------------------
    // fields
    //---------------------------------------------------------------------------------
    public static final String IP_COMMER = "58.180.28.213";
    public static final String IP_DEV = "183.99.72.173";
    public static final String IP_LIVE_PACKET = "58.180.28.212";
    public static final int PORT_DEV = 3000;


    private static NetworkManager instance = new NetworkManager();

    private Connector connector;
    private Connector livePacketConnector;
    private String ip;
    private int port;
    private ArrayList<NetworkListener> networkListeners;

    //---------------------------------------------------------------------------------
    // constructor
    //---------------------------------------------------------------------------------
    private NetworkManager() {
        networkListeners = new ArrayList<>();
    }

    public static NetworkManager getInstance() {
        return instance;
    }

    //---------------------------------------------------------------------------------
    // public
    //---------------------------------------------------------------------------------
    public void addNetworkListener(NetworkListener listener) {
        networkListeners.add(listener);
    }

    public void removeNetworkListener(NetworkListener listener) {
        networkListeners.remove(listener);
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isConnected() {
        return connector != null && connector.isConnected();
    }

    public void connect(String ip, int port) {
        if (connector != null && connector.isConnected()) {
            LogHelper.d(">> Already connected.");
        } else {
            this.ip = ip;
            this.port = port;

            connector = new Connector(resultHandler);
            connector.connect(this.ip, this.port);
        }
    }

    public void livePacketConnect(String ip, int port) {
        if (livePacketConnector != null && livePacketConnector.isConnected()) {
            LogHelper.d(">> Already connected.");
        } else {
            livePacketConnector = new Connector(resultHandler);
            livePacketConnector.connect(ip, port);
        }
    }

    public void disconnect() {
        if (connector != null && connector.isConnected()) {
            connector.disconnect();
        }
        connector = null;
    }

    public void livePacketDisconnect() {
        if (livePacketConnector != null && livePacketConnector.isConnected()) {
            livePacketConnector.disconnect();
        }
        livePacketConnector = null;
    }

    /**
     * 서버에 패킷을 전송한다.
     * 연결이 끊어져 있는 경우 connection을 새로 맺고 전송 한다.
     * @param context
     * @param packet 전달하고자 하는 packet
     */
    public synchronized void request(Context context, RequestPacket packet) {
        LogHelper.write(">> REQ " + packet);
        // 연결이 끊어지는 케이스
        // 1. 단말의 네트워크 오류 : API 이용
        // 2. 서버의 소켓이 닫히는 경우 : Buffer Write시 Exception으로 판단
        // 3. 단말의 소켓이 닫히는 경우 : Socket의 isConnected()로 판단
        // 주기전송과 Live 패킷이 polling 되므로 위 3개의 케이스로 연결 상태 판단 가능하다.


        // 2017. 08. 17 - 권석범
        // Live 패킷 전송 별도 서버로 분기 처리 및 livePacketConnect, livePacketDisconnect 메서드 추가
        if ( packet instanceof LivePacket) {
            if (livePacketConnector == null
                    || !livePacketConnector.isConnected()
                    || !isAvailableNetwork(context)) {
                livePacketDisconnect();
                port = ConfigurationLoader.getInstance().getCallServerPort();
                livePacketConnect(IP_LIVE_PACKET, port);
            }
            if (livePacketConnector != null) {
                livePacketConnector.request(packet.toBytes());
            }
        } else {
            if (connector == null
                    || !connector.isConnected()
                    || !isAvailableNetwork(context)) {
                disconnect();
                ip = ConfigurationLoader.getInstance().getCallServerIp();
                port = ConfigurationLoader.getInstance().getCallServerPort();
                connect(ip, port);
            }
            if (connector != null) {
                connector.request(packet.toBytes());
            }
        }
    }

    /**
     * 네트워크 상태 체크
     * @param context
     * @return 네트워트 사용 가능 상태
     */
    public boolean isAvailableNetwork(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = mgr.getActiveNetworkInfo();
        return info != null && info.isAvailable() && info.isConnected();
    }

    //---------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------
    private Handler resultHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Connector.MSG_CONNECTED:
//                    LogHelper.write(">> Connected.");
                    for (NetworkListener l : networkListeners) {
                        l.onConnectedServer();
                    }
                    break;
                case Connector.MSG_CONNECT_FAILED:
//                    LogHelper.d(">> NetworkManager.MSG_CONNECT_FAILED");
                    for (NetworkListener l : networkListeners) {
                        l.onDisconnectedServer(NetworkListener.ErrorCode.ConnectFailed);
                    }
                    break;
                case Connector.MSG_SOCKET_CREATE_FAILED:
//                    LogHelper.d(">> NetworkManager.MSG_SOCKET_CREATE_FAILED");
                    for (NetworkListener l : networkListeners) {
                        l.onDisconnectedServer(NetworkListener.ErrorCode.SocketCreateFailed);
                    }
                    break;
                case Connector.MSG_STREAM_CREATE_FAILED:
//                    LogHelper.d(">> NetworkManager.MSG_SOCKET_CREATE_FAILED");
                    for (NetworkListener l : networkListeners) {
                        l.onDisconnectedServer(NetworkListener.ErrorCode.StreamCreateFailed);
                    }
                    break;
                case Connector.MSG_DISCONNECTED:
//                    LogHelper.write(">> Disonnected.");
                    for (NetworkListener l : networkListeners) {
                        l.onDisconnectedServer(NetworkListener.ErrorCode.Disconnected);
                    }
                    break;
                case Connector.MSG_RECEIVED_DATA:
//                    LogHelper.d(">> NetworkManager.MSG_RECEIVED_DATA");
                    byte[] bytes = (byte[]) msg.obj;
                    if (bytes != null && bytes.length > 2) {
                        splitPackets(bytes);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private int parseMessageType(byte[] bytes) {
        int messageType = (bytes[1] & 0x000000FF) << 8;
        messageType += (bytes[0] & 0x000000FF) << 0;
        return messageType;
    }

    private void splitPackets(byte[] response) {
        // 서버에서 내려오는 패킷의 길이가 일정하지 않으므로
        // 메시지 타입을 먼저 파싱해서 정의된 버퍼 사이즈를 가져온다.
        int messageType = parseMessageType(response);
        int size = Packets.getSize(messageType);
        if (size == 0) {
            return;
        }

        byte[] splits;
        if (response.length > size) {
            // 패킷이 여러개 붙어서 내려오는 경우가 있다.
            // 패킷 정의서에 Fix 된 사이즈를 이용하여 패킷을 자른 뒤 각각 파싱 한다.
            splits = Arrays.copyOfRange(response, 0, size);
        } else {
            splits = response;
        }

        // Response Packet을 생성하면서 파싱을 동시에 진행 한다.
        ResponsePacket packet = ResponsePacket.create(messageType, splits);
        for (NetworkListener l : networkListeners) {
            l.onReceivedPacket(packet);
        }

        if (response.length > size) {
            splitPackets(Arrays.copyOfRange(response, size, response.length));
        }
    }
}
