package com.christophesmet.getiton.library.core.identify.lan;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.christophesmet.getiton.library.logging.ILoggingService;

import java.util.List;

import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;

/**
 * Created by christophesmet on 05/09/15.
 */

public class DefaultLanModuleIdentifier implements ILanModuleIdentifier {
    @NonNull
    private ILoggingService mILoggingService;
    @NonNull
    private List<ILanModuleIdentifier> mIModuleIdentifiers;

    public DefaultLanModuleIdentifier(@NonNull Context context, @NonNull ILoggingService ILoggingService, @NonNull List<ILanModuleIdentifier> identifiers) {
        this.mILoggingService = ILoggingService;
        this.mIModuleIdentifiers = identifiers;
    }

    @Nullable
    @Override
    public LanModule identifyLanModule(@NonNull LanModule lanModule) {
        for (ILanModuleIdentifier identifier : mIModuleIdentifiers) {
            LanModule output = identifier.identifyLanModule(lanModule);
            if (output != null) {
                return output;
            }
        }
        return null;
    }
}