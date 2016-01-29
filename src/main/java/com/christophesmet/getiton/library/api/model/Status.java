package com.christophesmet.getiton.library.api.model;

/**
 * Created by christophesmet on 12/04/15.
 */

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

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

public class Status {

    @Expose
    public int Version;
    @Expose
    @NonNull
    public String Type;
    @Expose
    @NonNull
    public String Mac;
    @Expose
    @NonNull
    public String Id;

    @Override
    public String toString() {
        return "Status{" +
            "Version=" + Version +
            ", Type='" + Type + '\'' +
            ", Mac='" + Mac + '\'' +
            ", Id='" + Id + '\'' +
            '}';
    }
}