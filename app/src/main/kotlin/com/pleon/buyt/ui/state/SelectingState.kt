package com.pleon.buyt.ui.state

import android.graphics.drawable.Animatable
import android.os.Bundle
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
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

object SelectingState : State() {

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

    private fun onFabClicked() = with(activity) {
        if (itemsFragment.isSelectedEmpty) showSnackbar(snbContainer, R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
        else buySelectedItems()
    }

    private fun buySelectedItems() = with(activity) {
        if (itemsFragment.validateSelectedItemsPrice()) {
            if (viewModel.foundStores.size == 0) {
                viewModel.shouldCompletePurchase = true
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            } else if (viewModel.foundStores.size == 1) {
                completeBuy(viewModel.foundStores[0])
            } else { // show store selection dialog
                val selectionList = ArrayList<SelectDialogFragment.SelectDialogRow>() // dialog requires ArrayList
                for (store in viewModel.foundStores) {
                    val selection = SelectDialogFragment.SelectDialogRow(store.name, store.category.storeImageRes)
                    selectionList.add(selection)
                }
                val selectDialog = SelectDialogFragment.newInstance(this, R.string.dialog_title_select_store, selectionList)
                selectDialog.show(supportFragmentManager, "SELECT_STORE_DIALOG")
                // next this::completeBuy() is called
            }
        }
    }

    private fun completeBuy(store: Store) {
        // With toList(), a new list is passed to buy() so clearing selected items wont effect it
        activity.viewModel.buy(activity.itemsFragment.selectedItems.toList(), store, Date())
        shiftToIdleState()
    }

    private fun shiftToIdleState() = with(activity) {
        super.shiftToIdleState(fabResId = R.drawable.avd_done_buyt)
        itemsFragment.toggleItemsCheckbox(false)
        itemsFragment.clearSelectedItems()
        itemsFragment.sortItemsByOrder()
    }

    private fun onOptionsMenuCreated() = with(activity) {
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (bottom_bar.navigationIcon as Animatable).start()
        addMenuItem.isVisible = false

        setStoreMenuItemIcon()
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END

        if (!itemsFragment.isListEmpty) addMenuItem.setIcon(R.drawable.avd_add_hide)
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
