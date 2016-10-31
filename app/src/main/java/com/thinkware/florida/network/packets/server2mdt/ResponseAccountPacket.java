package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 콜정산정보 (GT-1612) 148 Byte
 * Server -> MDT
 */
public class ResponseAccountPacket extends ResponsePacket {

    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String phoneNumber; // Phone Number (13)
    private int normalCallNumber; // 일반 콜 건수 (2) -> 총 콜수
    private int normalCallFee; // 일반 콜 회비 (4) -> 일반콜수
    private int businessCallNumber; // 업무 콜 건수 (2)
    private int businessCallFee; // 업무 콜 회비 (4)
    private int arrear; // 미납금 (4) -> 앱 콜수
    private int lastMonthOffsetting; // 전월 상계금 (4) -> 외곽 콜수
    private int adjustment; // 가감 금액 (4)
    private int totalAmount; // 총 청구액 (4)
    private String memo; // 비고 (100)

    public ResponseAccountPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCorporationCode() {
        return corporationCode;
    }

    public int getCarId() {
        return carId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getNormalCallNumber() {
        return normalCallNumber;
    }

    public int getNormalCallFee() {
        return normalCallFee;
    }

    public int getBusinessCallNumber() {
        return businessCallNumber;
    }

    public int getBusinessCallFee() {
        return businessCallFee;
    }

    public int getArrear() {
        return arrear;
    }

    public int getLastMonthOffsetting() {
        return lastMonthOffsetting;
    }

    public int getAdjustment() {
        return adjustment;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getMemo() {
        return memo;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        corporationCode = readInt(2);
        carId = readInt(2);
        phoneNumber = readString(13);
        normalCallNumber = readInt(2);
        normalCallFee = readInt(4);
        businessCallNumber = readInt(2);
        businessCallFee = readInt(4);
        arrear = readInt(4);
        lastMonthOffsetting = readInt(4);
        adjustment = readInt(4);
        totalAmount = readInt(4);
        memo = readString(100);
    }

    @Override
    public String toString() {
        return "콜정산정보 (0x" + Integer.toHexString(messageType) + ") " +
                "corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", normalCallNumber=" + normalCallNumber +
                ", normalCallFee=" + normalCallFee +
                ", businessCallNumber=" + businessCallNumber +
                ", businessCallFee=" + businessCallFee +
                ", arrear=" + arrear +
                ", lastMonthOffsetting=" + lastMonthOffsetting +
                ", adjustment=" + adjustment +
                ", totalAmount=" + totalAmount +
                ", memo='" + memo + '\'';
    }
}