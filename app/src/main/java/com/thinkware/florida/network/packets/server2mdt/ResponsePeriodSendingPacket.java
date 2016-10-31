package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 주기응답 (GT-1212) 8 Byte
 * Server -> MDT
 */
public class ResponsePeriodSendingPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private boolean hasOrder; // 배차상태 (1)
    private int callNumber; // 콜번호 (2)
    private boolean hasMessage; // 메시지 유무 (1)

    public ResponsePeriodSendingPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public boolean hasOrder() {
        return hasOrder;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public boolean hasMessage() {
        return hasMessage;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int order = readInt(1);
        hasOrder = order == 0x01;
        callNumber = readInt(2);
        int message = readInt(1);
        hasMessage = message == 0x01;
    }

    @Override
    public String toString() {
        return "주기응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", hasOrder=" + hasOrder +
                ", callNumber=" + callNumber +
                ", hasMessage=" + hasMessage;
    }
}