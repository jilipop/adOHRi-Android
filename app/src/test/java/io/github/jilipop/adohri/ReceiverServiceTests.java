package io.github.jilipop.adohri;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;
import androidx.core.app.ServiceCompat;
import io.github.jilipop.adohri.audio.HeadphoneChecker;
import io.github.jilipop.adohri.audio.HeadphoneDisconnectionHandler;
import io.github.jilipop.adohri.jni.AdReceiver;
import io.github.jilipop.adohri.wifi.WifiHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReceiverServiceTests {

    @Mock
    private WifiManager.WifiLock wifiLock;
    @Mock
    private PowerManager.WakeLock wakeLock;
    @Mock
    private NotificationManager notificationManager;
    @Mock
    private Notification notification;
    @Mock
    private HeadphoneChecker headphoneChecker;
    @Mock
    private WifiHandler wifiHandler;
    @Mock
    private HeadphoneDisconnectionHandler headphoneDisconnectionHandler;
    @Mock
    private InterruptionCallback interruptionCallback;
    @Mock
    private Handler handler;
    @Mock
    private Intent intent;
    @Mock
    private Toast toast;
    @Mock
    private AdReceiver adReceiver;

    private ReceiverService receiverService;
    private MockedStatic<ServiceCompat> serviceCompatStaticMock;
    private MockedStatic<Toast> toastStaticMock;

    @Before
    public void setUp() {
        receiverService = new ReceiverService();
        receiverService.wifiLock = wifiLock;
        receiverService.wakeLock = wakeLock;
        receiverService.notificationManager = notificationManager;
        receiverService.notification = notification;
        receiverService.headphoneChecker = headphoneChecker;
        receiverService.wifiHandler = wifiHandler;
        receiverService.headphoneDisconnectionHandler = headphoneDisconnectionHandler;
        receiverService.adReceiver = adReceiver;
        receiverService.handler = handler;
        receiverService.setInterruptionCallback(interruptionCallback);

        serviceCompatStaticMock = mockStatic(ServiceCompat.class);
        toastStaticMock = mockStatic(Toast.class);

        toastStaticMock.when(() -> Toast.makeText(any(Context.class), anyInt(), anyInt())).thenAnswer(invocation -> toast);
        doNothing().when(toast).show();
        when(adReceiver.create()).thenReturn(true);
        when(adReceiver.start()).thenReturn(0);
        when(adReceiver.stop()).thenReturn(0);
    }

    @After
    public void tearDown() {
        toastStaticMock.close();
        serviceCompatStaticMock.close();
    }

    @Test
    public void onStartCommand_startsWifiHandler() {
        int result = receiverService.onStartCommand(intent, 0, 0);

        verify(wifiHandler).connect();
        assertEquals(Service.START_STICKY, result);
    }

    @Test
    public void onSenderConnected_startsReceivingIfHeadphonesConnected() {
        when(headphoneChecker.areHeadphonesConnected()).thenReturn(true);
        serviceCompatStaticMock.when(() -> ServiceCompat.startForeground(any(Service.class), anyInt(), eq(notification), anyInt())).thenAnswer(invocation -> null);

        receiverService.onSenderConnected();

        serviceCompatStaticMock.verify(() -> ServiceCompat.startForeground(any(ReceiverService.class), eq(Constants.NOTIFICATION.NOTIFICATION_ID), eq(notification), eq(ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)));
        verify(adReceiver).create();
        verify(adReceiver).start();
        assertTrue(receiverService.isReceiving());
    }

    @Test
    public void onSenderConnected_acquiresLocks() {
        when(headphoneChecker.areHeadphonesConnected()).thenReturn(true);

        receiverService.onSenderConnected();

        verify(wifiLock).acquire();
        verify(wakeLock).acquire(anyLong());
    }

    @Test
    public void onSenderConnected_handlesHeadphonesDisconnected() {
        when(headphoneChecker.areHeadphonesConnected()).thenReturn(false);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(handler.post(runnableCaptor.capture())).thenReturn(true);

        receiverService.onSenderConnected();

        verifyNoInteractions(adReceiver);
        assertFalse(receiverService.isReceiving());
        verify(headphoneDisconnectionHandler).cleanup();
        verify(interruptionCallback).onInterruption();

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        toastStaticMock.verify(() -> Toast.makeText(any(Context.class), eq(R.string.headphones_disconnected), eq(Toast.LENGTH_LONG)));
        verify(toast).show();
    }

    @Test
    public void onSenderDisconnected_cleansUpAndNotifies() {
        when(headphoneChecker.areHeadphonesConnected()).thenReturn(true);

        receiverService.onSenderConnected();
        receiverService.onSenderDisconnected();

        verify(headphoneDisconnectionHandler).cleanup();
        verify(interruptionCallback).onInterruption();
    }

    @Test
    public void onUserDeniedConnection_cleansUpAndNotifies() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(handler.post(runnableCaptor.capture())).thenReturn(true);

        receiverService.onUserDeniedConnection();

        assertFalse(receiverService.isReceiving());
        verify(headphoneDisconnectionHandler).cleanup();
        verify(interruptionCallback).onInterruption();

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        toastStaticMock.verify(() -> Toast.makeText(any(Context.class), eq(R.string.connection_denied), eq(Toast.LENGTH_LONG)));
        verify(toast).show();
    }

    @Test
    public void onConnectionFailed_cleansUpAndNotifies() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(handler.post(runnableCaptor.capture())).thenReturn(true);

        receiverService.onConnectionFailed();

        assertFalse(receiverService.isReceiving());
        verify(headphoneDisconnectionHandler).cleanup();
        verify(interruptionCallback).onInterruption();

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        toastStaticMock.verify(() -> Toast.makeText(any(Context.class), eq(R.string.connection_failed), eq(Toast.LENGTH_LONG)));
        verify(toast).show();
    }

    @Test
    public void onHeadphonesDisconnected_cleansUpAndNotifies() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(handler.post(runnableCaptor.capture())).thenReturn(true);

        receiverService.onHeadphonesDisconnected();

        assertFalse(receiverService.isReceiving());
        verify(headphoneDisconnectionHandler).cleanup();
        verify(interruptionCallback).onInterruption();

        Runnable capturedRunnable = runnableCaptor.getValue();
        capturedRunnable.run();

        toastStaticMock.verify(() -> Toast.makeText(any(Context.class), eq(R.string.headphones_disconnected), eq(Toast.LENGTH_LONG)));
        verify(toast).show();
    }

    @Test
    public void onDestroy_releasesLocks() {
        when(wakeLock.isHeld()).thenReturn(true);
        when(wifiLock.isHeld()).thenReturn(true);

        receiverService.onDestroy();

        verify(wakeLock).release();
        verify(wifiLock).release();
    }

    @Test
    public void onDestroy_cleansUp() {
        when(headphoneChecker.areHeadphonesConnected()).thenReturn(true);
        serviceCompatStaticMock.when(() -> ServiceCompat.stopForeground(any(Service.class), anyInt())).thenAnswer(invocation -> null);

        receiverService.onSenderConnected();
        receiverService.onDestroy();

        verify(adReceiver).stop();
        serviceCompatStaticMock.verify(() -> ServiceCompat.stopForeground(any(ReceiverService.class), eq(ServiceCompat.STOP_FOREGROUND_REMOVE)));

        verify(headphoneDisconnectionHandler).cleanup();
        verify(notificationManager).cancel(Constants.NOTIFICATION.NOTIFICATION_ID);
        verify(wifiHandler).disconnect();
    }
}
