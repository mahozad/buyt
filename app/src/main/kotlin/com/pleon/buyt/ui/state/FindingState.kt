package com.pleon.buyt.ui.state

import android.content.Intent
import android.graphics.drawable.Animatable
import android.location.Location
import androidx.lifecycle.Observer
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.pleon.buyt.R
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.fragment.PREF_VIBRATE
import com.pleon.buyt.ui.state.Event.*
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.util.VibrationUtil.vibrate
import kotlinx.android.synthetic.main.activity_main.*

object FindingState : State() {

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> {}
            is FindingSkipped -> onSkipClicked()
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is SaveInstanceCalled -> onSaveInstanceCalled()
            is HomeClicked, BackClicked -> shiftToIdleState()
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            is StoresFound -> onStoresFound(event.stores)
            is LocationFound -> onLocationFound(event.location)
            is ItemListChanged -> onItemListChanged(event.isListEmpty)
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    private fun onSkipClicked() = with(activity) {
        viewModel.shouldAnimateNavIcon = true
        viewModel.isFindingSkipped = true
        viewModel.allStores.observe(this, Observer<List<Store>> { onStoresFound(it) })
    }

    private fun shiftToIdleState() {
        super.shiftToIdleState(fabResId = R.drawable.avd_buyt_reverse)
        activity.stopService(Intent(activity, GpsService::class.java))
    }

    private fun onLocationFound(location: Location) = with(activity) {
        viewModel.location = location
        if (prefs.getBoolean(PREF_VIBRATE, true)) vibrate(this, 200, 255)
        val here = Coordinates(viewModel.location!!)
        viewModel.findNearStores(here).observe(this, Observer { onStoresFound(it) })
    }

    private fun shiftToSelectingState() = with(activity) {
        itemsFragment.toggleItemsCheckbox(true)

        if (viewModel.shouldAnimateNavIcon) {
            bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            (bottom_bar.navigationIcon as Animatable).start()
        }

        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END

        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()

        viewModel.state = SelectingState
    }

    private fun onStoresFound(stores: List<Store>) = with(activity) {
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
            itemsFragment.sortItemsByCategory(viewModel.foundStores[0].category)
            setStoreMenuItemIcon()
        }
    }

    private fun onOptionsMenuCreated() = with(activity) {
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (bottom_bar.navigationIcon as Animatable).start()
        addMenuItem.isVisible = false
        reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)
    }

    private fun onSaveInstanceCalled() {
        // In FINDING state, app runs a FOREGROUND service and is unkillable
        // so this state also doesn't need to save its data
    }

    private fun onRestoreInstanceCalled() {
        activity.fab.setImageResource(R.drawable.avd_finding)
        (activity.fab.drawable as Animatable).start()
    }

    private fun onItemListChanged(isListEmpty: Boolean) {
        if (isListEmpty) {
            shiftToIdleState()
            animateAddIcon()
        }
    }

    override fun toString() = "FINDING state"

}
