package com.getcapacitor.community.admob.helpers

import com.getcapacitor.JSObject
import com.getcapacitor.community.admob.models.AdMobPluginError
import com.getcapacitor.community.admob.models.LoadPluginEventNames
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.gms.common.util.BiConsumer

class FullscreenPluginCallback(private val loadPluginObject: LoadPluginEventNames,
                               private val notifyListenersFunction: BiConsumer<String, JSObject>) {

    fun onAdShowedFullScreenContent() {
        notifyListenersFunction.accept(loadPluginObject.Showed, JSObject())
    }

    fun onAdFailedToShowFullScreenContent(error: FullScreenContentError) {
        val adMobError = AdMobPluginError(error.code.ordinal, error.message ?: "Unknown error")
        notifyListenersFunction.accept(
                loadPluginObject.FailedToShow, adMobError
        )
    }

    fun onAdDismissedFullScreenContent() {
        notifyListenersFunction.accept(loadPluginObject.Dismissed, JSObject())
    }
}
