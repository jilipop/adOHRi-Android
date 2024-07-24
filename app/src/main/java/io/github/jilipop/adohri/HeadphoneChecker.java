package io.github.jilipop.adohri;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

public class HeadphoneChecker {

    private final AudioManager audioManager;

    public HeadphoneChecker(Context context) {
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean areHeadphonesConnected() {
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo audioDevice: audioDevices) {
            int type = audioDevice.getType();
            if (type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    type == AudioDeviceInfo.TYPE_USB_HEADSET) {
                return true;
            }
        }
        return false;
    }
}
