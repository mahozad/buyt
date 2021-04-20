package com.pleon.buyt.ui.state

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.pleon.buyt.R
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.fragment.PREF_VIBRATE
import com.pleon.buyt.util.animateIcon
import com.pleon.buyt.util.showSnackbar
import com.pleon.buyt.util.vibrate
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.snackbar_container.*

object FindingState : State() {

    override fun onReorderSkipClicked() = super.skipFinding()

    override fun onBackClicked() {
        super.shiftToIdleState(fabResId = R.drawable.avd_find_reverse)
        activity.stopService(Intent(activity, GpsService::class.java))
    }

    override fun onLocationFound(location: Location) = with(activity) {
        viewModel.location = location
        if (prefs.getBoolean(PREF_VIBRATE, true)) vibrate(this, 200, 255)
        val here = Coordinates(viewModel.location!!)
        viewModel.findNearStores(here).observe(this) { onStoresFound(it) }
    }

    private fun shiftToSelectingState() = with(activity) {
        window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING) // required
        itemsFragment.toggleItemsCheckbox(true)

        if (viewModel.shouldAnimateNavIcon) {
            bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            animateIcon(bottom_bar.navigationIcon!!)
        }

        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END

        fab.setImageResource(R.drawable.avd_find_done)
        animateIcon(fab.drawable)

        viewModel.state = SelectingState
    }

    override fun onStoresFound(stores: List<Store>) = with(activity) {
        viewModel.foundStores = stores.toMutableList()

        if (stores.isEmpty()) {
            if (viewModel.isFindingSkipped) {
                showSnackbar(snbContainer, R.string.snackbar_message_no_store_found, LENGTH_LONG)
                viewModel.isFindingSkipped = false // Reset the flag
            } else {
                setStoreMenuItemIcon()
                shiftToSelectingState()
            }
        } else {
            if (addMenuItem.isVisible) addMenuItem.isVisible = false
            stopService(Intent(this, GpsService::class.java)) // for the case if finding skipped
            shiftToSelectingState()
            itemsFragment.sortItemsByCategory(viewModel.foundStores.first().category)
            setStoreMenuItemIcon()
        }
    }

    override fun onOptionsMenuCreated() {
        super.onOptionsMenuCreated()
        activity.reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)
    }

    // In FINDING state, app runs a FOREGROUND service and is unkillable
    // so this state also doesn't need to save its data
    override fun onSaveInstance(outState: Bundle) {}

    override fun onRestoreInstance(savedState: Bundle) = with(activity) {
        fab.setImageResource(R.drawable.avd_find)
        animateIcon(fab.drawable)
    }

    override fun onItemListChanged(isListEmpty: Boolean) {
        if (isListEmpty) {
            onBackClicked()
            animateAddIcon()
        }
    }

    override fun toString() = "FINDING state"

}
