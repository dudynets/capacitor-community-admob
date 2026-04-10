package com.getcapacitor.community.admob.rewardedinterstitial

import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.getcapacitor.community.admob.helpers.FullscreenPluginCallback
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.AdOptions
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewardedinterstitial.RewardedInterstitialAdEventCallback
import com.google.android.gms.common.util.BiConsumer

object RewardedInterstitialAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Rewarded, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>, adOptions: AdOptions): AdLoadCallback<RewardedInterstitialAd> {
        return object : AdLoadCallback<RewardedInterstitialAd> {
            override fun onAdLoaded(ad: RewardedInterstitialAd) {
                AdRewardInterstitialExecutor.mRewardedInterstitialAd = ad
                val fullscreenCallback = FullscreenPluginCallback(RewardInterstitialAdPluginEvents, notifyListenersFunction)
                AdRewardInterstitialExecutor.mRewardedInterstitialAd.adEventCallback = object : RewardedInterstitialAdEventCallback {
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

                val adInfo = JSObject()
                adInfo.put("adUnitId", ad.adUnitId)
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(RewardInterstitialAdPluginEvents.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

}
