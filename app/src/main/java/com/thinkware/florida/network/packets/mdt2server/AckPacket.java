package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 7..
 * ACK (GT-F111) 11 Byte
 * MDT -> Server
 * <p>
 * 배차데이터 처리(GT-1314) 응답을 서버에서 받았을 경우에만 사용 된다.
 * ackMessage : 0x1314
 * parameter : 배차받은 콜번호
 */
public class AckPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private int ackMessage; // 메시지구분 (2)
    private int parameter; // 처리인자 (2)

    public AckPacket() {
        super(Packets.ACK);
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

    public int getAckMessage() {
        return ackMessage;
    }

    public void setAckMessage(int ackMessage) {
        this.ackMessage = ackMessage;
    }

    public int getParameter() {
        return parameter;
    }

    public void setParameter(int parameter) {
        this.parameter = parameter;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeInt(ackMessage, 2);
        writeInt(parameter, 2);
        return buffers;
    }

    @Override
    public String toString() {
        return "ACK패킷 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", ackMessage=" + ackMessage +
                ", parameter=" + parameter;
    }
}