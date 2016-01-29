package com.christophesmet.getiton.library.db;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by christophesmet on 25/09/15.
 */

public class SavedModuleRepo {

    @NonNull
    private Context mContext;

    public SavedModuleRepo(@NonNull Context context) {
        mContext = context;
    }

    @Nullable
    public SavedModule findModuleForId(@NonNull String id) {
        return new Select().from(SavedModule.class).where("moduleId = ?", id).executeSingle();
    }

    @NonNull
    public List<SavedModule> getAllModules() {
        return new Select().from(SavedModule.class).execute();
    }
}