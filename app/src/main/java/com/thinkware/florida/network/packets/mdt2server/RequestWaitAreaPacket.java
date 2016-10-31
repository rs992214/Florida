package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기지역요청 (GT-1511) 22 Byte
 * MDT -> Server
 */
public class RequestWaitAreaPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String gpsTime; // GPS시간 (6) (년월일시분초 - ex : 090805112134)
    private float longitude; // 경도 (4)
    private float latitude; // 위도 (4)
    private Packets.BoardType taxiState; // 택시상태 (1)

    public RequestWaitAreaPacket() {
        super(Packets.REQUEST_WAIT_AREA);
    }

    public int getServiceNumber() {
        return serviceNumber;
    }

    public void setServiceNumber(int serviceNumber) {
        this.serviceNumber = serviceNumber;
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

    public String getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(String gpsTime) {
        this.gpsTime = gpsTime;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public Packets.BoardType getTaxiState() {
        return taxiState;
    }

    public void setTaxiState(Packets.BoardType taxiState) {
        this.taxiState = taxiState;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeDateTime(gpsTime, 6);
        writeFloat(longitude, 4);
        writeFloat(latitude, 4);
        writeInt(taxiState.value, 1);
        return buffers;
    }

    @Override
    public String toString() {
        return "대기지역요청 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", gpsTime='" + gpsTime + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", taxiState=" + taxiState;
    }
}
