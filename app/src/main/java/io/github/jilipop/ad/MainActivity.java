package io.github.jilipop.ad;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.github.jilipop.ad.databinding.ActivityMainBinding;
import io.github.jilipop.ad.jni.AdReceiver;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sampleText.setText("something bla");
        AdReceiver.create(this);
        AdReceiver.start();
    }
}