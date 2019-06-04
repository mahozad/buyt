package com.pleon.buyt.ui.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Animatable
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.google.android.material.snackbar.Snackbar.*
import com.pleon.buyt.R
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.component.ACTION_LOCATION_EVENT
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.component.LocationReceiver
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment.CreateStoreListener
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment
import com.pleon.buyt.ui.dialog.RationaleDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.ui.fragment.*
import com.pleon.buyt.util.AnimationUtil.animateIconInfinitely
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.util.VibrationUtil.vibrate
import com.pleon.buyt.viewmodel.MainViewModel
import com.pleon.buyt.viewmodel.MainViewModel.State.*
import com.pleon.buyt.viewmodel.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import javax.inject.Inject

private const val STATE_LOCATION = "com.pleon.buyt.state.LOCATION"
private const val REQUEST_LOCATION_PERMISSION = 1

/**
 * UI controllers such as activities and fragments are primarily intended to display UI data,
 * react to user actions, or handle operating system communication, such as permission requests.
 */
class MainActivity : BaseActivity(), SelectDialogFragment.Callback, CreateStoreListener {

    // To force kill the app, go to the desired activity, press home button and then run this command:
    // adb shell am kill com.pleon.buyt
    // return to the app from recent apps screen (or maybe by pressing its launcher icon)

    // For a good article about dagger see
    // [https://medium.com/@iammert/new-android-injector-with-dagger-2-part-1-8baa60152abe]

    /* FIXME: The bug that sometimes occur when expanding an item
     *  (the bottom item jumps up one moment), is produced when another item was swiped partially */

    // SKU for our product: the premium upgrade (non-consumable)
    val SKU_PREMIUM = "full_features"
    // Does the user have the premium upgrade?
    var mIsPremium = false
    // (arbitrary) request code for the purchase flow
    val RC_REQUEST: Int = 62026
    // The helper object
    lateinit var iabHelper: IabHelper

    @Inject internal lateinit var viewModelFactory: ViewModelFactory<MainViewModel>
    @Inject internal lateinit var locationReceiver: LocationReceiver
    @Inject internal lateinit var broadcastMgr: LocalBroadcastManager
    @Inject internal lateinit var locationMgr: LocationManager
    private lateinit var viewModel: MainViewModel
    private lateinit var itemsFragment: ItemsFragment
    private lateinit var addMenuItem: MenuItem
    private lateinit var reorderMenuItem: MenuItem
    private lateinit var storeMenuItem: MenuItem
    private lateinit var addStorePopup: PopupMenu

    override fun layout() = R.layout.activity_main

    /**
     * The broadcast receiver is registered in this method because of this quote: "Does the receiver
     * need to know about the broadcast even when the activity isn't visible? For example,
     * does it need to remember that something has happened, so that when the activity becomes
     * visible, it can reflect the resulting state of affairs? Then you need to use
     * onCreate()/onDestroy() to register/unregister. (Note there are other ways to implement
     * this kind of functionality.)" See this answer: [https://stackoverflow.com/a/44526685/8583692]
     *
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
     *
     * for fragment example see [https://developer.android.com/guide/components/fragments#Example]
     * As in android developers guild, make this variable a field if needed:
     * boolean wideLayout = findViewById(R.id.chart) != null;
     * if (wideLayout) {
     *     Do whatever needed
     * }
     *
     * @param savedState
     */
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        showIntroIfNeeded()
        restoreBottomDrawerIfNeeded()



        // TODO: Encrypt premium attribute in preferences to prevent the user from hacking it.
        //  See [https://developer.android.com/jetpack/androidx/releases/security]
        //  and [https://developer.android.com/topic/security/data]


        // It is recommended to add more security than just pasting it in your source code;
        val base64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDW/+Cgaba85mg16U2qNlPChs" +
                "7LrqiEnfwZX1odxiY1mO9SPNM2uE8B8kAND9OuXENeYQVLtXISJ9sjdJ2a3WW6ZWGLMUzDKuVSRBSnGM632" +
                "hvWLh9xye/WsFP2Q9zZH2xi5/dbQ/mix1VcdxycWCgHtCJ7lFGfq9yVvJ+ZHoIivIMEWy5NbksQziTgwHK0" +
                "fDh1kIN6qDB8zJIH2ak0kENK6Mk0r75hI6MkPHz8f/sCAwEAAQ=="
        iabHelper = IabHelper(this, base64EncodedPublicKey)
        // Start billing setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        iabHelper.startSetup { setupResult ->
            if (!setupResult.isSuccess) {
                /* Oh noes, there was a problem.*/
                Log.e(TAG, "Problem setting up in-app billing: $setupResult")
            }
            // Hooray, IAB is fully set up!

