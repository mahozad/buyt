package com.pleon.buyt.ui;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pleon.buyt.R;
import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.viewmodel.MainViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.getkeepsafe.taptargetview.TapTarget.forView;

public class MainActivity extends AppCompatActivity implements ItemListFragment.Callable, AddItemFragment.OnFragmentInteractionListener {

    // TODO: add the functionality to export and import all app data
    // TODO: Try to first provide an MVP (minimally viable product) version of the app
    // TODO: implement the app with Flutter
    // TODO: Show a prompt (or an emoji or whatever) when there is no items in the home screen
    //    to do this, add a new View to the layout and play with its setVisibility as appropriate
    // TODO: Add android.support.annotation to the app
    // TODO: for item_list_row prices user can enter a inexact value (range)
    // TODO: for every new version of the app display a what's new page on first app open
    // TODO: convert the app architecture to MVVM
    // TODO: use loaders to get data from database?
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    private static final String TAG = "MainActivity";

    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION = 1;

    private FloatingActionButton fab;
    private BottomAppBar mBottomAppBar;
    private Location location;
    private AsyncTask<Void, Void, Void> locationTask;
    private MainViewModel mMainViewModel;

//    UI controllers such as activities and fragments are primarily intended to display UI data,
//    react to user actions, or handle operating system communication, such as permission requests.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);
        Log.d(TAG, "bottom app bar created");

        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // FragmentManager of an activity is responsible for calling
        // the lifecycle methods of the fragments in its list.
        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment itemsFragment = fragMgr.findFragmentById(R.id.container_fragment_items);
        // fragMgr saves the list of fragments when activity is destroyed and then retrieves them
        // so first we check if the fragment we want does not exist, then we create it
        if (itemsFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_items, ItemListFragment.newInstance())
                    .commit();
        }


        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {

            // Check if the location permission is already available.
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                // Location permissions is already available, go on
                // ...
            } else {
                // Location permission has not been granted.
                requestCameraPermission();
            }


            LocationManager locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
                // GPS is off on the device
            }
            locationMgr.requestLocationUpdates(GPS_PROVIDER, 1000, 1, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.e(TAG, location.toString());
                    MainActivity.this.location = location;
                    locationMgr.removeUpdates(this); // stop the app from using GPS
//                    if (location.distanceTo(/* one of existent stores */) < 5) {
//                        // the store is saved already
//                    }
                    ItemListFragment itemsFragment = (ItemListFragment) fragMgr.findFragmentById(R.id.container_fragment_items);
                    itemsFragment.enableCheckboxes();
                }

                @Override // @formatter:off
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    // TODO: handle gps disabled
                }
            });
        });

        //
        //
        // show tap target for FAB
        new TapTargetSequence(this).targets(
                forView(findViewById(R.id.fab), "Tap here when you're ready")
                        .outerCircleColor(R.color.colorAccent)
                        .targetCircleColor(android.R.color.background_light)
                        .transparentTarget(true)
                        .textColor(R.color.colorPrimaryDark))
                .start();
    }





    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a the user will be prompted
     * to grant the permission, otherwise it is requested directly.
     */
    private void requestCameraPermission() {
        // When the user responds to your app's permission request, the system invokes your app's onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            // TODO: here show a dialog or ... and provide the rationale to the user and then
            // when he clicks OK execute the following statement to request the permission:
            // you don't want to overwhelm the user with too much explanation
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                }
            }
        }
    }








    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the application form and temp data to survive config changes and force-kills
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add) {
            Fragment newFragment = AddItemFragment.newInstance();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container_fragment_items, newFragment).addToBackStack(null).commit();

            mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
            fab.setImageResource(R.drawable.ic_done);
            mBottomAppBar.setNavigationIcon(null); // causes the fab animation to not run
            mBottomAppBar.replaceMenu(R.menu.menu_add_item);

            View chartView = findViewById(R.id.container_fragment_chart);
            chartView.setVisibility(View.GONE); // TODO: maybe replacing the fragment is a better practice


        } else if (item.getItemId() == android.R.id.home) { /* If you use setSupportActionBar() to set up the BottomAppBar
             you can handle the navigation menu click by checking if the menu item id is android.R.id.home. */
            BottomSheetDialogFragment bottomDrawerFragment = BottomDrawerFragment.newInstance();
            bottomDrawerFragment.show(getSupportFragmentManager(), "alaki");
        }
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // save any permanent data that you have not saved yet
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        locationTask.cancel(true);
        AppDatabase.destroyInstance();
    }

    @Override
    public void onListFragmentInteraction(Item item) {
        Log.i(TAG, "Item with id ***" + item.getId() + "*** was clicked");
        mMainViewModel.buy(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
