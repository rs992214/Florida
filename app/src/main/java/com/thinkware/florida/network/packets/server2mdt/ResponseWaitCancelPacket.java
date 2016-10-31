package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기취소응답 (GT-1516) 5 Byte
 * Server -> MDT
 */
public class ResponseWaitCancelPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private Packets.WaitCancelType waitCancelType; // 대기취소처리 구분 (1)

    public ResponseWaitCancelPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public Packets.WaitCancelType getWaitCancelType() {
        return waitCancelType;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int type = readInt(1);
        if (type == Packets.WaitCancelType.Success.value) {
            waitCancelType = Packets.WaitCancelType.Success;
        } else if (type == Packets.WaitCancelType.Fail.value) {
            waitCancelType = Packets.WaitCancelType.Fail;
        }
//        else if (type == Packets.WaitCancelType.Exist.value) {
//            waitCancelType = Packets.WaitCancelType.Exist;
//        } else if (type == Packets.WaitCancelType.AlreadyCancel.value) {
//            waitCancelType = Packets.WaitCancelType.AlreadyCancel;
//        }
    }

    @Override
    public String toString() {
        return "대기취소응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", waitCancelType=" + waitCancelType;
    }
}