package com.thinkware.florida.external.service.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 빈차등에서 수신 받은 데이터를 표현하는 클래스이다.
 */
public final class VacancyLightData implements Parcelable {

    /**
     * 빈차
     */
    public static final int VACANCY = 0x01;
    /**
     * 승차
     */
    public static final int RIDDEN = 0x02;
    /**
     * 예약
     */
    public static final int RESERVATION = 0x04;
    /**
     * 휴무
     */
    public static final int DAY_OFF = 0x08;

    public static final Creator<VacancyLightData> CREATOR = new Creator<VacancyLightData>() {
        @Override
        public VacancyLightData createFromParcel(Parcel in) {
            return new VacancyLightData(in);
        }

        @Override
        public VacancyLightData[] newArray(int size) {
            return new VacancyLightData[size];
        }
    };

    private int status;

    public VacancyLightData() {

    }

    private VacancyLightData(Parcel in) {
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
