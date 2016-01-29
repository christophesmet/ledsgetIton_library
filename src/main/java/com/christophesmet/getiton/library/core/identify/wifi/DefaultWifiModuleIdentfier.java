package com.christophesmet.getiton.library.core.identify.wifi;

import android.support.annotation.NonNull;

import com.christophesmet.getiton.library.core.discovery.wifi.model.WifiScanResult;
import com.christophesmet.getiton.library.core.module.WifiModule;

/**
 * Created by christophesmet on 08/09/15.
 */

public class DefaultWifiModuleIdentfier implements IWifiModuleIdentifier {

    private int thressholdRange = -46;

    @Override
    public WifiModule identifyLanModule(@NonNull WifiScanResult scanResult) {
        if (scanResult.getSSID().toLowerCase().contains("Leds Get It On".toLowerCase())) {
            WifiModule output = new WifiModule(scanResult.getSSID(), scanResult.getBSSID(), scanResult.getLevel() >= thressholdRange);
            return output;
        }
        return null;
    }
}