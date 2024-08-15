package io.github.jilipop.adohri;

import android.net.*;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import io.github.jilipop.adohri.utils.VersionProvider;
import io.github.jilipop.adohri.wifi.NetworkRequestBuilder;
import io.github.jilipop.adohri.wifi.SenderConnectionCallback;
import io.github.jilipop.adohri.wifi.WifiHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WifiHandlerTests {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ConnectivityManager connectivityManager;
    @Mock
    private WifiManager wifiManager;
    @Mock
    private WifiConfiguration wifiConfig;
    @Mock
    private SenderConnectionCallback senderConnectionCallback;
    @Mock
    private VersionProvider versionProvider;
    @Mock
    private NetworkRequestBuilder networkRequestbuilder;
    @Mock
    private ScheduledExecutorService executorService;
    @Mock
    Network network;
    @Mock
    LinkAddress linkAddress;
    @Mock
    LinkProperties linkProperties;

    private WifiHandler wifiHandler;

    @Before
    public void setUp() {
        wifiHandler = spy(new WifiHandler(connectivityManager, wifiManager, wifiConfig, versionProvider, networkRequestbuilder, executorService));
        wifiHandler.setSenderConnectionCallback(senderConnectionCallback);
    }

    @Test
    public void watchForConnection_onNetworkAvailable_setsIsConnected_andBindsProcessToNetwork_andCallsOnSenderConnected() {
        when(networkRequestbuilder.build()).thenReturn(mock(NetworkRequest.class));
        when(linkAddress.toString()).thenReturn(Secrets.subnet + ".1");
        when(linkProperties.getLinkAddresses()).thenReturn(Collections.singletonList(linkAddress));
        when(connectivityManager.getLinkProperties(network)).thenReturn(linkProperties);

        wifiHandler.watchForConnection();

        ArgumentCaptor<ConnectivityManager.NetworkCallback> networkCallbackCaptor = ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback.class);
        verify(connectivityManager).registerNetworkCallback(any(NetworkRequest.class), networkCallbackCaptor.capture());
        ConnectivityManager.NetworkCallback networkCallback = networkCallbackCaptor.getValue();

        networkCallback.onAvailable(network);

        assert(wifiHandler.isConnected());
        verify(connectivityManager).bindProcessToNetwork(network);
        verify(senderConnectionCallback).onSenderConnected();
    }

    @Test
    public void watchForConnection_onNetworkLost_unsetsIsConnected_andCallsOnSenderDisconnected() {
        when(networkRequestbuilder.build()).thenReturn(mock(NetworkRequest.class));
        when(linkAddress.toString()).thenReturn(Secrets.subnet + ".1");
        when(linkProperties.getLinkAddresses()).thenReturn(Collections.singletonList(linkAddress));
        when(connectivityManager.getLinkProperties(network)).thenReturn(linkProperties);

        wifiHandler.watchForConnection();

        ArgumentCaptor<ConnectivityManager.NetworkCallback> networkCallbackCaptor = ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback.class);
        verify(connectivityManager).registerNetworkCallback(any(NetworkRequest.class), networkCallbackCaptor.capture());
        ConnectivityManager.NetworkCallback networkCallback = networkCallbackCaptor.getValue();

        networkCallback.onAvailable(network);
        networkCallback.onLost(network);

        assertFalse(wifiHandler.isConnected());
        verify(senderConnectionCallback).onSenderDisconnected();
    }

    @Test
    public void connect_SdkVersionAboveP_requestsNetwork_andCallsOnUserDeniedConnectionOnUnavailable() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.Q);
        when(networkRequestbuilder.build()).thenReturn(mock(NetworkRequest.class));

        wifiHandler.connect();

        ArgumentCaptor<ConnectivityManager.NetworkCallback> networkCallbackCaptor = ArgumentCaptor.forClass(ConnectivityManager.NetworkCallback.class);
        verify(connectivityManager).requestNetwork(any(NetworkRequest.class), networkCallbackCaptor.capture());
        ConnectivityManager.NetworkCallback networkCallback = networkCallbackCaptor.getValue();

        networkCallback.onUnavailable();

        verify(senderConnectionCallback).onUserDeniedConnection();
    }

    @Test
    public void connect_SdkVersionBelowOrEqualP_addsAndEnablesNetwork_andEnablesWifi() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.P);

        wifiHandler.connect();

        verify(wifiManager).addNetwork(wifiConfig);
        verify(wifiManager).enableNetwork(anyInt(), eq(true));
        verify(wifiManager).setWifiEnabled(eq(true));
    }

    @Test
    public void connect_SdkVersionBelowOrEqualP_andAlreadyConnected_doesNotAddNetworkAgain() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.P);

        wifiHandler.connect();
        wifiHandler.connect();

        verify(wifiManager, times(1)).addNetwork(wifiConfig);
    }

    @Test
    public void connect_SdkVersionBelowOrEqualP_andConnectionTimesOut_callsOnConnectionFailed() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.P);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        wifiHandler.connect();

        verify(executorService).schedule(runnableCaptor.capture(), anyLong(), any(TimeUnit.class));

        runnableCaptor.getValue().run();

        verify(wifiHandler).disconnect();
        verify(senderConnectionCallback).onConnectionFailed();
    }

    @Test
    public void disconnect_SdkVersionBelowOrEqualP_removesNetworkWhenConnected() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.P);

        wifiHandler.connect();
        wifiHandler.disconnect();

        verify(wifiManager).removeNetwork(anyInt());
    }

    @Test
    public void disconnect_SdkVersionBelowOrEqualP_doesNothingWhenNotConnected() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.P);

        wifiHandler.disconnect();

        verifyNoInteractions(wifiManager);
    }

    @Test
    public void disconnect_SdkVersionAboveP_unregistersBothNetworkCallbacks() {
        when(versionProvider.getSdkInt()).thenReturn(Build.VERSION_CODES.Q);

        wifiHandler.watchForConnection();
        wifiHandler.connect();
        wifiHandler.disconnect();

        verify(connectivityManager, times(2)).unregisterNetworkCallback(any(ConnectivityManager.NetworkCallback.class));
    }
}