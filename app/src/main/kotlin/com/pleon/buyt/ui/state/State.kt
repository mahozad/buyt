package com.pleon.buyt.ui.state

import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.location.Location
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.activity.MainActivity
import com.pleon.buyt.util.AnimationUtil.animateIcon
import com.pleon.buyt.util.AnimationUtil.animateIconInfinitely
import kotlinx.android.synthetic.main.activity_main.*

lateinit var activity: MainActivity

// State Design Pattern
abstract class State {

    open fun onFabClicked() {}

    open fun onBackClicked() {}

    open fun onReorderSkipClicked() {}

    open fun onLocationPermissionGranted() {}

    open fun onHomeClicked() = onBackClicked()

    open fun onStoresFound(stores: List<Store>) {}

    open fun onStoreCreated(store: Store) {}

    open fun onSaveInstance(outState: Bundle) {}

    open fun onStoreSelected(storeIndex: Int) {}

    open fun onLocationFound(location: Location) {}

    open fun onRestoreInstance(savedState: Bundle) {}

    open fun onItemListChanged(isListEmpty: Boolean) {}

    open fun onStoreMenuItemClicked() {}

    open fun onOptionsMenuCreated() = with(activity) {
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        animateIcon(bottom_bar.navigationIcon!!)
        addMenuItem.isVisible = false
    }

    fun skipFinding() = with(activity) {
        viewModel.shouldAnimateNavIcon = true
        viewModel.isFindingSkipped = true
        viewModel.allStores.observe(this, Observer { FindingState.onStoresFound(it) })
    }

    protected fun shiftToIdleState(fabResId: Int) = with(activity) {
        window.setSoftInputMode(SOFT_INPUT_ADJUST_PAN) // required
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_CENTER
        bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
        animateIcon(bottom_bar.navigationIcon!!)

        storeMenuItem.isVisible = false
        reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
        animateIcon(reorderMenuItem.icon)
        addMenuItem.setIcon(R.drawable.avd_add_show).isVisible = true
        animateIcon(addMenuItem.icon)
        fab.setImageResource(fabResId).also { animateIcon(fab.drawable) }

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

        viewModel.shiftToIdleState()
    }

    protected fun setStoreMenuItemIcon() = with(activity.storeMenuItem.actionView) {
        val visibility = if (activity.viewModel.foundStores.size == 1) GONE else VISIBLE
        findViewById<FrameLayout>(R.id.textContainer).visibility = visibility
        findViewById<ImageView>(R.id.icon).setImageResource(activity.viewModel.getStoreIcon())
        findViewById<TextView>(R.id.text).text = activity.viewModel.getStoreTitle()
        activity.storeMenuItem.isVisible = true
    }

    protected fun animateAddIcon() = with(activity) {
        addMenuItem.setIcon(R.drawable.avd_add_glow)
        animateIconInfinitely(addMenuItem.icon)
    }

}
