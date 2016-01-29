package com.christophesmet.getiton.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;

import java.util.ArrayList;

import com.christophesmet.getiton.library.api.ApiUtils;
import com.christophesmet.getiton.library.api.IApiService;
import com.christophesmet.getiton.library.core.discovery.lan.LanDiscoverer;
import com.christophesmet.getiton.library.core.discovery.lan.repo.LanModuleRepo;
import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;
import com.christophesmet.getiton.library.core.identify.lan.DefaultLanModuleIdentifier;
import com.christophesmet.getiton.library.core.identify.lan.ILanModuleIdentifier;
import com.christophesmet.getiton.library.core.register.Registrator;
import com.christophesmet.getiton.library.logging.ILoggingService;
import com.christophesmet.getiton.library.rgb.RgbModuleIdentifier;
import com.christophesmet.getiton.library.utils.WifiUtils;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by christophesmet on 05/09/15.
 */

public class GetItOn {

    @NonNull
    private ILoggingService mILoggingService;
    @NonNull
    private Context mContext;
    @NonNull
    IApiService mIApiService;

    //Identification;
    private ILanModuleIdentifier mIModuleIdentifier;

    //Registration
    private Registrator mRegistrator;

    //Lan
    private LanDiscoverer mLanDiscoverer;

    public GetItOn(@NonNull Context context, @NonNull ILoggingService ILoggingService) {
        mContext = context;
        ActiveAndroid.initialize(mContext);
        mILoggingService = ILoggingService;
        init();
    }

    private void init() {
        mIApiService = ApiUtils.getInstance(mContext, mILoggingService).createApiService();
        mLanDiscoverer = new LanDiscoverer(mContext, mILoggingService, new LanModuleRepo());
        mRegistrator = new Registrator(mContext, mILoggingService, ApiUtils.getInstance(mContext, mILoggingService).createApiService(), mLanDiscoverer);
        mIModuleIdentifier = new DefaultLanModuleIdentifier(mContext, mILoggingService, new ArrayList<ILanModuleIdentifier>(1) {{
            add(new RgbModuleIdentifier(mContext, mILoggingService));
        }});
        loadDatabase();
    }

    private void loadDatabase() {
        Configuration dbConfig = new Configuration.Builder(mContext)
                .setDatabaseName("GetItOnFwDb")
                .create();
        ActiveAndroid.initialize(dbConfig);
    }

    public Registrator getRegistrator() {
        return mRegistrator;
    }

    public Observable<LanModule> scanForLanModulesWithCache() {
        String currentBSSID = WifiUtils.getCurrentBSSId(mContext);
        if (currentBSSID == null) {
            return scanForLanModules();
        } else {
            return Observable.from(mLanDiscoverer.queryCachedLanModules(currentBSSID))
                    .mergeWith(scanForLanModules());
        }
    }

    public Observable<LanModule> scanForLanModules() {
        return mLanDiscoverer.scanSubnet()
                .subscribeOn(Schedulers.computation())
                .map(new Func1<LanModule, LanModule>() {
                    @Override
                    public LanModule call(LanModule lanModule) {
                        return mIModuleIdentifier.identifyLanModule(lanModule);
                    }
                })
                .filter(new Func1<LanModule, Boolean>() {
                    @Override
                    public Boolean call(LanModule module) {
                        return module != null;
                    }
                });
    }

    @Nullable
    public LanModule identifyModule(@Nullable LanModule module) {
        if (module == null) {
            return null;
        }
        return mIModuleIdentifier.identifyLanModule(module);
    }

    @Nullable
    public LanModule queryCachedLanModuleForMac(@Nullable String mac) {
        if (mac == null) {
            return null;
        }
        String currentBSSID = WifiUtils.getCurrentBSSId(mContext);
        if (currentBSSID == null) {
            return null;
        }
        return mLanDiscoverer.queryCachedLanModuleForMac(mac, currentBSSID);
    }


    public Observable<LanModule> getModuleForId(@NonNull final String id) {
        LanModule module = identifyModule(queryCachedLanModuleForMac(id));
        if (module != null) {
            return Observable.just(module);
        } else {
            return scanForLanModules()
                    .filter(new Func1<LanModule, Boolean>() {
                        @Override
                        public Boolean call(LanModule lanModule) {
                            return id.equals(lanModule.getMac());
                        }
                    });
        }
    }
}
