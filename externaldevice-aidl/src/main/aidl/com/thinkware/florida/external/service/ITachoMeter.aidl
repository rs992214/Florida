// ITachoMeter.aidl
package com.thinkware.florida.external.service;

import com.thinkware.florida.external.service.ITachoMeterCallback;
import com.thinkware.florida.external.service.data.TachoMeterData;

// Declare any non-default types here with import statements

interface ITachoMeter {
   boolean registerCallback(ITachoMeterCallback callback);
   boolean unregisterCallback(ITachoMeterCallback callback);
   TachoMeterData getCurrentData();
}
