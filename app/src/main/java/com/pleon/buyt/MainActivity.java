package com.pleon.buyt;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.ItemListFragment.OnListFragmentInteractionListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static com.getkeepsafe.taptargetview.TapTarget.forView;

public class MainActivity extends AppCompatActivity implements OnListFragmentInteractionListener {

    // TODO: implement the app with Flutter
    // TODO: Add android.support.annotation to the app
    // TODO: for item prices user can enter a inexact value (range)
    // TODO: for every new version of the app display a what's new page on first app open
    // TODO: convert the app architecture to MVVM
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomAppBar bottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(bottomAppBar);
        Log.d(TAG, "bottom app bar created");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(button->{
            Intent intent = new Intent(this, AddItemActivity.class);
            startActivity(intent);
        });

        // FragmentManager of an activity is responsible for calling
        // the lifecycle methods of the fragments in its list.
        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment itemsFragment = fragMgr.findFragmentById(R.id.container_fragment_item);
        // fragMgr saves the list of fragments when activity is destroyed and then retrieves them
        // so first we check if the fragment we want does not exist, then we create it
        if (itemsFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_item, new ItemListFragment())
                    .commit();
        }

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

    @Override
    public void onListFragmentInteraction(ItemContent.Item item) {

    }
}
