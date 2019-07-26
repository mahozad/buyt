package com.pleon.buyt.ui.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.pleon.buyt.R
import com.pleon.buyt.component.ACTION_LOCATION_EVENT
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.component.LocationReceiver
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment.CreateStoreListener
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment.LocationEnableListener
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.ui.fragment.AddItemFragment.FullScreen
import com.pleon.buyt.ui.fragment.BottomDrawerFragment
import com.pleon.buyt.ui.fragment.ItemsFragment
import com.pleon.buyt.ui.fragment.ItemsFragment.ItemListListener
import com.pleon.buyt.ui.fragment.PREF_TASK_RECREATED
import com.pleon.buyt.ui.state.AddItemState
import com.pleon.buyt.ui.state.activity
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.dip
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

const val STATE_LOCATION = "com.pleon.buyt.state.LOCATION"
const val REQUEST_LOCATION_PERMISSION = 1

/**
 * UI controllers such as activities and fragments are primarily intended to display UI data,
 * react to user actions, or handle operating system communication, such as permission requests.
 */
class MainActivity : BaseActivity(), SelectDialogFragment.Callback, FullScreen,
        CreateStoreListener, LocationEnableListener, ItemListListener {

    // To force kill the app, go to the desired activity, press home button and then run this command:
    // adb shell am kill com.pleon.buyt
    // return to the app from recent apps screen (or maybe by pressing its launcher icon)

    /* FIXME: The bug that sometimes occur when expanding an item
     *  (the bottom item jumps up one moment), is produced when another item was swiped partially */

    /* FIXME: While store creation dialog is shown, if a config change occurs and then the store
     *  is created, the behaviour is buggy */

    val viewModel: MainViewModel by viewModel()
    private val broadcastMgr: LocalBroadcastManager by inject()
    private val locationReceiver: LocationReceiver by inject()
    lateinit var itemsFragment: ItemsFragment
    lateinit var reorderMenuItem: MenuItem
    lateinit var storeMenuItem: MenuItem
    lateinit var addMenuItem: MenuItem
    lateinit var addStorePopup: PopupMenu

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
        restoreBottomDrawerIfNeeded()

        activity = this
        broadcastMgr.registerReceiver(locationReceiver, IntentFilter(ACTION_LOCATION_EVENT))
        locationReceiver.getLocation().observe(this, Observer { viewModel.state.onLocationFound(it) })
        itemsFragment = supportFragmentManager.findFragmentById(R.id.itemsFragment) as ItemsFragment

        scrim.setOnClickListener { if (scrim.alpha == 1f) onBackPressed() }
        fab.setOnClickListener { viewModel.state.onFabClicked() }
    }

    private fun restoreBottomDrawerIfNeeded() {
        if (prefs.getBoolean(PREF_TASK_RECREATED, false)) {
            BottomDrawerFragment().show(supportFragmentManager, "BOTTOM_SHEET")
            prefs.edit().putBoolean(PREF_TASK_RECREATED, false).apply()
        }
    }

    override fun onItemListChanged(isListEmpty: Boolean) {
        if (::addMenuItem.isInitialized) viewModel.state.onItemListChanged(isListEmpty)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_home, menu)
        addMenuItem = menu.findItem(R.id.action_add)
        storeMenuItem = menu.findItem(R.id.found_stores)
        reorderMenuItem = menu.findItem(R.id.action_reorder_skip)

        initializeAddStorePopup(storeMenuItem.actionView)
        storeMenuItem.actionView.setOnClickListener {
            if (!viewModel.isFindingSkipped && viewModel.foundStores.isNotEmpty()) addStorePopup.show()
        }

        viewModel.state.onOptionsMenuCreated()
        return true
    }

    private fun initializeAddStorePopup(view: View) {
        addStorePopup = PopupMenu(this, view)
        addStorePopup.menuInflater.inflate(R.menu.menu_popup_add_store, addStorePopup.menu)
        addStorePopup.setOnMenuItemClickListener(OnMenuItemClickListener {
            return@OnMenuItemClickListener onOptionsItemSelected(it)
        })
    }

    /**
     * If setSupportActionBar() is used to set up the BottomAppBar, navigation menu item
     * can be identified by checking if the id of menu item equals android.R.id.home.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                viewModel.state = AddItemState

                val layoutParams = fragContainer.layoutParams
                layoutParams.height = dip(263)
                fragContainer.layoutParams = layoutParams

                // Note that because AddItemFragment has setHasOptionsMenu(true) every time the
                // fragment manager adds or replaces that fragment, the onCreateOptionsMenu() of
                // this activity is called so we had to animate views in there.
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                        .replace(R.id.fragContainer, AddItemFragment())
                        .addToBackStack("AddItemFrag")
                        .commit()
            }

            R.id.action_add_store -> {
                viewModel.shouldCompletePurchase = false
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            }

            R.id.action_reorder_skip -> viewModel.state.onFindingSkipped()

            android.R.id.home -> viewModel.state.onHomeClicked()
        }
        return true
    }

    /**
     * If you override the onBackPressed() method, we still highly recommend that you invoke
     * super.onBackPressed() from your overridden method. Otherwise the Back button behavior
     * may be jarring to the user.
     */
    override fun onBackPressed() = viewModel.state.onBackClicked()

    fun callSuperOnBackPressed() = super.onBackPressed()

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
        viewModel.state.onSaveInstance(outState)
        /* TODO: Use Saved State module for ViewModel instead.
         *  See [https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate] */
    }

    // NOTE: menu items are restored in onCreateOptionsMenu()
    override fun onRestoreInstanceState(savedState: Bundle) {
        super.onRestoreInstanceState(savedState)
        viewModel.state.onRestoreInstance(savedState)
        if (savedState.containsKey(STATE_LOCATION)) {
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
            if (grants.isNotEmpty() && grants[0] == PERMISSION_GRANTED) viewModel.state.onLocationPermissionGranted()
        }
        super.onRequestPermissionsResult(reqCode, perms, grants)
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
        // if activity being destroyed because of back button (not because of config change)
        if (isFinishing) {
            stopService(Intent(this, GpsService::class.java))
            // FIXME: This call is necessary but if the app is relaunched, the buy premium button in
            //  help activity throws exception (maybe because of android cashing activities and objects)
            // (application as BuytApplication).disposeIabHelper()
        }
    }

    override fun onEnableLocationDenied() = viewModel.state.onFindingSkipped()

    override fun onStoreCreated(store: Store) = viewModel.state.onStoreCreated(store)

    // On store selected from store selection dialog
    override fun onSelected(index: Int) = viewModel.state.onStoreSelected(index)

    override fun expandToFullScreen(fragmentRootView: View) {
        if (parentView.measuredHeight < dip(600)) {
            (supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment).isScrollLocked = false
            fragmentRootView.setPadding(0, 0, 0, dip(88))
        }

        val anim = ValueAnimator.ofInt(fragContainer.measuredHeight, parentView.measuredHeight)
        anim.setDuration(300).addUpdateListener { valueAnimator ->
            fragContainer.layoutParams.height = valueAnimator.animatedValue as Int
            fragContainer.layoutParams = fragContainer.layoutParams
        }
        anim.start()
    }
}
