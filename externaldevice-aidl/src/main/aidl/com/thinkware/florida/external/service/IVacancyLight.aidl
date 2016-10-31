// IVacancyLight.aidl
package com.thinkware.florida.external.service;

import com.thinkware.florida.external.service.IVacancyLightCallback;
import com.thinkware.florida.external.service.data.VacancyLightData;

// Declare any non-default types here with import statements

interface IVacancyLight {
   boolean registerCallback(IVacancyLightCallback callback);
   boolean unregisterCallback(IVacancyLightCallback callback);
   VacancyLightData getCurrentData();
}
