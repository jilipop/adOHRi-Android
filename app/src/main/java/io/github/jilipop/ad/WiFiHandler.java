package io.github.jilipop.ad;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class WiFiHandler {

    private final Context context;

    public WiFiHandler(Context context) {
        this.context = context;
    }

    public void connect() {
        final NetworkSpecifier specifier =
                new WifiNetworkSpecifier.Builder()
                        .setSsid(Secrets.ssid)
                        .setWpa2Passphrase(Secrets.password)
                        .build();
        final NetworkRequest request =
                new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        };

        connectivityManager.requestNetwork(request, networkCallback);
    }
}
