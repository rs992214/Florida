package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기결정응답 (GT-1514) 19 Byte
 * Server -> MDT
 */
public class ResponseWaitDecisionPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private Packets.WaitProcType waitProcType; // 대기처리 구분 (1)
    private String waitPlaceCode; // 대기지역 코드 (4)
    private float longitude; // 대기지역 경도 (4)
    private float latitude; // 대기지역 위도 (4)
    private int waitRange; // 대기범위 (2)

    public ResponseWaitDecisionPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public Packets.WaitProcType getWaitProcType() {
        return waitProcType;
    }

    public String getWaitPlaceCode() {
        return waitPlaceCode;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public int getWaitRange() {
        return waitRange;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int type = readInt(1);
        if (type == Packets.WaitProcType.Success.value) {
            waitProcType = Packets.WaitProcType.Success;
        } else if (type == Packets.WaitProcType.Fail.value) {
            waitProcType = Packets.WaitProcType.Fail;
        } else if (type == Packets.WaitProcType.Exist.value) {
            waitProcType = Packets.WaitProcType.Exist;
        }
        waitPlaceCode = readString(4);
        longitude = readFloat(4);
        latitude = readFloat(4);
        waitRange = readInt(2);
    }

    @Override
    public String toString() {
        return "대기결정응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", waitProcType=" + waitProcType +
                ", waitPlaceCode='" + waitPlaceCode + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", waitRange=" + waitRange;
    }
}