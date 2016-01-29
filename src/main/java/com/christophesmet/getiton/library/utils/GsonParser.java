package com.christophesmet.getiton.library.utils;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.lang.reflect.Type;

public class GsonParser {
    private static GsonParser _instance;
    private GsonBuilder gsonBuilder;
    private Gson gson;

    public static GsonParser get() {
        if (_instance == null) {
            _instance = new GsonParser();
        }
        return _instance;
    }

    private GsonParser() {
        this.gsonBuilder = new GsonBuilder();
        this.gson = gsonBuilder.create();
    }

    public String GsonDataToString(Object data) {
        try {
            if (data != null) {
                return gson.toJson(data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    @Nullable
    public <T> T parseFromJsonString(String data, Class<T> type) {
        if (data != null && data.length() > 0) {
            try {
                return gson.fromJson(data, type);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    @Nullable
    public <T> T parseFromJsonString(String data, Type type) {
        if (data != null && data.length() > 0) {
            try {
                return gson.fromJson(data, type);
            } catch (Exception ex) {

            }
        }
        return null;
    }
}
