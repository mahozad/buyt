package com.pleon.buyt.ui.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Animatable
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AlphaAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.google.android.material.snackbar.Snackbar.*
import com.pleon.buyt.R
import com.pleon.buyt.database.destroyDatabase
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.service.ACTION_LOCATION_EVENT
import com.pleon.buyt.service.EXTRA_LOCATION
import com.pleon.buyt.service.GpsService
import com.pleon.buyt.ui.dialog.*
import com.pleon.buyt.ui.dialog.Callback
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment.CreateStoreListener
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.ui.fragment.*
import com.pleon.buyt.util.AnimationUtil
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.util.VibrationUtil.vibrate
import com.pleon.buyt.viewmodel.MainViewModel
import com.pleon.buyt.viewmodel.MainViewModel.State.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private const val STATE_LOCATION = "com.pleon.buyt.state.LOCATION"
private const val REQUEST_LOCATION_PERMISSION = 1

/**
 * UI controllers such as activities and fragments are primarily intended to display UI data,
 * react to user actions, or handle operating system communication, such as permission requests.
 */
class MainActivity : BaseActivity(), SelectDialogFragment.Callback, Callback, CreateStoreListener {

    // To force kill the app, go to the desired activity, press home button and then run this command:
    // adb shell am kill com.pleon.buyt
    // return to the app from recent apps screen (or maybe by pressing its launcher icon)

    // FIXME: The bug that sometimes occur when expanding an item (the bottom item jumps up one moment),
    //     is produced when another item was swiped partially
    // FIXME: when dragging items, in some situations item moves from behind of other cards
    //   this happens if the card being dragged over by this card, has itself dragged over this card in the past.
    //   steps to reproduce: drag card1 over card2 and then drop it (you can also drop it to its previous position).
    //   now drag card2 over card1. Then again drag card1 over card2; it moves behind of card2 and in front of other cards.
    //   NOTE: This is caused by "public void clearView..." method in TouchHelperCallback class
    //   see the following to probably fix it:
    //   https://github.com/brianwernick/RecyclerExt/blob/master/library/src/main/java/com/devbrackets/android/recyclerext/adapter/helper/SimpleElevationItemTouchHelperCallback.java

    private lateinit var viewModel: MainViewModel
    private lateinit var prefs: SharedPreferences
    private lateinit var itemsFragment: ItemsFragment
    private lateinit var locationMgr: LocationManager
    private lateinit var locationReceiver: BroadcastReceiver
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
        fab.setOnClickListener { onFabClick() }

