package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 접속종료 (GT-FF11) 3 Byte
 * ACK(F111)의 응답 기능으로 사용 한다.
 * Server -> MDT
 */
public class ResponseAckPacket extends ResponsePacket {

    private int kind; // 접속종료 구분 (1)

    public ResponseAckPacket(byte[] bytes) {
        super(bytes);
    }

    public int getKind() {
        return kind;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        kind = readInt(1);
    }

    @Override
    public String toString() {
        return "ACK응답 패킷 (0x" + Integer.toHexString(messageType) + ") " +
                "kind=" + kind;
    }
}