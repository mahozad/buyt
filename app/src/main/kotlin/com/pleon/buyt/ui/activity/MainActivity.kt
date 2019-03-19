package com.pleon.buyt.ui.activity

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat.registerAnimationCallback
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTarget.forToolbarMenuItem
import com.getkeepsafe.taptargetview.TapTarget.forView
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.*
import com.pleon.buyt.GpsService
import com.pleon.buyt.R
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.dialog.*
import com.pleon.buyt.ui.dialog.Callback
import com.pleon.buyt.ui.fragment.BottomDrawerFragment
import com.pleon.buyt.ui.fragment.CreateStoreFragment
import com.pleon.buyt.ui.fragment.ItemsFragment
import com.pleon.buyt.viewmodel.MainViewModel
import com.pleon.buyt.viewmodel.MainViewModel.State.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseActivity(), SelectDialogFragment.Callback, ConfirmExitDialog.Callback, Callback {

    // UI controllers such as activities and fragments are primarily intended to display UI data,
    // react to user actions, or handle operating system communication, such as permission requests.

    private val TAG by lazy { MainActivity::class.java.simpleName }

    private lateinit var viewModel: MainViewModel
    private lateinit var itemsFragment: ItemsFragment
    private lateinit var preferences: SharedPreferences
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
     * @param savedState
     */
    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        preferences = getDefaultSharedPreferences(this)

        if (preferences.getBoolean("themeChanged", false)) { // Restore drawer
            BottomDrawerFragment().show(supportFragmentManager, "BOTTOM_SHEET")
            preferences.edit().putBoolean("themeChanged", false).apply()
        }

        if (preferences.getBoolean("NEWBIE", true)) showTutorial()

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        itemsFragment = supportFragmentManager.findFragmentById(R.id.itemsFragment) as ItemsFragment
        locationMgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fab.setOnClickListener { onFabClick() }

        // for fragment example see [https://developer.android.com/guide/components/fragments#Example]
        // As in android developers guild, make this variable a field if needed:
        // boolean wideLayout = findViewById(R.id.chart) != null;
        // if (wideLayout) {
        //      Do whatever needed
        // }

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

        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = onLocationFound(intent)
        }
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationReceiver, IntentFilter(GpsService.ACTION_LOCATION_EVENT))
    }

    private fun showTutorial() {
        bottom_bar.inflateMenu(R.menu.menu_bottom_home) // To ensure menu item is ready
        TapTargetSequence(this).targets(
                forToolbarMenuItem(bottom_bar, R.id.action_add, getString(R.string.tutorial_add_item))
                        .transparentTarget(true).cancelable(false).targetRadius(36),
                forView(fab, getString(R.string.tutorial_tap_buyt))
                        .transparentTarget(true).cancelable(false),
                forToolbarMenuItem(bottom_bar, R.id.action_reorder, getString(R.string.tutorial_skip_finding))
                        .transparentTarget(true).cancelable(false).targetRadius(36)
        ).listener(object : TapTargetSequence.Listener {
            override fun onSequenceStep(lastTarget: TapTarget?, clicked: Boolean) {}
            override fun onSequenceCanceled(lastTarget: TapTarget?) {}
            override fun onSequenceFinish() {
                preferences.edit().putBoolean("NEWBIE", false).apply()
                reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).also {
                    (it.icon as Animatable).start()
                }
                addMenuItem.setIcon(R.drawable.avd_add_glow).also { animateIconInfinitely(it.icon) }
            }
        }).start()
    }

    private fun onLocationFound(intent: Intent) {
        if (preferences.getBoolean("vibrate", true)) {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(150) // FIXME: Deprecated method
        }
        viewModel.location = intent.getParcelableExtra(GpsService.EXTRA_LOCATION)
        val here = Coordinates(viewModel.location as Location)
        viewModel.findNearStores(here).observe(this@MainActivity, Observer { onStoresFound(it) })
    }

    private fun onFabClick() {
        if (viewModel.state == IDLE) { // act as find
            if (itemsFragment.isCartEmpty) showSnackbar(R.string.snackbar_message_cart_empty, LENGTH_SHORT)
            else {
                itemsFragment.clearSelectedItems() // clear items of previous purchase
                findLocation()
            }
        } else if (viewModel.state == SELECTING) { // act as done
            if (itemsFragment.isSelectedEmpty) showSnackbar(R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
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

        if (viewModel.state == FINDING) {
            bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)
        } else if (viewModel.state == SELECTING) {
            bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
            with(storeMenuItem.actionView) {
                this.findViewById<FrameLayout>(R.id.textContainer).visibility = if (viewModel.foundStores.size == 1) GONE else VISIBLE
                this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
                this.findViewById<TextView>(R.id.text).text = viewModel.getStoreTitle()
                storeMenuItem.isVisible = true
            }
            reorderMenuItem.isVisible = false
            bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        }

        // Enable/Disable add menuItem animation
        viewModel.allItems.observe(this, Observer { items ->
            // If user is newbie don't animate icon; the tutorial will animate it at the end
            if (items.isEmpty() && !preferences.getBoolean("NEWBIE", true)) {
                addMenuItem.setIcon(R.drawable.avd_add_glow)
                animateIconInfinitely(addMenuItem.icon)
            } else addMenuItem.setIcon(R.drawable.avd_add_hide)
        })

        // Set icon for the tutorial; At the end of tutorial the icon is corrected
        if (preferences.getBoolean("NEWBIE", true))
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder)

        return true
    }

    private fun initializeAddStorePopup(view: View) {
        addStorePopup = PopupMenu(this, view)
        addStorePopup.menuInflater.inflate(R.menu.menu_popup_add_store, addStorePopup.menu)
        addStorePopup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener {
            onOptionsItemSelected(it)
            return@OnMenuItemClickListener false
        })
    }

    private fun showAddStorePopup() {
        if (!viewModel.isFindingSkipped && viewModel.foundStores.isNotEmpty())
            addStorePopup.show()
    }

    private fun animateIconInfinitely(icon: Drawable) {
        // Make plus icon glow a little bit if the user is a newbie!
        // see this answer [https://stackoverflow.com/a/49431260/8583692] for why we are doing this!
        registerAnimationCallback(icon, object : AnimationCallback() {
            private val handler = Handler(Looper.getMainLooper())
            override fun onAnimationEnd(drawable: Drawable) {
                handler.post { (drawable as Animatable).start() }
            }
        })
        (icon as Animatable).start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                val intent = Intent(this, AddItemActivity::class.java)
                intent.putExtra(EXTRA_ITEM_ORDER, itemsFragment.nextItemPosition)
                startActivity(intent)
            }

            R.id.action_reorder -> {
                if (viewModel.state == IDLE) {
                    if (!itemsFragment.isCartEmpty) itemsFragment.toggleEditMode()
                } else skipFinding()
            }

            R.id.action_add_store -> {
                viewModel.shouldCompletePurchase = false
                val intent = Intent(this, CreateStoreActivity::class.java)
                intent.putExtra(CreateStoreFragment.ARG_LOCATION, viewModel.location)
                startActivityForResult(intent, CREATE_STORE_REQUEST_CODE)
            }

            /* If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
             * can be identified by checking if the id of menu item equals android.R.id.home. */
            android.R.id.home -> when {
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
        if (viewModel.state == FINDING || viewModel.state == SELECTING) {
            ConfirmExitDialog().show(supportFragmentManager, "CONFIRM_EXIT_DIALOG")
        } else super.onBackPressed()
    }

    override fun onExitConfirmed() = super.onBackPressed()

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
                showSnackbar(R.string.snackbar_message_start_over, LENGTH_INDEFINITE, android.R.string.ok)
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
        // TODO: Move AppDatabase.destroyInstance() to the onCleared() method of the viewModel
        // see [https://developer.android.com/guide/components/activities/activity-lifecycle#ondestroy]
        // if activity being destroyed is because of back button (not because of config change)
        if (isFinishing) {
            stopService(Intent(this, GpsService::class.java))
            AppDatabase.destroyInstance()
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
                showSnackbar(R.string.snackbar_message_no_store_found, LENGTH_LONG)
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
            setStoreMenuItemIcon(viewModel.foundStores)
            storeMenuItem.isVisible = true
        }
    }

    private fun showSnackbar(message: Int, length: Int, action: Int? = null) {
        val snackbar = Snackbar.make(snackBarContainer, message, length)
        if (action != null) {
            snackbar.setAction(action) { /* to dismiss snackbar on click */ }
        }
        snackbar.show()
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
        bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END

        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()

        viewModel.state = SELECTING
    }

    private fun shiftToIdleState() {
        itemsFragment.sortItemsByOrder()
        if (viewModel.state == FINDING || viewModel.state == SELECTING) {
            itemsFragment.toggleItemsCheckbox(false)

            fab.setImageResource(if (viewModel.state == FINDING)
                R.drawable.avd_buyt_reverse
            else
                R.drawable.avd_done_buyt)
            (fab.drawable as Animatable).start()

            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_CENTER
            bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
            (bottom_bar.navigationIcon as Animatable).start()
            storeMenuItem.isVisible = false
            reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
            (reorderMenuItem.icon as Animatable).start()
        }
        viewModel.resetFoundStores()
        viewModel.shouldCompletePurchase = false
        viewModel.shouldAnimateNavIcon = false
        viewModel.isFindingSkipped = false
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
                val intent = Intent(this, CreateStoreActivity::class.java)
                intent.putExtra(CreateStoreFragment.ARG_LOCATION, viewModel.location)
                startActivityForResult(intent, CREATE_STORE_REQUEST_CODE)
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

    private fun setStoreMenuItemIcon(stores: List<Store>) {
        if (stores.size == 1) {
            val icon = stores[0].category.storeImageRes
            viewModel.storeIcon = icon // to use on config change
            storeMenuItem.actionView.findViewById<FrameLayout>(R.id.textContainer).visibility = GONE
            storeMenuItem.actionView.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
            itemsFragment.sortItemsByCategory(stores[0].category) // TODO: move this to another method
        } else { // if MORE than one store found
            viewModel.storeIcon = R.drawable.ic_store // to use on config change
            viewModel.storeTitle = 0 // fixme
            with(storeMenuItem.actionView) {
                this.findViewById<FrameLayout>(R.id.textContainer).visibility = VISIBLE
                this.findViewById<ImageView>(R.id.icon).setImageResource(viewModel.storeIcon)
                this.findViewById<TextView>(R.id.text).text = stores.size.toString()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CREATE_STORE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val store = data?.getSerializableExtra("STORE") as Store
            if (viewModel.shouldCompletePurchase) {
                completeBuy(store)
            } else {
                viewModel.foundStores.add(store)
                setStoreMenuItemIcon(viewModel.foundStores)
            }
        }
    }

    /**
     * On store selected from selection dialog
     *
     * @param index
     */
    override fun onSelected(index: Int) = completeBuy(viewModel.foundStores[index])

    private fun completeBuy(store: Store) {
        viewModel.buy(itemsFragment.selectedItems, store, Date())
        shiftToIdleState()
    }

    companion object {

        const val EXTRA_ITEM_ORDER = "com.pleon.buyt.extra.ITEM_ORDER"
        private const val STATE_LOCATION = "com.pleon.buyt.state.LOCATION"
        private const val CREATE_STORE_REQUEST_CODE = 1
        private const val REQUEST_LOCATION_PERMISSION = 1

        // To force kill the app, go to the desired activity, press home button and then run this command:
        // adb shell am kill com.pleon.buyt
        // return to the app from recent apps screen (or maybe by pressing its launcher icon)

        // the app can be described as both a shopping list app and an expense manager app

        // TODO: Make separate free and paid version flavors for the app
        // TODO: application with upgrade to paid option vs two separate free and paid flavors
        // TODO: Limit the max buys in a day in free version to 3

        // TODO: Suggestion: instead of embedding map in the application, the app can use an implicit
        // intent to show map provided by other apps (e.g. google map)
        // see [https://developer.android.com/training/basics/intents/sending]

        // FIXME: The bug that sometimes occur when expanding an item (the bottom item jumps up one moment),
        // is produced when another item was swiped partially

        // TODO: In onDestroy(), onPause() and... do the reverse things you did in onCreate(), onResume() and...
        // TODO: Use kotlin coroutines see[https://medium.com/androiddevelopers/room-coroutines-422b786dc4c5]
        /*
         * DONE: if the bottomAppBar is hidden (by scrolling) and then you expand an Item, the fab jumps up
         * The bug seems to have nothing to do with the expanding animation and persists even without that animation
         */
        // FIXME: Use srcCompat instead of src in layout files
        // FIXME: the bottom shadow (elevation) of item cards is broken. Maybe because of swipe-to-delete background layer
        // TODO: For testing app components see [https://developer.android.com/jetpack/docs/guide#test-components]
        // FIXME: when dragging items, in some situations** item moves from behind of other cards
        // **: this happens if the card being dragged over by this card, has itself dragged over this card in the past.
        // steps to reproduce: drag card1 over card2 and then drop it (you can also drop it to its previous position).
        // now drag card2 over card1. Then again drag card1 over card2; it moves behind of card2 and in front of other cards.
        // NOTE: This is caused by "public void clearView..." method in TouchHelperCallback class
        // see the following to probably fix it:
        // https://github.com/brianwernick/RecyclerExt/blob/master/library/src/main/java/com/devbrackets/android/recyclerext/adapter/helper/SimpleElevationItemTouchHelperCallback.java

        // FIXME: Shift to idle state if the app is in finding state and all items are deleted meanwhile
        // OR disable swipe-to-delete when the state is not in IDLE
        // FIXME: Slide-up bottom bar if it was hidden (because of scroll) and some items were deleted and
        // now cannot scroll to make it slide up again
        // TODO: Use a ViewStub in AddItemFragment layout for the part that is not shown until bought is checked
        // TODO: Redesign the logo in 24 by 24 grid in inkscape to make it crisp (like standard icons)
        // TODO: Add widgets for the app see[https://developer.android.com/guide/topics/appwidgets/overview]
        // TODO: Make icons animation durations consistent
        // TODO: Convert the logo to path (with "path -> stroke to path" option) and then recreate the logo
        // FIXME: Update position field of items if an item is deleted
        // TODO: Add option in settings to enable/disable showing urgent items at top of the list
        // DONE: Add a button (custom view) at the end of SelectionListAdapter to create a new Store
        // DONE: Add a separator (e.g. comma) in every 3 digits of price and other numeric fields
        // TODO: Add option in settings to set the default item quantity in add new item activity (1 seems good)
        // TODO: Reimplement item unit switch button with this approach: https://stackoverflow.com/a/48640424/8583692
        // TODO: Add a functionality to merge another device data to a device (e.g. can merge all family spending data to father's phone)
        // TODO: Add an action in bottomAppBar in Add Item activity to select a date for showing the item in home page
        // TODO: Use DiffUtil class (google it!) instead of calling notifyDataSetChanged() method of adapter
        // TODO: For correct margins of cards, Texts, ... see the page of that component in design section of material.io
        // TODO: disable the reorder items icon in bottomAppBar when number of items is less than 2 (by 'enabled' property of the menu item)
        // TODO: Embed ads in between of regular items
        // TODO: Add an option in settings for the user to be able to add a pinned shortcut to e.g. add item screen
        // see [https://developer.android.com/guide/topics/ui/shortcuts/creating-shortcuts]
        // TODO: Add snap to center for recyclerView items
        // DONE: Convert the main screen layout to ConstraintLayout and animate it (it seems possible with the help of guidelines)
        // TODO: Collapse the chart when scrolling down (with coordinatorLayout)
        // TODO: extract margins and dimensions into xml files
        // DONE: Add feature to select a date to see its costs
        // TODO: for the item list to only one item be expanded see https://stackoverflow.com/q/27203817/8583692
        // TODO: I can request the necessary permissions in the end of the app tutorial
        // FIXME: Correct all names and ids according to best practices
        // FIXME: Fix the query for chart data to start from the beginning of the first day (instead of just -7 days)
        // TODO: Enable the user to disable location rationale dialog and always enter stores manually
        // TODO: What is Spherical Law of Cosines? (for locations)
        // TODO: Add the functionality to export and import all app data
        // TODO: Try to first provide an MVP (minimally viable product) version of the app
        // TODO: Make viewing stores on map a premium feature
        // TODO: Add ability to remove all app data
        // TODO: Add android.support.annotation to the app
        // TODO: For every new version of the app display a what's new page on first app open
        // TODO: Convert all ...left and ...right attributes to ...start and ...end
        // TODO: Show a small progress bar of how much has been spent if user has set a limit on spends
        // TODO: use downloadable fonts instead of integrating the font in the app to reduce the app size
        // TODO: new version of MaterialCardView will include a setCheckedIcon. check it out
        /* TODO: Show a prompt (or an emoji or whatever) when there is no items in the home screen
       to do this, add a new View to the layout and play with its setVisibility as appropriate
    */

        /* TODO: Do you have multiple tables in your database and find yourself copying the same Insert,
     * Update and Delete methods? DAOs support inheritance, so create a BaseDao<T> class, and define
     * your generic @Insert,... there. Have each DAO extend the BaseDao and add methods specific to each of them.
     */
    }
}
