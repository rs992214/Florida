package com.thinkware.florida.external.service.tachometer;

import com.thinkware.florida.utility.ByteUtil;
import com.thinkware.florida.utility.log.LogHelper;
import com.thinkware.florida.external.service.DataParser;
import com.thinkware.florida.external.service.data.TachoMeterData;

/**
 * Combo 계열 미터기 데이터 파싱
 */
final class HankukComboDataParser extends DataParser {

    private static final int STEP_NONE = 0x00;
    private static final int STEP_DONE_HEADER1 = 0x01;
    private static final int STEP_DONE_HEADER2 = 0x02;
    private static final int STEP_DONE_COMMAND = 0x04;
    private static final int STEP_DONE_NEW_BUFFER = 0x08;

    private int step = STEP_NONE;
    private byte command;
    private int length;
    private int index;
    private byte[] readBuffer;
    private byte[] cmdBuffer = { (byte)0xAA, (byte)0xd0, 0x35, 0x0b, 0x70, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte)0xD6 };

    private enum POS {
        FLAG(0, 1),
        BUTTON_STATUS(1, 2),
        FARE(16, 3),
        MILEAGE(27, 3),
	    VACANCY_MILEAGE(30, 3),;

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
//        8bit 2의 보수를 이용하여 check sum 하려면
//        byte a = 0x21;
//        int b = (~(a) + 0x01) & 0xff;
//        or
//        int c = (0x100 - a) & 0xff;
//
//        LogHelper.d("bbbbb:" + (b & 0xff));
//        LogHelper.d("ccccc:" + (c & 0xff));
        for (int i = 0; i < size; i++) {
            if (step == STEP_NONE) {
                if (buffer[i] == (byte) 0xAA) {
                    step = STEP_DONE_HEADER1;
                }
            } else if (step == STEP_DONE_HEADER1) {
                if (buffer[i] == (byte) 0xD4) {
                    step = STEP_DONE_HEADER2;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_HEADER2) {
                command = buffer[i];
                step = STEP_DONE_COMMAND;
            } else if (step == STEP_DONE_COMMAND) {
                // command가 0x75 일때만
                if (command == (byte) 0x75) {
                    length = (buffer[i] & 0xFF) + 1;
                    if (readBuffer != null) {
                        readBuffer = null;
                    }
                    readBuffer = new byte[length];
                    index = 0;
                    step = STEP_DONE_NEW_BUFFER;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_NEW_BUFFER) {
                if (index < length) {
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

        int flag = buffer[POS.FLAG.getIndex()];
        if (flag == 0x70) {

            int status = getConvertStatusData(buffer[1]);
            int fare = 0;
            int mileage = 0;
            int vacancyMileage = 0;
            String fareStr = ByteUtil.toHexString(buffer, POS.FARE.getIndex(), POS.FARE.getLength());
            String mileageStr = ByteUtil.toHexString(buffer, POS.MILEAGE.getIndex(), POS.MILEAGE.getLength());
	        /** 2017. 10. 25 - 권석범
	         *  인솔라인 김용태 팀장 요청으로 빈차 -> 주행 전환시 빈차이동 거리를 전송하기 위해 vacancyMileage 추가
	         */
			String vacancyMileageStr = ByteUtil.toHexString(buffer, POS.VACANCY_MILEAGE.getIndex(), POS.VACANCY_MILEAGE.getLength());

            try {
                fare = Integer.valueOf(fareStr);
                mileage =Integer.valueOf(mileageStr);
	            vacancyMileage =Integer.valueOf(vacancyMileageStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            TachoMeterData data = new TachoMeterData();
            data.setCommand(command);
            data.setStatus(status);
            data.setFare(fare);
            data.setMileage(mileage);
	        data.setVacancyMileage(vacancyMileage);

            if (callback != null) {
                callback.onParse(data);
            }
        }
    }

    private int getConvertStatusData(int status) {
        // TachoMeterData의 Status 값이랑 동일하기 때문에 status를 그냥 반환한다
        // 순서가 달라지면 아래와 같이 변경하여야 한다.
//        int result = 0;
//        for (int i = 0; i < 8 ; i++) {
//            int shift = 0x01 << i;
//            if ((status & shift) == shift) {
//                switch (i) {
//                    case 0:
//                        result |= TachoMeterData.STATUS_VACANCY;
//                        break;
//                    case 1:
//                        result |= TachoMeterData.STATUS_DRIVING;
//                        break;
//                    case 2:
//                        result |= TachoMeterData.STATUS_EXTRA_CHARGE;
//                        break;
//                    case 3:
//                        result |= TachoMeterData.STATUS_PAYMENT;
//                        break;
//                    case 4:
//                        result |= TachoMeterData.STATUS_COMPLEX;
//                        break;
//                    case 5:
//                        result |= TachoMeterData.STATUS_CALL;
//                        break;
//                }
//            }
//        }
//        return result;
        return  status;
    }

}
