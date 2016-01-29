package com.christophesmet.getiton.library.core.identify.wifi;

import android.support.annotation.NonNull;

import com.christophesmet.getiton.library.core.discovery.wifi.model.WifiScanResult;
import com.christophesmet.getiton.library.core.module.WifiModule;

/**
 * Created by christophesmet on 08/09/15.
 */

public interface IWifiModuleIdentifier {
    public WifiModule identifyLanModule(@NonNull WifiScanResult scanResult);

}