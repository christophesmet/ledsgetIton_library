package com.christophesmet.getiton.library.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.christophesmet.getiton.library.utils.GsonParser;

import java.net.InetAddress;

import com.christophesmet.getiton.library.api.model.Status;

/**
 * Created by christophesmet on 25/09/15.
 */

@Table(name = "SavedModules")
public class SavedModule extends Model {

    @NonNull
    private Status mStatus;
    @Column(name = "status")
    @NonNull
    private String mDbstrStatus;

    @NonNull
    @Column(name = "address")
    private InetAddress mAddress;

    @Column(name = "Name")
    @NonNull
    private String mName;

    @Column(name = "moduleId")
    @NonNull
    private String mModuleId;

    public SavedModule(@NonNull InetAddress address, @NonNull Status status, @NonNull String name) {
        mAddress = address;
        mStatus = status;
        this.mDbstrStatus = GsonParser.get().GsonDataToString(status);
        mName = name;
        mModuleId = status.Id;
    }

    @Nullable
    public Status getStatus() {
        if (mStatus == null) {
            mStatus = GsonParser.get().parseFromJsonString(mDbstrStatus, Status.class);
        }
        return mStatus;
    }

    @NonNull
    public InetAddress getAddress() {
        return mAddress;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        mName = name;
    }
}