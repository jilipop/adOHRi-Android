package io.github.jilipop.adohri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class HeadphoneDisconnectionHandler {

    private final Context context;
    private final HeadphoneBroadcastReceiver headphoneBroadcastReceiver;

    private HeadphoneDisconnectionCallback headphoneDisconnectionCallback;

    public HeadphoneDisconnectionHandler(Context context) {
        headphoneBroadcastReceiver = new HeadphoneBroadcastReceiver();
        this.context = context;

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(headphoneBroadcastReceiver, filter);
    }

    public void setHeadphoneDisconnectionCallback(HeadphoneDisconnectionCallback headphoneDisconnectionCallback) {
        this.headphoneDisconnectionCallback = headphoneDisconnectionCallback;
    }

    public void cleanup() {
        try {
            context.unregisterReceiver(headphoneBroadcastReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private class HeadphoneBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                headphoneDisconnectionCallback.onHeadphonesDisconnected();
            }
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        headphoneDisconnectionCallback.onHeadphonesDisconnected();
                        break;
                    case 1:
                        break;
                }
            }
        }
    }

}
