package io.github.jilipop.adohri;

import android.content.Context;
import android.net.*;
import android.net.wifi.*;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;

import java.util.List;

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
    private static boolean isConnectedToSender;

    private static final String LOG_TAG = "WiFi Handler";

    public WiFiHandler(Context appContext) {
        connectivityManager = (ConnectivityManager) appContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager) appContext.getApplicationContext().getSystemService(WIFI_SERVICE);

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
        Log.d(LOG_TAG, "reporting that isConnectedToSender is " + isConnectedToSender);
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
                Log.v(LOG_TAG, "new network available");
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
                Log.d(LOG_TAG, "connected to " + (isSender ? "sender" : "other wifi"));
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                Log.v(LOG_TAG, "network connection lost");
                if (network.equals(senderReference)) {
                    isConnectedToSender = false;
                    Log.d(LOG_TAG, "disconnected from sender");
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
            Log.v(LOG_TAG, address.toString());
            if (address.toString().startsWith(Secrets.subnet)) {
                return true;
            }
        }
        return false;
    }

    public void connect() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (networkId == null) {
                networkId = wifiManager.addNetwork(wifiConfig);
                Log.d(LOG_TAG, "added network with id " + networkId);
            }
            boolean enableNetworkResult = wifiManager.enableNetwork(networkId, true);
            Log.d(LOG_TAG, "enableNetwork returned " + enableNetworkResult);
            wifiManager.setWifiEnabled(true);
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
                    Log.d(LOG_TAG, "The user denied the connection request");
                    senderConnectionCallback.onUserDeniedConnection();
                }
            };
            connectivityManager.requestNetwork(request, connectionResultCallback);
            wiFiConnectorCallback = connectionResultCallback;
        }
    }

    public void disconnect() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (networkId != null) {
                wifiManager.removeNetwork(networkId);
                Log.d(LOG_TAG, "removed network with id " + networkId);
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
