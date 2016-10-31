package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 휴식/운행재개 (GT-1B11) 7 Byte
 * MDT -> Server
 */
public class RequestRestPacket extends RequestPacket {

    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private Packets.RestType restType; // 구분 (1)

    public RequestRestPacket() {
        super(Packets.REQUEST_REST);
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public void setCorporationCode(int corporationCode) {
        this.corporationCode = corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public Packets.RestType getRestType() {
        return restType;
    }

    public void setRestType(Packets.RestType restType) {
        this.restType = restType;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeInt(restType.value, 1);
        return buffers;
    }

    @Override
    public String toString() {
        return "휴식/운행재개 (0x" + Integer.toHexString(messageType) + ") " +
                "corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", restType=" + restType;
    }
}
