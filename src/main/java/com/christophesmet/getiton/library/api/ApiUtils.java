package com.christophesmet.getiton.library.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.christophesmet.getiton.library.logging.ILoggingService;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Created by christophesmet on 06/04/15.
 */

public class ApiUtils {

    private static final String URL_BASE = "http://192.168.4.1";

    private static ApiUtils mInstance;
    private Context mContext;

    private RestAdapter mRestAdapter;
    private ILoggingService mLoggingService;
    private OkClient mOkClient;

    public static ApiUtils getInstance(@NonNull Context context, @NonNull ILoggingService loggingService) {
        if (mInstance == null) {
            mInstance = new ApiUtils(context, loggingService);
        }
        return mInstance;
    }

    private ApiUtils(@NonNull Context context, @NonNull ILoggingService loggingService) {
        this.mContext = context;
        this.mLoggingService = loggingService;
        init();
    }

    private void init() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(5,TimeUnit.SECONDS);
        mOkClient = new OkClient(client);
        mRestAdapter = createRetroFit(URL_BASE);

    }

    private RestAdapter createRetroFit(@NonNull String baseUrl) {
        return new RestAdapter.Builder()
                .setClient(mOkClient)
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String message) {
                        //mLoggingService.log(message);
                    }
                })
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setEndpoint(baseUrl)
                .setConverter(new GsonConverter(new Gson()))
                .build();
    }


    public IApiService createApiService() {
        return mRestAdapter.create(IApiService.class);
    }

    public <T> T createApiServiceForInterface(Class<T> interfaceType) {
        return mRestAdapter.create(interfaceType);
    }

    public <T> T createApiServiceForInterface(Class<T> interfaceType, String baseUrl) {
        RestAdapter retrofit = createRetroFit(baseUrl);
        return retrofit.create(interfaceType);
    }
}