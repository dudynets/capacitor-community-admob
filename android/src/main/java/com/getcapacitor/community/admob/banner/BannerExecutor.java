package com.getcapacitor.community.admob.banner;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Supplier;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.helpers.AdViewIdHelper;
import com.getcapacitor.community.admob.helpers.RequestHelper;
import com.getcapacitor.community.admob.models.AdMobPluginError;
import com.getcapacitor.community.admob.models.AdOptions;
import com.getcapacitor.community.admob.models.Executor;
import com.google.android.gms.common.util.BiConsumer;
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize;
import com.google.android.libraries.ads.mobile.sdk.banner.AdView;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback;
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest;
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback;
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError;

public class BannerExecutor extends Executor {

    private final JSObject emptyObject = new JSObject();
    private RelativeLayout mAdViewLayout;
    private AdView mAdView;
    private ViewGroup mViewGroup;

    public BannerExecutor(
        Supplier<Context> contextSupplier,
        Supplier<Activity> activitySupplier,
        BiConsumer<String, JSObject> notifyListenersFunction,
        String pluginLogTag
    ) {
        super(contextSupplier, activitySupplier, notifyListenersFunction, pluginLogTag, "BannerExecutor");
    }

    public void initialize() {
        mViewGroup = (ViewGroup) ((ViewGroup) activitySupplier.get().findViewById(android.R.id.content)).getChildAt(0);
    }

