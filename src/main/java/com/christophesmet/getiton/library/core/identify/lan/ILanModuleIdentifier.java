package com.christophesmet.getiton.library.core.identify.lan;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;

/**
 * Created by christophesmet on 05/09/15.
 */

public interface ILanModuleIdentifier {
    @Nullable
    public LanModule identifyLanModule(@NonNull LanModule module);

}