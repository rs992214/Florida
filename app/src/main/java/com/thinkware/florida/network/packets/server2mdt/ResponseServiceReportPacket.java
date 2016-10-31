package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 운행보고응답 (GT-1412) 7 Byte
 * Server -> MDT
 */
public class ResponseServiceReportPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private int callNumber; // 콜번호 (2)
    private Packets.ReportKind reportKind; // 운행구분 (1)

    public ResponseServiceReportPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public Packets.ReportKind getReportKind() {
        return reportKind;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        callNumber = readInt(2);
        int kind = readInt(1);
        if (kind == Packets.ReportKind.GetOn.value) {
            reportKind = Packets.ReportKind.GetOn;
        } else if (kind == Packets.ReportKind.GetOff.value) {
            reportKind = Packets.ReportKind.GetOff;
        } else if (kind == Packets.ReportKind.Failed.value) {
            reportKind = Packets.ReportKind.Failed;
        }
    }

    @Override
    public String toString() {
        return "운행보고 응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", callNumber=" + callNumber +
                ", reportKind=" + reportKind;
    }
}