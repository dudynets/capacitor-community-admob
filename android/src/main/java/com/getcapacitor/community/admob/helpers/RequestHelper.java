package com.getcapacitor.community.admob.helpers;

import android.os.Bundle;
import com.getcapacitor.community.admob.models.AdOptions;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest;

public final class RequestHelper {

    private RequestHelper() {}

    private static Bundle createNpaExtras(AdOptions adOptions) {
        if (adOptions.npa) {
            Bundle extras = new Bundle();
            extras.putString("npa", "1");
            return extras;
        }
        return null;
    }

    /**
     * Create a fullscreen ad request (for interstitial, rewarded, rewarded interstitial)
     */
    public static AdRequest createRequest(AdOptions adOptions, String adUnitId) {
        AdRequest.Builder adRequestBuilder = new AdRequest.Builder(adUnitId);

        Bundle extras = createNpaExtras(adOptions);
        if (extras != null) {
            adRequestBuilder.setGoogleExtrasBundle(extras);
        }

        return adRequestBuilder.build();
    }

    /**
     * Create a banner ad request
     */
    public static BannerAdRequest createBannerRequest(AdOptions adOptions, String adUnitId, AdSize adSize) {
        BannerAdRequest.Builder adRequestBuilder = new BannerAdRequest.Builder(adUnitId, adSize);

        Bundle extras = createNpaExtras(adOptions);
        if (extras != null) {
            adRequestBuilder.setGoogleExtrasBundle(extras);
        }

        return adRequestBuilder.build();
    }
}
