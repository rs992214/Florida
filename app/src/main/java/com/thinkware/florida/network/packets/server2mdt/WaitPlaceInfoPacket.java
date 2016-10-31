package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by zic325 on 2016. 9. 8..
 * 대기지역정보 (GT-1512) 51 Byte
 * Server -> MDT
 */
public class WaitPlaceInfoPacket extends ResponsePacket {

    private int carId; // Car ID (2)
    private String waitPlaceCode; // 대기지역코드 (4)
    private String waitPlaceName; // 대기지역 장소 명 (41)
    private int waitingCarCount; // 대기 차량 수 (1)
    private boolean hasNextPlace; // 추가 대기장소 유무 (1)

    public WaitPlaceInfoPacket(byte[] bytes) {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public String getWaitPlaceCode() {
        return waitPlaceCode;
    }

    public String getWaitPlaceName() {
        return waitPlaceName;
    }

    public int getWaitingCarCount() {
        return waitingCarCount;
    }

    /**
     * 대기요청 한 지역에 대기 장소가 N개 존재 시 1 ~ N-1 까지의 대기지역정보는 추가장소유무를 0x01로 올리고
     * 마지막 N개째의 대기지역정보 전송 시 추가장소유무를 0x02를 전송한다.
     * 단말UI 디자인상 N은 4개 이하의 수가 된다. => 변경 N 은 20개 이하
     * 단말은 최초 대기지역 정보 수신 후 특정 시간 경과 후에도 추가대기장소 유무가 0x02인 패킷이 수신되지 않으면 대기장소 UI를 PopUp할 수 있다.
     * 이 후 대기 장소를 PopUp에 후에 추가적으로 패킷이 들어오면 해당 패킷의 장소를 리스트 추가 표시한다.
     * 단, 운전자가 장소 선택을 이미 한 후 (GT-1513 전송 후)에 수신된 패킷은 무시한다.
     */
    public boolean hasNextPlace() {
        return hasNextPlace;
    }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        carId = readInt(2);
        waitPlaceCode = readString(4);
        waitPlaceName = readString(41);
        waitingCarCount = readInt(1);
        int wait = readInt(1);
        hasNextPlace = wait == 0x01;
    }

    @Override
    public String toString() {
        return "대기지역정보 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", waitPlaceCode='" + waitPlaceCode + '\'' +
                ", waitPlaceName='" + waitPlaceName + '\'' +
                ", waitingCarCount=" + waitingCarCount +
                ", hasNextPlace=" + hasNextPlace;
    }
}