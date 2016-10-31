package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 환경설정요청 (GT-1115) 9 Byte
 * MDT -> Server
 */
public class RequestConfigPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private int configurationCode; // 현재환경설정 코드 (2)

    public RequestConfigPacket() {
        super(Packets.REQUEST_CONFIG);
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

    public int getConfigurationCode() {
        return configurationCode;
    }

    public void setConfigurationCode(int configurationCode) {
        this.configurationCode = configurationCode;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeInt(configurationCode, 2);
        return buffers;
    }

    @Override
    public String toString() {
        return "환경설정요청 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", configurationCode=" + configurationCode;
    }
}
