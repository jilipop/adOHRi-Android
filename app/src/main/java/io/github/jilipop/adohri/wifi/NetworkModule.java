package io.github.jilipop.adohri.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    public WifiManager provideWifiManager(@ApplicationContext Context context) {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Provides
    public ConnectivityManager provideConnectivityManager(@ApplicationContext Context context) {
        return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    public WifiConfiguration provideWifiConfiguration() {
        return new WifiConfiguration();
    }

    @Provides
    public NetworkRequestBuilder provideNetworkRequestBuilder() {
        return new NetworkRequestBuilderImpl();
    }
}