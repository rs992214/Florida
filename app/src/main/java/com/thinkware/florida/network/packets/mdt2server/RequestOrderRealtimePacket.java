package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 실시간 위치 및 배차요청 (GT-1911) 62 Byte
 * MDT -> Server
 */
public class RequestOrderRealtimePacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String phoneNumber; // 운전자 전화번호 (13)
    private int callNumber; // 콜번호 (2)
    private String callReceiptDate; // 콜접수일자 (11) (ex : 2009-01-13)
    private Packets.OrderDecisionType decisionType; // 배차결정구분 (1)
    private String sendTime; // 전송 시간 (6) (년월일시분초 - ex : 090805112134)
    private String gpsTime; // GPS시간 (6) (년월일시분초 - ex : 090805112134)
    private int direction; // 주행방향 (2)
    private float longitude; // 경도 (4)
    private float latitude; // 위도 (4)
    private int speed; // 속도 (1)
    private float distance; // 고객과의 거리 (4)
    private int orderCount; // 배차횟수 (1) (배차데이터(GT-1312)에서 받은 횟수)

    public RequestOrderRealtimePacket() {
        super(Packets.REQUEST_ORDER_REALTIME);
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(int callNumber) {
        this.callNumber = callNumber;
    }

    public String getCallReceiptDate() {
        return callReceiptDate;
    }

    public void setCallReceiptDate(String callReceiptDate) {
        this.callReceiptDate = callReceiptDate;
    }

    public Packets.OrderDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(Packets.OrderDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(String gpsTime) {
        this.gpsTime = gpsTime;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
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

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeString(phoneNumber, 13);
        writeInt(callNumber, 2);
        writeString(callReceiptDate, 11);
        writeInt(decisionType.value, 1);
        writeDateTime(sendTime, 6);
        writeDateTime(gpsTime, 6);
        writeInt(direction, 2);
        writeFloat(longitude, 4);
        writeFloat(latitude, 4);
        writeInt(speed, 1);
        writeFloat(distance, 4);
        writeInt(orderCount, 1);
        return buffers;
    }

    @Override
    public String toString() {
        return "실시간 위치 및 배차결정 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", callNumber=" + callNumber +
                ", callReceiptDate='" + callReceiptDate + '\'' +
                ", decisionType=" + decisionType +
                ", sendTime='" + sendTime + '\'' +
                ", gpsTime='" + gpsTime + '\'' +
                ", direction=" + direction +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", speed=" + speed +
                ", distance=" + distance +
                ", orderCount=" + orderCount;
    }
}
