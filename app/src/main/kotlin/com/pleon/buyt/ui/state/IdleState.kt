package com.pleon.buyt.ui.state

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.LocationManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pleon.buyt.R
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.isPremium
import com.pleon.buyt.ui.activity.REQUEST_LOCATION_PERMISSION
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment
import com.pleon.buyt.ui.dialog.LocationOffDialogFragment.RationalType
import com.pleon.buyt.ui.dialog.UpgradePromptDialogFragment
import com.pleon.buyt.ui.fragment.BottomDrawerFragment
import com.pleon.buyt.util.animateIcon
import com.pleon.buyt.viewmodel.FREE_BUY_LIMIT
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
object IdleState : State(), KoinComponent {

    private val locationMgr: LocationManager by inject()

    override fun onFabClicked(): Unit = with(activity) {
        lifecycleScope.launchWhenStarted {
            val purchaseCount = viewModel.getPurchaseCountInPeriod()
            if (itemsFragment.isListEmpty) {
                itemsFragment.emphasisEmpty()
            } else if (!isPremium && purchaseCount >= FREE_BUY_LIMIT) {
                UpgradePromptDialogFragment
                    .newInstance(activity.getText(R.string.dialog_message_free_limit_reached))
                    .show(supportFragmentManager, "UPGRADE_DIALOG")
            } else {
                findLocation()
            }
        }
    }

    override fun onReorderSkipClicked() {
        if (!activity.itemsFragment.isListEmpty) activity.itemsFragment.toggleDragMode()
    }

    private fun findLocation() {
        // Dangerous permissions should be checked EVERY time
        if (ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestLocationPermission()
        } else if (!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance(RationalType.LOCATION_OFF)
            rationaleDialog.show(activity.supportFragmentManager, "LOCATION_OFF_DIALOG")
        } else {
            activity.addMenuItem.setIcon(R.drawable.avd_add_hide).apply { animateIcon(icon) }
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
            val rationaleDialog = LocationOffDialogFragment.newInstance(RationalType.LOCATION_PERMISSION_DENIED)
            rationaleDialog.show(activity.supportFragmentManager, "LOCATION_RATIONALE_DIALOG")
        } else {
            // Location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(activity, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun shiftToFindingState() = with(activity) {
        viewModel.state = FindingState

        fab.setImageResource(R.drawable.avd_find).apply { animateIcon(fab.drawable) }
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        animateIcon(bottom_bar.navigationIcon!!)
        reorderMenuItem.setIcon(R.drawable.avd_reorder_skip).setTitle(R.string.menu_hint_skip_finding)
        animateIcon(reorderMenuItem.icon)

        // Make sure the bottomAppBar is not hidden and make it not hide on scroll
        // new BottomAppBar.Behavior().slideUp(mBottomAppBar);
    }

    override fun onHomeClicked() {
        BottomDrawerFragment().show(activity.supportFragmentManager, "BOTTOM_SHEET")
    }

    override fun onBackClicked() = activity.callSuperOnBackPressed()

    override fun onItemListChanged(isListEmpty: Boolean) {
        if (isListEmpty) animateAddIcon() else activity.addMenuItem.setIcon(R.drawable.avd_add_hide)
    }

    override fun onLocationPermissionGranted() = findLocation()

    override fun onOptionsMenuCreated() {
        if (activity.itemsFragment.isListEmpty) animateAddIcon()
    }

    override fun toString() = "IDLE state"
}
