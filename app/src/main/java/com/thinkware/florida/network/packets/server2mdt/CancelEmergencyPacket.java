package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 응급해제 (GT-1712) 6 Byte
 * Server -> MDT
 */
public class CancelEmergencyPacket extends ResponsePacket {

    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)

    public CancelEmergencyPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        corporationCode = readInt(2);
        carId = readInt(2);
    }

    @Override
    public String toString() {
        return "Emergency 응답 (0x" + Integer.toHexString(messageType) + ") " +
                "corporationCode=" + corporationCode +
                ", carId=" + carId;
    }
}