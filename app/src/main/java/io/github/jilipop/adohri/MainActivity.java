package io.github.jilipop.adohri;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import io.github.jilipop.adohri.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private WiFiHandler wiFiHandler = new WiFiHandler(this);
    private ToggleButton button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setContentView(R.layout.activity_main);
        button = findViewById(R.id.receiverServiceToggleButton);
        button.setOnClickListener(this);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            ReceiverService.ServiceBinder binder = (ReceiverService.ServiceBinder) service;
            ReceiverService mService = binder.getService();
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
            //wiFiHandler.connect();
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