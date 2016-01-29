package com.christophesmet.getiton.library.core.discovery.lan.repo;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

import com.christophesmet.getiton.library.core.discovery.lan.repo.model.LanModule;

/**
 * Created by christophesmet on 05/10/15.
 */

public class LanModuleRepo {

    public void saveLanModule(@NonNull LanModule module) {
        LanModule savedModule = getSavedModule(module.getMac(), module.getNetworkBSSID());
        if (savedModule != null) {
            savedModule.setLastContacted(new Date());
            savedModule.setStatus(module.getStatus());
            savedModule.setAddress(module.getRawAddress());
            savedModule.save();
        } else {
            module.save();
        }
    }

    public boolean hasModule(@NonNull String macModule, @NonNull String networkBSSID) {
        return new Select()
                .from(LanModule.class)
                .where(LanModule.COLUMN_MAC + " = ? AND " + LanModule.COLUMN_NETWORK_BSSID + " = ?", macModule, networkBSSID)
                .count() > 0;
    }

    public List<LanModule> getSavedModulesForNetwork(@NonNull String networkBSSId) {
        return new Select()
                .from(LanModule.class)
                .where(LanModule.COLUMN_NETWORK_BSSID + " = ?", networkBSSId)
                .execute();
    }

    @Nullable
    public LanModule getSavedModule(@NonNull String macModule, @NonNull String networkBSSID) {
        return new Select()
                .from(LanModule.class)
                .where(LanModule.COLUMN_MAC + " = ? AND " + LanModule.COLUMN_NETWORK_BSSID + " = ?", macModule, networkBSSID)
                .executeSingle();
    }
}