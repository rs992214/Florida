package com.thinkware.florida.network.packets.mdt2server;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * Live패킷 (GT-F1F1) 4 Byte
 * MDT -> Server
 */
public class LivePacket extends RequestPacket {

    private int carId; // Car ID (2)

    public LivePacket() {
        super(Packets.LIVE);
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(carId, 2);
        return buffers;
    }

    @Override
    public String toString() {
        return "라이브패킷 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId;
    }
}