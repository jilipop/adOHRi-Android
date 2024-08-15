package io.github.jilipop.adohri.jni;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AdReceiverModule {
    @Provides
    public AdReceiver provideAdReceiver() {
        return new AdReceiver();
    }
}
