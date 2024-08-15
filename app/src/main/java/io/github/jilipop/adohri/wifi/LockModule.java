package io.github.jilipop.adohri.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class LockModule {
    @Provides
    public PowerManager.WakeLock provideWakeLock(@ApplicationContext Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AD:WakeLock");
    }

    @Provides
    public WifiManager.WifiLock provideWifiLock(@ApplicationContext Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "AD:WifiLock");
    }
}
