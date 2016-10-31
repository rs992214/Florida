package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 환경설정 (GT-1116) 38 Byte (규격서의 37 byte가 잘못된 정보임)
 * Server -> MDT
 */
public class ServiceConfigPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private int version; // 환결설정버전 (2)
    private int periodSendingTime; // 주기전송시간(단위:초) (1)
    private int periodSendingRange; // 주기전송판단거리(단위:미터) (2)
    private int retryNumber; // 데이터 재전송 시도 횟수 (1)
    private int retryTime; // 데이터 재전송 주기 시간(단위:초) (1)
    private int callAcceptanceTime; // 콜수랑창 표시시간(단위:초) (1)
    private int periodEmergency; // Emergency주기시간(단위:초) (1)
    private boolean isLogging; // 콜로그저장 (1)
    private String callServerIp; // 콜서버IP (4)
    private int callServerPort; // 콜서버Port (2)
    private String updateServerIp; // 업데이트서버IP (4)
    private int updateServerPort; // 업데이트서버Port (2)
    private String password; // 패스워드(기본값 : 0) (11)
    private int checkTimeForPeriodSending; // 주기전송거리체크시간(주기 전송을 해야되는지 판단하기 위한 시간(초단위)) (1)

    public ServiceConfigPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public int getVersion() {
        return version;
    }

    public int getPeriodSendingTime() {
        return periodSendingTime;
    }

    public int getPeriodSendingRange() {
        return periodSendingRange;
    }

    public int getRetryNumber() {
        return retryNumber;
    }

    public int getRetryTime() {
        return retryTime;
    }

    public int getCallAcceptanceTime() {
        return callAcceptanceTime;
    }

    public int getPeriodEmergency() {
        return periodEmergency;
    }

    public boolean isLogging() {
        return isLogging;
    }

    public String getCallServerIp() {
        return callServerIp;
    }

    public int getCallServerPort() {
        return callServerPort;
    }

    public String getUpdateServerIp() {
        return updateServerIp;
    }

    public int getUpdateServerPort() {
        return updateServerPort;
    }

    public String getPassword() {
        return password;
    }

    public int getCheckTimeForPeriodSending() {
        return checkTimeForPeriodSending;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        version = readInt(2);
        periodSendingTime = readInt(1);
        periodSendingRange = readInt(2);
        retryNumber = readInt(1);
        retryTime = readInt(1);
        callAcceptanceTime = readInt(1);
        periodEmergency = readInt(1);
        int logging = readInt(1);
        isLogging = logging == 0x01;
        callServerIp = readInt(1) + "." + readInt(1) + "." + readInt(1) + "." + readInt(1);
        callServerPort = readInt(2);
        updateServerIp = readInt(1) + "." + readInt(1) + "." + readInt(1) + "." + readInt(1);
        updateServerPort = readInt(2);
        password = readString(11);
        checkTimeForPeriodSending = readInt(1);
    }

    @Override
    public String toString() {
        return "환경설정 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", version=" + version +
                ", periodSendingTime=" + periodSendingTime +
                ", periodSendingRange=" + periodSendingRange +
                ", retryNumber=" + retryNumber +
                ", retryTime=" + retryTime +
                ", callAcceptanceTime=" + callAcceptanceTime +
                ", periodEmergency=" + periodEmergency +
                ", isLogging=" + isLogging +
                ", callServerIp='" + callServerIp + '\'' +
                ", callServerPort='" + callServerPort + '\'' +
                ", updateServerIp='" + updateServerIp + '\'' +
                ", updateServerPort='" + updateServerPort + '\'' +
                ", password='" + password + '\'' +
                ", checkTimeForPeriodSending=" + checkTimeForPeriodSending;
    }
}