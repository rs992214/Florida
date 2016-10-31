package com.thinkware.florida.external.service.tachometer;

import com.thinkware.florida.utility.ByteUtil;
import com.thinkware.florida.utility.log.LogHelper;
import com.thinkware.florida.external.service.DataParser;
import com.thinkware.florida.external.service.data.TachoMeterData;

/**
 * 금호 K 계열 미터기 데이터 파싱
 */
final class KumhoKhTopDataParser extends DataParser {

    private static final int STEP_NONE = 0x00;
    private static final int STEP_DONE_STX = 0x01;
    private static final int STEP_DONE_LENGTH1 = 0x02;
    private static final int STEP_DONE_LENGTH2 = 0x04;
    private static final int STEP_DONE_NEW_BUFFER = 0x08;

    private int step = STEP_NONE;
    private byte command;
    private int length;
    private int index;
    private byte[] readBuffer;
    // 초기
    private byte[] cmdBuffer = {(byte) 0xFF, 0x02, 0x30, 0x33, 0x3A, 0x36, 0x33, 0x03};
    private StringBuilder sb = new StringBuilder(2);

    // 데이터 위치
    private enum POS {
        MODEL_CODE(0, 1),
        BUTTON_STATUS(1, 2),
        FARE(21, 6),
        MILEAGE(27, 6),;

        private int index;
        private int length;

        POS(int index, int length) {
            this.index = index;
            this.length = length;
        }

        int getIndex() {
            return index;
        }

        int getLength() {
            return length;
        }
    }

    @Override
    public byte[] getInitCommandData() {
        return cmdBuffer;
    }

    @Override
    public void parse(byte[] buffer, int size) {
        for (int i = 0; i < size; i++) {
            if (step == STEP_NONE) {
                if (buffer[i] == 0x02) {
                    byte b = (byte) 0xFF;
                    step = STEP_DONE_STX;
                    length = 0;
                    sb.delete(0, sb.length());
                }
            } else if (step == STEP_DONE_STX) {
                char c = (char) buffer[i];
                if (c >= '0' && c <= '9') {
                    sb.append(c - '0');
                    step = STEP_DONE_LENGTH1;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_LENGTH1) {
                char c = (char) buffer[i];
                if (c >= '0' && c <= '9') {
                    sb.append(c - '0');
                    step = STEP_DONE_LENGTH2;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_LENGTH2) {
                // command가 0x5A 일때만
                if (buffer[i] == 0x5A) {
                    command = buffer[i];

                    if (readBuffer != null) {
                        readBuffer = null;
                    }
                    length = Integer.parseInt(sb.toString(), 16);
                    readBuffer = new byte[length];
                    index = 0;
                    step = STEP_DONE_NEW_BUFFER;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_NEW_BUFFER) {
                if (index < length) {
//                    LogHelper.d("index : %d, \tbytes: 0x%x", index, buffer[i]);
                    readBuffer[index++] = buffer[i];
                }
            }
        }
        if (step == STEP_DONE_NEW_BUFFER && index == length) {
            step = STEP_NONE;
            makeData(command, readBuffer);
        }
    }

    private void makeData(int command, byte[] buffer) {
        LogHelper.d(ByteUtil.toHexString(buffer));

        // 모델코드(1): "2" (32h)
        // 버튼상태(2): "10" 지불", "20" 빈차, "40" 주행, "80" 할증, "04" 호출, "08" 복합
        // 날짜, 시간(10)
        // 승차시간(4)
        // 하차시간(4)
        // 승차요금(6)
        // 승차거리(6)
        // 호출요금(6)
        // 예비(6)

        int status = getConvertStatusData(getParsedData(buffer, POS.BUTTON_STATUS.getIndex(), POS.BUTTON_STATUS.getLength()));
        int fare = getParsedData(buffer, POS.FARE.getIndex(), POS.FARE.getLength());
        int mileage = getParsedData(buffer, POS.MILEAGE.getIndex(), POS.MILEAGE.getLength());

        TachoMeterData data = new TachoMeterData();
        data.setCommand(command);
        data.setStatus(status);
        data.setFare(fare);
        data.setMileage(mileage);

        if (callback != null) {
            callback.onParse(data);
        }
    }

    private int getParsedData(byte[] buffer, int index, int length) {
        int value = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = index; i < (index + length); i++) {
            char c = (char) buffer[i];
            sb.append(c - '0');
        }

        try {
            value = Integer.valueOf(sb.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return value;
    }

    private int getConvertStatusData(int status) {
        if (status == 20) {
            return TachoMeterData.STATUS_VACANCY;
        } else if (status == 40) {
            return TachoMeterData.STATUS_DRIVING;
        } else if (status == 80) {
            return TachoMeterData.STATUS_EXTRA_CHARGE;
        } else if (status == 10) {
            return TachoMeterData.STATUS_PAYMENT;
        } else if (status == 8) {
            return TachoMeterData.STATUS_COMPLEX;
        } else if (status == 4) {
            return TachoMeterData.STATUS_CALL;
        }
        return -1;
    }

}
