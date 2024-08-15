package io.github.jilipop.adohri.wifi;

import android.net.*;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.annotation.NonNull;
import io.github.jilipop.adohri.Secrets;
import io.github.jilipop.adohri.utils.VersionProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Singleton
public class WifiHandler {

    private final VersionProvider versionProvider;
    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;
    private final WifiConfiguration wifiConfig;
    private final NetworkRequestBuilder networkRequestBuilder;
    private final ScheduledExecutorService executorService;

    private Integer networkId;

    private SenderConnectionCallback senderConnectionCallback;

    private Network senderReference;
    private static boolean isConnectedToSender = false;

    private ConnectivityManager.NetworkCallback wifiListenerCallback;
    private ConnectivityManager.NetworkCallback wifiConnectorCallback;

    @Inject
    public WifiHandler(ConnectivityManager connectivityManager, WifiManager wifiManager, WifiConfiguration wifiConfig, VersionProvider versionProvider, NetworkRequestBuilder networkRequestBuilder, ScheduledExecutorService executorService) {
        this.versionProvider = versionProvider;
        this.connectivityManager = connectivityManager;
        this.wifiManager = wifiManager;
        this.wifiConfig = wifiConfig;
        this.networkRequestBuilder = networkRequestBuilder;
        this.executorService = executorService;

        if (versionProvider.getSdkInt() <= Build.VERSION_CODES.P) {
            wifiConfig.SSID = "\"" + Secrets.ssid + "\"";
            wifiConfig.preSharedKey = "\"" + Secrets.password + "\"";
        }
    }

    public void setSenderConnectionCallback(SenderConnectionCallback connectionCallback) {
        this.senderConnectionCallback = connectionCallback;
    }

    public boolean isConnected() {
        return isConnectedToSender;
    }

    public void watchForConnection() { //TODO: This could probably be integrated with connect(), since currently it should never be the case that the connection outlives the service and is already there on subsequent service startups
        final NetworkRequest request = networkRequestBuilder.build();
        wifiListenerCallback = createWifiListenerCallback();
        connectivityManager.registerNetworkCallback(request, wifiListenerCallback);
    }

    private ConnectivityManager.NetworkCallback createWifiListenerCallback() {
        return new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                boolean isSender = checkIfSender(network);
                if (isSender) {
                    senderReference = network;
                    isConnectedToSender = true;
                    connectivityManager.bindProcessToNetwork(network);
                    senderConnectionCallback.onSenderConnected();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                if (network.equals(senderReference)) {
                    isConnectedToSender = false;
                    senderConnectionCallback.onSenderDisconnected();
                }
            }
        };
    }

    private boolean checkIfSender(Network network) {
        List<LinkAddress> addresses = connectivityManager
                .getLinkProperties(network)
                .getLinkAddresses();
        for (LinkAddress address : addresses) {
            if (address.toString().startsWith(Secrets.subnet)) {
                return true;
            }
        }
        return false;
    }

    public void connect() {
        if (versionProvider.getSdkInt() > Build.VERSION_CODES.P) {
            final NetworkRequest request = networkRequestBuilder.build();
            wifiConnectorCallback = createWifiConnectorCallback();
            connectivityManager.requestNetwork(request, wifiConnectorCallback);
        } else {
            if (networkId == null) {
                networkId = wifiManager.addNetwork(wifiConfig);
            }
            wifiManager.enableNetwork(networkId, true);
            wifiManager.setWifiEnabled(true);
            executorService.schedule(() -> {
                if (!isConnectedToSender) {
                    disconnect();
                    senderConnectionCallback.onConnectionFailed();
                }
            }, 5, TimeUnit.SECONDS);
        }
    }

    private ConnectivityManager.NetworkCallback createWifiConnectorCallback() {
        return new ConnectivityManager.NetworkCallback() {
            @Override
            public void onUnavailable() {
                senderConnectionCallback.onUserDeniedConnection();
            }
            //TODO: when connection is unsuccessful despite the user accepting, stop button is displayed
        };
    }

    public void disconnect() {
        if (versionProvider.getSdkInt() > Build.VERSION_CODES.P) {
            if (wifiConnectorCallback != null) {
                connectivityManager.unregisterNetworkCallback(wifiConnectorCallback);
            }
            if (wifiListenerCallback != null) {
                connectivityManager.unregisterNetworkCallback(wifiListenerCallback);
            }
        } else {
            if (networkId != null) {
                wifiManager.removeNetwork(networkId);
                networkId = null;
            }
        }
    }
}
