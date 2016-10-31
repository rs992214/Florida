// IEmergency.aidl
package com.thinkware.florida.external.service;

import com.thinkware.florida.external.service.IEmergencyCallback;
import com.thinkware.florida.external.service.data.EmergencyData;

// Declare any non-default types here with import statements

interface IEmergency {
   boolean registerCallback(IEmergencyCallback callback);
   boolean unregisterCallback(IEmergencyCallback callback);
   EmergencyData getCurrentData();
}
