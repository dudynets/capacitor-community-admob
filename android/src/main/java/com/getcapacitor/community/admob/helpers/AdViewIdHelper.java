package com.getcapacitor.community.admob.helpers;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.community.admob.models.AdOptions;

public final class AdViewIdHelper {

    private AdViewIdHelper() {}

    public static String getFinalAdId(AdOptions adOptions, String logTag, Context context) {
        if (!adOptions.isTesting) {
            return adOptions.adId;
        }

        return adOptions.getTestingId();
    }
}
