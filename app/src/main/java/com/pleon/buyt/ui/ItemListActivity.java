package com.pleon.buyt.ui;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.pleon.buyt.R;
import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.getkeepsafe.taptargetview.TapTarget.forView;
import static java.lang.Math.cos;

public class ItemListActivity extends AppCompatActivity implements
        ItemListFragment.Callable, AddItemFragment.OnFragmentInteractionListener,
        CreateStoreFragment.OnFragmentInteractionListener {

    // TODO: Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
    // TODO: Collapse the chart a little in main screen when scrolling down (with coordinatorLayout)
    // TODO: extract margins and dimensions into xml files
    // TODO: Add ability to select a date to see its costs
    // TODO: Dark material colors: https://stackoverflow.com/q/36915508
    // TODO: for the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
    // FIXME: Correct all names and ids according to best practices
    // TODO: Use butter knife to declare activity views and view handlers
    // TODO: Enable the user to disable location rationale dialog and always enters stores manually
    // TODO: What is Spherical Law of Cosines? (for locations)
    // TODO: Add the functionality to export and import all app data
    // TODO: Try to first provide an MVP (minimally viable product) version of the app
    // TODO: Implement the app with Flutter
    // TODO: Make viewing stores on map a premium feature
    // TODO: Maybe instead of a fragment, I can use full-screen dialog for adding new Item?
    // TODO: Enable the user to change the radius that app uses to find near stores
    // TODO: Add ability to remove all app data
    // TODO: Add android.support.annotation to the app
    // TODO: For item_list_row prices user can enter an inexact value (range)
    // TODO: For every new version of the app display a what's new page on first app open
    // DONE: Convert the app architecture to MVVM
    // TODO: Use loaders to get data from database?
    // TODO: Add ability (an icon) for each item to mark it as high priority
    // TODO: Ability to add details (description) for each item
    // TODO: Show a small progress bar of how much has been spent if user has set a limit on spends
    /* FIXME: What happens if two stores are near each other and only one of them is saved in the app.
       ~ now if user has bought something from the other store, it is saved for the persisted store */
    /* TODO: Show a prompt (or an emoji or whatever) when there is no items in the home screen
       ~ to do this, add a new View to the layout and play with its setVisibility as appropriate
    */
    /* TODO: Do you have multiple tables in your database and find yourself copying the same Insert,
       ~ Update and Delete methods? DAOs support inheritance, so create a BaseDao<T> class, and define
       ~ your generic @Insert,... there. Have each DAO extend the BaseDao and add methods specific to each of them.
    */

    // If want to replace a fragment as the whole activity pass android.R.id.content to fragment manager
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    private static final String TAG = "ItemListActivity";
    private static final double NEAR_STORES_DISTANCE = cos(0.1 / 6371); // == 100m (6371km is the radius of the Earth)

    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    private Location location;
    private AsyncTask<Void, Void, Void> locationTask;
    private ItemListViewModel mItemListViewModel;

//    UI controllers such as activities and fragments are primarily intended to display UI data,
//    react to user actions, or handle operating system communication, such as permission requests.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);
        Log.d(TAG, "bottom app bar created");

        mItemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);

        // FragmentManager of an activity is responsible for calling the lifecycle methods of the fragments in its list.
        FragmentManager fragMgr = getSupportFragmentManager();
        Fragment itemsFragment = fragMgr.findFragmentById(R.id.container_fragment_items);
        // fragMgr saves the list of fragments when activity is destroyed and then retrieves them
        // so first we check if the fragment we want does not exist, then we create it
        if (itemsFragment == null) {
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_items, ItemListFragment.newInstance())
                    .commit(); // TODO: commit vs commitNow?
        }


        GraphView graph = findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(0, 21),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 2),
                new DataPoint(5, 2),
                new DataPoint(6, 6)
        });
        series.setColor(R.color.colorPrimaryDark);
        graph.addSeries(series);


        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(fab -> findLocation());

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

    private void findLocation() {
        LocationManager locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        /* you must check whether you have dangerous permission every time */
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission();
        } else if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
            // TODO: GPS is off on the device; handle it
        } else {
            GpsListener gpsListener = new GpsListener();
            locationMgr.requestLocationUpdates(GPS_PROVIDER, 0, 0, gpsListener);
        }
    }

    private void onLocationFound(Location location) {
        Log.d(TAG, location.toString());
        ItemListActivity.this.location = location;
        ItemListFragment itemsFragment = (ItemListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.container_fragment_items);
        itemsFragment.enableCheckboxes();
    }

    private class GpsListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            // stop using GPS
            ((LocationManager) getSystemService(LOCATION_SERVICE)).removeUpdates(this);
            onLocationFound(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO: handle gps disabled
        }
    }


    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a the user will be prompted
     * to grant the permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        // When the user responds to the app's permission request, the system invokes app's onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            RationaleDialogFragment rationaleDialog = new RationaleDialogFragment();
            rationaleDialog.show(getSupportFragmentManager(), "LOCATION_RATIONALE_DIALOG");
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(ItemListActivity.this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    findLocation();
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                } // break; not needed here
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        switch (item.getItemId()) {
            case R.id.action_add:
                Fragment addItemFragment = AddItemFragment.newInstance();

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.container_fragment_items, addItemFragment)
                        /*.setCustomAnimations()*/
                        .addToBackStack(null).commit();

                mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
                mFab.setImageResource(R.drawable.ic_done);
                mBottomAppBar.setNavigationIcon(null); // causes the fab animation to not run
                mBottomAppBar.replaceMenu(R.menu.menu_add_item);

//                View chartView = findViewById(R.id.container_fragment_chart);
//                chartView.setVisibility(View.GONE); // TODO: maybe replacing the fragment is a better practice
                break;
            case android.R.id.home: /* If you use setSupportActionBar() to set up the BottomAppBar
             you can handle the navigation menu click by checking if the menu item id is android.R.id.home. */

                // to change its theme to dark or light, we need to set "bottomSheetDialogTheme"
                // item to "@style/Theme.MaterialComponents.BottomSheetDialog" in application theme
                BottomSheetDialogFragment bottomDrawerFragment = BottomDrawerFragment.newInstance();
                bottomDrawerFragment.show(getSupportFragmentManager(), "alaki");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        locationTask.cancel(true);
        AppDatabase.destroyInstance();
    }

    @Override
    public void onItemCheckboxClicked(Item item) {
        Coordinates originCoordinates = new Coordinates(location);
        mItemListViewModel.findNearStores(originCoordinates, NEAR_STORES_DISTANCE).observe(this,
                nearStores -> {
                    if (nearStores.size() == 0) {
                        ViewModelProviders.of(this).get(ItemListViewModel.class)
                                .getLatestCreatedStore()
                                .observe(this, store -> mItemListViewModel.buy(item, store));
                        Intent intent = new Intent(this, CreateStoreActivity.class);
                        intent.putExtra("LOCATION", location);
                        startActivity(intent);
                    } else if (nearStores.size() == 1) {
                        mItemListViewModel.buy(item, nearStores.get(0));
                    } else {
                        // TODO: show a dialog to choose from stores
                    }
                }
        );
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onStoreCreated(long storeId) {

    }
}
