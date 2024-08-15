package io.github.jilipop.adohri;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class App extends Application {

    static {
        System.loadLibrary("andrx");
    }
}
