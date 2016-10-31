package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.Packets;
import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 7..
 * 서비스 요청 결과 (GT-1112) 23 Byte
 * Server -> MDT
 */
public class ServiceRequestResultPacket extends ResponsePacket {

    private int serviceNumber; // 서비스번호 (1)
    private int carId; // car ID (2)
    private Packets.CertificationResult certificationResult; // 인증결과 (1)
    private int noticeCode; // 공지사항코드 (2)
    private int configurationVersion; // 환경설정버전 (2)
    private int programVersion; // 콜프로그램버전 (2)
    private boolean isWaiting; // 대기배차 유무 (1)
    private int extra01; // 시정광고_1_처리요청 (1)
    private int extra02; // 시정광고_2_처리요청 (1)
    private int extra03; // 시정광고_3_처리요청 (1)
    private int extra04; // 시정광고_4_처리요청 (1)
    private int extra05; // 시정광고_5_처리요청 (1)
    private int extra06; // 시정광고_6_처리요청 (1)
    private int extra07; // 시정광고_7_처리요청 (1)
    private int extra08; // 시정광고_8_처리요청 (1)
    private int extra09; // 시정광고_9_처리요청 (1)
    private int extra10; // 시정광고_10_처리요청 (1)
    private int certCode; // 알 수 없는 오류 사항에 대한 예외처리 값

    public ServiceRequestResultPacket(byte[] bytes) {
        super(bytes);
    }

    public int getServiceNumber() {
        return serviceNumber;
    }

    public int getCarId() {
        return carId;
    }

    public Packets.CertificationResult getCertificationResult() {
        return certificationResult;
    }

    public int getNoticeCode() {
        return noticeCode;
    }

    public int getConfigurationVersion() {
        return configurationVersion;
    }

    public int getProgramVersion() {
        return programVersion;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public int getExtra01() {
        return extra01;
    }

    public int getExtra02() {
        return extra02;
    }

    public int getExtra03() {
        return extra03;
    }

    public int getExtra04() {
        return extra04;
    }

    public int getExtra05() {
        return extra05;
    }

    public int getExtra06() {
        return extra06;
    }

    public int getExtra07() {
        return extra07;
    }

    public int getExtra08() {
        return extra08;
    }

    public int getExtra09() {
        return extra09;
    }

    public int getExtra10() {
        return extra10;
    }

    public int getCertCode() {
        return certCode;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        serviceNumber = readInt(1);
        carId = readInt(2);
        certCode = readInt(1);
        if (certCode == Packets.CertificationResult.Success.value) {
            certificationResult = Packets.CertificationResult.Success;
        } else if (certCode == Packets.CertificationResult.InvalidCar.value) {
            certificationResult = Packets.CertificationResult.InvalidCar;
        } else if (certCode == Packets.CertificationResult.InvalidContact.value) {
            certificationResult = Packets.CertificationResult.InvalidContact;
        } else if (certCode == Packets.CertificationResult.DriverPenalty.value) {
            certificationResult = Packets.CertificationResult.DriverPenalty;
        } else if (certCode == Packets.CertificationResult.InvalidHoliday.value) {
            certificationResult = Packets.CertificationResult.InvalidHoliday;
        }

        noticeCode = readInt(2);
        configurationVersion = readInt(2);
        programVersion = readInt(2);
        int waiting = readInt(1);
        isWaiting = waiting == 0x01;
        extra01 = readInt(1);
        extra02 = readInt(1);
        extra03 = readInt(1);
        extra04 = readInt(1);
        extra05 = readInt(1);
        extra06 = readInt(1);
        extra07 = readInt(1);
        extra08 = readInt(1);
        extra09 = readInt(1);
        extra10 = readInt(1);
    }

    @Override
    public String toString() {
        return "서비스요청결과 (0x" + Integer.toHexString(messageType) + ") " +
                "serviceNumber=" + serviceNumber +
                ", carId=" + carId +
                ", certificationResult=" + certificationResult +
                ", noticeCode=" + noticeCode +
                ", configurationVersion=" + configurationVersion +
                ", programVersion=" + programVersion +
                ", isWaiting=" + isWaiting +
                ", extra01=" + extra01 +
                ", extra02=" + extra02 +
                ", extra03=" + extra03 +
                ", extra04=" + extra04 +
                ", extra05=" + extra05 +
                ", extra06=" + extra06 +
                ", extra07=" + extra07 +
                ", extra08=" + extra08 +
                ", extra09=" + extra09 +
                ", extra10=" + extra10 ;
    }
}
