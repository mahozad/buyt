package com.pleon.buyt.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Animatable;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import com.db.chart.animation.Animation;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.LineChartView;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pleon.buyt.GpsService;
import com.pleon.buyt.R;
import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.model.Coordinates;
import com.pleon.buyt.model.Store;
import com.pleon.buyt.model.WeekdayCost;
import com.pleon.buyt.ui.dialog.Callback;
import com.pleon.buyt.ui.dialog.ConfirmExitDialog;
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment;
import com.pleon.buyt.ui.dialog.RationaleDialogFragment;
import com.pleon.buyt.ui.dialog.SelectDialogFragment;
import com.pleon.buyt.ui.dialog.SelectionDialogRow;
import com.pleon.buyt.ui.fragment.BottomDrawerFragment;
import com.pleon.buyt.ui.fragment.ItemListFragment;
import com.pleon.buyt.viewmodel.MainViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.db.chart.renderer.AxisRenderer.LabelPosition.NONE;
import static com.getkeepsafe.taptargetview.TapTarget.forView;
import static com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_SHORT;
import static com.pleon.buyt.viewmodel.MainViewModel.State.FINDING;
import static com.pleon.buyt.viewmodel.MainViewModel.State.IDLE;
import static com.pleon.buyt.viewmodel.MainViewModel.State.SELECTING;

public class MainActivity extends AppCompatActivity
        implements SelectDialogFragment.Callback, ConfirmExitDialog.Callback, Callback {

    // the app can be described as both a t0do app and an expense manager and also a shopping list app

    // TODO: Make separate free and paid version flavors for the app
    // TODO: application with upgrade to paid option vs two separate free and paid flavors
    // TODO: Limit the max buys in a day in free version to 5

    // FIXME: The bug that adding new items won't show in main screen is because of configuration change;
    // after config change the observer in fragment is no longer triggered no matter you again change the config or...

    // FIXME: The bug that sometimes occur when expanding an item (the bottom item jumps up one moment),
    // is produced when another item was swiped partially

    // TODO: In onDestroy(), onPause() and... do the reverse things you did in onCreate(), onResume() and...

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

    // FIXME: Shift to idle state if the app is in finding state and all items are deleted meanwhile
    // FIXME: Slide-up bottom bar if it was hidden (because of scroll) and some items were deleted and
    // now cannot scroll to make it slide up again
    // TODO: Use a ViewStub in AddItemFragment layout for the part that is not shown until bought is checked
    // TODO: Redesign the logo in 24 by 24 grid in inkscape to make it crisp (like standard icons)
    // TODO: You can view a location in google map by starting an implicit activity with
    // the ACTION_VIEW intent and URI schema of geo:...
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
    // TODO: disable the reorder items icon in bottomAppBar when number of items is less than 2 (by 'enabled' property of the menu item)
    // TODO: Embed ads in between of regular items
    // TODO: Add snap to center for recyclerView items
    // TODO: Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
    // TODO: Collapse the chart a little in main screen when scrolling down (with coordinatorLayout)
    // TODO: extract margins and dimensions into xml files
    // TODO: Add ability to select a date to see its costs
    // TODO: Dark material colors: https://stackoverflow.com/q/36915508
    // TODO: for the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
    // FIXME: Correct all names and ids according to best practices
    // FIXME: Fix the query for chart data to start from the beginning of the first day (instead of just -7 days)
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

    // TODO: use downloadable fonts instead of integrating the font in the app to reduce the app size
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

    // If want to replace a fragment as the whole activity pass android.R.id.content to fragment manager
    // My solution: to have both the top and bottom app bars create the activity with top app bar
    // and add a fragment that includes the bottom app bar in it in this activity
    // If you want to use the standard libraries instead of the support, make these changes:
    // • make your activities extend the "Activity" instead of "AppCompatActivity"
    // • make your fragments subclass "android.app.fragment" instead of the support one
    // • to get the fragment manager, call getFragmentManager() instead of getSupportFragment...

    public static final String EXTRA_LOCATION = "com.pleon.buyt.extra.LOCATION";
    public static final String EXTRA_ITEM_ORDER = "com.pleon.buyt.extra.ITEM_ORDER";
    private static final String TAG = "MainActivity";
    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.bottom_bar) BottomAppBar mBottomAppBar;
    @BindView(R.id.chart_container) CardView chartContainer;
    @BindView(R.id.toggleChart) CheckBox chartToggle;
    @BindView(R.id.chart) BarChartView barChart;
    @BindView(R.id.lineChart) LineChartView lineChart;
    @BindView(R.id.lineChartCaption) TextView lineChartCaption;
    @BindView(R.id.guideline) Guideline guideline;

    private LocationManager locationMgr;
    private BroadcastReceiver locationReceiver;
    private MainViewModel viewModel;
    private ItemListFragment itemListFragment;
    private boolean newbie;

