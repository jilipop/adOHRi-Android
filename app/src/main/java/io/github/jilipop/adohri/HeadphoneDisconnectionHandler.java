package io.github.jilipop.adohri;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

public class HeadphoneDisconnectionHandler {

    private final Context context;

    public HeadphoneDisconnectionHandler(Context context) {
        HeadphoneBroadcastReceiver headphoneBroadcastReceiver = new HeadphoneBroadcastReceiver();
        this.context = context;

        IntentFilter filter = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        context.registerReceiver(headphoneBroadcastReceiver, filter);
    }

    private class HeadphoneBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                Log.d("action was", "ACTION_AUDIO_BECOMING NOISY");
            }
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Log.d("this happened", "Headset unplugged");
                        break;
                    case 1:
                        Log.d("this happened", "Headset plugged");
                        break;
                }
            }
        }
    }

}