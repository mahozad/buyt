package com.pleon.buyt.ui.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.fragment.BottomDrawerFragment;
import com.pleon.buyt.ui.fragment.ItemListFragment;
import com.pleon.buyt.ui.fragment.RationaleDialogFragment;
import com.pleon.buyt.ui.fragment.SelectStoreDialogFragment;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import java.util.ArrayList;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.getkeepsafe.taptargetview.TapTarget.forView;
import static java.lang.Math.cos;

public class ItemListActivity extends AppCompatActivity implements SelectStoreDialogFragment.Callback {

    /*
     * FIXME: if the bottomAppBar is hidden (by scrolling) and then you expand an Item, the fab jumps up
     * The bug seems to have nothing to do with the expanding animation and persists even without that animation
     */
    // FIXME: Use srcCompat instead of src in layout files?
    // FIXME: If number of Items to buy is less than e.g. 4 then don't show the "items to buy" prompt
    // DONE: the bottom shadow (elevation) of item cards is broken. Maybe because of swipe-to-delete background layer

    // FIXME: when dragging items, in some situations** item moves from behind of other cards
    // **: this happens if the card being dragged over by this card, has itself dragged over this card in the past.
    // steps to reproduce: drag card1 over card2 and then drop it (you can also drop it to its previous position).
    // now drag card2 over card1. Then again drag card1 over card2; it moves behind of card2 and in front of other cards.
    // NOTE: This is caused by "public void clearView..." method in ItemTouchHelperCallback class
    // see the following to probably fix it:
    // https://github.com/brianwernick/RecyclerExt/blob/master/library/src/main/java/com/devbrackets/android/recyclerext/adapter/helper/SimpleElevationItemTouchHelperCallback.java

    // FIXME: What if someone forgets to tick items of a shop and then later wants to tick them
    // the app can be described as both a t0do app and an expense manager and also a shopping list app
    // After clicking Buyt fab button it converts to a done button and then by clicking on each item it is highlighted and finally click done

    // FIXME: Update position field of items if an item is deleted
    // TODO: Add ability to cancel completely when in input price mode
    // TODO: Add option in settings to enable/disable showing urgent items at top of the list
    // TODO: Add a button (custom view) at the end of StoreListAdapter to create a new Store
    // TODO: Add option in settings to disable/enable store confirmation (only one near store found)
    // TODO: Add option in settings to disable/enable price confirmation dialog
    // TODO: Add a separator (e.g. comma) in every 3 digits of price and other numeric fields
    // TODO: Add option in settings to set the default item quantity in add new item activity (1 seems good)
    // TODO: Reimplement item unit switch button with this approach: https://stackoverflow.com/a/48640424/8583692
    // TODO: Add a functionality to merge another device data to a device (e.g. can merge all family spending data to father's phone)
    // TODO: Add an action in bottomAppBar in Add Item activity to select a date for showing the item in home page
    // TODO: For circular coloring of swipe background, see https://stackoverflow.com/q/46460978/8583692
    // TODO: Use DiffUtil class (google it!) instead of calling notifyDataSetChanged() method of adapter
    // TODO: Add an reorder icon to bottomAppBar so when taped, the cards show a handle to order them
    // TODO: Disable buyt fab button when there is no item
    // TODO: For correct margins of cards, Texts, ... see the page of that component in design section of material.io
    // TODO: Difference between <ImageView>s to <AppcompatImageView>s ?
    // TODO: hide the reorder items icon in bottomAppBar when number of items is less than 2
    // TODO: Embed ads in between of regular items
    // TODO: Add snap to center for recyclerView items
    // TODO: Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
    // TODO: Collapse the chart a little in main screen when scrolling down (with coordinatorLayout)
    // TODO: extract margins and dimensions into xml files
    // TODO: Add ability to select a date to see its costs
    // TODO: Dark material colors: https://stackoverflow.com/q/36915508
    // TODO: for the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
    // FIXME: Correct all names and ids according to best practices
    // TODO: Use butter knife to declare activity views and view handlers
    // TODO: Enable the user to disable location rationale dialog and always enter stores manually
    // TODO: What is Spherical Law of Cosines? (for locations)
    // TODO: Add the functionality to export and import all app data
    // TODO: Try to first provide an MVP (minimally viable product) version of the app
    // TODO: Implement the app with Flutter
    // TODO: Make viewing stores on map a premium feature
    // TODO: Maybe instead of a fragment, I can use full-screen dialog for adding new Item?
    // TODO: Enable the user to change the radius that app uses to find near stores in settings
    // TODO: Add ability to remove all app data
    // TODO: Add android.support.annotation to the app
    // TODO: For item_list_row prices user can enter an inexact value (range)
    // TODO: For every new version of the app display a what's new page on first app open
    // DONE: Convert the app architecture to MVVM
    // TODO: Use loaders to get data from database?
    // TODO: Convert all ...left and ...right attributes to ...start and ...end
    // TODO: Add ability (an icon) for each item to mark it as high priority
    // TODO: Ability to add details (description) for each item
    // TODO: Show a small progress bar of how much has been spent if user has set a limit on spends
    /* FIXME: What happens if two stores are near each other and only one of them is saved in the app.
       now if user has bought something from the other store, it is saved for the persisted store */
    /* TODO: Show a prompt (or an emoji or whatever) when there is no items in the home screen
       to do this, add a new View to the layout and play with its setVisibility as appropriate
    */
    /* TODO: Do you have multiple tables in your database and find yourself copying the same Insert,
     * Update and Delete methods? DAOs support inheritance, so create a BaseDao<T> class, and define
     * your generic @Insert,... there. Have each DAO extend the BaseDao and add methods specific to each of them.
     */

