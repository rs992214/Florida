package com.thinkware.florida.network.packets.mdt2server;

import android.text.TextUtils;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.RequestPacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 콜 정산 요청 (GT-1611) 27 Byte
 * MDT -> Server
 */
public class RequestAccountPacket extends RequestPacket {

    private int serviceNumber; // 서비스번호 (1)
    private int corporationCode; // 법인코드 (2)
    private int carId; // Car ID (2)
    private String phoneNumber; // Phone Number (13)
    private Packets.AccountType accountType; // 조회요청구분(대분류) (1)
    private String beginDate; // 조회요청 시작일 (3) (년월일 - ex : 091101)
    private String endDate; // 조회요청 종료일 (3) (년월일 - ex : 091101)
    private int beginYY, beginMM, beginDD;
    private int endYY, endMM, endDD;

    public RequestAccountPacket() {
        super(Packets.REQUEST_ACCOUNT);
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Packets.AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(Packets.AccountType accountType) {
        this.accountType = accountType;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
        if (beginDate.length() > 2) {
            beginYY = Integer.parseInt(beginDate.substring(0, 2));
        }
        if (beginDate.length() > 4) {
            beginMM = Integer.parseInt(beginDate.substring(2, 4));
        }
        if (beginDate.length() >= 6) {
            beginDD = Integer.parseInt(beginDate.substring(4, 6));
        }
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
        if (endDate.length() > 2) {
            endYY = Integer.parseInt(endDate.substring(0, 2));
        }
        if (endDate.length() > 4) {
            endMM = Integer.parseInt(endDate.substring(2, 4));
        }
        if (endDate.length() >= 6) {
            endDD = Integer.parseInt(endDate.substring(4, 6));
        }
    }

    @Override
    public byte[] toBytes() {
        super.toBytes();
        writeInt(serviceNumber, 1);
        writeInt(corporationCode, 2);
        writeInt(carId, 2);
        writeString(phoneNumber, 13);
        writeInt(accountType.value, 1);
        // 앞에서 1byte 씩 년/월/일 이다
        if (TextUtils.isEmpty(beginDate)) {
            writeInt(0, 3);
        } else {
            writeInt(beginYY, 1);
            writeInt(beginMM, 1);
            writeInt(beginDD, 1);
        }
        // 앞에서 1byte 씩 년/월/일 이다
        if (TextUtils.isEmpty(endDate)) {
            writeInt(0, 3);
        } else {
            writeInt(endYY, 1);
            writeInt(endMM, 1);
            writeInt(endDD, 1);
        }
        return buffers;
    }

    @Override
    public String toString() {
        return "콜정산요청 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", corporationCode=" + corporationCode +
                ", carId=" + carId +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", accountType=" + accountType +
                ", beginDate='" + beginDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", beginYY=" + beginYY +
                ", beginMM=" + beginMM +
                ", beginDD=" + beginDD +
                ", endYY=" + endYY +
                ", endMM=" + endMM +
                ", endDD=" + endDD;
    }
}
