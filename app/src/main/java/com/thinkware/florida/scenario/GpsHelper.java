package com.thinkware.florida.scenario;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.thinkware.florida.utility.log.LogHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by zic325 on 2016. 9. 9..
 */
public class GpsHelper {

    private static final int MIN_TIME = 1 * 1000;
    private static final int MIN_DISTANCE = 0;

    private Context context;
    private LocationManager locationManager;
    private SimpleDateFormat simpleDateFormat;
    private Location lastLocation; // 위도, 경도가 올바르지 않게 수신 될 경우(0) 마지막의 위치 정보를 전달하기 위함.

    public GpsHelper(Context context) {
        this.context = context;
        simpleDateFormat = new SimpleDateFormat("yyMMddHHmmss");
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
    }

    public void destroy() {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * @return GPS 경도
     */
    public float getLongitude() {
        findLastKnownLocation();
        if (lastLocation != null) {
            LogHelper.d(">> gps longitude : " + lastLocation.getLongitude());
            return (float) lastLocation.getLongitude();
        }
        return 0.0f;
    }

    /**
     * @return GPS 위도
     */
    public float getLatitude() {
        findLastKnownLocation();
        if (lastLocation != null) {
            LogHelper.d(">> gps latitude : " + lastLocation.getLatitude());
            return (float) lastLocation.getLatitude();
        }
        return 0.0f;
    }

    /**
     * @return GPS 속도
     */
    public int getSpeed() {
        findLastKnownLocation();
        if (lastLocation != null) {
            LogHelper.d(">> gps speed : " + lastLocation.getSpeed());
            return (int) lastLocation.getSpeed();
        }
        return 0;
    }

    /**
     * @return GPS 방향
     */
    public int getBearing() {
        findLastKnownLocation();
        if (lastLocation != null) {
            LogHelper.d(">> gps bearing : " + lastLocation.getBearing());
            return (int) lastLocation.getBearing();
        }
        return 0;
    }

    /**
     * @return GPS 시간 yyMMddHHmmss
     */
    public String getTime() {
        findLastKnownLocation();
        if (lastLocation != null) {
            LogHelper.d(">> gps time : " + lastLocation.getTime());
            return simpleDateFormat.format(new Date(lastLocation.getTime()));
        }
        return simpleDateFormat.format(new Date(System.currentTimeMillis()));
    }

    /**
     * @return 주소 (ex, 구로구 구로3동)
     */
    public String getAddress() {
        findLastKnownLocation();
        if (lastLocation != null) {
            return getAddress(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        return null;
    }


    //---------------------------------------------------------------------------------------------
    // private
    //---------------------------------------------------------------------------------------------
    private void findLastKnownLocation() {
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null && location.getLatitude() > 0 && location.getLongitude() > 0) {
            lastLocation = location;
        }
    }

    private String getAddress(double lat, double lng) {
        String address = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> list = null;

        try {
            list = geocoder.getFromLocation(lat, lng, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list == null) {
            return null;
        }

        if (list.size() > 0) {
            Address addr = list.get(0);
            address = addr.getLocality() + " "
                    + addr.getThoroughfare() + " "
                    + addr.getFeatureName();
        }

        return address;
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            LogHelper.d(">> Location = " + location);
            if (location != null && location.getLatitude() > 0 && location.getLongitude() > 0) {
                lastLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
