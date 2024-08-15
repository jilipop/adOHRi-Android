package io.github.jilipop.adohri;

import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import io.github.jilipop.adohri.audio.HeadphoneChecker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class HeadphoneCheckerTests {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private AudioManager audioManager;
    @Mock
    private AudioDeviceInfo device;

    private HeadphoneChecker headphoneChecker;

    @Before
    public void setUp() {
        headphoneChecker = new HeadphoneChecker(audioManager);
    }

    @Test
    public void areHeadphonesConnected_returnsTrue_whenBluetoothLowEnergyHeadsetConnected() {
        when(device.getType()).thenReturn(AudioDeviceInfo.TYPE_BLE_HEADSET);
        when(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(new AudioDeviceInfo[]{device});

        assertTrue(headphoneChecker.areHeadphonesConnected());
    }

    @Test
    public void areHeadphonesConnected_returnsTrue_whenAnyOtherHeadphoneTypeConnected() {
        for (int type : HeadphoneChecker.HEADPHONE_TYPES) {
            when(device.getType()).thenReturn(type);
            when(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(new AudioDeviceInfo[]{device});

            assertTrue(headphoneChecker.areHeadphonesConnected());
        }
    }

    @Test
    public void areHeadphonesConnected_returnsFalse_whenNoHeadphonesConnected() {
        when(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(new AudioDeviceInfo[]{});

        assertFalse(headphoneChecker.areHeadphonesConnected());
    }

    @Test
    public void areHeadphonesConnected_returnsFalse_whenNonHeadphoneDevicesConnected() {
        when(device.getType()).thenReturn(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER);
        when(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)).thenReturn(new AudioDeviceInfo[]{device});

        assertFalse(headphoneChecker.areHeadphonesConnected());
    }
}