    // If want to replace a fragment as the whole activity pass android.R.id.content to fragment manager
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    private static final String TAG = "ItemListActivity";
    private static final double NEAR_STORES_DISTANCE = cos(0.2 / 6371); // == 200m (6371km is the radius of the Earth)

    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private FloatingActionButton mFab;
    private BottomAppBar mBottomAppBar;
    private Location location;
    private AsyncTask<Void, Void, Void> locationTask;
    private ItemListViewModel mItemListViewModel;
    private boolean findLocationMode = true;
    private ItemListFragment itemListFragment;
    private Set<Item> selectedItems;

//    UI controllers such as activities and fragments are primarily intended to display UI data,
//    react to user actions, or handle operating system communication, such as permission requests.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);

        mBottomAppBar = findViewById(R.id.bottom_bar);
        setSupportActionBar(mBottomAppBar);

        // FragmentManager of an activity is responsible for calling the lifecycle methods of the fragments in its list.
        FragmentManager fragMgr = getSupportFragmentManager();
        itemListFragment = (ItemListFragment) fragMgr.findFragmentById(R.id.container_fragment_items);
        // fragMgr saves the list of fragments when activity is destroyed and then retrieves them
        // so first we check if the fragment we want does not exist, then we create it
        if (itemListFragment == null) {
            itemListFragment = ItemListFragment.newInstance();
            fragMgr.beginTransaction()
                    .add(R.id.container_fragment_items, itemListFragment)
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

        mItemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);

        // observe() methods should be set only once (e.g. in activity onCreate() method) so if you
        // call it every time you want some data, maybe you're doing something wrong
        mItemListViewModel.getNearStores().observe(this, nearStores -> {
                    if (nearStores.size() == 0) {
                        Intent intent = new Intent(this, CreateStoreActivity.class);
                        intent.putExtra("LOCATION", location);
                        startActivity(intent);
                        mItemListViewModel.getLatestCreatedStore().observe(this, this::onStoreSelected);
                    } else {
                        SelectStoreDialogFragment selectStoreDialog = SelectStoreDialogFragment.newInstance((ArrayList<Store>) nearStores);
                        selectStoreDialog.show(getSupportFragmentManager(), "selectStoreFragment");
                        // handle selected Store in this::onStoreSelected()
                    }
                }
        );


        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(fab -> {
            if (findLocationMode) {
                itemListFragment.clearSelectedItems(); // clear items of previous purchase
                findLocation();
                findLocationMode = !findLocationMode;
            } else {
                buySelectedItems();
            }
        });


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
        /* Check for dangerous permissions should be done EVERY time */
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission();
        } else if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
            RationaleDialogFragment rationaleDialog =
                    RationaleDialogFragment.newInstance(R.string.location_turn_on_title,
                            R.string.location_turn_on_rationale, false);
            rationaleDialog.show(getSupportFragmentManager(), "LOCATION_RATIONALE_DIALOG");
        } else {
            GpsListener gpsListener = new GpsListener();
            locationMgr.requestLocationUpdates(GPS_PROVIDER, 0, 0, gpsListener);
        }
    }

    private void onLocationFound(Location location) {
        ItemListActivity.this.location = location;
        itemListFragment.enableItemsCheckbox();
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
        // When the user responds to the app's permission request, the system invokes onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            RationaleDialogFragment rationaleDialog =
                    RationaleDialogFragment.newInstance(R.string.location_permission_title,
                            R.string.location_permission_rationale, true);
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
                Intent intent = new Intent(this, AddItemActivity.class);
                intent.putExtra("NEXT_ITEM_ORDER", itemListFragment.getNextItemPosition());
                startActivity(intent);

                // View chartView = findViewById(R.id.container_fragment_chart);
                // chartView.setVisibility(View.GONE); // TODO: maybe replacing the fragment is a better practice
                break;

            case R.id.action_reorder:
                ItemListFragment itemListFragment = (ItemListFragment)
                        getSupportFragmentManager().findFragmentById(R.id.container_fragment_items);
                itemListFragment.toggleEditMode();
                break;

            /* If you use setSupportActionBar() to set up the BottomAppBar
             * you can handle the navigation menu click by checking if the menu item id is android.R.id.home.
             */
            case android.R.id.home:
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

    public void buySelectedItems() {
        if (itemListFragment.validateSelectedItemsPrice()) {
            selectedItems = itemListFragment.getSelectedItems();
            Coordinates originCoordinates = new Coordinates(location);
            mItemListViewModel.findNearStores(originCoordinates, NEAR_STORES_DISTANCE);
        }
    }

    @Override
    public void onStoreSelected(Store store) {
        mItemListViewModel.buy(selectedItems, store);
        findLocationMode = !findLocationMode;
    }
}
