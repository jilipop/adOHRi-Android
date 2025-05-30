package io.github.jilipop.adohri.jni;

import android.media.AudioManager;

import javax.inject.Inject;

public class AdReceiver {

    private long mEngineHandle = 0;
    private final AudioManager audioManager;

    @Inject
    public AdReceiver(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public boolean create() {
        if (mEngineHandle == 0) {
            setDefaultStreamValues();
            mEngineHandle = native_createEngine();
        }
        return (mEngineHandle != 0);
    }



    private void setDefaultStreamValues() {
        String sampleRateStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int defaultSampleRate = Integer.parseInt(sampleRateStr);
        String framesPerBurstStr = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);

        native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
    }

    public int start() {
        if (mEngineHandle != 0) {
            return native_startEngine(mEngineHandle);
        } else {
            return -1;
        }
    }

    public int stop() {
        if (mEngineHandle != 0) {
            return native_stopEngine(mEngineHandle);
        } else {
            return -1;
        }
    }

    public void delete() {
        if (mEngineHandle != 0) {
            native_deleteEngine(mEngineHandle);
        }
        mEngineHandle = 0;
    }

    public void setAudioApi(int audioApi) {
        if (mEngineHandle != 0) native_setAudioApi(mEngineHandle, audioApi);
    }

    public void setAudioDeviceId(int deviceId) {
        if (mEngineHandle != 0) native_setAudioDeviceId(mEngineHandle, deviceId);
    }

    public void setChannelCount(int channelCount) {
        if (mEngineHandle != 0) native_setChannelCount(mEngineHandle, channelCount);
    }

    public void setBufferSizeInBursts(int bufferSizeInBursts) {
        if (mEngineHandle != 0) native_setBufferSizeInBursts(mEngineHandle, bufferSizeInBursts);
    }

    // Native methods
    private native long native_createEngine();
    private native int native_startEngine(long engineHandle);
    private native int native_stopEngine(long engineHandle);
    private native void native_deleteEngine(long engineHandle);
    private native void native_setAudioApi(long engineHandle, int audioApi);
    private native void native_setAudioDeviceId(long engineHandle, int deviceId);
    private native void native_setChannelCount(long mEngineHandle, int channelCount);
    private native void native_setBufferSizeInBursts(long engineHandle, int bufferSizeInBursts);
    private native void native_setDefaultStreamValues(int sampleRate, int framesPerBurst);
}
