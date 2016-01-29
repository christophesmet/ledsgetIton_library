package com.christophesmet.getiton.library.core.register;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.google.common.base.CharMatcher;
import com.christophesmet.getiton.library.logging.ILoggingService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.christophesmet.getiton.library.api.IApiService;
import com.christophesmet.getiton.library.api.model.Empty;
import com.christophesmet.getiton.library.api.model.StatusResponse;
import com.christophesmet.getiton.library.core.discovery.lan.LanDiscoverer;
import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;
import com.christophesmet.getiton.library.core.discovery.wifi.WifiDiscoverer;
import com.christophesmet.getiton.library.core.discovery.wifi.model.WifiScanResult;
import com.christophesmet.getiton.library.core.identify.wifi.DefaultWifiModuleIdentfier;
import com.christophesmet.getiton.library.core.identify.wifi.IWifiModuleIdentifier;
import com.christophesmet.getiton.library.core.module.WifiModule;
import com.christophesmet.getiton.library.utils.RxUtils;
import rx.Notification;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by christophesmet on 08/09/15.
 */

public class Registrator {

    private static final int LAN_SUBNET_SCAN_RETRY_COUNT = 9;


    @NonNull
    Context mContext;
    @NonNull
    ILoggingService mILoggingService;
    @NonNull
    IApiService mDefaultApiService;

    private WifiDiscoverer mWifiDiscoverer;
    private LanDiscoverer mLanDiscoverer;
    private IWifiModuleIdentifier mWifiModuleIdentifier;
    private WifiManager mWifiManager;

    private boolean mIsRegistering = false;

    public Registrator(@NonNull Context context, @NonNull ILoggingService ILoggingService, @NonNull IApiService apiService, @NonNull LanDiscoverer lanDiscoverer) {
        mContext = context;
        mILoggingService = ILoggingService;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiDiscoverer = new WifiDiscoverer(context, mILoggingService, mWifiManager);
        mWifiModuleIdentifier = new DefaultWifiModuleIdentfier();
        this.mDefaultApiService = apiService;
        this.mLanDiscoverer = lanDiscoverer;
    }

    public Observable<List<WifiScanResult>> requestWifiScan(boolean inclCache) {
        return mWifiDiscoverer.requestWifScan(inclCache);
    }

    public Observable<ArrayList<WifiModule>> scanWifiForCanidates(boolean inclCache) {
        return mWifiDiscoverer.requestWifScan(inclCache)
                .map(new Func1<List<WifiScanResult>, ArrayList<WifiModule>>() {
                    @Override
                    public ArrayList<WifiModule> call(List<WifiScanResult> wifiScanResults) {
                        ArrayList<WifiModule> output = new ArrayList<WifiModule>(wifiScanResults.size());
                        for (WifiScanResult result : wifiScanResults) {
                            WifiModule identifiedModule = mWifiModuleIdentifier.identifyLanModule(result);
                            if (identifiedModule != null) {
                                mILoggingService.log(identifiedModule.toString());
                                output.add(identifiedModule);
                            }
                        }
                        return output;
                    }
                });
    }

