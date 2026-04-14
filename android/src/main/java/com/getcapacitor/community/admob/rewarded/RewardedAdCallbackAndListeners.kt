package com.getcapacitor.community.admob.rewarded

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
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.rewarded.ServerSideVerificationOptions
import com.google.android.gms.common.util.BiConsumer

object RewardedAdCallbackAndListeners {

    fun getOnUserEarnedRewardListener(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>): OnUserEarnedRewardListener {
        return OnUserEarnedRewardListener { item: RewardItem ->
            val response = JSObject()
            response.put("type", item.type)
                    .put("amount", item.amount)
            notifyListenersFunction.accept(RewardAdPluginEvents.Rewarded, response)
            call.resolve(response)
        }
    }

    fun getRewardedAdLoadCallback(call: PluginCall, notifyListenersFunction: BiConsumer<String, JSObject>, adOptions: AdOptions): AdLoadCallback<RewardedAd> {
        return object : AdLoadCallback<RewardedAd> {
            override fun onAdLoaded(ad: RewardedAd) {
                val immersiveMode = call.getBoolean("immersiveMode")
                ad.setImmersiveMode(immersiveMode ?: false)

                AdRewardExecutor.mRewardedAd = ad
                val fullscreenCallback = FullscreenPluginCallback(RewardAdPluginEvents, notifyListenersFunction)
                AdRewardExecutor.mRewardedAd.adEventCallback = object : RewardedAdEventCallback {
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

                if(adOptions.ssvInfo.hasInfo){
                    val ssvOptionsBuilder = ServerSideVerificationOptions.Builder()
                    adOptions.ssvInfo.customData?.let {
                        ssvOptionsBuilder.setCustomData(it)
                    }

                    adOptions.ssvInfo.userId?.let {
                        ssvOptionsBuilder.setUserId(it)
                    }
                    AdRewardExecutor.mRewardedAd.setServerSideVerificationOptions(ssvOptionsBuilder.build())
                }

                val adInfo = JSObject()
                adInfo.put("adUnitId", call.getString("adId", ""))
                call.resolve(adInfo)

                notifyListenersFunction.accept(RewardAdPluginEvents.Loaded, adInfo)
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                val adMobError = AdMobPluginError(adError)

                notifyListenersFunction.accept(RewardAdPluginEvents.FailedToLoad, adMobError)
                call.reject(adError.message)
            }
        }
    }

}
