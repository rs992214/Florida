package com.thinkware.florida.external.service.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 미터기에서 수신 받은 데이터를 표현하는 클래스이다.
 */
public final class TachoMeterData implements Parcelable {

    /**
     * 빈차
     */
    public static final int STATUS_VACANCY = 0x0001;
    /**
     * 주행
     */
    public static final int STATUS_DRIVING = 0x0002;
    /**
     * 할증
     */
    public static final int STATUS_EXTRA_CHARGE = 0x0004;
    /**
     * 지불
     */
    public static final int STATUS_PAYMENT = 0x0008;
    /**
     * 복합
     */
    public static final int STATUS_COMPLEX = 0x0010;
    /**
     * 콜
     */
    public static final int STATUS_CALL = 0x0020;

    public static final Creator<TachoMeterData> CREATOR = new Creator<TachoMeterData>() {
        @Override
        public TachoMeterData createFromParcel(Parcel in) {
            return new TachoMeterData(in);
        }

        @Override
        public TachoMeterData[] newArray(int size) {
            return new TachoMeterData[size];
        }
    };

    private int command;
    private int status;
    private int fare;
    private int mileage;

    public TachoMeterData() {
    }

    private TachoMeterData(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(command);
        dest.writeInt(status);
        dest.writeInt(fare);
        dest.writeInt(mileage);
    }

    public void readFromParcel(Parcel in) {
        command = in.readInt();
        status = in.readInt();
        fare = in.readInt();
        mileage = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    /**
     * 미터기의 버튼 상태를 반환한다.
     */
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * 요금을 반환한다.
     *
     * @return int
     */
    public int getFare() {
        return fare;
    }

    public void setFare(int fare) {
        this.fare = fare;
    }

    /**
     * 주행거리를 반환한다.
     *
     * @return int
     */
    public int getMileage() {
        return mileage;
    }

    public void setMileage(int mileage) {
        this.mileage = mileage;
    }

}
