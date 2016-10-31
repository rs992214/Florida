package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 메시지 전송용청 응답 (GT-1812) 207 Byte
 * Server -> MDT
 */
public class ResponseMessagePacket extends ResponsePacket {

    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String message; // 메시지 본문 (201)

    public ResponseMessagePacket(byte[] bytes) {
        super(bytes);
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        corporationCode = readInt(2);
        carId = readInt(2);
        message = readString(201);
    }

    @Override
    public String toString() {
        return "메시지 응답 (0x" + Integer.toHexString(messageType) + ") " +
                "corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", message='" + message + '\'';
    }
}