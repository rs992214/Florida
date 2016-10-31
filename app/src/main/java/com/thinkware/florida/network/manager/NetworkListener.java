package com.thinkware.florida.network.manager;

import android.support.annotation.NonNull;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 7..
 */
public interface NetworkListener {

    enum ErrorCode {
        Disconnected,
        ConnectFailed,
        SocketCreateFailed,
        StreamCreateFailed
    }

    /**
     * Socket이 연결 되었음을 전달 한다.
     */
    void onConnectedServer();

    /**
     * Socket이 끊겼을 경우 에러를 전달 한다.
     * @param code
     */
    void onDisconnectedServer(ErrorCode code);

    /**
     * 서버의 응답으로 전달 받은 Response 패킷을 파싱하여 전달 한다.
     * @param response
     */
    void onReceivedPacket(@NonNull ResponsePacket response);
}
