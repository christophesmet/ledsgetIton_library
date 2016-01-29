package com.christophesmet.getiton.library.core.discovery.wifi.model;

import com.christophesmet.getiton.library.utils.WifiUtils;

/**
 * Created by christophesmet on 07/09/15.
 */

public class WifiScanResult {

    private String mSSID;
    private String mBSSID;
    private String mCapabilities;
    private int mDistanceCm;
    private int level;
    private int mFrequency;

    public WifiScanResult(String SSID, String BSSID, String capabilities, int distanceCm, int level, int frequency) {
        mSSID = SSID;
        mBSSID = BSSID;
        mCapabilities = capabilities;
        mDistanceCm = distanceCm;
        this.level = level;
        mFrequency = frequency;
    }

    @Override
    public String toString() {
        return "WifiScanResult{" +
                "mSSID='" + mSSID + '\'' +
                ", mBSSID='" + mBSSID + '\'' +
                ", mCapabilities='" + mCapabilities + '\'' +
                ", mDistanceCm=" + mDistanceCm +
                ", level=" + level +
                '}';
    }

    public String getSSID() {
        return mSSID;
    }

    public String getBSSID() {
        return mBSSID;
    }

    public String getCapabilities() {
        return mCapabilities;
    }

    public int getDistanceCm() {
        return mDistanceCm;
    }

    public int getLevel() {
        return level;
    }

    public int getFrequency() {
        return mFrequency;
    }

    public WifiUtils.WifiSecurity getSecurity() {
        return WifiUtils.getScanResultSecurity(mCapabilities);
    }

    public boolean is24Ghz() {
        return mFrequency >= 2400 && mFrequency <= 2500;
    }
}