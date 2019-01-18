package com.pleon.buyt.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pleon.buyt.GpsService;
import com.pleon.buyt.R;
import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.ui.dialog.Callback;
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment;
import com.pleon.buyt.ui.dialog.RationaleDialogFragment;
import com.pleon.buyt.ui.fragment.BottomDrawerFragment;
import com.pleon.buyt.ui.fragment.ItemListFragment;
import com.pleon.buyt.ui.fragment.SelectStoreDialogFragment;
import com.pleon.buyt.viewmodel.ItemListViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static com.getkeepsafe.taptargetview.TapTarget.forView;
import static com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;
import static java.lang.Math.cos;

public class MainActivity extends AppCompatActivity
        implements SelectStoreDialogFragment.Callback, Callback {

    // the app can be described as both a t0do app and an expense manager and also a shopping list app

    // FIXME: The bug that adding new items won't show in main screen is because of configuration change;
    // after config change the observer in fragment is no longer triggered no matter you again change the config or...

    // FIXME: The bug that sometimes occur when expanding an item (the bottom item jumps up one moment),
    // is produced when another item was swiped partially

    /*
     * DONE: if the bottomAppBar is hidden (by scrolling) and then you expand an Item, the fab jumps up
     * The bug seems to have nothing to do with the expanding animation and persists even without that animation
     */
    // FIXME: Use srcCompat instead of src in layout files
    // FIXME: If number of Items to buy is less than e.g. 4 then don't show the "items to buy" prompt
    // DONE: the bottom shadow (elevation) of item cards is broken. Maybe because of swipe-to-delete background layer

    // FIXME: when dragging items, in some situations** item moves from behind of other cards
    // **: this happens if the card being dragged over by this card, has itself dragged over this card in the past.
    // steps to reproduce: drag card1 over card2 and then drop it (you can also drop it to its previous position).
    // now drag card2 over card1. Then again drag card1 over card2; it moves behind of card2 and in front of other cards.
    // NOTE: This is caused by "public void clearView..." method in TouchHelperCallback class
    // see the following to probably fix it:
    // https://github.com/brianwernick/RecyclerExt/blob/master/library/src/main/java/com/devbrackets/android/recyclerext/adapter/helper/SimpleElevationItemTouchHelperCallback.java

    // DONE: What if someone forgets to tick items of a shop and then later wants to tick them: He can skip finding location

    // TODO: disable swipe-to-delete when the state is not in IDLE
    // TODO: Show the found store (icon or name) in bottomAppBar when location found (selecting mode)
    // TODO: Make icons animation durations consistent
    // TODO: round and filled icons of material design are here: https://material.io/tools/icons/?icon=done&style=round
    // TODO: Convert the logo to path (with path -> stroke to path option) and then recreate the logo
    // FIXME: Update position field of items if an item is deleted
    // DONE: Add ability to cancel completely when in input price mode
    // TODO: Add option in settings to enable/disable showing urgent items at top of the list
    // TODO: Add a button (custom view) at the end of StoreListAdapter to create a new Store
    // TODO: Add option in settings to disable/enable store confirmation (only one near store found)
    // TODO: Add option in settings to disable/enable price confirmation dialog
    // TODO: Add a separator (e.g. comma) in every 3 digits of price and other numeric fields
    // TODO: Add option in settings to set the default item quantity in add new item activity (1 seems good)
    // TODO: Reimplement item unit switch button with this approach: https://stackoverflow.com/a/48640424/8583692
    // TODO: Add a functionality to merge another device data to a device (e.g. can merge all family spending data to father's phone)
    // TODO: Add an action in bottomAppBar in Add Item activity to select a date for showing the item in home page
    // DONE: For circular coloring of swipe background, see https://stackoverflow.com/q/46460978/8583692
    // TODO: Use DiffUtil class (google it!) instead of calling notifyDataSetChanged() method of adapter
    // DONE: Add an reorder icon to bottomAppBar so when taped, the cards show a handle to order them
    // DONE: Disable buyt fab button when there is no item
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
    // DONE: Use butter knife to declare activity views and view handlers
    // TODO: Enable the user to disable location rationale dialog and always enter stores manually
    // TODO: What is Spherical Law of Cosines? (for locations)
    // TODO: Add the functionality to export and import all app data
    // TODO: Try to first provide an MVP (minimally viable product) version of the app
    // TODO: Make viewing stores on map a premium feature
    // TODO: Enable the user to change the radius that app uses to find near stores in settings
    // TODO: Add ability to remove all app data
    // TODO: Add android.support.annotation to the app
    // TODO: For item prices user can enter an inexact value (range)
    // TODO: For every new version of the app display a what's new page on first app open
    // DONE: Convert the app architecture to MVVM
    // TODO: Use loaders to get data from database?
    // TODO: Convert all ...left and ...right attributes to ...start and ...end
    // DONE: Add ability (an icon) for each item to mark it as high priority
    // TODO: Add animation to item expand icon
    // DONE: Ability to add details (description) for each item
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

    // TODO: new version of MaterialCardView will include a setCheckedIcon. check it out

    // TODO: In case you are using Proguard, you will need to whitelist MPAndroidChart,
    // which requires to add the following line to your Proguard configuration file:
    //-keep class com.github.mikephil.charting.** { *; }

    // If want to replace a fragment as the whole activity pass android.R.id.content to fragment manager
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    private static final String TAG = "MainActivity";
    private static final double NEAR_STORES_DISTANCE = cos(0.1 / 6371); // == 200m (6371km is the radius of the Earth)
    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    public static final String EXTRA_LOCATION = "com.pleon.buyt.extra.LOCATION";
    public static final String EXTRA_ITEM_ORDER = "com.pleon.buyt.extra.ITEM_ORDER";


    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.bottom_bar) BottomAppBar mBottomAppBar;

    private Location location;
    // volatile because user clicking the fab and location may be found at the same time
    private volatile State state = State.IDLE;
    private LocationManager locationMgr;
    private BroadcastReceiver locationReceiver;
    private ItemListViewModel mItemListViewModel;
    private ItemListFragment itemListFragment;
    private boolean findingStateSkipped = false;
    private Set<Item> selectedItems;
    private boolean newbie;

    private enum State {
        IDLE, FINDING, SELECTING
    }

