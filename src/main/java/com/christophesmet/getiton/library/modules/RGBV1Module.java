package com.christophesmet.getiton.library.modules;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;
import com.christophesmet.getiton.library.rgb.RgbApiPwmResponse;
import com.christophesmet.getiton.library.rgb.RgbApiService;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by christophesmet on 05/09/15.
 */

public class RGBV1Module extends LanModule {
    private static final int PWM_MAX_RANGE = 1023;
    private static final int UDP_PORT = 81;
    private static final int DEBOUNCE = 12;
    public static final String TYPE = "PWM-RGB";

    @NonNull
    private RgbApiService mApiService;

    @Nullable
    private DatagramSocket mDatagramSocket;
    private PublishSubject<ColorRequest> mColorRequestObservable;

    public RGBV1Module() {
    }


    public RGBV1Module(@NonNull LanModule module, @NonNull RgbApiService apiService) {
        super(module.getAddress(), module.getStatus(), module.getNetworkBSSID());

        this.mApiService = apiService;
        this.mColorRequestObservable = PublishSubject.create();
        this.mColorRequestObservable
                .observeOn(Schedulers.io())
                .debounce(DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribe(new Subscriber<ColorRequest>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ColorRequest colorRequest) {
                        setPwmByUdp(colorRequest.getR(), colorRequest.g, colorRequest.getB());
                    }
                });
    }

    public void requestPwmByUdp(@IntRange(from = 0, to = 255) int r, @IntRange(from = 0, to = 255) int g, @IntRange(from = 0, to = 255) int b, @IntRange(from = 0, to = 100) int brightness) {
        int pwmR = (int) ((((r / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmG = (int) ((((g / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmB = (int) ((((b / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);

        this.mColorRequestObservable.onNext(new ColorRequest(pwmR, pwmG, pwmB));
    }

    public Observable<Response> requestPwmAnimateByTcp(@IntRange(from = 0, to = 255) int r, @IntRange(from = 0, to = 255) int g, @IntRange(from = 0, to = 255) int b, @IntRange(from = 0, to = 100) int brightness) {
        int pwmR = (int) ((((r / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmG = (int) ((((g / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmB = (int) ((((b / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);

        return mApiService.animatepwm(GPIO_14, pwmR, GPIO_12, pwmG, GPIO_13, pwmB);
    }
    public Observable<Response> requestPwmByTcp(@IntRange(from = 0, to = 255) int r, @IntRange(from = 0, to = 255) int g, @IntRange(from = 0, to = 255) int b, @IntRange(from = 0, to = 100) int brightness) {
        int pwmR = (int) ((((r / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmG = (int) ((((g / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);
        int pwmB = (int) ((((b / (float) 255) * PWM_MAX_RANGE) / 100) * brightness);

        return mApiService.pwm(GPIO_14, pwmR, GPIO_12, pwmG, GPIO_13, pwmB);
    }

    public Observable<int[]> requestPwmValues() {
        return mApiService.getPwmValues(GPIO_14, GPIO_12, GPIO_13)
                .map(new Func1<RgbApiPwmResponse, int[]>() {
                    @Override
                    public int[] call(RgbApiPwmResponse rgbApiPwmResponse) {
                        return rgbApiPwmResponse.values;
                    }
                })
                .filter(new Func1<int[], Boolean>() {
                    @Override
                    public Boolean call(int[] ints) {
                        return ints.length > 0;
                    }
                })
                .map(new Func1<int[], int[]>() {
                    @Override
                    public int[] call(int[] ints) {
                        return ints;
                    }
                });
    }

    public Observable<Boolean> isOn() {
        return requestPwmValues()
                .map(new Func1<int[], Boolean>() {
                    @Override
                    public Boolean call(int[] ints) {
                        return ints[0] != 0 || ints[1] != 0 && ints[2] != 0;
                    }
                });
    }

    private boolean setPwmByUdp(int r, int g, int b) {
        if (mDatagramSocket == null) {
            try {
                mDatagramSocket = new DatagramSocket();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        final String command = "/pwm/" + GPIO_14 + "/" + r + "/" + GPIO_12 + "/" + g + "/" + GPIO_13 + "/" + b;
        Log.d("ledsgetiton.shroomy", command);
        byte[] dataBlob = command.getBytes();
        final DatagramPacket packet = new DatagramPacket(dataBlob, dataBlob.length);
        packet.setAddress(getAddress());
        packet.setPort(UDP_PORT);
        try {
            mDatagramSocket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private class ColorRequest {
        private int r;
        private int g;
        private int b;

        public ColorRequest(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int getR() {
            return r;
        }

        public int getG() {
            return g;
        }

        public int getB() {
            return b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ColorRequest that = (ColorRequest) o;

            if (r != that.r) return false;
            if (g != that.g) return false;
            return b == that.b;

        }

        @Override
        public int hashCode() {
            int result = r;
            result = 31 * result + g;
            result = 31 * result + b;
            return result;
        }
    }
}