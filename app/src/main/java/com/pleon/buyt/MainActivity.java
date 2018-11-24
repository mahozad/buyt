package com.pleon.buyt;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomappbar.BottomAppBar;

import androidx.appcompat.app.AppCompatActivity;

import static com.getkeepsafe.taptargetview.TapTarget.forView;

public class MainActivity extends AppCompatActivity {

    // TODO: implement the app with Flutter
    // TODO: Add android.support.annotation to the app
    // TODO: for item prices user can enter a inexact value (range)
    // TODO: for every new version of the app display a what's new page on first app open
    // TODO: convert the app architecture to MVVM
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(bottomAppBar);
        Log.d(TAG, "onCreate: bottom app bar created");

        // show tap target for FAB
        new TapTargetSequence(this).targets(
                forView(findViewById(R.id.fab), "Gonna")
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(android.R.color.background_light)
                        .transparentTarget(true)
                        .textColor(android.R.color.holo_green_light))
                .start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the application form and temp data to survive config changes and force-kills
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save any permanent data that you have not saved yet
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_home, menu);
        return true;
    }
}