//    UI controllers such as activities and fragments are primarily intended to display UI data,
//    react to user actions, or handle operating system communication, such as permission requests.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        ButterKnife.bind(this); // unbind() is not required for activities

        locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationReceiver = new BroadcastReceiver() { // on location found
            public void onReceive(Context context, Intent intent) {
                location = intent.getParcelableExtra("LOCATION");
                shiftToSelectingState();
            }
        };

        newbie = getPreferences(MODE_PRIVATE).getBoolean("NEWBIE", true);
        if (newbie) {
            // show tap target for FAB
            new TapTargetSequence(this).targets(
                    forView(findViewById(R.id.fab), "Tap here when you're ready")
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(android.R.color.background_light)
                            .transparentTarget(true)
                            .textColor(R.color.colorPrimaryDark))
                    .start();
        }
        getPreferences(MODE_PRIVATE).edit().putBoolean("NEWBIE", false).apply();

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

//        GraphView graph = findViewById(R.id.graph);
//        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[]{
//                new DataPoint(0, 21),
//                new DataPoint(1, 5),
//                new DataPoint(2, 3),
//                new DataPoint(3, 2),
//                new DataPoint(4, 2),
//                new DataPoint(5, 2),
//                new DataPoint(6, 6)
//        });
//        series.setColor(R.color.colorPrimaryDark);
//        graph.addSeries(series);

        BarChart lineChart = findViewById(R.id.chart);
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 10));
        entries.add(new BarEntry(1, 107));
        entries.add(new BarEntry(2, 1));
        entries.add(new BarEntry(3, 15));
        entries.add(new BarEntry(4, 8));
        entries.add(new BarEntry(5, 8));
        entries.add(new BarEntry(6, 3));
        lineChart.animateY(2000);
        BarDataSet dataSet = new BarDataSet(entries, "Label"); // add entries to dataset
        BarData data = new BarData(dataSet);
        lineChart.setData(data);
        lineChart.invalidate(); // refresh

        mItemListViewModel = ViewModelProviders.of(this).get(ItemListViewModel.class);
        // observe() methods should be set only once (e.g. in activity onCreate() method) so if you
        // call it every time you want some data, maybe you're doing something wrong
        mItemListViewModel.getNearStores().observe(this, this::onStoresFound);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_home, menu);
        if (newbie) {
            // Make plus icon glow a little bit if the user is a newbie!
            ((Animatable) menu.getItem(0).getIcon()).start();
        }
        return true;
    }

    /**
     * Caution: If you use an intent to start a Service, ensure that your app is secure
     * by using an explicit intent.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddItemActivity.class);
                intent.putExtra(EXTRA_ITEM_ORDER, itemListFragment.getNextItemPosition());
                startActivity(intent);

                // View chartView = findViewById(R.id.container_fragment_chart);
                // chartView.setVisibility(View.GONE); // TODO: maybe replacing the fragment is a better practice
                break;

            case R.id.action_reorder:
                if (state == State.IDLE) {
                    ItemListFragment itemListFragment = (ItemListFragment)
                            getSupportFragmentManager().findFragmentById(R.id.container_fragment_items);
                    itemListFragment.toggleEditMode();
                } else { // if state == FINDING
                    findingStateSkipped = true;
                    stopService(new Intent(this, GpsService.class));
                    shiftToSelectingState();
                }
                break;

            /* If you use setSupportActionBar() to set up the BottomAppBar you can handle the
             * navigation menu click by checking if the menu item id equals android.R.id.home. */
            case android.R.id.home:
                if (state == State.IDLE) {
                    BottomSheetDialogFragment bottomDrawerFragment = BottomDrawerFragment.newInstance();
                    bottomDrawerFragment.show(getSupportFragmentManager(), "alaki");
                } else if (state == State.FINDING) { // then it is cancel button
                    stopService(new Intent(this, GpsService.class));
                    shiftToIdleState();
                } else {
                    itemListFragment.toggleItemsCheckbox(false);
                    shiftToIdleState();
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Registers the location broadcast receiver.
     * <p>
     * Note that Local broadcast receivers can be registered only dynamically in code
     * (like in this case) and not in the manifest file.
     */
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).
                registerReceiver(locationReceiver, new IntentFilter("LOCATION_INTENT"));
    }

    /**
     * Unregisters the location broadcast receiver.
     * <p>
     * Note that <b>Local</b> broadcast receivers can be registered only dynamically in code
     * (like in this case) and not in the manifest file.
     */
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the application form and temp data to survive config changes and force-kills
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grantResults) {
        if (reqCode == REQUEST_LOCATION_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                findLocation();
            } else { // if permission denied
                findingStateSkipped = true;
                shiftToSelectingState();
            }
        }
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, GpsService.class));
        AppDatabase.destroyInstance();
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        if (state == State.IDLE) { // act as find
            if (itemListFragment.isCartEmpty()) {
                showShortSnackbar(R.string.cart_empty_message);
            } else {
                itemListFragment.clearSelectedItems(); // clear items of previous purchase
                findLocation();
            }
        } else if (state == State.SELECTING) { // act as done
            if (itemListFragment.isSelectedEmpty()) {
                showShortSnackbar(R.string.no_item_selected_message);
            } else {
                buySelectedItems();
            }
        }
    }

    private void findLocation() {
        // Dangerous permissions should be checked EVERY time
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission();
        } else if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
            LocationOffDialogFragment rationaleDialog = LocationOffDialogFragment.newInstance();
            rationaleDialog.show(getSupportFragmentManager(), "LOCATION_OFF_DIALOG");
        } else {
            shiftToFindingState();
            Intent intent = new Intent(this, GpsService.class);
            ContextCompat.startForegroundService(this, intent); // no need to check api lvl
        }
    }

    private void onStoresFound(Collection<Store> foundStores) {
        if (foundStores.size() == 0 && findingStateSkipped) {
            shiftToIdleState();
            showIndefiniteSnackbar(R.string.no_store, R.string.ok);
        } else if (foundStores.size() == 0) {
            Intent intent = new Intent(this, CreateStoreActivity.class);
            intent.putExtra(EXTRA_LOCATION, location);
            startActivity(intent);
            mItemListViewModel.getLatestCreatedStore().observe(this, this::onStoreSelected);
        } else {
            ArrayList<Store> stores = new ArrayList<>(foundStores); // dialog requires ArrayList
            SelectStoreDialogFragment selectStoreDialog = SelectStoreDialogFragment.newInstance(stores);
            selectStoreDialog.show(getSupportFragmentManager(), "SELECT_STORE_DIALOG");
            // handle selected Store in this::onStoreSelected()
        }
    }

    private void showShortSnackbar(int message) {
        Snackbar.make(findViewById(R.id.snackBarContainer),
                getString(message), LENGTH_SHORT)
                .show();
    }

    private void showIndefiniteSnackbar(int message, int action) {
        Snackbar.make(findViewById(R.id.snackBarContainer),
                getString(message), LENGTH_INDEFINITE).setAction(action, v -> {
        })
                .show();
    }

    private void shiftToFindingState() {
        state = State.FINDING;

        mFab.setImageResource(R.drawable.avd_buyt);
        ((Animatable) mFab.getDrawable()).start();

        mBottomAppBar.setNavigationIcon(R.drawable.avd_nav_cancel);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();

//        ((Animatable) mBottomAppBar.getMenu().getItem(0).getIcon()).start();
//        mBottomAppBar.getMenu().getItem(0).setVisible(false);

        mBottomAppBar.getMenu().getItem(1).setIcon(R.drawable.avd_reorder_skip);
        mBottomAppBar.getMenu().getItem(1).setTitle(R.string.skip);
        ((Animatable) mBottomAppBar.getMenu().getItem(1).getIcon()).start();

        Animatable fabDrawable = (Animatable) mFab.getDrawable();
        fabDrawable.start();

        // Make sure the bottomAppBar is not hidden and make it not hide on scroll
        new BottomAppBar.Behavior().slideUp(mBottomAppBar);
        mBottomAppBar.setHideOnScroll(false);
    }

    private void shiftToSelectingState() {
        itemListFragment.toggleItemsCheckbox(true);

        mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);
        mFab.setImageResource(R.drawable.avd_find_done);
        ((Animatable) mFab.getDrawable()).start();
        mBottomAppBar.getMenu().getItem(1).setVisible(false);

        state = State.SELECTING;
    }

    private void shiftToIdleState() {
        itemListFragment.clearSelectedItems();
        itemListFragment.toggleItemsCheckbox(false);

        mBottomAppBar.setFabAlignmentMode(FAB_ALIGNMENT_MODE_CENTER);
        mFab.setImageResource(R.drawable.avd_buyt_reverse);
        ((Animatable) mFab.getDrawable()).start();

        mBottomAppBar.setNavigationIcon(R.drawable.avd_cancel_nav);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();

        mBottomAppBar.setHideOnScroll(true);

//        mBottomAppBar.getMenu().getItem(0).setVisible(true);
//        mBottomAppBar.getMenu().getItem(0).setIcon(R.drawable.avd_add_show);
//        ((Animatable) mBottomAppBar.getMenu().getItem(0).getIcon()).start();

        mBottomAppBar.getMenu().getItem(1).setVisible(true);
        mBottomAppBar.getMenu().getItem(1).setIcon(R.drawable.avd_skip_reorder);
        mBottomAppBar.getMenu().getItem(1).setTitle(R.string.reorder_items);
        ((Animatable) mBottomAppBar.getMenu().getItem(1).getIcon()).start();

        state = State.IDLE;
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a the user will be prompted
     * to grant the permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        // When the user responds to the app's permission request, the system invokes onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            RationaleDialogFragment rationaleDialog = RationaleDialogFragment.newInstance();
            rationaleDialog.show(getSupportFragmentManager(), "LOCATION_RATIONALE_DIALOG");
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    public void buySelectedItems() {
        if (itemListFragment.validateSelectedItemsPrice()) {
            selectedItems = itemListFragment.getSelectedItems();
            if (findingStateSkipped) {
                mItemListViewModel.getAllStores();
            } else {
                Coordinates originCoordinates = new Coordinates(location);
                mItemListViewModel.findNearStores(originCoordinates, NEAR_STORES_DISTANCE);
            }
            // next this::onStoresFound() is called
        }
    }

    @Override
    public void onStoreSelected(Store store) {
        mItemListViewModel.buy(selectedItems, store);
        shiftToIdleState();
    }

    @Override
    public void onEnableLocationDenied() {
        mBottomAppBar.getMenu().getItem(0).setVisible(false);
        mBottomAppBar.setNavigationIcon(R.drawable.avd_nav_cancel);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();
        findingStateSkipped = true;
        shiftToSelectingState();
    }
}
