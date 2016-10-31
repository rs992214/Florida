package com.thinkware.florida.external.service.emergency;

import com.thinkware.florida.external.service.DataParser;

/**
 * Emergency 데이터 파싱
 */
public final class EmergencyDataParser extends DataParser {

    @Override
    public void parse(byte[] buffer, int size) {
        // 2byte 만 와야한다.
        if (size == 2 && (buffer[0] == (byte) 0x30 || buffer[0] == (byte) 0x31)) {
            int status = (buffer[0] - '0') & 0xFF;
            makeData(status);
        }
    }

    private void makeData(int status) {
        if (callback != null) {
            callback.onParse(status);
        }
    }

}
