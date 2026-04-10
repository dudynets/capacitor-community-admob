package com.getcapacitor.community.admob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.getcapacitor.JSArray;
import com.getcapacitor.PluginCall;
import com.getcapacitor.community.admob.banner.BannerExecutor;
import com.google.android.libraries.ads.mobile.sdk.MobileAds;
import com.google.android.libraries.ads.mobile.sdk.common.RequestConfiguration;
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AdMobTest {

    @Mock
    Context mockedContext;

    @Mock
    AppCompatActivity mockedActivity;

    @Mock
    PluginCall pluginCallMock;

    @Mock
    MockedConstruction<BannerExecutor> bannerExecutorMockedConstruction;

    AdMob sut;

    @BeforeEach
    public void beforeEach() {
        reset(pluginCallMock, mockedContext);

        sut = new AdMob() {
            @Override
            public Context getContext() {
                return mockedContext;
            }

            @Override
            public AppCompatActivity getActivity() {
                return mockedActivity;
            }

            @Override
            public String getLogTag() {
                return "LogTag";
            }
        };
    }

    @AfterEach
    public void afterEach() {
        bannerExecutorMockedConstruction.close();
    }

    @Nested
    @DisplayName("Initialize()")
    class Initialize {

        MockedStatic<MobileAds> mobileAdsMockedStatic;

        ArgumentCaptor<InitializationConfig> configCaptor;

        @BeforeEach
        void beforeEachInitializeTest() {
            mobileAdsMockedStatic = Mockito.mockStatic(MobileAds.class);
            configCaptor = ArgumentCaptor.forClass(InitializationConfig.class);

            // Mock PackageManager to return application info with meta-data
            try {
                ApplicationInfo appInfo = new ApplicationInfo();
                appInfo.metaData = new Bundle();
                appInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", "ca-app-pub-test~test");
                PackageManager pm = Mockito.mock(PackageManager.class);
                when(mockedContext.getPackageManager()).thenReturn(pm);
                when(mockedContext.getPackageName()).thenReturn("com.test.app");
                when(pm.getApplicationInfo("com.test.app", PackageManager.GET_META_DATA)).thenReturn(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @AfterEach
        void afterEachInitializeTest() {
            mobileAdsMockedStatic.close();
        }

        @Test
        @DisplayName("Initializes the banner executor")
        public void bannerExecutorInitialize() {
            when(pluginCallMock.getBoolean("initializeForTesting", false)).thenReturn(false);

            sut.initialize(pluginCallMock);

            BannerExecutor bannerExecutor = bannerExecutorMockedConstruction.constructed().get(0);
            verify(bannerExecutor).initialize();
        }
    }
}
