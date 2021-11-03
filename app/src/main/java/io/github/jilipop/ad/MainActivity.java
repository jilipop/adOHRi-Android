package io.github.jilipop.ad;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.github.jilipop.ad.databinding.ActivityMainBinding;
import io.github.jilipop.ad.jni.AdReceiver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.sampleText.setText("something bla");
        AdReceiver.create(this);
        AdReceiver.start();
        executorService.schedule((Runnable) AdReceiver::stop, 10, TimeUnit.SECONDS);

    }
}