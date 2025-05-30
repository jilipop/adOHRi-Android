package io.github.jilipop.adohri.jni;

import android.content.Context;
import android.media.AudioManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AdReceiverModule {

    @Provides
    public static AudioManager provideAudioManager(@ApplicationContext Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
    @Provides
    public AdReceiver provideAdReceiver(AudioManager audioManager) {
        return new AdReceiver(audioManager);
    }
}
