package io.github.jilipop.adohri.utils;

import android.os.Handler;
import android.os.Looper;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Module
@InstallIn(SingletonComponent.class)
public class ThreadingModule {

    @Provides
    public Handler provideHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Provides
    public ScheduledExecutorService provideExecutorService() {
        return Executors.newSingleThreadScheduledExecutor();
    }
}
