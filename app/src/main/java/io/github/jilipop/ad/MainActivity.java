package io.github.jilipop.ad;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.github.jilipop.ad.databinding.ActivityMainBinding;
import io.github.jilipop.ad.jni.AdReceiver;
import io.github.jilipop.ad.jni.AdReceiverJNI;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("andrx");
        //System.loadLibrary("ortp");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sampleText.setText("something bla");
        AdReceiver.run();
    }
}