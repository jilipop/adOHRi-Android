package io.github.jilipop.adohri.jni;

import android.content.Context;
import android.media.AudioManager;

public class AdReceiver {
    static long mEngineHandle = 0;

    static {
        System.loadLibrary("andrx");
    }

    public static boolean create(Context context){
        if (mEngineHandle == 0){
            setDefaultStreamValues(context);
            mEngineHandle = native_createEngine();
        }
        return (mEngineHandle != 0);
    }

    private static void setDefaultStreamValues(Context context) {
        AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        int defaultSampleRate = Integer.parseInt(sampleRateStr);
        String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);

        native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
    }

    public static int start() {
        if (mEngineHandle != 0) {
            return native_startEngine(mEngineHandle);
        } else {
            return -1;
        }
    }

    public static int stop() {
        if (mEngineHandle != 0) {
            return native_stopEngine(mEngineHandle);
        } else {
            return -1;
        }
    }

    public static void delete(){
        if (mEngineHandle != 0){
            native_deleteEngine(mEngineHandle);
        }
        mEngineHandle = 0;
    }

    public static void setAudioApi(int audioApi){
        if (mEngineHandle != 0) native_setAudioApi(mEngineHandle, audioApi);
    }

    public static void setAudioDeviceId(int deviceId){
        if (mEngineHandle != 0) native_setAudioDeviceId(mEngineHandle, deviceId);
    }

    public static void setChannelCount(int channelCount) {
        if (mEngineHandle != 0) native_setChannelCount(mEngineHandle, channelCount);
    }

    public static void setBufferSizeInBursts(int bufferSizeInBursts){
        if (mEngineHandle != 0) native_setBufferSizeInBursts(mEngineHandle, bufferSizeInBursts);
    }

    // Native methods
    private static native long native_createEngine();
    private static native int native_startEngine(long engineHandle);
    private static native int native_stopEngine(long engineHandle);
    private static native void native_deleteEngine(long engineHandle);
    private static native void native_setAudioApi(long engineHandle, int audioApi);
    private static native void native_setAudioDeviceId(long engineHandle, int deviceId);
    private static native void native_setChannelCount(long mEngineHandle, int channelCount);
    private static native void native_setBufferSizeInBursts(long engineHandle, int bufferSizeInBursts);
    private static native void native_setDefaultStreamValues(int sampleRate, int framesPerBurst);
}