    /**
     * We will mrge the wifi changes and regristration states
     *
     * @param targetSSID
     * @param moduleSSID
     * @param moduleAPBSSID
     * @param targetPass
     * @return
     */
    public Observable<RegistrationState> registerModuleToNetwork(@NonNull final String targetBSSID, @NonNull final String targetSSID, @NonNull final String moduleSSID, @NonNull final String moduleAPBSSID, @NonNull final String targetPass, @NonNull final String modulePass) {
        if (mIsRegistering) {
            return Observable.just(RegistrationState.REGISTRATION_STATE_ALREADY_BUSY);
        }
        removeGetItOnNetworkConfigurations(moduleSSID);
        final RegistrationState[] mCurrentState = {RegistrationState.REGISTRATION_STATE_STARTING};
        int previousNetworkId = -1;
        final WifiInfo info = mWifiManager.getConnectionInfo();
        if (info != null) {
            previousNetworkId = info.getNetworkId();
        }
        mILoggingService.log("Saving previous network id as : " + previousNetworkId);
        final int finalPreviousNetworkId = previousNetworkId;
        final String[] moduleLanBSSID = {null};

        return RxUtils.fixNoInternetAP(mContext).map(new Func1<Boolean, RegistrationState>() {
            @Override
            public RegistrationState call(Boolean aBoolean) {
                mILoggingService.log("fixing no internet ap");
                return RegistrationState.REGISTRATION_STATE_STARTING;
            }
        }).concatWith(
                getWifiUpdates()
                        .filter(new Func1<WifiInfo, Boolean>() {
                            @Override
                            public Boolean call(WifiInfo wifiInfo) {
                                return wifiInfo.getSupplicantState() == SupplicantState.COMPLETED;
                            }
                        })
                        .flatMap(new Func1<WifiInfo, Observable<? extends WifiInfo>>() {
                            @Override
                            public Observable<? extends WifiInfo> call(final WifiInfo wifiInfo) {
                                return RxUtils.fixNoInternetAP(mContext)
                                        .map(new Func1<Boolean, WifiInfo>() {
                                            @Override
                                            public WifiInfo call(Boolean aBoolean) {
                                                return wifiInfo;
                                            }
                                        });
                            }
                        })
                        .flatMap(new Func1<WifiInfo, Observable<? extends RegistrationState>>() {
                            @Override
                            public Observable<? extends RegistrationState> call(WifiInfo wifiInfo) {
                                mILoggingService.log("state: " + wifiInfo.getSupplicantState().toString());
                                mILoggingService.log("Wifi update " + wifiInfo.toString() + " in state: -> " + mCurrentState[0].toString());
                                if (mCurrentState[0] == RegistrationState.REGISTRATION_STATE_STARTING || mCurrentState[0] == RegistrationState.REGISTRATION_STATE_CONNECTING || mCurrentState[0] == RegistrationState.REGISTRATION_STATE_CONNECTED) {
                                    //Connecting to module
                                    if (isOnSameNetworkAs(wifiInfo, moduleAPBSSID)) {
                                        //Already on network
                                        return Observable.just(RegistrationState.REGISTRATION_STATE_CONNECTED)
                                                .concatWith(Observable.just(RegistrationState.REGISTRATION_STATE_GETTING_DEVICE_INFO))
                                                .concatWith(
                                                        mDefaultApiService.getStatus()
                                                                .map(new Func1<StatusResponse, RegistrationState>() {
                                                                    @Override
                                                                    public RegistrationState call(StatusResponse statusResponse) {
                                                                        if (statusResponse != null && statusResponse.getStatus() != null) {
                                                                            moduleLanBSSID[0] = CharMatcher.JAVA_LETTER_OR_DIGIT.retainFrom(statusResponse.getStatus().Mac);
                                                                        }
                                                                        return RegistrationState.REGISTRATION_STATE_RECEIVED_DEVICE_INFO;
                                                                    }
                                                                })
                                                                .concatWith(saveConfigToModule(targetSSID, moduleSSID, moduleAPBSSID, targetPass)
                                                                        .flatMap(new Func1<RegistrationState, Observable<? extends RegistrationState>>() {
                                                                            @Override
                                                                            public Observable<? extends RegistrationState> call(RegistrationState registrationState) {
                                                                                mILoggingService.log("Saveconfig to module emitted: " + registrationState);
                                                                                if (registrationState == RegistrationState.REGISTRATION_STATE_CONFIGURED) {
                                                                                    mILoggingService.log("Received configured, end registration process");
                                                                                    //We are done
                                                                                    return Observable.just(RegistrationState.REGISTRATION_STATE_CONFIGURED)
                                                                                            .concatWith(
                                                                                                    connectBack(finalPreviousNetworkId, moduleAPBSSID)
                                                                                            );
                                                                                }
                                                                                return Observable.just(registrationState);
                                                                            }
                                                                        })));
                                    } else {
                                        //Connect to it
                                        if (!connectToNetwork(moduleSSID, moduleAPBSSID, modulePass)) {
                                            return Observable.error(new Exception("Unable to connecto to network"));
                                        }
                                        //Now connecting to module
                                        return Observable.just(RegistrationState.REGISTRATION_STATE_CONNECTING);
                                    }
                                } else if (mCurrentState[0] == RegistrationState.REGISTRATION_STATE_CONNECTING_BACK) {
                                    //We changed wifi info and did a connectBack.
                                    //We can look for the lan module now
                                    if (isOnSameNetworkAs(wifiInfo, targetBSSID) || targetSSID.equalsIgnoreCase(wifiInfo.getSSID().replace("\"", "").trim())) {
                                        mILoggingService.log("We are back on the targeted network, start lan scan");
                                        return scanforLanModule(moduleLanBSSID[0]);
                                    } else {
                                        mILoggingService.log("targetssid: " + targetSSID + " ?? currentssid: " + wifiInfo.getSSID());
                                        mILoggingService.log("Just passing it along, we are still not ready ..");
                                        mILoggingService.log("Connecting back but not back on network");
                                    }
                                    return Observable.empty();
                                }
                                mILoggingService.log("No one responsed to state -> " + mCurrentState[0]);
                                return Observable.empty();
                            }
                        })
                        .map(new Func1<RegistrationState, RegistrationState>() {
                            @Override
                            public RegistrationState call(RegistrationState registrationState) {
                                mCurrentState[0] = registrationState;
                                return registrationState;
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends RegistrationState>>() {
                            @Override
                            public Observable<? extends RegistrationState> call(Throwable throwable) {
                                mILoggingService.log("Registration error");
                                mILoggingService.log(throwable);
                                return connectBack(finalPreviousNetworkId, moduleAPBSSID).concatWith(Observable.just(RegistrationState.REGISTRATION_STATE_FAILED));
                            }
                        }));
    }

    private Observable<RegistrationState> scanforLanModule(@NonNull final String moduleMac) {
        final String cleanModuleMac = CharMatcher.JAVA_LETTER_OR_DIGIT.retainFrom(moduleMac).toLowerCase();
        //Delay because of the module that is reconnecting
        return Observable.just(RegistrationState.REGISTRATION_STATE_LAN_SCAN_FOR_ADDED_MODULE)
                .concatWith(scanLanWithRetryForModule(cleanModuleMac, 0)
                        .delaySubscription(25, TimeUnit.SECONDS)
                )
                .concatWith(Observable.just(RegistrationState.REGISTRATION_STATE_DONE));
    }

    private Observable<RegistrationState> scanLanWithRetryForModule(@NonNull final String mac, final int tryCount) {
        mILoggingService.log("Trying lan scan: " + tryCount);
        if (tryCount >= LAN_SUBNET_SCAN_RETRY_COUNT) {
            return Observable.empty();
        }
        return
                mLanDiscoverer
                        .scanSubnet()
                        .filter(new Func1<LanModule, Boolean>() {
                            @Override
                            public Boolean call(LanModule lanModule) {
                                if (lanModule.getStatus() != null) {
                                    mILoggingService.log("comparing lan scan: mac: " + lanModule.getStatus().Mac + " ?? " + mac + " -> " + String.valueOf(lanModule.getStatus().Mac.equalsIgnoreCase(mac)));
                                    return lanModule.getStatus().Mac.equalsIgnoreCase(mac);
                                }
                                return false;
                            }
                        })
                        .firstOrDefault(null)
                        .flatMap(new Func1<LanModule, Observable<? extends RegistrationState>>() {
                            @Override
                            public Observable<? extends RegistrationState> call(LanModule lanModule) {
                                if (lanModule == null) {
                                    mILoggingService.log("no module found,retrying ?");
                                    return Observable.error(new Exception("No module found"));
                                }
                                mILoggingService.log("We have a module, fire the module found with mac -> " + mac);
                                RegistrationState state = RegistrationState.REGISTRATION_STATE_LAN_SCAN_MODULE_FOUND;
                                state.setExtra(mac);
                                return Observable.just(state);
                            }
                        })
                        .retryWhen(new Func1<Observable<? extends Notification<?>>, Observable<?>>() {
                            @Override
                            public Observable<?> call(Observable<? extends Notification<?>> observable) {
                                mILoggingService.log("Register done scan lan failed, retry ?");
                                return Observable.timer(10, TimeUnit.SECONDS)
                                        .flatMap(new Func1<Long, Observable<RegistrationState>>() {
                                            @Override
                                            public Observable<RegistrationState> call(Long aLong) {
                                                return Observable.just(RegistrationState.REGISTRATION_STATE_LAN_SCAN_FOR_ADDED_MODULE)
                                                        .concatWith(
                                                                scanLanWithRetryForModule(mac, tryCount + 1));
                                            }
                                        });
                            }
                        })
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends RegistrationState>>() {
                            @Override
                            public Observable<? extends RegistrationState> call(Throwable throwable) {
                                mILoggingService.log(throwable);
                                return Observable.empty();
                            }
                        });
    }

    private Observable<RegistrationState> connectBack(int networkId, @NonNull String moduleBSSID) {
        mILoggingService.log("Trying connectBack for previous network id : " + networkId);
        if (networkId != -1) {
            switchToNetwork(networkId);
        }
        removeGetItOnNetworkConfigurations(moduleBSSID);
        return Observable.just(RegistrationState.REGISTRATION_STATE_CONNECTING_BACK);
    }

    private boolean isOnSameNetworkAs(@NonNull WifiInfo info, @NonNull String BSSID) {
        if (info.getBSSID() != null && info.getBSSID().replace("\"", "").trim().equalsIgnoreCase(BSSID.replace("\"", "").trim())) {
            mILoggingService.log("On same network: " + info.getBSSID() + " =? " + BSSID);
            return true;
        } else {

            mILoggingService.log("Not on same network: " + info.getBSSID() + " =? " + BSSID);
            return false;
        }
    }

    /**
     * Emit default wificonfig, then emit wifichanges
     *
     * @return
     */
    public Observable<WifiInfo> getWifiUpdates() {
        IntentFilter wifiChangeIntentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        return
                Observable.just(mWifiManager.getConnectionInfo()).filter(new Func1<WifiInfo, Boolean>() {
                    @Override
                    public Boolean call(WifiInfo wifiInfo) {
                        return wifiInfo != null;
                    }
                })
                        .concatWith(
                                RxUtils.fromBroadcast(mContext, wifiChangeIntentFilter, false)
                                        .map(new Func1<Intent, WifiInfo>() {
                                            @Override
                                            public WifiInfo call(Intent intent) {
                                                return mWifiManager.getConnectionInfo();
                                            }
                                        })
                                        .filter(new Func1<WifiInfo, Boolean>() {
                                            @Override
                                            public Boolean call(WifiInfo wifiInfo) {
                                                return wifiInfo != null;
                                            }
                                        })
                        );
    }


    /**
     * Save the config, when already connected to the module.
     *
     * @param targetSSID
     * @param moduleSSID
     * @param moduleBSSID
     * @param targetPass
     * @return
     */
    private Observable<RegistrationState> saveConfigToModule(@NonNull String targetSSID, @NonNull String moduleSSID, @NonNull String moduleBSSID, @NonNull String targetPass) {
        mILoggingService.log("Sending config to module " + targetSSID + " : " + targetPass);
        return Observable.just(RegistrationState.REGISTRATION_STATE_CONFIGURING).concatWith(
                mDefaultApiService.saveConfig(targetSSID, targetPass)
                        .flatMap(new Func1<Empty, Observable<? extends RegistrationState>>() {
                            @Override
                            public Observable<? extends RegistrationState> call(Empty response) {
                                return Observable.just(RegistrationState.REGISTRATION_STATE_CONFIGURED);
                            }
                        })
        );
    }

    private void removeGetItOnNetworkConfigurations(@NonNull String SSIDKey) {
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configurations) {
            if (conf.SSID != null && conf.SSID.replace("\"", "").trim().equalsIgnoreCase(SSIDKey)) {
                mWifiManager.removeNetwork(conf.networkId);
                mILoggingService.log("Removingnetwork with id " + conf.networkId);
            }
        }
    }

