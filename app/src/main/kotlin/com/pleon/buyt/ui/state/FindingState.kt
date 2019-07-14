package com.pleon.buyt.ui.state

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.location.Location
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.pleon.buyt.R
import com.pleon.buyt.component.GpsService
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.fragment.PREF_VIBRATE
import com.pleon.buyt.ui.state.Event.*
import com.pleon.buyt.util.AnimationUtil.animateIconInfinitely
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import com.pleon.buyt.util.VibrationUtil.vibrate
import kotlinx.android.synthetic.main.activity_main.*

object FindingState : UIState {

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> onFabClicked()
            is FindingSkipped -> onSkipClicked()
            is HomeClicked, BackClicked -> shiftToIdleState()
            is LocationFound -> onLocationFound(event.location)
            is StoresFound -> onStoresFound(event.stores)
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is SaveInstanceCalled -> onSaveInstanceCalled()
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            is ItemListEmptied -> onListEmptied()
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    // Do nothing
    private fun onFabClicked() {}

    private fun onSkipClicked() {
        activity.viewModel.shouldAnimateNavIcon = true
        activity.viewModel.isFindingSkipped = true
        activity.viewModel.allStores.observe(activity, Observer<List<Store>> { onStoresFound(it) })
    }

    private fun shiftToIdleState() {
        activity.stopService(Intent(activity, GpsService::class.java))

        activity.fab.setImageResource(R.drawable.avd_buyt_reverse)
        (activity.fab.drawable as Animatable).start()

        activity.bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
        (activity.bottom_bar.navigationIcon as Animatable).start()

        activity.reorderMenuItem.setIcon(R.drawable.avd_skip_reorder)
        (activity.reorderMenuItem.icon as Animatable).start()

        activity.addMenuItem.setIcon(R.drawable.avd_add_show).apply { (icon as Animatable).start() }.also { it.isVisible = true }

        (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

        activity.viewModel.shouldAnimateNavIcon = false // ???
        activity.viewModel.isFindingSkipped = false // ???


        activity.viewModel.state = IdleState
    }

    private fun onLocationFound(location: Location) {
        //  if (activity.viewModel.state != MainViewModel.State.FINDING) return // because of a bug on app relaunch
        activity.viewModel.location = location
        if (activity.prefs.getBoolean(PREF_VIBRATE, true)) vibrate(activity, 200, 255)
        val here = Coordinates(activity.viewModel.location!!)
        activity.viewModel.findNearStores(here).observe(activity, Observer { onStoresFound(it) })
    }

    private fun setStoreMenuItemIcon() {
        with(activity.storeMenuItem.actionView) {
            val visibility = if (activity.viewModel.foundStores.size == 1) android.view.View.GONE else android.view.View.VISIBLE
            this.findViewById<FrameLayout>(R.id.textContainer).visibility = visibility
            this.findViewById<ImageView>(R.id.icon).setImageResource(activity.viewModel.getStoreIcon())
            this.findViewById<TextView>(R.id.text).text = activity.viewModel.getStoreTitle()
        }
        activity.storeMenuItem.isVisible = true
    }

    private fun shiftToSelectingState() {
        activity.itemsFragment.toggleItemsCheckbox(true)

        if (activity.viewModel.shouldAnimateNavIcon) {
            activity.bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
            (activity.bottom_bar.navigationIcon as Animatable).start()
        }

        activity.reorderMenuItem.isVisible = false
        activity.bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END

        activity.fab.setImageResource(R.drawable.avd_find_done)
        (activity.fab.drawable as Animatable).start()

        activity.viewModel.state = SelectingState
    }

    private fun onStoresFound(stores: List<Store>) {
        activity.viewModel.foundStores = stores.toMutableList()

        if (stores.isEmpty()) {
            if (activity.viewModel.isFindingSkipped) {
                showSnackbar(activity.snbContainer, R.string.snackbar_message_no_store_found, LENGTH_LONG)
                activity.viewModel.isFindingSkipped = false // Reset the flag
            } else {
                setStoreMenuItemIcon()
                shiftToSelectingState()
            }
        } else {
            if (activity.addMenuItem.isVisible) activity.addMenuItem.isVisible = false
            activity.stopService(Intent(activity, GpsService::class.java)) // for the case if finding skipped
            shiftToSelectingState()
            activity.itemsFragment.sortItemsByCategory(activity.viewModel.foundStores[0].category)
            setStoreMenuItemIcon()
        }
    }

    private fun onOptionsMenuCreated() {
        activity.bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (activity.bottom_bar.navigationIcon as Animatable).start()
        activity.addMenuItem.isVisible = false
        activity.reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_skip_finding)

        if (!activity.itemsFragment.isListEmpty) activity.addMenuItem.setIcon(R.drawable.avd_add_hide)
    }

    private fun onSaveInstanceCalled() {
        // In FINDING state, app runs a
        // FOREGROUND service and is unkillable so this state also doesn't need to save its data
    }

    private fun onRestoreInstanceCalled() {
        activity.fab.setImageResource(R.drawable.avd_finding)
        (activity.fab.drawable as Animatable).start()
    }

    private fun onListEmptied() {
        shiftToIdleState()
        activity.addMenuItem.setIcon(R.drawable.avd_add_glow)
        animateIconInfinitely(activity.addMenuItem.icon)
    }

    override fun toString() = "FINDING state"

}
