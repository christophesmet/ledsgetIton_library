package com.christophesmet.getiton.library.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by christophesmet on 14/04/15.
 */

public class WifiUtils {
    @Nullable
    public static String getCurrentSSID(@NonNull Context context) {
        try {

            WifiInfo wifiInfo = getCurrentWifiInfo(context);
            if (wifiInfo != null) {
                return wifiInfo.getSSID();
            }
        } catch (Exception a) {
        }
        return null;
    }

    @Nullable
    public static String getCurrentBSSId(@NonNull Context context) {
        try {
            WifiInfo info = getCurrentWifiInfo(context);
            if (info != null) {
                return info.getBSSID();
            }
        } catch (Exception ex) {
        }
        return null;
    }

    @Nullable
    public static WifiInfo getCurrentWifiInfo(@NonNull Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getConnectionInfo();
        } catch (Exception ex) {
        }
        return null;
    }

    public static WifiSecurity getScanResultSecurity(String capabilities) {
        final String[] securityModes = {"WEP", "PSK", "EAP"};
        final WifiSecurity[] securityStates = {WifiSecurity.WEP, WifiSecurity.WPA2_PSK, WifiSecurity.WPA2};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (capabilities.contains(securityModes[i])) {
                return securityStates[i];
            }
        }

        return WifiSecurity.OPEN;
    }

    public enum WifiSecurity {
        OPEN, WEP, WPA2_PSK, WPA2
    }
}