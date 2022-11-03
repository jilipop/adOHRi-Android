package io.github.jilipop.adohri;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import io.github.jilipop.adohri.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private WiFiHandler wiFi;
    private ToggleButton button;

    private HeadphoneChecker headphoneChecker;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setContentView(R.layout.activity_main);
        button = findViewById(R.id.receiverServiceToggleButton);
        button.setOnClickListener(this);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        headphoneChecker = new HeadphoneChecker(this);
        connection = createConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.info_button) {
            startActivity(new Intent(this, InfoActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private ServiceConnection createConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                ReceiverService.ServiceBinder binder = (ReceiverService.ServiceBinder) service;
                ReceiverService mService = binder.getService();
                if (mService != null) {
                    wiFi = mService.getWiFi(); //for emergency Wi-Fi cleanup
                    mService.setInterruptionCallback(() -> MainActivity.this.runOnUiThread(() -> stopService()));
                    button.setChecked(true);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                button.setChecked(false);
                if (wiFi != null) {
                    wiFi.disconnect();
                    wiFi = null;
                }
            }
        };
    }

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
            if (!headphoneChecker.areHeadphonesConnected()) {
                Toast.makeText(this, R.string.headphones_disconnected, Toast.LENGTH_LONG).show();
                button.setChecked(false);
            } else {
                Intent startIntent = new Intent(MainActivity.this, ReceiverService.class);
                bindService(startIntent, connection, Context.BIND_ABOVE_CLIENT);
                startIntent.setAction(Constants.ACTION.STARTRECEIVER_ACTION);
                startService(startIntent);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().hide();
                }
            }
        } else {
            stopService();
        }
    }

    private void stopService() {
        Intent stopIntent = new Intent(MainActivity.this, ReceiverService.class);
        stopIntent.setAction(Constants.ACTION.STOPRECEIVER_ACTION);
        stopService(stopIntent);
        button.setChecked(false);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
    }
}