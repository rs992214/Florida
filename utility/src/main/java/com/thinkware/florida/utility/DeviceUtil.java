package com.thinkware.florida.utility;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Device utils.
 */
public class DeviceUtil {

    public static boolean hasBluetoothLeFeature(Context context) {
        return hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean hasSystemFeature(Context context, String feature) {
        PackageManager manager = context.getPackageManager();
        return manager.hasSystemFeature(feature);
    }

}
