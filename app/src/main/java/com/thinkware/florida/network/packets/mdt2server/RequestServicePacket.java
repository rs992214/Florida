package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 7..
 * 서비스 요청 (GT-1111) 36 Byte
 * MDT -> Server
 */
public class RequestServicePacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String phoneNumber; // Phone Number (13)
    private Packets.CorporationType corporationType; // 개인법인체크 (1)
    private int programVersion; // 프로그램 버전 (2)
    private String modemNumber; // 모뎀 번호 (13)

    public RequestServicePacket() {
        super(Packets.REQUEST_SERVICE);
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

    public Packets.CorporationType getCorporationType() {
        return corporationType;
    }

    public void setCorporationType(Packets.CorporationType corporationType) {
        this.corporationType = corporationType;
    }

    public int getProgramVersion() {
        return programVersion;
    }

    public void setProgramVersion(int programVersion) {
        this.programVersion = programVersion;
    }

    public String getModemNumber() {
        return modemNumber;
    }

    public void setModemNumber(String modemNumber) {
        this.modemNumber = modemNumber;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeString(phoneNumber, 13);
        writeInt(corporationType.value, 1);
        writeInt(programVersion, 2);
        writeString(modemNumber, 13);
        return buffers;
    }

    @Override
    public String toString() {
        return "서비스요청 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", corporationType=" + corporationType +
                ", programVersion=" + programVersion +
                ", modemNumber='" + modemNumber + '\'';
    }
}
