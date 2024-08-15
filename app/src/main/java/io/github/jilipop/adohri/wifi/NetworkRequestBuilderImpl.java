package io.github.jilipop.adohri.wifi;

import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import io.github.jilipop.adohri.Secrets;

public class NetworkRequestBuilderImpl implements NetworkRequestBuilder {
    @Override
    public NetworkRequest build() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_TRUSTED)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setNetworkSpecifier(new WifiNetworkSpecifier.Builder()
                    .setSsid(Secrets.ssid)
                    .setWpa2Passphrase(Secrets.password)
                    .build());
        }
        return builder.build();
    }
}