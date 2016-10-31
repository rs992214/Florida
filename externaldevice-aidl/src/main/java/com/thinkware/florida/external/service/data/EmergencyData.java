package com.thinkware.florida.external.service.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Emergency 수신 받은 데이터를 표현하는 클래스이다.
 */
public final class EmergencyData implements Parcelable {

    /**
     * Emergency Off
     */
    public static final int EMERGENCY_OFF = 0;
    /**
     * Emergency On
     */
    public static final int EMERGENCY_ON = 1;

    public static final Creator<EmergencyData> CREATOR = new Creator<EmergencyData>() {
        @Override
        public EmergencyData createFromParcel(Parcel in) {
            return new EmergencyData(in);
        }

        @Override
        public EmergencyData[] newArray(int size) {
            return new EmergencyData[size];
        }
    };

    private int status;

    public EmergencyData() {

    }

    private EmergencyData(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    public void readFromParcel(Parcel in) {
        status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