        prefs = getDefaultSharedPreferences(this)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        itemsFragment = supportFragmentManager.findFragmentById(R.id.itemsFragment) as ItemsFragment
        locationMgr = getSystemService(LOCATION_SERVICE) as LocationManager
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = onLocationFound(intent)
        }

        val broadcastMgr = LocalBroadcastManager.getInstance(this)
        broadcastMgr.registerReceiver(locationReceiver, IntentFilter(ACTION_LOCATION_EVENT))

        showIntroIfNeeded()
        restoreBottomDrawerIfNeeded()
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

    private fun onLocationFound(intent: Intent) {
        if (prefs.getBoolean(PREF_VIBRATE, true)) vibrate(this, 200, 255)
        viewModel.location = intent.getParcelableExtra(EXTRA_LOCATION)
        val here = Coordinates(viewModel.location!!)
        viewModel.findNearStores(here).observe(this, Observer { onStoresFound(it) })
    }

    private fun onFabClick() {
        if (viewModel.isAddingItem) {
            val addItemFragment = supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment
            addItemFragment.onDonePressed()
        } else if (viewModel.state == IDLE) { // act as find
            if (itemsFragment.isListEmpty)
                showSnackbar(snbContainer, R.string.snackbar_message_cart_empty, LENGTH_SHORT)
            else {
                addMenuItem.setIcon(R.drawable.avd_add_hide).apply { (icon as Animatable).start() }
                // disable effect of tapping on the menu item and its ripple
                Handler().postDelayed({ addMenuItem.isVisible = false }, 300)
                findLocation()
            }
        } else if (viewModel.state == SELECTING) { // act as done
            if (itemsFragment.isSelectedEmpty) showSnackbar(snbContainer, R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
            else buySelectedItems()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_home, menu)
        addMenuItem = menu.findItem(R.id.action_add)
        reorderMenuItem = menu.findItem(R.id.action_reorder)
        storeMenuItem = menu.findItem(R.id.found_stores)
        initializeAddStorePopup(storeMenuItem.actionView)
        storeMenuItem.actionView.setOnClickListener { showAddStorePopup() }

        if (viewModel.state != IDLE || viewModel.isAddingItem) {
            bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            (bottom_bar.navigationIcon as Animatable).start()
            addMenuItem.isVisible = false
            reorderMenuItem.isVisible = false
            // Because setting fab alignment was buggy in onOptionsItemSelected we set it here
            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
            fab.setImageResource(R.drawable.avd_find_done)
            (fab.drawable as Animatable).start()
        }

        if (viewModel.state == FINDING) {
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)
        } else if (viewModel.state == SELECTING) {
            with(storeMenuItem.actionView) {
                this.findViewById<FrameLayout>(R.id.textContainer).visibility = if (viewModel.foundStores.size == 1) GONE else VISIBLE
                this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
                this.findViewById<TextView>(R.id.text).text = viewModel.getStoreTitle()
                storeMenuItem.isVisible = true
            }
            reorderMenuItem.isVisible = false
            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
        }

        // Enable/Disable addMenuItem animation
        viewModel.allItems.observe(this, Observer { items ->
            if (items.isEmpty()) {
                addMenuItem.setIcon(R.drawable.avd_add_glow)
                AnimationUtil.animateIconInfinitely(addMenuItem.icon)
            } else addMenuItem.setIcon(R.drawable.avd_add_hide)
        })

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                viewModel.isAddingItem = true

                // Note that because AddItemFragment has setHasOptionsMenu(true) every time the
                // fragment manager adds or replaces that fragment, the onCreateOptionsMenu() of
                // this activity is called.
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                        .replace(R.id.fragContainer, AddItemFragment())
                        .addToBackStack("tag")
                        .commit()

                val animation = AlphaAnimation(0f, 1f).apply { duration = 300 }
                scrim.alpha = 1f
                scrim.startAnimation(animation)
            }

            R.id.action_reorder -> {
                if (viewModel.state == FINDING) skipFinding()
                else if (!itemsFragment.isListEmpty) itemsFragment.toggleDragMode()
            }

            R.id.action_add_store -> {
                viewModel.shouldCompletePurchase = false
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            }

            /* If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
             * can be identified by checking if the id of menu item equals android.R.id.home. */
            android.R.id.home -> when {
                viewModel.isAddingItem -> {
                    supportFragmentManager.popBackStack()
                    val animation = AlphaAnimation(1f, 0f).apply { duration = 300 }.also { it.fillAfter = true }
                    scrim.startAnimation(animation)
                    shiftToIdleState()
                }
                viewModel.state == IDLE -> {
                    BottomDrawerFragment().show(supportFragmentManager, "BOTTOM_SHEET")
                }
                viewModel.state == FINDING -> { // then it is cancel button
                    stopService(Intent(this, GpsService::class.java))
                    shiftToIdleState()
                }
                else -> shiftToIdleState()
            }
        }
        return true
    }

    /**
     * If you override the onBackPressed() method, we still highly recommend that you invoke
     * super.onBackPressed() from your overridden method. Otherwise the Back button behavior
     * may be jarring to the user.
     */
    override fun onBackPressed() {
        when {
            viewModel.state == FINDING -> stopService(Intent(this, GpsService::class.java))
            viewModel.isAddingItem -> {
                supportFragmentManager.popBackStack()
                val animation = AlphaAnimation(1f, 0f).apply { duration = 300 }.also { it.fillAfter = true }
                scrim.startAnimation(animation)
            }
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

        // TODO: Use Saved State module for ViewModel instead.
        // See [https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate]

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
            else { // if permission denied
                viewModel.shouldAnimateNavIcon = true
                skipFinding()
            }
        }
        super.onRequestPermissionsResult(reqCode, perms, grants)
    }

    override fun onEnableLocationDenied() {
        viewModel.shouldAnimateNavIcon = true
        skipFinding()
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
        // TODO: Move AppDatabase.destroyDatabase() to the onCleared() method of the viewModel
        // see [https://developer.android.com/guide/components/activities/activity-lifecycle#ondestroy]
        if (isFinishing) { // if activity being destroyed because of back button (not because of config change)
            stopService(Intent(this, GpsService::class.java))
            destroyDatabase()
        }
    }

    private fun findLocation() {
        // Dangerous permissions should be checked EVERY time
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission()
        } else if (!locationMgr.isProviderEnabled(GPS_PROVIDER)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance()
            rationaleDialog.show(supportFragmentManager, "LOCATION_OFF_DIALOG")
        } else {
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
                viewModel.storeIcon = R.drawable.ic_store // to use on config change
                viewModel.storeTitle = R.string.menu_text_new_store_found
                with(storeMenuItem.actionView) {
                    this.findViewById<FrameLayout>(R.id.textContainer).visibility = VISIBLE
                    this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
                    this.findViewById<TextView>(R.id.text).text = viewModel.getStoreTitle()
                    storeMenuItem.isVisible = true
                }
                shiftToSelectingState()
            }
        } else {
            stopService(Intent(this, GpsService::class.java)) // for the case if finding skipped
            shiftToSelectingState()
            setStoreMenuItemIcon()
            storeMenuItem.isVisible = true
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

        if (viewModel.shouldAnimateNavIcon) {
            bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            (bottom_bar.navigationIcon as Animatable).start()
        }
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END

        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()

        viewModel.state = SELECTING
    }

    private fun shiftToIdleState() {
        itemsFragment.sortItemsByOrder()
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

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        }
        viewModel.resetFoundStores()
        viewModel.shouldCompletePurchase = false
        viewModel.shouldAnimateNavIcon = false
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

    private fun setStoreMenuItemIcon() {
        if (viewModel.foundStores.size == 1) {
            val icon = viewModel.foundStores[0].category.storeImageRes
            viewModel.storeIcon = icon // to use on config change
            storeMenuItem.actionView.findViewById<FrameLayout>(R.id.textContainer).visibility = GONE
            storeMenuItem.actionView.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
            itemsFragment.sortItemsByCategory(viewModel.foundStores[0].category) // TODO: move this to another method
        } else { // if MORE than one store found
            viewModel.storeIcon = R.drawable.ic_store // to use on config change
            viewModel.storeTitle = 0 // fixme
            with(storeMenuItem.actionView) {
                this.findViewById<FrameLayout>(R.id.textContainer).visibility = VISIBLE
                this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
                this.findViewById<TextView>(R.id.text).text = viewModel.foundStores.size.toString()
            }
        }
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
     * On store selected from selection dialog
     *
     * @param index
     */
    override fun onSelected(index: Int) = completeBuy(viewModel.foundStores[index])

    private fun completeBuy(store: Store) {
        // With toList(), a new list is passed to buy() so clearing selected items wont effect it
        viewModel.buy(itemsFragment.selectedItems.toList(), store, Date())
        itemsFragment.clearSelectedItems()
        shiftToIdleState()
    }
}
