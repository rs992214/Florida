package com.thinkware.florida.external;

import java.util.ArrayList;
import java.util.List;

/**
 * 미터기 종류를 정의한다.
 */
public class TachoMeterType {
    private static ArrayList<String> tachoMeters;

    static {
        // 순서는 com.thinkware.florida.external.service.tachometer.TachoMeterDataParserFactory에서 사용함
        tachoMeters = new ArrayList<>();
        tachoMeters.add("금호");
        tachoMeters.add("한국");
        tachoMeters.add("광신");
    }


    /**
     * 미터기 종류를 반환한다.
     *
     * @return 미터기 종류 list
     */
    public static List getTachoMeterList() {
        return tachoMeters;
    }

    /**
     * index에 해당하는 미터기 종류를 반화한다.
     *
     * @param index
     * @return 미터기 이름
     */
    public static String getTachoMeter(int index) {
        return tachoMeters.get(index);
    }

    /**
     * 미터기의 index를 반환한다.
     *
     * @param name 찾고자 하는 미터기 이름
     * @return index
     */
    public static int getTachoMeterKey(String name) {
        int size = tachoMeters.size();
        int key = -1;
        if (size > 0 && name != null) {
            for (int i = 0; i < size; i++) {
                if (tachoMeters.get(i).compareTo(name) == 0) {
                    key = i;
                    break;
                }
            }
        }
        return key;
    }


}
