package com.christophesmet.getiton.library.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by christophesmet on 08/09/15.
 */

public class RxUtils {

    public static Observable<Intent> fromBroadcast(@NonNull final Context appContext, @NonNull final IntentFilter intentFilter, final boolean oneShot) {
        return Observable.create(new Observable.OnSubscribe<Intent>() {
            @Override
            public void call(final Subscriber<? super Intent> subscriber) {
                final BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        subscriber.onNext(intent);
                        if (oneShot) {
                            subscriber.unsubscribe();
                        }
                    }
                };
                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        Log.d(null, "Unsubscribe from broadcast");
                        appContext.unregisterReceiver(receiver);
                        subscriber.onCompleted();
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return false;
                    }
                });
                appContext.registerReceiver(receiver, intentFilter);
            }

        });
    }
    //M shit
    //https://code.google.com/p/android-developer-preview/issues/detail?id=2218
    //mfuckers
    public static Observable<Boolean> fixNoInternetAP(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //Only required from L and up
            return Observable.just(true);
        } else {
            return Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(final Subscriber<? super Boolean> subscriber) {
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }
                    final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkRequest request = new NetworkRequest.Builder()
                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                            .build();
                    ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
                        @Override
                        public void onAvailable(Network network) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                cm.setProcessDefaultNetwork(network);  // L and above
                            } else {
                                cm.bindProcessToNetwork(network); // M and above
                            }
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            }
                        }
                    };
                    cm.registerNetworkCallback(request, callback);
                }
            });
        }
    }
}
