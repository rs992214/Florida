package com.thinkware.florida.external.service.tachometer;

import com.thinkware.florida.external.service.DataParser;

/**
 * 미터기 데이터 파서 팩토리 클래스
 */
public class TachoMeterDataParserFactory {

    public static DataParser getDataParser(int type) {
        if (type == 0) {
            return new KumhoKhTopDataParser();
        }
        return new HankukComboDataParser();
    }
}
