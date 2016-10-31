package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 배차데이터 처리 (GT-1314) 7 Byte
 * Server -> MDT
 */
public class OrderInfoProcPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private Packets.OrderProcType orderProcType; // 처리구분 (1)
    private int callNumber; // 콜번호 (2)

    public OrderInfoProcPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public Packets.OrderProcType getOrderProcType() {
        return orderProcType;
    }

    public int getCallNumber() {
        return callNumber;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int type = readInt(1);
        if (Packets.OrderProcType.Display.value == type) {
            orderProcType = Packets.OrderProcType.Display;
        } else if (Packets.OrderProcType.Delete.value == type) {
            orderProcType = Packets.OrderProcType.Delete;
        }
        callNumber = readInt(2);
    }

    @Override
    public String toString() {
        return "배차데이터 처리 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", orderProcType=" + orderProcType +
                ", callNumber=" + callNumber;
    }
}