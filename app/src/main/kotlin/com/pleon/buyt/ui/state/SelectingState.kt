package com.pleon.buyt.ui.state

import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.activity.STATE_LOCATION
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.state.Event.*
import com.pleon.buyt.util.AnimationUtil
import com.pleon.buyt.util.SnackbarUtil.showSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

object SelectingState : MainScreenState {

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> onFabClicked()
            is HomeClicked, BackClicked -> shiftToIdleState()
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is SaveInstanceCalled -> onSaveInstanceCalled(event.outState)
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            is StoreSelected -> onStoreSelected(event.storeIndex)
            is StoreCreated -> onStoreCreated(event.store)
            is ItemListEmptied -> onListEmptied()
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    private fun onFabClicked() {
        if (activity.itemsFragment.isSelectedEmpty) showSnackbar(activity.snbContainer, R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
        else buySelectedItems()
    }

    private fun buySelectedItems() {
        if (activity.itemsFragment.validateSelectedItemsPrice()) {
            if (activity.viewModel.foundStores.size == 0) {
                activity.viewModel.shouldCompletePurchase = true
                val createStoreDialog = CreateStoreDialogFragment.newInstance(activity.viewModel.location!!)
                createStoreDialog.show(activity.supportFragmentManager, "CREATE_STORE_DIALOG")
            } else if (activity.viewModel.foundStores.size == 1) {
                completeBuy(activity.viewModel.foundStores[0])
            } else { // show store selection dialog
                val selectionList = ArrayList<SelectDialogFragment.SelectDialogRow>() // dialog requires ArrayList
                for (store in activity.viewModel.foundStores) {
                    val selection = SelectDialogFragment.SelectDialogRow(store.name, store.category.storeImageRes)
                    selectionList.add(selection)
                }
                val selectDialog = SelectDialogFragment.newInstance(activity, R.string.dialog_title_select_store, selectionList)
                selectDialog.show(activity.supportFragmentManager, "SELECT_STORE_DIALOG")
                // next this::completeBuy() is called
            }
        }
    }

    private fun completeBuy(store: Store) {
        // With toList(), a new list is passed to buy() so clearing selected items wont effect it
        activity.viewModel.buy(activity.itemsFragment.selectedItems.toList(), store, Date())
        shiftToIdleState()
    }

    private fun shiftToIdleState() {
        activity.itemsFragment.sortItemsByOrder()
        activity.itemsFragment.clearSelectedItems()
        activity.itemsFragment.toggleItemsCheckbox(false)

        activity.fab.setImageResource(R.drawable.avd_done_buyt)
        (activity.fab.drawable as Animatable).start()

        activity.bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
        activity.bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
        (activity.bottom_bar.navigationIcon as Animatable).start()

        activity.storeMenuItem.isVisible = false
        activity.reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
        (activity.reorderMenuItem.icon as Animatable).start()
        activity.addMenuItem.setIcon(R.drawable.avd_add_show).apply { (icon as Animatable).start() }.also { it.isVisible = true }

        (activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

        activity.viewModel.resetFoundStores()

        activity.viewModel.shouldCompletePurchase = false

        activity.viewModel.state = IdleState
    }

    private fun onOptionsMenuCreated() {
        activity.bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (activity.bottom_bar.navigationIcon as Animatable).start()
        activity.addMenuItem.isVisible = false

        setStoreMenuItemIcon()
        activity.reorderMenuItem.isVisible = false
        activity.bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END

        if (!activity.itemsFragment.isListEmpty) activity.addMenuItem.setIcon(R.drawable.avd_add_hide)
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

    private fun onSaveInstanceCalled(outState: Bundle) {
        outState.putParcelable(STATE_LOCATION, activity.viewModel.location)
    }

    private fun onRestoreInstanceCalled() {
        activity.fab.setImageResource(R.drawable.ic_done)
        activity.itemsFragment.toggleItemsCheckbox(true)
    }

    private fun onStoreCreated(store: Store) {
        if (activity.viewModel.shouldCompletePurchase) {
            completeBuy(store)
        } else {
            activity.viewModel.foundStores.add(store)
            setStoreMenuItemIcon()
        }
    }

    private fun onStoreSelected(index: Int) = completeBuy(activity.viewModel.foundStores[index])

    private fun onListEmptied() {
        shiftToIdleState()
        activity.addMenuItem.setIcon(R.drawable.avd_add_glow)
        AnimationUtil.animateIconInfinitely(activity.addMenuItem.icon)
    }

    override fun toString() = "SELECTING state"
}
