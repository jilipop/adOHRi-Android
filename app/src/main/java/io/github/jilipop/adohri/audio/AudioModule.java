package io.github.jilipop.adohri.audio;

import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AudioModule {

    @Provides
    public IntentFilter provideIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        return filter;
    }
}