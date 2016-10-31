package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 운행보고 (GT-1411) 62 Byte
 * MDT -> Server
 */
public class ServiceReportPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String phoneNumber; // Phone Number (13)
    private String callReceiptDate; // 콜접수일자 (11) (ex : 2009-01-13)
    private int callNumber; // 콜번호 (2)
    private Packets.ReportKind reportKind; // 운행보고구분 (1)
    private String gpsTime; // GPS시간 (6) (년월일시분초 - ex : 090805112134)
    private int direction; // 주행방향 (2)
    private float longitude; // 경도 (4)
    private float latitude; // 위도 (4)
    private int speed; // 속도 (1)
    private Packets.BoardType taxiState; // 택시상태 (1)
    private int fare; // 요금정보 (4)
    private int orderCount; // 배차횟수 (1)
    private Packets.OrderKind orderKind; // 배차구분 (1)
    private int distance; // 주행거리 (4)

    public ServiceReportPacket() {
        super(Packets.SERVICE_REPORT);
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

    public String getCallReceiptDate() {
        return callReceiptDate;
    }

    public void setCallReceiptDate(String callReceiptDate) {
        this.callReceiptDate = callReceiptDate;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(int callNumber) {
        this.callNumber = callNumber;
    }

    public Packets.ReportKind getReportKind() {
        return reportKind;
    }

    public void setReportKind(Packets.ReportKind reportKind) {
        this.reportKind = reportKind;
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

    public Packets.BoardType getTaxiState() {
        return taxiState;
    }

    public void setTaxiState(Packets.BoardType taxiState) {
        this.taxiState = taxiState;
    }

    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public Packets.OrderKind getOrderKind() {
        return orderKind;
    }

    public void setOrderKind(Packets.OrderKind orderKind) {
        this.orderKind = orderKind;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeString(phoneNumber, 13);
        writeString(callReceiptDate, 11);
        writeInt(callNumber, 2);
        writeInt(reportKind.value, 1);
        writeDateTime(gpsTime, 6);
        writeInt(direction, 2);
        writeFloat(longitude, 4);
        writeFloat(latitude, 4);
        writeInt(speed, 1);
        writeInt(taxiState.value, 1);
        writeInt(fare, 4);
        writeInt(orderCount, 1);
        writeInt(orderKind.value, 1);
        writeInt(distance, 4);
        return buffers;
    }

    @Override
    public String toString() {
        return "운행보고 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", callReceiptDate='" + callReceiptDate + '\'' +
                ", callNumber=" + callNumber +
                ", reportKind=" + reportKind +
                ", gpsTime='" + gpsTime + '\'' +
                ", direction=" + direction +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", speed=" + speed +
                ", taxiState=" + taxiState +
                ", fare=" + fare +
                ", orderCount=" + orderCount +
                ", orderKind=" + orderKind +
                ", distance=" + distance;
    }
}
