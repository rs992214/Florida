package com.thinkware.florida.scenario;

/**
 * Created by hoonlee on 2017. 5. 31..
 */

public final class ServiceNumber {
    //서비스번호는 1 BYTE 로 표현 가능해야 한다. 그러므로 범위는 0~255이다.

	/**
	 *  대기관리 UI 적용 : 서비스번호 ( 0, 5, 6, 100, 11, 12)
	 *  일반 대기 UI 적용 : 위의 서비스 번호 이외의 지역
	 */

    //성남은 개인과 법인이 동일 버전을 쓰므로 대표번호인 0을 쓴다.
    public final static int AREA_SUNGNAM_GEN = 0;       //성남 일반
    public final static int AREA_SUNGNAM_GAEIN = 5;     //성남 개인
    public final static int AREA_SUNGNAM_CORP = 6;      //성남 법인
    public final static int AREA_SUNGNAM_MOBUM = 9;     //성남 모범
    public final static int AREA_SUNGNAM_BOKJI = 22;    //성남 복지

    public final static int AREA_KWANGJU = 3;           //광주

    //하남은 개인과 법인이 동일 버전을 쓰므로 대표번호인 100을 쓴다.
    public final static int AREA_HANAM_GEN = 100;       //하남 일반
    public final static int AREA_HANAM_GAEIN = 11;      //하남 개인
    public final static int AREA_HANAM_CORP = 12;       //하남 법인

    public final static int AREA_ICHON = 13;            //이천
}
