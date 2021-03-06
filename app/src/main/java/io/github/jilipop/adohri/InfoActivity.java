package io.github.jilipop.adohri;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar toolbar = findViewById(R.id.info_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_info);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView appNameAndVersionNumberText = findViewById(R.id.app_name_and_version_number);
        PackageManager packageManager = getApplicationContext().getPackageManager();
        String packageName = getApplicationContext().getPackageName();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException exception) {
            exception.printStackTrace();
        }
        appNameAndVersionNumberText.setText(appNameAndVersionNumberText.getText() + " " + packageInfo.versionName);

        TextView licensesButton = findViewById(R.id.licensesButton);
        licensesButton.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.info_toolbar, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    public void openLicensesActivity(View view) {
        startActivity(new Intent(this, LicensesActivity.class));
    }

    public void browseSourceCode(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.sourceCodeURL)));
    }
}