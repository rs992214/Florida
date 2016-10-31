package com.thinkware.florida.network.packets;

/**
 * Created by zic325 on 2016. 9. 6..
 */
public class Packets {

    //----------------------------------------------------------------------------------------
    // MDT -> Server : 16
    //----------------------------------------------------------------------------------------
    public static final int ACK = 0xF111; // ACK
    public static final int REQUEST_SERVICE = 0x1111; // 서비스요청
    public static final int REQUEST_NOTICE = 0x1113; // 공지사항요청
    public static final int REQUEST_CONFIG = 0x1115; // 환경설정요청
    public static final int PERIOD_SENDING = 0x1211; // 주기전송
    public static final int SERVICE_REPORT = 0x1411; // 운행보고
    public static final int REQUEST_WAIT_AREA = 0x1511; // 대기지역요청
    public static final int WAIT_DECISION = 0x1513; // 대기결정
    public static final int WAIT_CANCEL = 0x1515; // 대기취소
    public static final int REQUEST_CALLER_INFO = 0x1517; // 대기배차고객정보 요청
    public static final int REQUEST_ACCOUNT = 0x1611; // 콜 정산 요청
    public static final int REQUEST_EMERGENCY = 0x1711; // Emergency 요청
    public static final int REQUEST_MESSAGE = 0x1811; // 메시지요청
    public static final int REQUEST_ORDER_REALTIME = 0x1911; // 실시간 위치 및 배차요청
    public static final int REQUEST_REST = 0x1B11; // 휴식/운행재개
    public static final int LIVE = 0xF1F1; // Live 패킷


    //----------------------------------------------------------------------------------------
    // Server -> MDT : 17
    //----------------------------------------------------------------------------------------
    public static final int RESPONSE_ACK = 0xFF11; // 접속종료 (ACK의 응답 목적으로 사용 한다.)
    public static final int SERVICE_REQUEST_RESULT = 0x1112; // 서비스요청결과
    public static final int NOTICES = 0x1114; // 공지사항
    public static final int SERVICE_CONFIG = 0x1116; // 환경설정
    public static final int RESPONSE_PERIOD_SENDING = 0x1212; // 주기응답
    public static final int ORDER_INFO = 0x1312; // 배차데이터
    public static final int ORDER_INFO_PROC = 0x1314; // 배차데이터 처리
    public static final int RESPONSE_SERVICE_REPORT = 0x1412; // 운행보고응답
    public static final int WAIT_PLACE_INFO = 0x1512; // 대기지역정보
    public static final int RESPONSE_WAIT_DECISION = 0x1514; // 대기결정응답
    public static final int RESPONSE_WAIT_CANCEL = 0x1516; // 대기취소응답
    public static final int WAIT_ORDER_INFO = 0x1518; // 대기배차고객정보
    public static final int RESPONSE_ACCOUNT = 0x1612; // 콜정산정보
    public static final int CANCEL_EMERGENCY = 0x1712; // Emergency 응답
    public static final int RESPONSE_MESSAGE = 0x1812; // 메시지 응답
    public static final int CALLER_INFO_RESEND = 0x1A12; // 고객정보재전송
    public static final int RESPONSE_REST = 0x1B12; // 휴식/운행응답

    //----------------------------------------------------------------------------------------
    // AT Command : 4
    //----------------------------------------------------------------------------------------
    public static final int REQUEST_AT_COMMNAD = 0x9996; // 모뎀 AT Command
    public static final int RESPONSE_AT_COMMAND = 0x9997; // 모뎀 AT Command
    public static final int REQUEST_MODEM_NO = 0x9998; // 모뎀의 번호를 가져오기 위함.
    public static final int RESPONSE_MODEM_NO = 0x9999; // 모뎀의 번호를 가져오기 위함.

    //----------------------------------------------------------------------------------------
    // Enum
    //----------------------------------------------------------------------------------------

