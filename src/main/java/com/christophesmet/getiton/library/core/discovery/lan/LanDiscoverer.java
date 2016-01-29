package com.christophesmet.getiton.library.core.discovery.lan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.CharMatcher;
import com.google.common.net.InetAddresses;
import com.christophesmet.getiton.library.logging.ILoggingService;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import com.christophesmet.getiton.library.api.ApiUtils;
import com.christophesmet.getiton.library.api.IApiService;
import com.christophesmet.getiton.library.api.model.StatusResponse;
import com.christophesmet.getiton.library.core.discovery.lan.model.LanScanResult;
import com.christophesmet.getiton.library.core.discovery.lan.repo.LanModuleRepo;
import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;
import com.christophesmet.getiton.library.utils.SubnetUtils;
import com.christophesmet.getiton.library.utils.WifiUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by christophesmet on 05/09/15.
 */

public class LanDiscoverer {

    private static final int PING_TIMEOUT_MS = 5000;

    private LanModuleRepo mLanModuleRepo;
    private ILoggingService mILoggingService;
    private Context mContext;

    public LanDiscoverer(@NonNull Context context, ILoggingService ILoggingService, @NonNull LanModuleRepo lanModuleRepo) {
        this.mContext = context;
        mILoggingService = ILoggingService;
        this.mLanModuleRepo = lanModuleRepo;
    }

    @Nullable
    public LanModule queryCachedLanModuleForMac(String mac, @NonNull String networkBSSID) {
        return mLanModuleRepo.getSavedModule(mac, networkBSSID);
    }

    @NonNull
    public List<LanModule> queryCachedLanModules(@NonNull String networkBSSID) {
        return mLanModuleRepo.getSavedModulesForNetwork(networkBSSID);
    }

    public Observable<LanModule> scanSubnet() {
        final InterfaceAddress activeAddress = getActiveNetworkInterface();
        if (activeAddress == null) {
            mILoggingService.log("Active address null");
            return Observable.empty();
        }

        final String mCurrentWifiBSSID = WifiUtils.getCurrentBSSId(mContext);

        final IApiService apiService = ApiUtils.getInstance(mContext, mILoggingService).createApiServiceForInterface(IApiService.class);

        return getScannableLanIps(getActiveNetworkInterface())
                .subscribeOn(Schedulers.io())
                .map(new Func1<InetAddress, LanScanResult>() {
                    @Override
                    public LanScanResult call(InetAddress inetAddress) {
                        //We used to ping, but it sucks.
                        return new LanScanResult(inetAddress, System.currentTimeMillis(), null);
                    }
                })
                .flatMap(new Func1<LanScanResult, Observable<LanModule>>() {
                    @Override
                    public Observable<LanModule> call(final LanScanResult lanScanResult) {
                        //Fetch the status
                        if (lanScanResult == null) {
                            return Observable.empty();
                        }
                        mILoggingService.log("requesting status: " + lanScanResult.getAddress().toString());

                        return apiService.getStatusForAbsoluteUr("http://" + lanScanResult.getAddress().getHostAddress() + "/status")
                                .flatMap(new Func1<StatusResponse, Observable<LanModule>>() {
                                    @Override
                                    public Observable<LanModule> call(StatusResponse statusResponse) {
                                        mILoggingService.log("got result for " + lanScanResult.toString());

                                        lanScanResult.setStatus(statusResponse.getStatus());
                                        if (lanScanResult.getStatus() != null) {
                                            lanScanResult.getStatus().Mac = CharMatcher.JAVA_LETTER_OR_DIGIT.retainFrom(lanScanResult.getStatus().Mac).toLowerCase();
                                            LanModule module = new LanModule(lanScanResult.getAddress(), lanScanResult.getStatus(), mCurrentWifiBSSID);
                                            mLanModuleRepo.saveLanModule(module);
                                            return Observable.just(module);

                                        }
                                        return Observable.empty();
                                    }
                                })
                                .onErrorResumeNext(new Func1<Throwable, Observable<? extends LanModule>>() {
                                    @Override
                                    public Observable<? extends LanModule> call(Throwable throwable) {
                                        //mILoggingService.log(throwable);
                                        return Observable.empty();
                                    }
                                });
                    }
                });
    }

