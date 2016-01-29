package com.christophesmet.getiton.library.rgb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.christophesmet.getiton.library.logging.ILoggingService;

import com.christophesmet.getiton.library.api.ApiUtils;
import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;
import com.christophesmet.getiton.library.core.identify.lan.ILanModuleIdentifier;
import com.christophesmet.getiton.library.modules.RGBV1Module;

/**
 * Created by christophesmet on 05/09/15.
 */

public class RgbModuleIdentifier implements ILanModuleIdentifier {
    @NonNull
    private Context mContext;
    @NonNull
    private ILoggingService mILoggingService;

    public RgbModuleIdentifier(@NonNull Context context, @NonNull ILoggingService mloggingService) {
        mContext = context;
        this.mILoggingService = mloggingService;
    }

    @Nullable
    @Override
    public LanModule identifyLanModule(@NonNull LanModule module) {
        if (module.getStatus() != null && module.getStatus().Version == 1 && module.getStatus().Type.equals(RGBV1Module.TYPE)) {
            return new RGBV1Module(module, ApiUtils.getInstance(mContext, mILoggingService).createApiServiceForInterface(RgbApiService.class, "http://" + module.getAddress().getHostAddress()));
        }
        return null;
    }
}