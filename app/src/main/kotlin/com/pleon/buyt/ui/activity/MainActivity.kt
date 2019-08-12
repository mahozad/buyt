package com.pleon.buyt.ui.activity

import android.animation.ValueAnimator
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.PopupMenu.OnMenuItemClickListener
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
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
import com.pleon.buyt.ui.state.IdleState
import com.pleon.buyt.ui.state.activity
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.snackbar_container.*
import org.jetbrains.anko.dimen
import org.jetbrains.anko.dip
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

const val STATE_LOCATION = "com.pleon.buyt.state.LOCATION"
const val REQUEST_LOCATION_PERMISSION = 1

class MainActivity : BaseActivity(), SelectDialogFragment.Callback, FullScreen,
        CreateStoreListener, LocationEnableListener, ItemListListener {

    // To force kill the app, go to the desired activity, press home button and then run this command:
    // adb shell am kill com.pleon.buyt
    // return to the app from recent apps screen (or maybe by pressing its launcher icon)

    /* FIXME: The bug that sometimes occur when expanding an item
     *  (the bottom item jumps up one moment), is produced when another item was swiped partially */

    /* FIXME: While store creation dialog is shown, if a config change occurs and then the store
     *  is created, the behaviour is buggy */

    /* FIXME: There is a bug with toggle bought item: tap add item menu icon; tap on name field
    *   to open the keyboard; tap on toggle full screen; tap on back to close the keyboard;
    *   tap on bought toggle: it doesn't work */

    val viewModel: MainViewModel by viewModel()
    private val broadcastMgr: LocalBroadcastManager by inject()
    private val locationReceiver: LocationReceiver by inject()
    lateinit var itemsFragment: ItemsFragment
    lateinit var reorderMenuItem: MenuItem
    lateinit var storeMenuItem: MenuItem
    lateinit var addMenuItem: MenuItem
    lateinit var addStorePopupMenu: PopupMenu

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
        window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN) // required
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
        initializeAddStorePopupMenu(storeMenuItem.actionView)
        storeMenuItem.actionView.setOnClickListener { viewModel.state.onStoreMenuItemClicked() }
        viewModel.state.onOptionsMenuCreated()
        return true
    }

    private fun initializeAddStorePopupMenu(view: View) {
        addStorePopupMenu = PopupMenu(this, view)
        addStorePopupMenu.menuInflater.inflate(R.menu.menu_popup_add_store, addStorePopupMenu.menu)
        addStorePopupMenu.setOnMenuItemClickListener(OnMenuItemClickListener {
            return@OnMenuItemClickListener onOptionsItemSelected(it)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                viewModel.state = AddItemState
                resetFragContainerHeight()
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
            R.id.action_reorder_skip -> viewModel.state.onReorderSkipClicked()
            android.R.id.home -> viewModel.state.onHomeClicked()
        }
        return true
    }

    private fun resetFragContainerHeight() = fragContainer.layoutParams.apply { height = dimen(R.dimen.frag_container_height) }

    override fun onBackPressed() = viewModel.state.onBackClicked()

    fun callSuperOnBackPressed() = super.onBackPressed()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.state.onSaveInstance(outState)
        // TODO: Use ViewModel SavedState instead.
    }

    // NOTE: menu items are restored in onCreateOptionsMenu()
    override fun onRestoreInstanceState(savedState: Bundle) {
        super.onRestoreInstanceState(savedState)
        viewModel.state.onRestoreInstance(savedState)
        if (savedState.containsKey(STATE_LOCATION) && viewModel.state == IdleState) {
            // Bundle contains location but we are not in SelectingState
            // so this is restore from a PROCESS KILL
            // val location = savedState.getParcelable<Location>(STATE_LOCATION)
            // TODO: Restore the selecting state
            bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_CENTER
            showSnackbar(snbContainer, R.string.snackbar_message_start_over, LENGTH_INDEFINITE, android.R.string.ok)
        }
    }

    override fun onRequestPermissionsResult(reqCode: Int, perms: Array<String>, grants: IntArray) {
        if (reqCode == REQUEST_LOCATION_PERMISSION) {
            // If request is cancelled, the result arrays are empty.
            if (grants.isNotEmpty() && grants.first() == PERMISSION_GRANTED) viewModel.state.onLocationPermissionGranted()
        }
        super.onRequestPermissionsResult(reqCode, perms, grants)
    }

    /**
     * Unregistering the broadcast receiver is done in this method instead of onPause() because
     * we want to get the broadcast even if the app went to background and then again resumed.
     * See [onCreate] javadoc for mor info.
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

    override fun onEnableLocationDenied() = viewModel.state.skipFinding()

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
