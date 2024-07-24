package io.github.jilipop.adohri;

import android.view.Menu;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

public class LicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_licenses);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        WebView licensesView = findViewById(R.id.licenses_view);
        licensesView.getSettings().setJavaScriptEnabled(false);
        licensesView.loadUrl("file:///android_asset/licenses.html");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.licenses_toolbar, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}