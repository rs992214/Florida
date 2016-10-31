// ITachoMeterCallback.aidl
package com.thinkware.florida.external.service;

import com.thinkware.florida.external.service.data.VacancyLightData;

// Declare any non-default types here with import statements
/**
* 빈차등 상태를 전달 받는 콜백 인터페이스.
*/
interface IVacancyLightCallback {
   /**
    * 빈차등 상태를 전달 받는다.
    * @param data 빈차등 상태 데이터. null 일 수 있다.
    */
    void onReceive(in VacancyLightData data);
    /**
     * 서비스 상태를 전달 받는다.
     * @param status {@link com.thinkware.florida.external.service.data.ServiceStatus}
     */
    void onServiceStatus(int status);
}