    private int findNetworkIdForBSSID(@NonNull String BSSID) {
        List<WifiConfiguration> configurations = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration conf : configurations) {
            if (conf.SSID != null && conf.SSID.replace("\"", "").trim().equalsIgnoreCase(BSSID)) {
                return conf.networkId;
            }
        }
        return -1;
    }

    private boolean connectToNetwork(@NonNull String moduleSSID, @NonNull String moduleBSSID, @NonNull String pass) {
        //removeGetItOnNetworkConfigurations(moduleSSID);
        boolean output = true;
        WifiConfiguration wc = generateWifiConfiguration(moduleSSID, moduleBSSID, pass);
        mWifiManager.setWifiEnabled(true);
        //mWifiManager.reconnect();
        wc.priority = 999;
        int netId = findNetworkIdForBSSID(moduleBSSID);
        if (netId == -1) {
            netId = mWifiManager.addNetwork(wc);
        }
        mILoggingService.log("Add to network -> id: " + netId);
        if (netId == -1) {
            return false;
        } else {
            mWifiManager.saveConfiguration();
        }
        if (!mWifiManager.enableNetwork(netId, false)) {
            mILoggingService.log("enable network failed");
            output = false;
        }
        if (!mWifiManager.saveConfiguration()) {
            mILoggingService.log("saveconfig failed wifimaanger");

            output = false;
        } else {
            mWifiManager.enableNetwork(netId, true);
        }

        return output;
    }

    private void switchToNetwork(int networkId) {
        mWifiManager.setWifiEnabled(true);
        if (!mWifiManager.enableNetwork(networkId, false)) {
        }
        if (!mWifiManager.saveConfiguration()) {
        } else {
            mWifiManager.enableNetwork(networkId, true);
        }
    }


    private WifiConfiguration generateWifiConfiguration(String SSID, String BSSID, String pass) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.BSSID = BSSID;
        wc.SSID = "\"" + SSID + "\"";
        wc.preSharedKey = "\"" + pass + "\"";
        wc.status = WifiConfiguration.Status.ENABLED;
        return wc;
    }
}