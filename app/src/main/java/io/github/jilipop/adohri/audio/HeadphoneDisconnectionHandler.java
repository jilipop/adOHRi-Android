package io.github.jilipop.adohri.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;
import androidx.core.content.ContextCompat;
import dagger.hilt.android.qualifiers.ApplicationContext;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HeadphoneDisconnectionHandler {

    private final Context context;
    private final BroadcastReceiver headphoneBroadcastReceiver;

    private HeadphoneDisconnectionCallback callback;

    @Inject
    public HeadphoneDisconnectionHandler(@ApplicationContext Context context, IntentFilter filter) {
        this.context = context;
        headphoneBroadcastReceiver = createReceiver();
        ContextCompat.registerReceiver(context, headphoneBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private @NotNull BroadcastReceiver createReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                    callback.onHeadphonesDisconnected();
                } else if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                    int state = intent.getIntExtra("state", -1);
                    if (state == 0) {
                        callback.onHeadphonesDisconnected();
                    }
                }
            }
        };
    }

    public void setHeadphoneDisconnectionCallback(HeadphoneDisconnectionCallback callback) {
        this.callback = callback;
    }

    public void cleanup() {
        try {
            context.unregisterReceiver(headphoneBroadcastReceiver);
        } catch (IllegalArgumentException exception) {
            Log.i("HeadphoneDisconnectionHandler", "Failed to unregister receiver: " + exception.getMessage());
        }
    }
}