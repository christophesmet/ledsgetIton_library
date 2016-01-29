package com.christophesmet.getiton.library.core.discovery.lan.repo.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import com.christophesmet.getiton.library.api.model.Status;
import com.christophesmet.getiton.library.core.module.AbstractModule;
import com.christophesmet.getiton.library.utils.GsonParser;

/**
 * Created by christophesmet on 05/10/15.
 * This is a a module saved in the db.
 * Last contacted,
 * scan miss, counter we won't keep it forever
 * id based on mac
 * found on what network ?
 */

@Table(name = "lanmodules")
public class LanModule extends AbstractModule {


    public static final String COLUMN_MAC = "mac";
    public static final String COLUMN_NETWORK_BSSID = "networkBSSID";

    @Column(name = "strStatus")
    protected String strStatus;
    private Status mStatus;

    @Column(name = "lastContacted")
    protected Date mLastContacted;

    @Column(name = "timesMissed")
    protected int mLastMissed;

    @Column(name = "mac")
    protected String mMac;

    @Column(name = "networkBSSID")
    protected String mNetworkBSSID;

    @Column(name = "networkAddress")
    protected byte[] mAddress;


    public LanModule() {
        super();
    }

    public LanModule(@NonNull InetAddress address, @NonNull Status status, @NonNull String networkBSSID) {
        this.mLastContacted = new Date();
        this.mMac = status.Mac;
        this.mNetworkBSSID = networkBSSID;
        this.mAddress = address.getAddress();
        this.mStatus = status;
        this.strStatus = GsonParser.get().GsonDataToString(status);
    }

    @Nullable
    public Status getStatus() {
        if (mStatus == null) {
            mStatus = GsonParser.get().parseFromJsonString(strStatus, Status.class);
        }
        return mStatus;
    }

    public void setAddress(@NonNull InetAddress address) {
        mAddress = address.getAddress();
    }
    public void setAddress(byte[] address)
    {
        mAddress = address;
    }

    public Date getLastContacted() {
        return mLastContacted;
    }

    public int getLastMissed() {
        return mLastMissed;
    }

    public String getMac() {
        return mMac;
    }

    public String getNetworkBSSID() {
        return mNetworkBSSID;
    }

    @Nullable
    public InetAddress getAddress() {
        if (mAddress != null) {
            try {
                return InetAddress.getByAddress(mAddress);
            } catch (UnknownHostException e) {
                return null;
            }
        }
        return null;
    }

    @Nullable
    public byte[] getRawAddress() {
        return mAddress;
    }

    public void setStatus(Status status) {
        mStatus = status;
        strStatus = GsonParser.get().GsonDataToString(status);
    }


    public void setLastContacted(Date lastContacted) {
        mLastContacted = lastContacted;
    }

    public void setLastMissed(int lastMissed) {
        mLastMissed = lastMissed;
    }
}