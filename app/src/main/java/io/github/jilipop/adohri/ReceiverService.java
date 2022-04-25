package io.github.jilipop.adohri;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import io.github.jilipop.adohri.jni.AdReceiver;

public class ReceiverService extends Service implements SenderConnectionCallback {
    private NotificationManager notificationManager;

    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    private WiFiHandler wiFi;

    private HeadphoneDisconnectionHandler headphoneDisconnectionHandler;

    private InterruptionCallback interruptionCallback;

    private boolean isReceiving = false;

    private static final String LOG_TAG = "Receiver Service";

    private final IBinder receiverServiceBinder = new ServiceBinder();

    private final int pendingIntentFlags = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;

    public WiFiHandler getWiFi() {
        return wiFi;
    }

    public void setInterruptionCallback(InterruptionCallback interruptionCallback) {
        this.interruptionCallback = interruptionCallback;
    }

    public class ServiceBinder extends Binder {
        ReceiverService getService() {
            return ReceiverService.this;
        }
    }

    @Override
    public void onCreate() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "AD:WifiLock");
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AD:WakeLock");
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        wiFi = new WiFiHandler(this.getApplicationContext());
        wiFi.setSenderConnectionCallback(this);
        wiFi.watchForConnection();

        headphoneDisconnectionHandler = new HeadphoneDisconnectionHandler(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return receiverServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "Received Receiver Start Intent");
        wiFi.connect();
        return START_STICKY;
    }

    @Override
    public void onSenderConnected() {
        startReceiving();
    }

    @Override
    public void onSenderDisconnected() {
        if (isReceiving) {
            Toast.makeText(this, R.string.wifi_connection_lost, Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "calling the interruption callback because the wifi connection was lost");
            interruptionCallback.onServiceInterrupted();
        }
    }

    @Override
    public void onUserDeniedConnection() {
        Toast.makeText(this, R.string.connection_denied, Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "calling the interruption callback because the user denied the connection request");
        interruptionCallback.onServiceInterrupted();
    }

    private void setupForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, pendingIntentFlags);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.NOTIFICATION.CHANNEL_ID,
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION.CHANNEL_ID)
                .setContentTitle(getString(R.string.notification_content_title))
                .setTicker(getString(R.string.notification_ticker_text))
                .setContentText(getString(R.string.notification_content_text))
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION.NOTIFICATION_ID, notification);
    }

    public void startReceiving() {
        setupForegroundNotification();
        wifiLock.acquire();
        wakeLock.acquire(3*60*60*1000L /*3 hours*/);

        Log.d(LOG_TAG, "AdReceiver creation " + (AdReceiver.create(this) ? "successful." : "failed."));
        AdReceiver.start();
        isReceiving = true;
    }

    @Override
    public void onDestroy() {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        if (wifiLock != null) {
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
        }
        notificationManager.cancel(Constants.NOTIFICATION.NOTIFICATION_ID);
        wiFi.disconnect();
        if (isReceiving) {
            AdReceiver.stop();
        }
        isReceiving = false;
        Log.d(LOG_TAG, "destroying receiver service");
    }
}
