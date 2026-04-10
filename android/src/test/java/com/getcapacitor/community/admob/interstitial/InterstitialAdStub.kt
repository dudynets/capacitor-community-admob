package com.getcapacitor.community.admob.interstitial

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

internal class InterstitialAdStub: InterstitialAd() {

    private var immersiveMode = false;
    var adEventCallbackField: InterstitialAdEventCallback? = null

    override fun getAdUnitId(): String {
        return "adUnit"
    }

    override fun show(p0: Activity) {
        TODO("Not yet implemented")
    }

    override fun setAdEventCallback(p0: InterstitialAdEventCallback?) {
        adEventCallbackField = p0
    }

    override fun getAdEventCallback(): InterstitialAdEventCallback? {
        return adEventCallbackField
    }

    override fun setImmersiveMode(p0: Boolean) {
        immersiveMode = p0
    }
}
