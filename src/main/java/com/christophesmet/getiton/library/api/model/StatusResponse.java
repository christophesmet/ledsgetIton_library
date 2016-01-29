package com.christophesmet.getiton.library.api.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

/**
 * Created by christophesmet on 15/09/15.
 */

public class StatusResponse {
    /**
     * {
     * "Status": {
     * "Version": 1,
     * "Type": "PWM-RGB",
     * "Mac": "'..wifi.sta.getmac()..'",
     * Id: "'..node.chipid()..'"
     * }
     * }'
     */

    @NonNull
    @Expose
    Status Status;

    @NonNull
    public Status getStatus() {
        return Status;
    }
}