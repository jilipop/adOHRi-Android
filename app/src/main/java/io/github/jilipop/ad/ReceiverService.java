package io.github.jilipop.ad;

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
import io.github.jilipop.ad.jni.AdReceiver;

public class ReceiverService extends Service {
    private NotificationManager notificationManager;

    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;

    private static final String LOG_TAG = "Receiver Service";

    private final IBinder receiverServiceBinder = new ServiceBinder();

    private final int pendingIntentFlags = android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;

    public class ServiceBinder extends Binder {
        ReceiverService getService() {
            return ReceiverService.this;
        }
    }

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting. We put an icon in the status bar.
        showNotification();
        Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
        AdReceiver.create(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return receiverServiceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG_TAG, "Received Receiver Start Intent");
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
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(Constants.NOTIFICATION.NOTIFICATION_ID, notification);

        startReceiving();
        return START_STICKY;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.local_service_started);
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), pendingIntentFlags);
        // Set the info for the views that show in the notification panel.
        Notification notification = new NotificationCompat.Builder(this, Constants.NOTIFICATION.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();
        // Send the notification.
        notificationManager.notify(Constants.NOTIFICATION.NOTIFICATION_ID, notification);
    }

    public void startReceiving() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "AD:WifiLock");
        wifiLock.acquire();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AD:WakeLock");
        wakeLock.acquire(3*60*60*1000L /*3 hours*/);

        AdReceiver.start();
    }

    @Override
    public void onDestroy() {
        AdReceiver.stop();
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
        // Cancel the persistent notification.
        notificationManager.cancel(Constants.NOTIFICATION.NOTIFICATION_ID);
        // Tell the user we stopped.
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
    }
}
