package com.pleon.buyt.ui.state

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.location.LocationManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.pleon.buyt.R
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.isPremium
import com.pleon.buyt.ui.activity.REQUEST_LOCATION_PERMISSION
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment
import com.pleon.buyt.ui.dialog.UpgradePromptDialogFragment
import com.pleon.buyt.ui.fragment.BottomDrawerFragment
import com.pleon.buyt.ui.state.Event.*
import com.pleon.buyt.viewmodel.FREE_BUY_LIMIT
import kotlinx.android.synthetic.main.activity_main.*

object IdleState : UIState {

    lateinit var locationMgr: LocationManager

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> onFabClicked()
            is FindingSkipped -> onSkipClicked()
            is HomeClicked -> onHomeClicked()
            is BackClicked -> onBackClicked()
            is LocationFound, ItemListEmptied -> {}
            is LocationPermissionGranted -> onLocationPermissionGranted()
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is SaveInstanceCalled -> onSaveInstanceCalled()
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    private fun onFabClicked() {
        activity.viewModel.purchaseCountInPeriod.observe(activity, Observer { purchaseCount ->
            if (activity.itemsFragment.isListEmpty) activity.itemsFragment.emphasisEmpty()
            else if (!isPremium && purchaseCount >= FREE_BUY_LIMIT)
                UpgradePromptDialogFragment.newInstance(activity.getText(R.string.dialog_message_free_limit_reached))
                        .show(activity.supportFragmentManager, "UPGRADE_DIALOG")
            else findLocation()
        })
    }

    private fun findLocation() {
        // Dangerous permissions should be checked EVERY time
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
        } else if (!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance(LocationOffDialogFragment.RationalType.LOCATION_OFF)
            rationaleDialog.show(activity.supportFragmentManager, "LOCATION_OFF_DIALOG")
        } else {
            activity.addMenuItem.setIcon(R.drawable.avd_add_hide).apply { (icon as Animatable).start() }
            // disable effect of tapping on the menu item and also hide its ripple
            Handler().postDelayed({ activity.addMenuItem.isVisible = false }, 300)
            shiftToFindingState()
            val intent = Intent(activity, GpsService::class.java)
            ContextCompat.startForegroundService(activity, intent) // no need to check api lvl
        }
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a the user will be prompted
     * to grant the permission, otherwise it is requested directly.
     */
    private fun requestLocationPermission() {
        // When the user responds to the app's permission request, the system invokes onRequestPermissionsResult() method
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance(LocationOffDialogFragment.RationalType.LOCATION_PERMISSION_DENIED)
            rationaleDialog.show(activity.supportFragmentManager, "LOCATION_RATIONALE_DIALOG")
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun shiftToFindingState() {
        activity.viewModel.state = FindingState

        activity.fab.setImageResource(R.drawable.avd_buyt)
        (activity.fab.drawable as Animatable).start()

        activity.bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (activity.bottom_bar.navigationIcon as Animatable).start()

        activity.reorderMenuItem.setIcon(R.drawable.avd_reorder_skip).setTitle(R.string.menu_hint_skip_finding)
        (activity.reorderMenuItem.icon as Animatable).start()

        // Make sure the bottomAppBar is not hidden and make it not hide on scroll
        // new BottomAppBar.Behavior().slideUp(mBottomAppBar);
    }

    private fun onSkipClicked() {
        if (!activity.itemsFragment.isListEmpty) activity.itemsFragment.toggleDragMode()
    }

    private fun onHomeClicked() {
        BottomDrawerFragment().show(activity.supportFragmentManager, "BOTTOM_SHEET")
    }

    private fun onBackClicked() = activity.callSuperOnBackPressed()

    private fun onLocationPermissionGranted() = findLocation()

    private fun onOptionsMenuCreated() {
        if (!activity.itemsFragment.isListEmpty) activity.addMenuItem.setIcon(R.drawable.avd_add_hide)
    }

    //There is nothing special in IDLE state to save here;
    private fun onSaveInstanceCalled() {}

    private fun onRestoreInstanceCalled() {}

    override fun toString() = "IDLE state"
}
