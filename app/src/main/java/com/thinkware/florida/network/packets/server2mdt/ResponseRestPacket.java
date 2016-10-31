package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 휴식/운행응답 (GT-1B12) 5 Byte
 * Server -> MDT
 */
public class ResponseRestPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private Packets.RestType restType; // 휴식 상태 (1) (규격서의 Description이 잘못 되었음)

    public ResponseRestPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public Packets.RestType getRestType() {
        return restType;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int type = readInt(1);
        if (Packets.RestType.Rest.value == type) {
            restType = Packets.RestType.Rest;
        } else if (Packets.RestType.Working.value == type) {
            restType = Packets.RestType.Working;
        } else if (Packets.RestType.Vacancy.value == type) {
            restType = Packets.RestType.Vacancy;
        } else if (Packets.RestType.KumHo.value == type) {
            restType = Packets.RestType.KumHo;
        } else if (Packets.RestType.Hankook.value == type) {
            restType = Packets.RestType.Hankook;
        } else if (Packets.RestType.Kwangshin.value == type) {
            restType = Packets.RestType.Kwangshin;
        } else if (Packets.RestType.VacancyError.value == type) {
            restType = Packets.RestType.VacancyError;
        } else if (Packets.RestType.TachoMeterError.value == type) {
            restType = Packets.RestType.TachoMeterError;
        } else if (Packets.RestType.ModemError.value == type) {
            restType = Packets.RestType.ModemError;
        }
    }

    @Override
    public String toString() {
        return "휴식/운행응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", restType=" + restType;
    }
}