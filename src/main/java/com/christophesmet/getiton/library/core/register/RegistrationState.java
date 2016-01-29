package com.christophesmet.getiton.library.core.register;

import android.support.annotation.Nullable;

/**
 * Created by christophesmet on 10/09/15.
 */

public enum RegistrationState {

    REGISTRATION_STATE_STARTING(null),
    REGISTRATION_STATE_CONNECTING(null),
    REGISTRATION_STATE_CONNECTED(null),
    REGISTRATION_STATE_GETTING_DEVICE_INFO(null),
    REGISTRATION_STATE_RECEIVED_DEVICE_INFO(null),
    REGISTRATION_STATE_CONFIGURING(null),
    REGISTRATION_STATE_CONFIGURED(null),
    REGISTRATION_STATE_CONNECTING_BACK(null),
    REGISTRATION_STATE_LAN_SCAN_FOR_ADDED_MODULE(null),
    REGISTRATION_STATE_LAN_SCAN_MODULE_FOUND(null),
    REGISTRATION_STATE_ALREADY_BUSY(null),
    REGISTRATION_STATE_FAILED(null),
    REGISTRATION_STATE_DONE(null);

    RegistrationState(@Nullable Object extra) {
        this.extra = extra;
    }

    @Nullable
    private Object extra;

    @Nullable
    public Object getExtra() {
        return extra;
    }

    public void setExtra(@Nullable Object extra) {
        this.extra = extra;
    }
}