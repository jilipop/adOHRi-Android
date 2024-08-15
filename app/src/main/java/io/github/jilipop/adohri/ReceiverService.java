package io.github.jilipop.adohri;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.net.wifi.WifiManager;
import android.os.*;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ServiceCompat;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.jilipop.adohri.audio.HeadphoneChecker;
import io.github.jilipop.adohri.audio.HeadphoneDisconnectionCallback;
import io.github.jilipop.adohri.audio.HeadphoneDisconnectionHandler;
import io.github.jilipop.adohri.jni.AdReceiver;
import io.github.jilipop.adohri.wifi.SenderConnectionCallback;
import io.github.jilipop.adohri.wifi.WifiHandler;

import javax.inject.Inject;

@AndroidEntryPoint
public class ReceiverService extends Service implements SenderConnectionCallback, HeadphoneDisconnectionCallback {

    @Inject
    WifiManager.WifiLock wifiLock;

    @Inject
    PowerManager.WakeLock wakeLock;

    @Inject
    NotificationManager notificationManager;

    @Inject
    Notification notification;

    @Inject
    HeadphoneChecker headphoneChecker;

    @Inject
    WifiHandler wifiHandler;

    @Inject
    HeadphoneDisconnectionHandler headphoneDisconnectionHandler;

    @Inject
    Handler handler;

    @Inject
    AdReceiver adReceiver;

    private InterruptionCallback interruptionCallback;

    private boolean isReceiving = false;

    private final IBinder receiverServiceBinder = new ServiceBinder();

    public void setInterruptionCallback(InterruptionCallback interruptionCallback) {
        this.interruptionCallback = interruptionCallback;
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public class ServiceBinder extends Binder {
        ReceiverService getService() {
            return ReceiverService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wifiHandler.setSenderConnectionCallback(this);
        wifiHandler.watchForConnection();

        headphoneDisconnectionHandler.setHeadphoneDisconnectionCallback(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return receiverServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wifiHandler.connect();
        return START_STICKY;
    }

    @Override
    public void onSenderConnected() {
        if (headphoneChecker.areHeadphonesConnected()) {
            startReceiving();
        } else {
            onHeadphonesDisconnected();
        }
    }

    @Override
    public void onSenderDisconnected() {
        if (isReceiving) {
            headphoneDisconnectionHandler.cleanup();
            showToast(R.string.wifi_connection_lost);
            interruptionCallback.onInterruption();
        }
    }

    @Override
    public void onUserDeniedConnection() {
        isReceiving = false;
        headphoneDisconnectionHandler.cleanup();
        showToast(R.string.connection_denied);
        interruptionCallback.onInterruption();
    }

    @Override
    public void onConnectionFailed() {
        isReceiving = false;
        showToast(R.string.connection_failed);
        headphoneDisconnectionHandler.cleanup();
        interruptionCallback.onInterruption();
    }

    @Override
    public void onHeadphonesDisconnected() {
        isReceiving = false;
        headphoneDisconnectionHandler.cleanup();
        showToast(R.string.headphones_disconnected);
        interruptionCallback.onInterruption();
    }

    @SuppressLint("InlinedApi")
    public void startReceiving() {
        ServiceCompat.startForeground(this, Constants.NOTIFICATION.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        wifiLock.acquire();
        wakeLock.acquire(3*60*60*1000L /*3 hours*/);

        adReceiver.create();
        adReceiver.start();
        isReceiving = true;
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (wifiLock != null && wifiLock.isHeld()) {
            wifiLock.release();
        }
        headphoneDisconnectionHandler.cleanup();
        notificationManager.cancel(Constants.NOTIFICATION.NOTIFICATION_ID);
        wifiHandler.disconnect();
        if (isReceiving) {
            adReceiver.stop();
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
        }
        isReceiving = false;
    }

    private void showToast(int message) {
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }
}
