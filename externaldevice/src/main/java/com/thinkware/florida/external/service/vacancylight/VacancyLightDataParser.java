package com.thinkware.florida.external.service.vacancylight;

import com.thinkware.florida.external.service.DataParser;
import com.thinkware.florida.external.service.data.VacancyLightData;

/**
 * 빈차등 데이터 파싱
 */
public final class VacancyLightDataParser extends DataParser {

    private static final int STEP_NONE = 0x00;
    private static final int STEP_DONE_SYNC = 0x01;
    private static final int STEP_DONE_FUNC = 0x02;

    private int step = STEP_NONE;
    private int status;

    @Override
    public void parse(byte[] buffer, int size) {
        for (int i = 0; i < size; i++) {
            if (step == STEP_NONE) {
                if ((buffer[i] & 0xFF) == 0x80) {
                    step = STEP_DONE_SYNC;
                }
            } else if (step == STEP_DONE_SYNC) {
                if (((buffer[i] & 0xFF) & 0x0F) == buffer[i]) {
                    status = buffer[i] & 0xFF;
                    step = STEP_DONE_FUNC;
                } else {
                    step = STEP_NONE;
                }
            } else if (step == STEP_DONE_FUNC) {
                if ((buffer[i] & 0xFF) == (status | 0x80)) {
                    makeData(status);
                }
                step = STEP_NONE;
            }
        }
    }

    private synchronized void makeData(int status) {
        VacancyLightData data = new VacancyLightData();
        data.setStatus(status);

        if (callback != null) {
            callback.onParse(data);
        }
    }

}
