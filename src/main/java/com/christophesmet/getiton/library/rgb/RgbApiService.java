package com.christophesmet.getiton.library.rgb;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by christophesmet on 06/04/15.
 */
public interface RgbApiService {
    /**
     * Value goes from 0-1023
     */
    @GET("/animatepwm/{index1}/{value1}/{index2}/{value2}/{index3}/{value3}")
    public Observable<Response> animatepwm(@Path("index1") int gpioIndex1, @Path("value1") int pwmValue1, @Path("index2") int gpioIndex2, @Path("value2") int pwmValue2, @Path("index3") int gpioIndex3,
                                           @Path("value3") int pwmValue3);

    @GET("/pwm/{index1}/{value1}/{index2}/{value2}/{index3}/{value3}")
    public Observable<Response> pwm(@Path("index1") int gpioIndex1, @Path("value1") int pwmValue1, @Path("index2") int gpioIndex2, @Path("value2") int pwmValue2, @Path("index3") int gpioIndex3,
                                    @Path("value3") int pwmValue3);

    @GET("/getpwm/{index1}/{index2}/{index3}")
    public Observable<RgbApiPwmResponse> getPwmValues(@Path("index1") int gpioIndex1,
                                                      @Path("index2") int gpioIndex2,
                                                      @Path("index3") int gpioIndex3
    );

}
