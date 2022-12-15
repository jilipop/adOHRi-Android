package io.github.jilipop.adohri;

import android.content.Context;
import android.net.*;
import android.net.wifi.*;
import android.os.Build;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static android.content.Context.WIFI_SERVICE;

public class WiFiHandler {

    private final ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;

    private SenderConnectionCallback senderConnectionCallback;

    private ConnectivityManager.NetworkCallback wiFiListenerCallback;
    private ConnectivityManager.NetworkCallback wiFiConnectorCallback;

    private WifiConfiguration wifiConfig;

    private Integer networkId;

    private Network senderReference;
    private static boolean isConnectedToSender = false;

    public WiFiHandler(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            wifiConfig = new WifiConfiguration();
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
        final NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        final ConnectivityManager.NetworkCallback connectionStatusCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                boolean isSender = checkIfSender(network);
                if (isSender) {
                    senderReference = network;
                    isConnectedToSender = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        connectivityManager.bindProcessToNetwork(network);
                    } else {
                        try {
                            ConnectivityManager.setProcessDefaultNetwork(network);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                    senderConnectionCallback.onSenderConnected();
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                if (network.equals(senderReference)) {
                    isConnectedToSender = false;
                    senderConnectionCallback.onSenderDisconnected();
                }
            }
        };
        connectivityManager.registerNetworkCallback(request, connectionStatusCallback);
        wiFiListenerCallback = connectionStatusCallback;
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
        isConnectedToSender = false; //TODO: This is only safe if connect() is never called while already connected! Check if this is the case.
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (networkId == null) {
                networkId = wifiManager.addNetwork(wifiConfig);
            }
            wifiManager.enableNetwork(networkId, true);
            wifiManager.setWifiEnabled(true);
            executorService.schedule(()-> {
                if (!isConnectedToSender) {
                    disconnect();
                    senderConnectionCallback.onConnectionFailed();
                }
            }, 5, TimeUnit.SECONDS);
        } else {
            final NetworkSpecifier specifier;
            specifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(Secrets.ssid)
                    .setWpa2Passphrase(Secrets.password)
                    .build();
            final NetworkRequest request = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) //TODO: Is this necessary? If not, then this request would be a duplicate of the one at the top
                    .setNetworkSpecifier(specifier)
                    .build();
            final ConnectivityManager.NetworkCallback connectionResultCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                    senderConnectionCallback.onUserDeniedConnection();
                }
                //TODO: when connection is unsuccessful despite the user accepting, stop button is displayed
            };
            connectivityManager.requestNetwork(request, connectionResultCallback);
            wiFiConnectorCallback = connectionResultCallback;
        }
    }

    public void disconnect() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (networkId != null) {
                wifiManager.removeNetwork(networkId);
                networkId = null;
            }
        } else {
            if (wiFiConnectorCallback != null) {
                connectivityManager.unregisterNetworkCallback(wiFiConnectorCallback);
            }
            if (wiFiListenerCallback != null) {
                connectivityManager.unregisterNetworkCallback(wiFiListenerCallback);
            }
        }
    }
}
