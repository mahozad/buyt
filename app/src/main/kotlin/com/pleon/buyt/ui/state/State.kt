package com.pleon.buyt.ui.state

import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.drawable.Animatable
import android.location.Location
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.activity.MainActivity
import com.pleon.buyt.util.AnimationUtil.animateIconInfinitely
import kotlinx.android.synthetic.main.activity_main.*

lateinit var activity: MainActivity

// State Design Pattern
abstract class State {

    open fun onFabClicked() {}

    open fun onBackClicked() {}

    open fun onFindingSkipped() {}

    open fun onOptionsMenuCreated() {}

    open fun onLocationPermissionGranted() {}

    open fun onHomeClicked() = onBackClicked()

    open fun onStoresFound(stores: List<Store>) {}

    open fun onStoreCreated(store: Store) {}

    open fun onSaveInstance(outState: Bundle) {}

    open fun onStoreSelected(storeIndex: Int) {}

    open fun onLocationFound(location: Location) {}

    open fun onRestoreInstance(savedState: Bundle) {}

    open fun onItemListChanged(isListEmpty: Boolean) {}

    protected fun shiftToIdleState(@DrawableRes fabResId: Int) = with(activity) {
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_CENTER
        bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
        (bottom_bar.navigationIcon as Animatable).start()

        storeMenuItem.isVisible = false
        reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
        (reorderMenuItem.icon as Animatable).start()
        addMenuItem.setIcon(R.drawable.avd_add_show).isVisible = true
        (addMenuItem.icon as Animatable).start()

        fab.setImageResource(fabResId)
        (fab.drawable as Animatable).start()

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
