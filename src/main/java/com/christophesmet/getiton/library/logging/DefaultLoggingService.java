package com.christophesmet.getiton.library.logging;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by christophesmet on 26/03/15.
 */

public class DefaultLoggingService implements ILoggingService {

    String mDefaultTag = null;

    public DefaultLoggingService(@NonNull Context context) {
        mDefaultTag = context.getApplicationContext().getPackageName();
    }

    public void log(String input) {
        Log.d(mDefaultTag, input);
    }

    @Override
    public void log(Throwable ex) {
        ex.printStackTrace();
        if (ex.getMessage() != null) {
            Log.d(mDefaultTag, ex.getMessage());
        } else {
            Log.d(mDefaultTag, ex.toString());
        }
    }
}