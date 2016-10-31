package com.thinkware.florida.external.service.data;

/**
 * Service Status를 정의하고 있다.
 */

public class ServiceStatus {
    public static final int NO_ERROR = 0;
    /**
     * 알 수 없는 오류
     */
    public static final int UNKNOWN_ERROR = 10;
    /**
     * 서비스 기능이 시작되지 않음(초기상태)
     */
    public static final int SERVICE_NOT_LAUNCHED = 20;
    /**
     * 서비스 기능이 시작됨
     */
    public static final int SERVICE_LAUNCHED = 30;
    /**
     * 서비스에서 사용하는 handler 생성 오류
     */
    public static final int FAILED_SET_HANDLER = 40;
    /**
     * 외부 포트 열기 실패
     */
    public static final int FAILED_PORT_OPENED = 50;
    /**
     * 내부 파서 생성 실패
     */
    public static final int FAILED_SET_PARSER = 60;
    /**
     * Thread 생성 실패
     */
    public static final int FAILED_SET_THREAD = 70;
    /**
     * Queue 생성 실패
     */
    public static final int FAILED_SET_QUEUE = 80;
    /**
     * 외부기기에서 데이터 읽기 실패
     */
    public static final int FAILED_READ_DATA = 90;
    /**
     * 외부기기에 데이터 쓰기 실패
     */
    public static final int FAILED_WRITE_DATA = 100;
    /**
     * 포트가 물리적으로 연결되어 있지 않음
     */
    public static final int FAILED_USE_NOT_CONNECTED = 110;
}
