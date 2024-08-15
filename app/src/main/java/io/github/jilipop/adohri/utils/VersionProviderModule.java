package io.github.jilipop.adohri.utils;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;


@Module
@InstallIn(SingletonComponent.class)
public class VersionProviderModule {

    @Provides
    public VersionProvider provideVersionProvider() {
        return new VersionProviderImpl();
    }
}