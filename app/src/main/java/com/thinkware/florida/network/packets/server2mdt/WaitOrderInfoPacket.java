package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기배차고객정보 (GT-1518) 182 Byte
 * Server -> MDT
 */
public class WaitOrderInfoPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private Packets.OrderKind orderKind; // 배차구분 (1)
    private String callReceiptDate; // 콜접수일자(ex : 2009-01-23) (11)
    private int callNumber; // 콜번호 (2)
    private float longitude; // 고객 경도 (4)
    private float latitude; // 고객 위도 (4)
    private String callerPhone; // 고객 연락처 (13)
    private String place; // 탑승지 (41)
    private String placeExplanation; // 탑승지 설명 (101)
    private int orderCount; // 배차횟수 (1)
    private boolean isReported; // 운행보고 여부 (Local에서만 사용하는 값이다.)

    public WaitOrderInfoPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public Packets.OrderKind getOrderKind() {
        return orderKind;
    }

    public String getCallReceiptDate() {
        return callReceiptDate;
    }

    public int getCallNumber() {
        return callNumber;
    }

    public float getLongitude() {
        return longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public String getCallerPhone() {
        return callerPhone;
    }

    public String getPlace() {
        return place;
    }

    public String getPlaceExplanation() {
        return placeExplanation;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        int order = readInt(1);
        if (Packets.OrderKind.Normal.value == order) {
            orderKind = Packets.OrderKind.Normal;
        } else if (Packets.OrderKind.Wait.value == order) {
            orderKind = Packets.OrderKind.Wait;
        } else if (Packets.OrderKind.Forced.value == order) {
            orderKind = Packets.OrderKind.Forced;
        } else if (Packets.OrderKind.Manual.value == order) {
            orderKind = Packets.OrderKind.Manual;
        } else if (Packets.OrderKind.WaitOrder.value == order) {
            orderKind = Packets.OrderKind.WaitOrder;
        } else if (Packets.OrderKind.WaitOrderTwoWay.value == order) {
            orderKind = Packets.OrderKind.WaitOrderTwoWay;
        } else if (Packets.OrderKind.GetOnOrder.value == order) {
            orderKind = Packets.OrderKind.GetOnOrder;
        } else if (Packets.OrderKind.Mobile.value == order) {
            orderKind = Packets.OrderKind.Mobile;
        }
        callReceiptDate = readString(11);
        callNumber = readInt(2);
        longitude = readFloat(4);
        latitude = readFloat(4);
        callerPhone = readString(13);
        place = readString(41);
        placeExplanation = readString(101);
        orderCount = readInt(1);
    }

    @Override
    public String toString() {
        return "대기배차고객정보 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", orderKind=" + orderKind +
                ", callReceiptDate='" + callReceiptDate + '\'' +
                ", callNumber=" + callNumber +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", callerPhone='" + callerPhone + '\'' +
                ", place='" + place + '\'' +
                ", placeExplanation='" + placeExplanation + '\'' +
                ", orderCount=" + orderCount;
    }
}