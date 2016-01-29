package com.christophesmet.getiton.library.core.discovery.lan.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.InetAddress;

import com.christophesmet.getiton.library.api.model.Status;


/**
 * Created by christophesmet on 05/09/15.
 */

public class LanScanResult {
    private long mResponseDate;
    private InetAddress mAddress;
    @Nullable
    private Status mStatus;

    public LanScanResult(InetAddress address, long responseDate, Status status) {
        mAddress = address;
        mResponseDate = responseDate;
        mStatus = status;
    }

    public long getResponseDate() {
        return mResponseDate;
    }

    public InetAddress getAddress() {
        return mAddress;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(@NonNull Status status) {
        mStatus = status;
    }

    @Override
    public String toString() {
        return "LanScanResult{" +
                "mResponseDate=" + mResponseDate +
                ", mAddress=" + mAddress +
                ", mStatus=" + mStatus +
                '}';
    }
}