            iabHelper.queryInventoryAsync(IabHelper.QueryInventoryFinishedListener { result, inventory ->
                // Is called when we finish querying the items and subscriptions we own

                if (result.isFailure) {
                    Log.d(TAG, "Failed to query inventory: $result")
                    return@QueryInventoryFinishedListener
                } else {
                    Log.d(TAG, "Query inventory was successful.")
                    mIsPremium = inventory.hasPurchase(SKU_PREMIUM)
                    Log.d(TAG, "does the user have the premium upgrade? $mIsPremium")

                    // update UI accordingly

                }

            })
        }







        viewModel = of(this, viewModelFactory).get(MainViewModel::class.java)
        broadcastMgr.registerReceiver(locationReceiver, IntentFilter(ACTION_LOCATION_EVENT))
        locationReceiver.getLocation().observe(this, Observer { onLocationFound(it) })
        itemsFragment = supportFragmentManager.findFragmentById(R.id.itemsFragment) as ItemsFragment

        scrim.setOnClickListener { if (scrim.alpha == 1f) onBackPressed() }
        fab.setOnClickListener {
            // onFabClick()


            // Send the user to buy the premium version of the app.
            // We will be notified of completion via the listener parameter.
            /* TODO: for security, generate your payload here for verification.
             *  Since this is a SAMPLE, we just use a random string, but on a production app
             *  you should carefully generate this. */
            iabHelper.flagEndAsync() // To prevent error when previous purchases abandoned
            iabHelper.launchPurchaseFlow(
                    this, SKU_PREMIUM, RC_REQUEST,
                    IabHelper.OnIabPurchaseFinishedListener { result, info ->
                        Log.d(TAG, "Purchase finished. Result: $result")
                    },
                    "payload-string"
            )


        }
        setupEmptyListListener()
    }

    private fun setupEmptyListListener() {
        Handler().postDelayed({
            if (!::addMenuItem.isInitialized) return@postDelayed
            viewModel.allItems.observe(this@MainActivity, Observer { items ->
                if (items.isEmpty()) {
                    if (viewModel.state != IDLE) shiftToIdleState() // "if" is required
                    addMenuItem.setIcon(R.drawable.avd_add_glow)
                    animateIconInfinitely(addMenuItem.icon)
                } else addMenuItem.setIcon(R.drawable.avd_add_hide)
            })
        }, 3500)
    }

    private fun showIntroIfNeeded() {
        if (prefs.getBoolean(PREF_NEWBIE, true))
            startActivity(Intent(this, IntroActivity::class.java))
    }

    private fun restoreBottomDrawerIfNeeded() {
        if (prefs.getBoolean(PREF_TASK_RECREATED, false)) {
            BottomDrawerFragment().show(supportFragmentManager, "BOTTOM_SHEET")
            prefs.edit().putBoolean(PREF_TASK_RECREATED, false).apply()
        }
    }

    private fun onLocationFound(location: Location) {
        if (viewModel.state != FINDING) return // because of a bug on app relaunch
        viewModel.location = location
        if (prefs.getBoolean(PREF_VIBRATE, true)) vibrate(this, 200, 255)
        val here = Coordinates(viewModel.location!!)
        viewModel.findNearStores(here).observe(this, Observer { onStoresFound(it) })
    }

    private fun onFabClick() {
        if (viewModel.isAddingItem) {
            val addItemFragment = supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment
            addItemFragment.onDonePressed()
        } else if (viewModel.state == IDLE) { // act as find
            viewModel.isBuyLimitReached.observe(this, Observer { purchaseCount ->
                if (itemsFragment.isListEmpty) itemsFragment.emphasisEmpty()
                else if (purchaseCount >= /*buyLimit*/ 5) /*showUpgradeProDialog()*/
                else findLocation()
            })
        } else if (viewModel.state == SELECTING) { // act as done
            if (itemsFragment.isSelectedEmpty) showSnackbar(snbContainer, R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
            else buySelectedItems()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_home, menu)

        addMenuItem = menu.findItem(R.id.action_add)
        if (itemsFragment.isListEmpty) animateIconInfinitely(addMenuItem.icon) // This is needed
        reorderMenuItem = menu.findItem(R.id.action_reorder_skip)
        storeMenuItem = menu.findItem(R.id.found_stores)
        initializeAddStorePopup(storeMenuItem.actionView)
        storeMenuItem.actionView.setOnClickListener { showAddStorePopup() }

        if (viewModel.state != IDLE || viewModel.isAddingItem) {
            bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            (bottom_bar.navigationIcon as Animatable).start()
            addMenuItem.isVisible = false
        }

        // Because animating views was buggy in onOptionsItemSelected we do it here
        if (viewModel.isAddingItem) {
            reorderMenuItem.isVisible = false
            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
            fab.setImageResource(R.drawable.avd_find_done)
            (fab.drawable as Animatable).start()
            scrim.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(anim: Animator?) = scrim.setVisibility(VISIBLE)
            })
        } else if (!itemsFragment.isListEmpty) addMenuItem.setIcon(R.drawable.avd_add_hide)

        if (viewModel.state == FINDING) {
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)
        } else if (viewModel.state == SELECTING) {
            setStoreMenuItemIcon()
            reorderMenuItem.isVisible = false
            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
        }
        return true
    }

    private fun initializeAddStorePopup(view: View) {
        addStorePopup = PopupMenu(this, view)
        addStorePopup.menuInflater.inflate(R.menu.menu_popup_add_store, addStorePopup.menu)
        addStorePopup.setOnMenuItemClickListener(OnMenuItemClickListener {
            return@OnMenuItemClickListener onOptionsItemSelected(it)
        })
    }

    private fun showAddStorePopup() {
        if (!viewModel.isFindingSkipped && viewModel.foundStores.isNotEmpty()) addStorePopup.show()
    }

    /**
     * If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
     * can be identified by checking if the id of menu item equals android.R.id.home.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                viewModel.isAddingItem = true

                // Note that because AddItemFragment has setHasOptionsMenu(true) every time the
                // fragment manager adds or replaces that fragment, the onCreateOptionsMenu() of
                // this activity is called so we had to animate views in there.
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                        .replace(R.id.fragContainer, AddItemFragment())
                        .addToBackStack("tag")
                        .commit()
            }

            R.id.action_reorder_skip -> {
                if (viewModel.state == FINDING) skipFinding()
                else if (!itemsFragment.isListEmpty) itemsFragment.toggleDragMode()
            }

            R.id.action_add_store -> {
                viewModel.shouldCompletePurchase = false
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            }

            android.R.id.home -> when {
                viewModel.isAddingItem -> {
                    closeAddItemPopup()
                    shiftToIdleState()
                }
                viewModel.state == IDLE -> {
                    BottomDrawerFragment().show(supportFragmentManager, "BOTTOM_SHEET")
                }
                else -> shiftToIdleState()
            }
        }
        return true
    }

    private fun closeAddItemPopup() {
        supportFragmentManager.popBackStack()
        scrim.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) = scrim.setVisibility(GONE)
        })
    }

    /**
     * If you override the onBackPressed() method, we still highly recommend that you invoke
     * super.onBackPressed() from your overridden method. Otherwise the Back button behavior
     * may be jarring to the user.
     */
    override fun onBackPressed() {
        when {
            viewModel.state == FINDING -> stopService(Intent(this, GpsService::class.java))
            viewModel.isAddingItem -> closeAddItemPopup()
            viewModel.state != SELECTING -> super.onBackPressed()
        }
        shiftToIdleState()
    }

    /**
     * [ViewModels][androidx.lifecycle.ViewModel] only survive configuration changes but
     * not process kills. On the other hand, [.onSaveInstanceState] method is called
     * for both configuration changes and process kills. So because we have ViewModel in our app,
     * here this method is used to save data just for the case of **process kills**.
     *
     * This method will NOT be called if the system determines that the current state will not
     * be resumed—for example, if the activity is closed by pressing the back button or if it calls
     * [.finish].
     *
     * Even if the system destroys the process while the activity is stopped, super.onSaveInstanceState();
     * still retains the state of the View objects with an 'android:id' attribute (such as text in
     * an EditText widget) in a Bundle and restores them if the user navigates back to the activity.
     *
     * @param outState
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        /* TODO: Use Saved State module for ViewModel instead.
         *  See [https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate] */

        // There is nothing special in IDLE state to save here; In FINDING state, app runs a
        // FOREGROUND service and is unkillable so this state also doesn't need to save its data

        if (viewModel.state == SELECTING)
            outState.putParcelable(STATE_LOCATION, viewModel.location)
    }

    override fun onRestoreInstanceState(savedState: Bundle) {
        super.onRestoreInstanceState(savedState)

        // NOTE: menu items are restored in onCreateOptionsMenu()

        when {
            viewModel.isAddingItem -> fab.setImageResource(R.drawable.ic_done)
            viewModel.state == FINDING -> {
                fab.setImageResource(R.drawable.avd_finding)
                (fab.drawable as Animatable).start()
            }
            viewModel.state == SELECTING -> {
                fab.setImageResource(R.drawable.ic_done)
                itemsFragment.toggleItemsCheckbox(true)
            }
            savedState.containsKey(STATE_LOCATION) ->
                // TODO: Restore the selecting state
                // Bundle contains location but previous condition (viewModel.getState() == SELECTING)
                // was not true, so this is restore from a PROCESS KILL
                // val location = savedState.getParcelable<Location>(STATE_LOCATION)
                // bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
                showSnackbar(snbContainer, R.string.snackbar_message_start_over, LENGTH_INDEFINITE, android.R.string.ok)
        }
    }

    override fun onRequestPermissionsResult(reqCode: Int, perms: Array<String>, grants: IntArray) {
        if (reqCode == REQUEST_LOCATION_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grants.isNotEmpty() && grants[0] == PERMISSION_GRANTED) findLocation()
        }
        super.onRequestPermissionsResult(reqCode, perms, grants)
    }

    private fun skipFinding() {
        viewModel.isFindingSkipped = true
        viewModel.allStores.observe(this, Observer<List<Store>> { onStoresFound(it) })
    }

    /**
     * Unregistering the broadcast receiver is done in this method instead of onPause() because
     * we want to get the broadcast even if the app went to background and then again resumed.
     *
     * See onCreate javadoc for mor info.
     */
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver)

        iabHelper.dispose()

        // if activity being destroyed because of back button (not because of config change)
        if (isFinishing) stopService(Intent(this, GpsService::class.java))
    }

    private fun findLocation() {
        // Dangerous permissions should be checked EVERY time
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission()
        } else if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance()
            rationaleDialog.show(supportFragmentManager, "LOCATION_OFF_DIALOG")
        } else {
            addMenuItem.setIcon(R.drawable.avd_add_hide).apply { (icon as Animatable).start() }
            // disable effect of tapping on the menu item and also hide its ripple
            Handler().postDelayed({ addMenuItem.isVisible = false }, 300)
            shiftToFindingState()
            val intent = Intent(this, GpsService::class.java)
            ContextCompat.startForegroundService(this, intent) // no need to check api lvl
        }
    }

    private fun onStoresFound(foundStores: List<Store>) {
        viewModel.foundStores = foundStores.toMutableList()
        if (foundStores.isEmpty()) {
            if (viewModel.isFindingSkipped) {
                showSnackbar(snbContainer, R.string.snackbar_message_no_store_found, LENGTH_LONG)
                viewModel.isFindingSkipped = false // Reset the flag
            } else {
                setStoreMenuItemIcon()
                shiftToSelectingState()
            }
        } else {
            stopService(Intent(this, GpsService::class.java)) // for the case if finding skipped
            shiftToSelectingState()
            itemsFragment.sortItemsByCategory(viewModel.foundStores[0].category)
            setStoreMenuItemIcon()
        }
    }

    private fun shiftToFindingState() {
        viewModel.state = FINDING

        fab.setImageResource(R.drawable.avd_buyt)
        (fab.drawable as Animatable).start()

        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (bottom_bar.navigationIcon as Animatable).start()

        reorderMenuItem.setIcon(R.drawable.avd_reorder_skip).setTitle(R.string.menu_hint_skip_finding)
        (reorderMenuItem.icon as Animatable).start()

        // Make sure the bottomAppBar is not hidden and make it not hide on scroll
        // new BottomAppBar.Behavior().slideUp(mBottomAppBar);
    }

    private fun shiftToSelectingState() {
        itemsFragment.toggleItemsCheckbox(true)

        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END

        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()

        viewModel.state = SELECTING
    }

    private fun shiftToIdleState() {
        itemsFragment.sortItemsByOrder()
        itemsFragment.clearSelectedItems()
        if (viewModel.state == FINDING || viewModel.state == SELECTING || viewModel.isAddingItem) {
            itemsFragment.toggleItemsCheckbox(false)

            fab.setImageResource(if (viewModel.state == FINDING) R.drawable.avd_buyt_reverse
            else R.drawable.avd_done_buyt)
            (fab.drawable as Animatable).start()

            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_CENTER
            bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
            (bottom_bar.navigationIcon as Animatable).start()
            storeMenuItem.isVisible = false
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
            (reorderMenuItem.icon as Animatable).start()
            addMenuItem.setIcon(R.drawable.avd_add_show).apply { (icon as Animatable).start() }
                    .also { it.isVisible = true }

            stopService(Intent(this, GpsService::class.java))
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        }
        viewModel.resetFoundStores()
        viewModel.shouldCompletePurchase = false
        viewModel.isFindingSkipped = false
        viewModel.isAddingItem = false
        viewModel.state = IDLE // this should be the last statement (because of the if above)
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a the user will be prompted
     * to grant the permission, otherwise it is requested directly.
     */
    private fun requestLocationPermission() {
        // When the user responds to the app's permission request, the system invokes onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            val rationaleDialog = RationaleDialogFragment.newInstance()
            rationaleDialog.show(supportFragmentManager, "LOCATION_RATIONALE_DIALOG")
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun buySelectedItems() {
        if (itemsFragment.validateSelectedItemsPrice()) {
            if (viewModel.foundStores.size == 0) {
                viewModel.shouldCompletePurchase = true
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            } else if (viewModel.foundStores.size == 1) {
                completeBuy(viewModel.foundStores[0])
            } else { // show store selection dialog
                val selectionList = ArrayList<SelectDialogRow>() // dialog requires ArrayList
                for (store in viewModel.foundStores) {
                    val selection = SelectDialogRow(store.name, store.category.storeImageRes)
                    selectionList.add(selection)
                }
                val selectDialog = SelectDialogFragment
                        .newInstance(this, R.string.dialog_title_select_store, selectionList)
                selectDialog.show(supportFragmentManager, "SELECT_STORE_DIALOG")
                // next this::completeBuy() is called
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Pass on the activity result to the helper for handling
        if (!iabHelper.handleActivityResult(requestCode, resultCode, data)) {
            // Not handled, so handle it ourselves (here's where you'd perform any handling of
            // activity results not related to in-app billing...
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun setStoreMenuItemIcon() {
        with(storeMenuItem.actionView) {
            val visibility = if (viewModel.foundStores.size == 1) GONE else VISIBLE
            this.findViewById<FrameLayout>(R.id.textContainer).visibility = visibility
            this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.getStoreIcon())
            this.findViewById<TextView>(R.id.text).text = viewModel.getStoreTitle()
        }
        storeMenuItem.isVisible = true
    }

    override fun onStoreCreated(store: Store) {
        if (viewModel.shouldCompletePurchase) {
            completeBuy(store)
        } else {
            viewModel.foundStores.add(store)
            setStoreMenuItemIcon()
        }
    }

    /**
     * On store selected from store selection dialog
     *
     * @param index
     */
    override fun onSelected(index: Int) = completeBuy(viewModel.foundStores[index])

    private fun completeBuy(store: Store) {
        // With toList(), a new list is passed to buy() so clearing selected items wont effect it
        viewModel.buy(itemsFragment.selectedItems.toList(), store, Date())
        shiftToIdleState()
    }
}
