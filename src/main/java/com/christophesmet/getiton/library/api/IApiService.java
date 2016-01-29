package com.christophesmet.getiton.library.api;

import com.christophesmet.getiton.library.api.model.Empty;
import com.christophesmet.getiton.library.api.model.StatusResponse;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Url;
import rx.Observable;

/**
 * Created by christophesmet on 06/04/15.
 */
public interface IApiService {

    @GET("/saveconfig/{ssid}/{pass}")
    public Observable<Empty> saveConfig(@Path("ssid") String ssid, @Path("pass") String pass);

    @GET("/status")
    public Observable<StatusResponse> getStatus();

    @GET("/")
    public Observable<StatusResponse> getStatusForAbsoluteUr(@Url String url);

}
