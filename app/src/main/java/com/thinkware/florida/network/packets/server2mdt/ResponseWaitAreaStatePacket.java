package com.thinkware.florida.network.packets.server2mdt;

import com.thinkware.florida.network.packets.ResponsePacket;

/**
 * Created by hoonlee on 2017. 5. 31..
 */

public class ResponseWaitAreaStatePacket extends ResponsePacket {

    /**
     * 하남에서만 사용하는 것으로 가정한다
     * 대기지역 장소의 수는 최대 20개로 가정한다.
     */
    private int carId; // Car ID (2)
    private static final int WAIT_AREA_NUM_MAX = 20;
    private String[] waitAreaNames;
    private int[] carNumInWaitAreas;

    public ResponseWaitAreaStatePacket(byte[] bytes)
    {
        super(bytes);
    }

    public int getCarId() {
        return carId;
    }

    public int getAreaNum() {return WAIT_AREA_NUM_MAX; }
    public String[] getWaitAreaNames() { return waitAreaNames; }
    public int[] getCarNumInWaitAreas() { return carNumInWaitAreas; }

    @Override
    public void parse(byte[] buffers) {
        super.parse(buffers);
        if(carNumInWaitAreas == null) {
            carNumInWaitAreas = new int[WAIT_AREA_NUM_MAX];
        }
        if(waitAreaNames == null) {
            waitAreaNames = new String[WAIT_AREA_NUM_MAX];
        }

        carId = readInt(2);

        for(int i = 0; i < WAIT_AREA_NUM_MAX; i++) {
            String areaName = readString(11);
            int carNum = readInt(1);
            carNumInWaitAreas[i] = carNum;
            waitAreaNames[i] = areaName;
        }

    }

    @Override
    public String toString() {
        int i = -1;
        return "대기지역현황응답 (0x" + Integer.toHexString(messageType) + ") " +
                "carId=" + carId +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i] +
                ", waitAreaNames["+ (++i) +"]=" + waitAreaNames[i] + ", carNumInWaitAreas["+i+"]=" + carNumInWaitAreas[i];
    }
}