    @Nullable
    private InterfaceAddress getActiveNetworkInterface() {
        Enumeration<NetworkInterface> networkInterfaces = null;
        try {
            networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                mILoggingService.log(networkInterface.toString());
                if (!networkInterface.isLoopback() && !networkInterface.isVirtual() && networkInterface.isUp()) {
                    List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : addresses) {
                        //Only want ipv4
                        if (interfaceAddress.getNetworkPrefixLength() <= 32) {
                            return interfaceAddress;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            mILoggingService.log(e.getMessage());
        }
        return null;
    }

    /**
     * Excluding mine.
     */
    private Observable<InetAddress> getScannableLanIps(final InterfaceAddress networkInterfaceAddress) {
        String subnetMask = new SubnetUtils("0.0.0.0/" + networkInterfaceAddress.getNetworkPrefixLength()).getInfo().getNetmask();
        SubnetUtils subnetUtils = new SubnetUtils(networkInterfaceAddress.getAddress().toString().replace("/", ""), subnetMask);
        final int myAddress = InetAddresses.coerceToInteger(networkInterfaceAddress.getAddress());

        String mCurrentNetworkBSSID = WifiUtils.getCurrentBSSId(mContext);
        if (mCurrentNetworkBSSID == null) {
            return Observable.error(new Exception("Unknown wifi network"));
        }
        final List<LanModule> savedModules = mLanModuleRepo.getSavedModulesForNetwork(mCurrentNetworkBSSID);

        mILoggingService.log("Merged previous saved network modules with subnet, saved count -> " + savedModules.size());

        return Observable.from(subnetUtils.getInfo().getAllAddresses())
                .observeOn(Schedulers.io())
                .map(new Func1<String, InetAddress>() {
                    @Override
                    public InetAddress call(String s) {
                        try {
                            return InetAddress.getByName(s);
                        } catch (UnknownHostException e) {
                        }
                        return null;
                    }
                })
                .filter(new Func1<InetAddress, Boolean>() {
                    @Override
                    public Boolean call(InetAddress inetAddress) {
                        //If null or same as my address
                        boolean isInvalidOrLoopback = inetAddress == null || inetAddress.equals(networkInterfaceAddress.getAddress());
                        if (isInvalidOrLoopback) {
                            return false;
                        }
                        //Remove savedModules
                        for (LanModule module : savedModules) {
                            if (module.getAddress() != null) {
                                if (Arrays.equals(module.getAddress().getAddress(), inetAddress.getAddress())) {
                                    //This address was successful last time.
                                    //Remove it so we can add it to the top later
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                })
                .toSortedList(new Func2<InetAddress, InetAddress, Integer>() {
                    @Override
                    public Integer call(InetAddress inetAddress, InetAddress inetAddress2) {
                        int leftAddr = InetAddresses.coerceToInteger(inetAddress);
                        int rightAddr = InetAddresses.coerceToInteger(inetAddress2);

                        int distance1 = leftAddr - myAddress;
                        int distance2 = rightAddr - myAddress;
                        return Math.abs(distance1) - Math.abs(distance2);
                    }
                })
                .map(new Func1<List<InetAddress>, List<InetAddress>>() {
                    @Override
                    public List<InetAddress> call(List<InetAddress> inetAddresses) {
                        //add the history inetaddresses to the top as the are filtered out.
                        ArrayList<InetAddress> output = new ArrayList<InetAddress>(inetAddresses);
                        for (LanModule module : savedModules) {
                            output.add(0, module.getAddress());
                        }
                        return output;
                    }
                }).flatMap(new Func1<List<InetAddress>, Observable<InetAddress>>() {
                    @Override
                    public Observable<InetAddress> call(List<InetAddress> inetAddresses) {
                        return Observable.from(inetAddresses);
                    }
                });
    }

}