    // 개인법인체크
    public enum CorporationType {
        Default(0x00),
        Corporation(0x01), // 법인
        Indivisual(0x02); // 개인

        public int value;

        CorporationType(int value) {
            this.value = value;
        }
    }

    // 승차상태 or 택시상태
    public enum BoardType {
        Empty(0x00), // 빈차
        Boarding(0x01); // 승차중

        public int value;

        BoardType(int value) {
            this.value = value;
        }
    }

    // 휴식상태
    public enum RestType {
        Working(0), // 인증-운행 or 운행재개
        Rest(1), // 휴식 or 휴식요청
        Vacancy(10), // 인증 후 미터기/빈차등 세팅값을 휴식 상태에 올려 보낸다.
        KumHo(20), // 인증 후 미터기/빈차등 세팅값을 휴식 상태에 올려 보낸다.
        Hankook(21), // 인증 후 미터기/빈차등 세팅값을 휴식 상태에 올려 보낸다.
        Kwangshin(22), // 인증 후 미터기/빈차등 세팅값을 휴식 상태에 올려 보낸다.
        VacancyError(30), // 미터기/빈차등 오류 발생시 휴식 패킷에 상태를 올려 보낸다.
        TachoMeterError(31), // 미터기/빈차등 오류 발생시 휴식 패킷에 상태를 올려 보낸다.
        ModemError(40); // 모뎀 오류 발생시 휴식 패킷에 상태를 올려 보낸다.

        public int value;

        RestType(int value) {
            this.value = value;
        }
    }

    // 운행보고구분
    public enum ReportKind {
        GetOn(0x01), // 승차보고
        GetOff(0x02), // 하차보고
        Failed(0x03), // 탑승실패
        GetOnOrder(0x04); // 승차중 배차

        public int value;

        ReportKind(int value) {
            this.value = value;
        }
    }

    // 조회요청구분(대분류)
    public enum AccountType {
        Monthly(0x01), // 월별 콜 조회
        Period(0x02), // 기간별 콜 조회
        Daily(0x03); // 일별 콜 조회

        public int value;

        AccountType(int value) {
            this.value = value;
        }
    }

    // Emergency 구분
    public enum EmergencyType {
        Begin(0x01), // 시작
        End(0x02); // 중지

        public int value;

        EmergencyType(int value) {
            this.value = value;
        }
    }

    // 배차결정구분
    public enum OrderDecisionType {
        Request(0x01), // 배차 요청
        Reject(0x02), // 배차 거부
        OutOfDistance(0x03), // 거리 벗어남
        Fail(0x04), // 탑승실패
        Disconnect(0x05), // 접속종료 - 내부적으로 서버에서만 사용
        MultipleOrder(0x14), // 배차가 2개 이상일 때
        AlreadyOrderd(0x0D), // 배차가 1개일 때 - 현재상태가 빈차일 경우 (운행보고 안함 : 콜받고 운행보고 안된상태에서 콜수신된경우)
        Waiting(0x0C), // 대기배차 상태인데 일반콜 수신될 경우
        Driving(0x0A); // 주행중 일반콜 수신될 경우

        public int value;

        OrderDecisionType(int value) {
            this.value = value;
        }
    }

    // 인증결과
    public enum CertificationResult {
        Success(0x01), // 인증성공
        InvalidCar(0x02), // 차량 인증실패
        InvalidContact(0x03), // 연락처인증실패
        DriverPenalty(0x04), // 기사패널티 인증실패
        InvalidHoliday(0x05); // 휴무 조 인증실패

        public int value;

        CertificationResult(int value) {
            this.value = value;
        }
    }

    // 배차구분
    public enum OrderKind {
        Normal(1), // 일반배차(양방향경쟁)
        Wait(3), // 대기배차 (SMS)
        Forced(05), // 강제배차
        Manual(6), // 수동배차, 지정배차
        WaitOrder(7), // 대기배차
        GetOnOrder(10), // 승차중 배차
        WaitOrderTwoWay(11), // 양방향 대기배차
        Mobile(15); // 모바일배차

