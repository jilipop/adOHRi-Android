package io.github.jilipop.adohri.audio;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import javax.inject.Inject;
import java.util.Arrays;

public class HeadphoneChecker {

    private final AudioManager audioManager;

    public static final int[] HEADPHONE_TYPES = new int[] {
        AudioDeviceInfo.TYPE_WIRED_HEADSET,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_USB_HEADSET
    };

    @Inject
    public HeadphoneChecker(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public boolean areHeadphonesConnected() {
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo audioDevice: audioDevices) {
            int type = audioDevice.getType();
            if (type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    Arrays.stream(HEADPHONE_TYPES).anyMatch(t -> t == type)) {
                return true;
            }
        }
        return false;
    }
}
