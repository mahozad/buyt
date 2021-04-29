package com.pleon.buyt.ui.state

import android.os.Bundle
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.activity.STATE_LOCATION
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.util.showSnackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.snackbar_container.*
import java.util.*

object SelectingState : State() {

    override fun onBackClicked() = with(activity) {
        super.shiftToIdleState(fabResId = R.drawable.avd_done_find)
        itemsFragment.toggleItemsCheckbox(false)
        itemsFragment.clearSelectedItems()
        itemsFragment.sortItemsByOrder()
    }

    override fun onFabClicked() = with(activity) {
        if (itemsFragment.isSelectedEmpty) showSnackbar(snbContainer, R.string.snackbar_message_no_item_selected, LENGTH_SHORT)
        else buySelectedItems()
    }

    private fun buySelectedItems() = with(activity) {
        if (itemsFragment.validateSelectedItemsPrice()) {
            if (viewModel.foundStores.size == 0) {
                viewModel.shouldCompletePurchase = true
                val probableStoreCategoryIndex = activity.itemsFragment.selectedItems.first().category.ordinal
                val createStoreDialog = CreateStoreDialogFragment.newInstance(viewModel.location!!, probableStoreCategoryIndex)
                createStoreDialog.show(supportFragmentManager, "CREATE_STORE_DIALOG")
            } else if (viewModel.foundStores.size == 1) {
                completeBuy(viewModel.foundStores.first())
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

    override fun onStoreMenuItemClicked() = with(activity) {
        if (!viewModel.isFindingSkipped && viewModel.foundStores.isNotEmpty()) addStorePopupMenu.show()
    }

    private fun completeBuy(store: Store) {
        // With toList(), a new list is passed to buy() so clearing selected items wont effect it
        activity.viewModel.buy(activity.itemsFragment.selectedItems.toList(), store, Date())
        onBackClicked()
    }

    override fun onOptionsMenuCreated() = with(activity) {
        super.onOptionsMenuCreated()
        setStoreMenuItemIcon()
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
    }

    override fun onSaveInstance(outState: Bundle) {
        outState.putParcelable(STATE_LOCATION, activity.viewModel.location)
    }

    override fun onRestoreInstance(savedState: Bundle) = with(activity) {
        window.setSoftInputMode(SOFT_INPUT_ADJUST_NOTHING) // required
        fab.setImageResource(R.drawable.ic_done)
        itemsFragment.toggleItemsCheckbox(true)
    }

    override fun onStoreCreated(store: Store) {
        if (activity.viewModel.shouldCompletePurchase) {
            completeBuy(store)
        } else {
            activity.viewModel.foundStores.add(store)
            setStoreMenuItemIcon()
        }
    }

    override fun onStoreSelected(storeIndex: Int) = completeBuy(activity.viewModel.foundStores[storeIndex])

    override fun onItemListChanged(isListEmpty: Boolean) {
        if (isListEmpty) {
            onBackClicked()
            animateAddIcon()
        }
    }

    override fun toString() = "SELECTING state"
}
