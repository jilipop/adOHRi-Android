package io.github.jilipop.adohri;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
// import android.os.Build;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;
// import androidx.activity.result.ActivityResultLauncher;
// import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
// import androidx.core.content.ContextCompat;
import dagger.hilt.android.AndroidEntryPoint;
import io.github.jilipop.adohri.audio.HeadphoneChecker;
import io.github.jilipop.adohri.databinding.ActivityMainBinding;
import io.github.jilipop.adohri.utils.VersionProvider;
import io.github.jilipop.adohri.wifi.WifiHandler;

import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ToggleButton button;

    @Inject
    HeadphoneChecker headphoneChecker;

    @Inject
    WifiHandler wifiHandler;

    @Inject
    VersionProvider versionProvider;

    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setContentView(R.layout.activity_main);
        button = binding.receiverServiceToggleButton;
        button.setOnClickListener(this);

        Toolbar toolbar = binding.toolbar.getRoot();
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(0, topInset, 0, bottomInset);
            return insets;
        });

        connection = createConnection();

//        if (versionProvider.getSdkInt() >= Build.VERSION_CODES.TIRAMISU) {
//            ActivityResultLauncher<String> notificationPermissionLauncher =
//                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                        if (isGranted) {
//                            // Permission is granted. Continue the action or workflow in your
//                            // app.
//                        } else {
//                            // Explain the                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         ain to the user that the feature is unavailable because the
//                            // feature requires a permission that the user has denied. At the
//                            // same time, respect the user's decision. Don't link to system
//                            // settings in an effort to convince the user to change their
//                            // decision.
//                        }
//                    });
//            if (!(ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED)) {
//                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
//            }
//        }
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
                    mService.setInterruptionCallback(() -> MainActivity.this.runOnUiThread(() -> stopService()));
                    button.setChecked(true);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                button.setChecked(false);
                if (wifiHandler != null) {
                    wifiHandler.disconnect();
                    wifiHandler = null;
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