    public void showBanner(final PluginCall call) {
        final AdOptions adOptions = AdOptions.getFactory().createBannerOptions(call);
        float density = contextSupplier.get().getResources().getDisplayMetrics().density;

        int defaultWidthPixels = contextSupplier.get().getResources().getDisplayMetrics().widthPixels;

        DisplayMetrics metrics = new DisplayMetrics();
        activitySupplier.get().getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realWidthPixels = metrics.widthPixels;

        boolean fullscreen = false;
        if ((activitySupplier.get().getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
            fullscreen = true;
        }

        if (mAdView != null) {
            updateExistingAdView(adOptions);
            return;
        }

        // Why a try catch block?
        try {
            mAdView = new AdView(contextSupplier.get());

            AdSize bannerSize;
            if (!adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                bannerSize = adOptions.adSize.getSize();
            } else {
                // ADAPTIVE BANNER
                bannerSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                    contextSupplier.get(),
                    (int) (defaultWidthPixels / density)
                );
            }

            // Setup AdView Layout
            mAdViewLayout = new RelativeLayout(contextSupplier.get());
            mAdViewLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            mAdViewLayout.setVerticalGravity(Gravity.BOTTOM);

            final CoordinatorLayout.LayoutParams mAdViewLayoutParams = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            // TODO: Make an enum like the AdSizeEnum?
            switch (adOptions.position) {
                case "TOP_CENTER":
                    mAdViewLayoutParams.gravity = Gravity.TOP;
                    break;
                case "CENTER":
                    mAdViewLayoutParams.gravity = Gravity.CENTER;
                    break;
                default:
                    mAdViewLayoutParams.gravity = Gravity.BOTTOM;
                    break;
            }

            // set Safe Area only for Android 15+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                View rootView = activitySupplier.get().getWindow().getDecorView();
                rootView.setOnApplyWindowInsetsListener((v, insets) -> {
                    int bottomInset = insets.getSystemWindowInsetBottom();
                    int topInset = insets.getSystemWindowInsetTop();

                    if ("TOP_CENTER".equals(adOptions.position)) {
                        mAdViewLayoutParams.setMargins(0, topInset, 0, 0);
                    } else {
                        mAdViewLayoutParams.setMargins(0, 0, 0, bottomInset);
                    }

                    mAdViewLayout.setLayoutParams(mAdViewLayoutParams);
                    return insets;
                });
            }

            mAdViewLayout.setLayoutParams(mAdViewLayoutParams);

            int densityMargin = (int) (adOptions.margin * density);

            // Center Banner Ads
            int adWidth = (int) (adOptions.adSize.getSize().getWidth() * density);

            if (adWidth <= 0 || adOptions.adSize.toString().equals("ADAPTIVE_BANNER")) {
                int margin = 0;
                if (fullscreen) {
                    margin = (realWidthPixels - defaultWidthPixels) / 2;
                }
                mAdViewLayoutParams.setMargins(margin, densityMargin, margin, densityMargin);
            } else {
                int sideMargin = ((int) defaultWidthPixels - adWidth) / 2;
                if (fullscreen) {
                    sideMargin = (realWidthPixels - adWidth) / 2;
                }
                mAdViewLayoutParams.setMargins(sideMargin, densityMargin, sideMargin, densityMargin);
            }

            final String adId = AdViewIdHelper.getFinalAdId(adOptions, logTag, contextSupplier.get());
            createNewAdView(adOptions, adId, bannerSize);

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void hideBanner(final PluginCall call) {
        if (mAdView == null) {
            call.reject("You tried to hide a banner that was never shown");
            return;
        }

        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    if (mAdViewLayout != null) {
                        mAdViewLayout.setVisibility(View.GONE);

                        final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);

                        notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                        call.resolve();
                    }
                });
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void resumeBanner(final PluginCall call) {
        try {
            activitySupplier
                .get()
                .runOnUiThread(() -> {
                    if (mAdViewLayout != null && mAdView != null) {
                        mAdViewLayout.setVisibility(View.VISIBLE);

                        final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(mAdView);
                        notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                        Log.d(logTag, "Banner AD Resumed");
                    }
                });

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    public void removeBanner(final PluginCall call) {
        try {
            if (mAdView != null) {
                activitySupplier
                    .get()
                    .runOnUiThread(() -> {
                        if (mAdView != null) {
                            mViewGroup.removeView(mAdViewLayout);
                            mAdViewLayout.removeView(mAdView);
                            mAdView.destroy();
                            mAdView = null;
                            Log.d(logTag, "Banner AD Removed");
                            final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                            notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                        }
                    });
            }

            call.resolve();
        } catch (Exception ex) {
            call.reject(ex.getLocalizedMessage(), ex);
        }
    }

    private void updateExistingAdView(AdOptions adOptions) {
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                final String adId = AdViewIdHelper.getFinalAdId(adOptions, logTag, contextSupplier.get());
                AdSize bannerSize = adOptions.adSize.getSize();
                final BannerAdRequest adRequest = RequestHelper.createBannerRequest(adOptions, adId, bannerSize);
                mAdView.loadAd(
                    adRequest,
                    new AdLoadCallback<BannerAd>() {
                        @Override
                        public void onAdLoaded(@NonNull BannerAd bannerAd) {}

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {}
                    }
                );
            });
    }

    /**
     * Follow iOS method Name:
     * https://developers.google.com/admob/ios/banner?hl=ja
     */
    private void createNewAdView(AdOptions adOptions, String adId, AdSize bannerSize) {
        // Run AdMob In Main UI Thread
        activitySupplier
            .get()
            .runOnUiThread(() -> {
                final BannerAdRequest adRequest = RequestHelper.createBannerRequest(adOptions, adId, bannerSize);

                mAdView.setAdEventCallback(
                    new BannerAdEventCallback() {
                        @Override
                        public void onAdImpression() {
                            notifyListeners(BannerAdPluginEvents.AdImpression.getWebEventName(), emptyObject);
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            notifyListeners(BannerAdPluginEvents.Opened.getWebEventName(), emptyObject);
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            notifyListeners(BannerAdPluginEvents.Closed.getWebEventName(), emptyObject);
                        }
                    }
                );

                // Add the AdView to the view hierarchy.
                mAdViewLayout.addView(mAdView);

                // Start loading the ad.
                mAdView.loadAd(
                    adRequest,
                    new AdLoadCallback<BannerAd>() {
                        @Override
                        public void onAdLoaded(@NonNull BannerAd bannerAd) {
                            activitySupplier
                                .get()
                                .runOnUiThread(() -> {
                                    final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(mAdView);

                                    notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);
                                    notifyListeners(BannerAdPluginEvents.Loaded.getWebEventName(), emptyObject);
                                });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                            activitySupplier
                                .get()
                                .runOnUiThread(() -> {
                                    if (mAdView != null) {
                                        mViewGroup.removeView(mAdViewLayout);
                                        mAdViewLayout.removeView(mAdView);
                                        mAdView.destroy();
                                        mAdView = null;
                                    }

                                    final BannerAdSizeInfo sizeInfo = new BannerAdSizeInfo(0, 0);
                                    notifyListeners(BannerAdPluginEvents.SizeChanged.getWebEventName(), sizeInfo);

                                    final AdMobPluginError adMobPluginError = new AdMobPluginError(adError);
                                    notifyListeners(BannerAdPluginEvents.FailedToLoad.getWebEventName(), adMobPluginError);
                                });
                        }
                    }
                );

                // Add AdViewLayout top of the WebView
                mViewGroup.addView(mAdViewLayout);
            });
    }
}
