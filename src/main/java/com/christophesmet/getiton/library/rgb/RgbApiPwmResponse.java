package com.christophesmet.getiton.library.rgb;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;

/**
 * Created by christophesmet on 30/10/15.
 */

public class RgbApiPwmResponse {

    @NonNull
    @Expose
    public int[] values;
}