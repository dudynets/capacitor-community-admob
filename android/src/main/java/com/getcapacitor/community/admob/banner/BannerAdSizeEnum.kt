package com.getcapacitor.community.admob.banner

import com.google.android.libraries.ads.mobile.sdk.banner.AdSize

/**
 * https://developers.google.com/admob/android/banner#banner_sizes
 */
enum class BannerAdSizeEnum(val size: AdSize) {
    BANNER(AdSize.BANNER),
    FULL_BANNER(AdSize.FULL_BANNER),
    LARGE_BANNER(AdSize.LARGE_BANNER),
    MEDIUM_RECTANGLE(AdSize.MEDIUM_RECTANGLE),
    LEADERBOARD(AdSize.LEADERBOARD),
    ADAPTIVE_BANNER(AdSize(0, 0)), // Placeholder - actual size is calculated dynamically
    SMART_BANNER(AdSize.BANNER); // SMART_BANNER removed in Next-Gen, fallback to BANNER

    override fun toString(): String {
        return name
    }
}
