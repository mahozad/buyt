package com.pleon.buyt.ui.activity;

import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.pleon.buyt.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.pleon.buyt.ui.activity.MainActivity.DEFAULT_THEME;
import static com.pleon.buyt.ui.activity.MainActivity.KEY_PREF_THEME;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.bottom_bar) BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(bottomAppBar);
    }

    private void setTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = preferences.getString(KEY_PREF_THEME, DEFAULT_THEME);
        setTheme(DEFAULT_THEME.equals(theme) ? R.style.AppTheme : R.style.LightTheme);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reset:
                ((Animatable) item.getIcon()).start();
                break;
            case android.R.id.home:
                finish();
        }
        return true;
    }
}
