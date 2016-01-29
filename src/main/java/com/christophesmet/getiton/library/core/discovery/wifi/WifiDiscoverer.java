package com.christophesmet.getiton.library.core.discovery.wifi;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.christophesmet.getiton.library.logging.ILoggingService;

import java.util.ArrayList;
import java.util.List;

import com.christophesmet.getiton.library.core.discovery.wifi.model.WifiScanResult;
import com.christophesmet.getiton.library.utils.RxUtils;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by christophesmet on 07/09/15.
 */

public class WifiDiscoverer {

    @NonNull
    private Context mContext;
    @NonNull
    private ILoggingService mILoggingService;

    @NonNull
    private WifiManager mWifiManager;

    public WifiDiscoverer(@NonNull Context context, @NonNull ILoggingService loggingService, @NonNull WifiManager wifiManager) {
        mContext = context;
        this.mILoggingService = loggingService;
        this.mWifiManager = wifiManager;
    }

    public Observable<List<WifiScanResult>> requestWifScan(boolean inclusiveCache) {

        IntentFilter intentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        Observable<List<ScanResult>> cachedResults;

        if (inclusiveCache) {
            cachedResults = Observable.just(mWifiManager.getScanResults());
        } else {
            cachedResults = Observable.empty();
        }

        Observable<List<WifiScanResult>> observableOutput = cachedResults
                .mergeWith(
                        RxUtils.fromBroadcast(mContext, intentFilter, true)
                                .flatMap(new Func1<Intent, Observable<List<ScanResult>>>() {
                                    @Override
                                    public Observable<List<ScanResult>> call(Intent intent) {
                                        return Observable.just(mWifiManager.getScanResults());
                                    }
                                })
                )
                .map(new Func1<List<ScanResult>, List<WifiScanResult>>() {
                    @Override
                    public List<WifiScanResult> call(List<ScanResult> scanResults) {
                        List<WifiScanResult> output = new ArrayList<WifiScanResult>(scanResults.size());
                        for (ScanResult result : scanResults) {
                            WifiScanResult scanResult = new WifiScanResult(result.SSID, result.BSSID, result.capabilities, 0, result.level, result.frequency);
                            output.add(scanResult);
                            //mILoggingService.log(scanResult.toString());
                        }
                        return output;
                    }
                });
        if (mWifiManager.startScan()) {
            mILoggingService.log("Wifi scan started");
        } else {
            mILoggingService.log("Wifi scan not started");
        }
        return observableOutput;
    }
}