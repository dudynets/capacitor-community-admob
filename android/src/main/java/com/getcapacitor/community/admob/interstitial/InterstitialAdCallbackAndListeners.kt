package com.getcapacitor.community.admob.interstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.gms.common.util.BiConsumer

object InterstitialAdCallbackAndListeners {

    fun getInterstitialAdLoadCallback(call: PluginCall,
                                      notifyListenersFunction: BiConsumer<String, JSObject>,
    ): AdLoadCallback<InterstitialAd> {
        return object : AdLoadCallback<InterstitialAd> {
            override fun onAdLoaded(ad: InterstitialAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                val fullscreenCallback = FullscreenPluginCallback(InterstitialAdPluginPluginEvent, notifyListenersFunction)
                ad.adEventCallback = object : InterstitialAdEventCallback {
                    override fun onAdShowedFullScreenContent() {
                        fullscreenCallback.onAdShowedFullScreenContent()
                    }
                    override fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
                        fullscreenCallback.onAdFailedToShowFullScreenContent(error)
                    }
                    override fun onAdDismissedFullScreenContent() {
                        fullscreenCallback.onAdDismissedFullScreenContent()
                    }
                    override fun onAdImpression() {}
                }
                ad.setImmersiveMode(immersiveMode ?: false)

                AdInterstitialExecutor.interstitialAd = ad

                val adInfo = JSObject()
                adInfo.put("adUnitId", call.getString("adId", ""))
                call.resolve(adInfo)

                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(InterstitialAdPluginPluginEvent.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }
}
