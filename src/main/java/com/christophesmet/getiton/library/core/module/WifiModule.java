package com.christophesmet.getiton.library.core.module;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * Created by christophesmet on 08/09/15.
 */

public class WifiModule implements Parcelable {

    private boolean isInRange;
    //core wifi data
    @Nullable
    private String mSSID;
    @Nullable
    private String mBSSID;

    public WifiModule(String SSID, String BSSID, boolean isInRange) {
        mSSID = SSID;
        mBSSID = BSSID;
        this.isInRange = isInRange;
    }

    protected WifiModule(Parcel in) {
        isInRange = in.readByte() != 0;
        mSSID = in.readString();
        mBSSID = in.readString();
    }

    public static final Creator<WifiModule> CREATOR = new Creator<WifiModule>() {
        @Override
        public WifiModule createFromParcel(Parcel in) {
            return new WifiModule(in);
        }

        @Override
        public WifiModule[] newArray(int size) {
            return new WifiModule[size];
        }
    };

    @Override
    public String toString() {
        return "WifiModule{" +
                "isInRange=" + isInRange +
                ", mSSID='" + mSSID + '\'' +
                ", mBSSID='" + mBSSID + '\'' +
                '}';
    }

    @Nullable
    public String getSSID() {
        return mSSID;
    }

    @Nullable
    public String getBSSID() {
        return mBSSID;
    }

    @Override
    public int describeContents() {
        return 0;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeByte((byte) (isInRange ? 1 : 0));
        dest.writeString(mSSID);
        dest.writeString(mBSSID);
    }

}