//    UI controllers such as activities and fragments are primarily intended to display UI data,
//    react to user actions, or handle operating system communication, such as permission requests.

    /**
     * The broadcast receiver is registered in this method because of this quote: "Does the receiver
     * need to know about the broadcast even when the activity isn't visible? For example,
     * does it need to remember that something has happened, so that when the activity becomes
     * visible, it can reflect the resulting state of affairs? Then you need to use
     * onCreate()/onDestroy() to register/unregister. (Note there are other ways to implement
     * this kind of functionality.)" See this answer: [https://stackoverflow.com/a/44526685/8583692]
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this); // unbind() is not required for activities

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        locationMgr = (LocationManager) getSystemService(LOCATION_SERVICE);

        newbie = getPreferences(MODE_PRIVATE).getBoolean("NEWBIE", true);
        if (newbie) {
            // show tap target for FAB
            new TapTargetSequence(this).targets(
                    forView(findViewById(R.id.fab), "Tap here when you're near or in the store")
                            .outerCircleColor(R.color.colorAccent)
                            .targetCircleColor(android.R.color.background_light)
                            .transparentTarget(true)
                            .textColor(android.R.color.background_dark))
                    .start();
        }

        locationReceiver = new BroadcastReceiver() { // on location found
            public void onReceive(Context context, Intent intent) {
                viewModel.setLocation(intent.getParcelableExtra(GpsService.EXTRA_LOCATION));
                Coordinates here = new Coordinates(viewModel.getLocation());
                viewModel.findNearStores(here).observe(MainActivity.this, stores -> onStoresFound(stores));
            }
        };
        LocalBroadcastManager.getInstance(this).
                registerReceiver(locationReceiver, new IntentFilter(GpsService.ACTION_LOCATION_EVENT));

        // This is just to disable add icon glow animation after first added item
        viewModel.getAllItems().observe(this, items -> {
            if (newbie && items.size() > 0) {
                getPreferences(MODE_PRIVATE).edit().putBoolean("NEWBIE", false).apply();
                mBottomAppBar.getMenu().getItem(1).setIcon(R.drawable.avd_add_hide);
            }
        });

        setSupportActionBar(mBottomAppBar);

        // FragmentManager of an activity is responsible for calling the lifecycle methods of the fragments in its list.
        FragmentManager fragMgr = getSupportFragmentManager();
        itemListFragment = (ItemListFragment) fragMgr.findFragmentById(R.id.fragment_items);

        /*
         * // If the activity is re-created due to a config change, any fragments added using the
         * // Fragment Manager will automatically be re-added. As a result, we only add a new fragment
         * // if this is not a configuration-change restart (by checking the savedInstanceState bundle)
         * if (savedInstanceState == null) {
         *     itemListFragment = ItemListFragment.newInstance();
         *     // call commit to add the fragment to the UI queue asynchronously, or
         *     // commitNow (preferred) to block until the transaction is fully complete.
         *     fragMgr.beginTransaction().add(R.id.fragment_items, itemListFragment).commitNow();
         * } else {
         *     itemListFragment = ((ItemListFragment) fragMgr.findFragmentById(R.id.fragment_items));
         * }
         */

        // observe() methods should be set only once (e.g. in activity onCreate() method) so if you
        // call it every time you want some data, maybe you're doing something wrong
        viewModel.getAllPurchases().observe(this, purchases -> {
            if (chartToggle.isChecked()) {
                show30DayCosts();
            } else {
                show7DayCosts();
            }
        });
    }

    private void show7DayCosts() {
        viewModel.getTotalWeekdayCosts().observe(this, weekdayCosts -> {
            if (weekdayCosts.size() == 0) {
                chartContainer.setVisibility(GONE);
                guideline.setGuidelinePercent(0);
            } else {
                // TODO: retrieve the past year costs and just show the past week costs,
                // if there is no cost in the past week, show costs for the past month,
                // if there is no cost in the past month, show costs in the past year
                chartContainer.setVisibility(VISIBLE);
                guideline.setGuidelinePercent(0.4f);

                barChart.reset(); // required (in case number of bars changed)

                DecimalFormat moneyFormat = new DecimalFormat("\u00A4##,###");
                if (getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
                    // for Farsi, \u00A4 is ریال but we want something else (e.g. ت)
                    moneyFormat = new DecimalFormat("##,### ت");
                }
                barChart.setLabelsFormat(moneyFormat);

                BarSet barSet = new BarSet();
                Map<Integer, Long> costs = new TreeMap<>();
                for (int i = 0; i < weekdayCosts.size(); i++) {
                    costs.put(weekdayCosts.get(i).getDay(), weekdayCosts.get(i).getCost());
                }
                for (int i = 0; i < 7; i++) { // this loop cannot be integrated into those two loops
                    if (costs.get(i) == null) {
                        costs.put(i, 0L);
                    }
                }
                for (int i = 0; i < 7; i++) {
                    int dayIndex;
                    // FIXME: .locale is deprecated
                    if (getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
                        dayIndex = WeekdayCost.Days.iranianOrder[i];
                    } else {
                        dayIndex = WeekdayCost.Days.internationalOrder[i];
                    }
                    String day = getString(WeekdayCost.Days.values()[dayIndex].getNameStringRes());
                    barSet.addBar(day, costs.get(dayIndex));
                }

                int[] colors = getResources().getIntArray(R.array.chartGradient);
                float[] steps = {0.05f, 0.5f, 1.0f};
                barSet.setGradientColor(colors, steps);

                barChart.addData(barSet);
                barChart.show(new Animation(500));
            }
        });
    }

    private void show30DayCosts() {
        viewModel.getLast30DaysCosts().observe(this, costs -> {
            lineChart.reset();

            int now = 0;
            for (WeekdayCost cost : costs) {
                if (cost.getCost() == -1) {
                    now = cost.getDay();
                    costs.remove(cost);
                }
            }

            Map<Integer, Long> dayToCostMap = new HashMap<>();
            for (WeekdayCost cost : costs) {
                dayToCostMap.put(cost.getDay(), cost.getCost());
            }

            LineSet dataSet = new LineSet();
            for (int i = now - 15; i <= now; i++) {
                dataSet.addPoint("" + i, dayToCostMap.containsKey(i) ? dayToCostMap.get(i) : 0);
            }

//            dataSet.setDotsColor(ContextCompat.getColor(this, R.color.colorAccent));
//            dataSet.setDotsRadius(2);
            dataSet.setSmooth(false); // TODO: Add an options in settings for the user to toggle this
            dataSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            dataSet.setThickness(2.5f);

            DecimalFormat moneyFormat = new DecimalFormat("\u00A4##,###");
            if (getResources().getConfiguration().locale.getDisplayName().equals("فارسی (ایران)")) {
                // for Farsi, \u00A4 is ریال but we want something else (e.g. ت)
                moneyFormat = new DecimalFormat("##,### ت");
            }
            lineChart.setLabelsFormat(moneyFormat);

            int[] colors2 = getResources().getIntArray(R.array.lineChartGradient);
            float[] steps2 = {0.0f, 0.5f, 1.0f};
            dataSet.setGradientFill(colors2, steps2);
            lineChart.addData(dataSet);
            lineChart.setXLabels(NONE);
            lineChart.show(new Animation(500));
        });
    }

    @OnCheckedChanged(R.id.toggleChart)
    void onToggleChartClick(boolean checked) {
        if (checked) {
            lineChart.reset(); // to fix previous chart blink
            chartToggle.setButtonDrawable(R.drawable.avd_toggle_chart_1);
            barChart.setVisibility(GONE);
            lineChart.setVisibility(VISIBLE);
            lineChartCaption.setVisibility(VISIBLE);
            show30DayCosts();
        } else {
            barChart.reset(); // to fix previous chart blink
            chartToggle.setButtonDrawable(R.drawable.avd_toggle_chart_2);
            barChart.setVisibility(VISIBLE);
            lineChart.setVisibility(GONE);
            lineChartCaption.setVisibility(GONE);
            show7DayCosts();
        }
        ((Animatable) CompoundButtonCompat.getButtonDrawable(chartToggle)).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bottom_home, menu);
        if (viewModel.getState() == FINDING) {
            mBottomAppBar.setNavigationIcon(R.drawable.avd_cancel_nav);
            menu.getItem(2).setIcon(R.drawable.avd_skip_reorder);
            menu.getItem(2).setTitle(R.string.menu_hint_skip_finding);
        } else if (viewModel.getState() == SELECTING) {
            mBottomAppBar.setNavigationIcon(R.drawable.avd_cancel_nav);
            menu.getItem(0).setIcon(viewModel.getStoreIcon()).setVisible(true);
            menu.getItem(2).setVisible(false);
            mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END); // this is because menu items go behind fab
        }
        if (newbie) {
            // Make plus icon glow a little bit if the user is a newbie!
            ((Animatable) menu.getItem(1).getIcon()).start();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddItemActivity.class);
                intent.putExtra(EXTRA_ITEM_ORDER, itemListFragment.getNextItemPosition());
                startActivity(intent);
                break;

            case R.id.action_reorder:
                if (viewModel.getState() == IDLE) {
                    if (!itemListFragment.isCartEmpty()) {
                        itemListFragment.toggleEditMode();
                    }
                } else { // if state == FINDING
                    viewModel.setFindingStateSkipped(true);
                    stopService(new Intent(this, GpsService.class));
                    viewModel.getAllStores().observe(this, this::onStoresFound);
                }
                break;

            /* If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
             * can be identified by checking if the id of menu item equals android.R.id.home. */
            case android.R.id.home:
                if (viewModel.getState() == IDLE) {
                    BottomSheetDialogFragment bottomDrawerFragment = BottomDrawerFragment.newInstance();
                    bottomDrawerFragment.show(getSupportFragmentManager(), "BOTTOM_SHEET");
                } else if (viewModel.getState() == FINDING) { // then it is cancel button
                    stopService(new Intent(this, GpsService.class));
                    shiftToIdleState();
                } else {
                    shiftToIdleState();
                }
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getState() == FINDING || viewModel.getState() == SELECTING) {
            ConfirmExitDialog confirmExitDialog = ConfirmExitDialog.newInstance();
            confirmExitDialog.show(getSupportFragmentManager(), "CONFIRM_EXIT_DIALOG");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onExitConfirmed() {
        super.onBackPressed();
    }

    /**
     * {@link androidx.lifecycle.ViewModel ViewModels} only survive configuration changes but
     * not process kills. On the other hand, {@link #onSaveInstanceState(Bundle)} method is called
     * for both configuration changes and process kills. So because we have ViewModel in our app,
     * here this method is used to save data just for the case of <b>process kills</b>.
     * <p>
     * This method will NOT be called if the system determines that the current state will not
     * be resumed—for example, if the activity is closed by pressing the back button or if it calls
     * {@link #finish()}.
     * <p>
     * Note that state for any View with an 'android:id' attribute is automatically saved and
     * restored by the framework (hence the call to {@code super.onSaveInstanceState()} method).
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        /*outState.putSerializable("STATE", viewModel.getState());
        // because in finding state the app runs a foreground service, no need to store its data
        if (viewModel.getState() == SELECTING) {
            outState.putParcelable("LOCATION", viewModel.getLocation());
        }*/
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (viewModel.getState() == FINDING) {
            mFab.setImageResource(R.drawable.avd_finding);
            ((Animatable) mFab.getDrawable()).start();
        } else if (viewModel.getState() == SELECTING) {
            mFab.setImageResource(R.drawable.ic_done);
            itemListFragment.toggleItemsCheckbox(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, String[] permissions, int[] grantResults) {
        if (reqCode == REQUEST_LOCATION_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                findLocation();
            } else { // if permission denied
                viewModel.setFindingStateSkipped(true);
                shiftToSelectingState();
            }
        }
        super.onRequestPermissionsResult(reqCode, permissions, grantResults);
    }

    /**
     * Unregistering the broadcast receiver is done in this method instead of onPause() because
     * we want to get the broadcast even if the app went to background and then again resumed.
     * <p>
     * See onCreate javadoc for mor info.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
        // if activity being destroyed is because of back button (not because of config change)
        if (isFinishing()) {
            stopService(new Intent(this, GpsService.class));
            AppDatabase.destroyInstance();
        }
    }

    @OnClick(R.id.fab)
    void onFabClick() {
        if (viewModel.getState() == IDLE) { // act as find
            if (itemListFragment.isCartEmpty()) {
                showShortSnackbar(R.string.snackbar_message_cart_empty);
            } else {
                itemListFragment.clearSelectedItems(); // clear items of previous purchase
                findLocation();
            }
        } else if (viewModel.getState() == SELECTING) { // act as done
            if (itemListFragment.isSelectedEmpty()) {
                showShortSnackbar(R.string.snackbar_message_no_item_selected);
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

    private void onStoresFound(List<Store> foundStores) {
        viewModel.setFoundStores(foundStores);
        if (foundStores.size() == 0) {
            if (viewModel.isFindingStateSkipped()) {
                showIndefiniteSnackbar(R.string.snackbar_message_no_store_found, android.R.string.ok);
                shiftToIdleState();
            } else {
                viewModel.setStoreIcon(R.drawable.ic_store_new); // to use on config change
                mBottomAppBar.getMenu().getItem(0).setIcon(viewModel.getStoreIcon());
                mBottomAppBar.getMenu().getItem(0).setVisible(true);
                shiftToSelectingState();
            }
        } else {
            shiftToSelectingState();
            if (foundStores.size() == 1) {
                int icon = foundStores.get(0).getCategory().getStoreImageRes();
                viewModel.setStoreIcon(icon); // to use on config change
                mBottomAppBar.getMenu().getItem(0).setIcon(icon);
                itemListFragment.sortStoreItemsFirst(foundStores.get(0).getCategory());
            } else {
                viewModel.setStoreIcon(R.drawable.ic_store_multi); // to use on config change
                mBottomAppBar.getMenu().getItem(0).setIcon(viewModel.getStoreIcon());
            }
            mBottomAppBar.getMenu().getItem(0).setVisible(true);
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
        viewModel.setState(FINDING);

        mFab.setImageResource(R.drawable.avd_buyt);
        ((Animatable) mFab.getDrawable()).start();

        mBottomAppBar.setNavigationIcon(R.drawable.avd_nav_cancel);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();

//        ((Animatable) mBottomAppBar.getMenu().getItem(0).getIcon()).start();
//        mBottomAppBar.getMenu().getItem(0).setVisible(false);

        mBottomAppBar.getMenu().getItem(2).setIcon(R.drawable.avd_reorder_skip);
        mBottomAppBar.getMenu().getItem(2).setTitle(R.string.menu_hint_skip_finding);
        ((Animatable) mBottomAppBar.getMenu().getItem(2).getIcon()).start();

        // Make sure the bottomAppBar is not hidden and make it not hide on scroll
        new BottomAppBar.Behavior().slideUp(mBottomAppBar);
        mBottomAppBar.setHideOnScroll(false);
    }

    private void shiftToSelectingState() {
        itemListFragment.toggleItemsCheckbox(true);

        mBottomAppBar.getMenu().getItem(2).setVisible(false);
        mBottomAppBar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END);

        mFab.setImageResource(R.drawable.avd_find_done);
        ((Animatable) mFab.getDrawable()).start();

        viewModel.setState(SELECTING);
    }

    private void shiftToIdleState() {
        // reset
        viewModel.resetFoundStores();
        viewModel.setFindingStateSkipped(false);

        itemListFragment.toggleItemsCheckbox(false);

        mBottomAppBar.setFabAlignmentMode(FAB_ALIGNMENT_MODE_CENTER);

        if (viewModel.getState() == FINDING) {
            mFab.setImageResource(R.drawable.avd_buyt_reverse);
        } else {
            mFab.setImageResource(R.drawable.avd_done_buyt);
        }
        ((Animatable) mFab.getDrawable()).start();

        mBottomAppBar.setNavigationIcon(R.drawable.avd_cancel_nav);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();

        mBottomAppBar.setHideOnScroll(true);

        mBottomAppBar.getMenu().getItem(0).setVisible(false);

//        mBottomAppBar.getMenu().getItem(1).setVisible(true);
//        mBottomAppBar.getMenu().getItem(1).setIcon(R.drawable.avd_add_show);
//        ((Animatable) mBottomAppBar.getMenu().getItem(1).getIcon()).start();

        mBottomAppBar.getMenu().getItem(2).setVisible(true);
        mBottomAppBar.getMenu().getItem(2).setIcon(R.drawable.avd_skip_reorder);
        mBottomAppBar.getMenu().getItem(2).setTitle(R.string.menu_hint_reorder_items);
        ((Animatable) mBottomAppBar.getMenu().getItem(2).getIcon()).start();

        viewModel.setState(IDLE); // this should be the last statement (because of the if above)
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
            if (viewModel.getFoundStores().size() == 0) {
                Intent intent = new Intent(this, CreateStoreActivity.class);
                intent.putExtra(EXTRA_LOCATION, viewModel.getLocation());
                startActivity(intent);
                viewModel.getLatestCreatedStore().observe(this, this::completeBuy);
            } else if (viewModel.getFoundStores().size() == 1) {
                completeBuy(viewModel.getFoundStores().get(0));
            } else { // show store selection dialog
                ArrayList<SelectionDialogRow> selectionList = new ArrayList<>(); // dialog requires ArrayList
                for (Store store : viewModel.getFoundStores()) {
                    SelectionDialogRow selection = new SelectionDialogRow(store.getName(), store.getCategory().getStoreImageRes());
                    selectionList.add(selection);
                }
                SelectDialogFragment selectStoreDialog = SelectDialogFragment.newInstance(this, selectionList);
                selectStoreDialog.show(getSupportFragmentManager(), "SELECT_STORE_DIALOG");
                // next this::completeBuy() is called
            }
        }
    }

    /**
     * On store selected from selection dialog
     *
     * @param index
     */
    @Override
    public void onSelected(int index) {
        completeBuy(viewModel.getFoundStores().get(index));
    }

    public void completeBuy(Store store) {
        viewModel.buy(itemListFragment.getSelectedItems(), store, new Date());
        shiftToIdleState();
    }

    @Override
    public void onEnableLocationDenied() {
        mBottomAppBar.getMenu().getItem(1).setVisible(false);
        mBottomAppBar.setNavigationIcon(R.drawable.avd_nav_cancel);
        ((Animatable) mBottomAppBar.getNavigationIcon()).start();
        viewModel.setFindingStateSkipped(true);
        shiftToSelectingState();
    }
}
