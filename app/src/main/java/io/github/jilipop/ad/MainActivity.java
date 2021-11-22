package io.github.jilipop.ad;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.github.jilipop.ad.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;

    private ReceiverService mService;

    private ToggleButton button;

    //private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setContentView(R.layout.activity_main);
        button = findViewById(R.id.receiverServiceToggleButton);
        button.setOnClickListener(this);

        //executorService.schedule((Runnable) AdReceiver::stop, 20, TimeUnit.SECONDS);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ReceiverService.ServiceBinder binder = (ReceiverService.ServiceBinder) service;
            mService = binder.getService();
            if (mService != null) {
                button.setChecked(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            button.setChecked(false);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to ReceiverService
        Intent intent = new Intent(this, ReceiverService.class);
        bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (button.isChecked()) {
            Intent startIntent = new Intent(MainActivity.this, ReceiverService.class);
            startIntent.setAction(Constants.ACTION.STARTRECEIVER_ACTION);
            startService(startIntent);
        } else {
            Intent stopIntent = new Intent(MainActivity.this, ReceiverService.class);
            stopIntent.setAction(Constants.ACTION.STOPRECEIVER_ACTION);
            stopService(stopIntent);
            button.setChecked(false);
        }
    }
}