        public int value;

        OrderKind(int value) {
            this.value = value;
        }
    }

    // 배차데이터 처리 구분
    public enum OrderProcType {
        Display(0x01), // 배차데이터 Display
        Delete(0x02); // 배차데이터 삭제

        public int value;

        OrderProcType(int value) {
            this.value = value;
        }
    }

    // 대기처리 구분
    public enum WaitProcType {
        Success(0x01), // 대기배차등록 성공
        Fail(0x02), // 대기배차등록 실패
        Exist(0x03); // 대기배차 있음

        public int value;

        WaitProcType(int value) {
            this.value = value;
        }
    }

    // 대기취소처리 구분
    public enum WaitCancelType {
        Success(0x01), // 대기취소 성공
        Fail(0x02); // 대기취소 실패
//        Exist(0x03), // 대기배차 있음 (사용 하지 않는다고 함)
//        AlreadyCancel(0x04); // 이미 취소 처리됨 (사용 하지 않는다고 함)

        public int value;

        WaitCancelType(int value) {
            this.value = value;
        }
    }

    //----------------------------------------------------------------------------------------
    // Packet Size
    //----------------------------------------------------------------------------------------
    public static int getSize(int type) {
        switch (type) {
            case ACK: // ACK
                return 11;
            case REQUEST_SERVICE: // 서비스요청
                return 36;
            case REQUEST_NOTICE: // 공지사항요청
                return 7;
            case REQUEST_CONFIG: // 환경설정요청
                return 9;
            case PERIOD_SENDING: // 주기전송
                return 32;
            case SERVICE_REPORT: // 운행보고
                return 62;
            case REQUEST_WAIT_AREA: // 대기지역요청
                return 22;
            case WAIT_DECISION: // 대기결정
                return 38;
            case WAIT_CANCEL: // 대기취소
                return 23;
            case REQUEST_CALLER_INFO: // 대기배차고객정보 요청
                return 7;
            case REQUEST_ACCOUNT: // 콜 정산 요청
                return 27;
            case REQUEST_EMERGENCY: // Emergency 요청
                return 26;
            case REQUEST_MESSAGE: // 메시지요청
                return 7;
            case REQUEST_ORDER_REALTIME: // 실시간 위치 및 배차요청
                return 62;
            case REQUEST_REST: // 휴식/운행재개
                return 7;
            case LIVE: // Live 패킷
                return 6;
            case RESPONSE_ACK: // 접속종료 (ACK의 응답 목적으로 사용 한다.)
                return 3;
            case SERVICE_REQUEST_RESULT: // 서비스요청결과
                return 23;
            case NOTICES: // 공지사항
                return 293;
            case SERVICE_CONFIG: // 환경설정
                return 38;
            case RESPONSE_PERIOD_SENDING: // 주기응답
                return 8;
            case ORDER_INFO: // 배차데이터
                return 186 + 64;
            case ORDER_INFO_PROC: // 배차데이터 처리
                return 7;
            case RESPONSE_SERVICE_REPORT: // 운행보고응답
                return 7;
            case WAIT_PLACE_INFO: // 대기지역정보
                return 51;
            case RESPONSE_WAIT_DECISION: // 대기결정응답
                return 19;
            case RESPONSE_WAIT_CANCEL: // 대기취소응답
                return 5;
            case WAIT_ORDER_INFO: // 대기배차고객정보
                return 182;
            case RESPONSE_ACCOUNT: // 콜정산정보
                return 148;
            case CANCEL_EMERGENCY: // Emergency 응답
                return 6;
            case RESPONSE_MESSAGE: // 메시지 응답
                return 207;
            case CALLER_INFO_RESEND: // 고객정보재전송
                return 250;
            case RESPONSE_REST: // 휴식/운행응답
                return 5;
            default:
                return 0;
        }
    }
}
