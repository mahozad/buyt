package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.ItemListAdapter
import com.pleon.buyt.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.util.*
import kotlin.Comparator

class ItemsFragment : Fragment(), ItemTouchHelperListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ItemListAdapter
    private lateinit var touchHelperCallback: TouchHelperCallback
    private var itemsReordered = false
    val nextItemPosition get() = adapter.itemCount
    val isSelectedEmpty get() = adapter.selectedItems.isEmpty()
    val selectedItems get() = adapter.selectedItems
    val isCartEmpty get() = adapter.items.isEmpty()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // In fragments use getViewLifecycleOwner() as owner argument
        viewModel.allItems.observe(viewLifecycleOwner, Observer { adapter.items = it.toMutableList() })

        // for swipe-to-delete and drag-n-drop of item
        touchHelperCallback = TouchHelperCallback(this)
        val touchHelper = ItemTouchHelper(touchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)
        adapter = ItemListAdapter(context!!, touchHelper).also { recyclerView.adapter = it }
    }

    override fun onMoved(oldPosition: Int, newPosition: Int) {
        adapter.getItem(oldPosition).position = newPosition
        adapter.getItem(newPosition).position = oldPosition
        Collections.swap(adapter.items, newPosition, oldPosition)
        adapter.notifyItemMoved(oldPosition, newPosition)
        itemsReordered = true
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        // Backup the item for undo purpose
        val item = adapter.getItem(viewHolder.adapterPosition)

        item.isFlaggedForDeletion = true
        viewModel.updateItems(listOf(item))

        showUndoSnackbar(item)
    }

    private fun showUndoSnackbar(item: Item) {
        val snackbar = Snackbar.make(activity!!.snackBarContainer, getString(R.string.snackbar_message_item_deleted, item.name), LENGTH_LONG)
        snackbar.setAction(getString(R.string.snackbar_action_undo)) {
            item.isFlaggedForDeletion = false
            viewModel.updateItems(listOf(item))
        }
        snackbar.addCallback(object : BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) {
                    // If dismiss wasn't because of "UNDO" then
                    // delete the item from database and update order of below items
                    for (i in item.position until adapter.items.size) {
                        adapter.getItem(i).position = adapter.getItem(i).position - 1
                    }
                    viewModel.updateItems(adapter.items)
                    // This should be the last statement because by deleting the item, the observer
                    // is notified and adapter is given the old items with their old positions
                    viewModel.deleteItem(item)
                }
            }
        })
        snackbar.show()
    }

    /**
     * From Android 3.0 Honeycomb on, it is guaranteed that this method is called before
     * the app process is killed. Also the onPause() method should be kept as
     * light as possible so this method is the preferred place to update items.
     */
    override fun onStop() {
        super.onStop()
        if (itemsReordered) {
            viewModel.updateItems(adapter.items)
            itemsReordered = false
        }
    }

    fun toggleEditMode() {
        adapter.toggleEditMode()
        touchHelperCallback.toggleDragMode()
    }

    fun clearSelectedItems() = adapter.clearSelectedItems()

    fun toggleItemsCheckbox(isEnabled: Boolean) = adapter.togglePriceInput(isEnabled)

    fun validateSelectedItemsPrice(): Boolean {
        var validated = true
        for (item in adapter.selectedItems) {
            if (item.totalPrice == 0L) {
                val itemIndex = adapter.items.indexOf(item) // FIXME: maybe heavy operation
                val itemView = recyclerView.layoutManager!!.findViewByPosition(itemIndex)
                val priceLayout = itemView!!.findViewById<TextInputLayout>(R.id.price_layout)
                priceLayout.error = "price cannot be empty"
                validated = false
            }
        }
        return validated
    }

    fun sortItemsByCategory(category: Category) {
        adapter.items.sortWith(Comparator { item1, item2 ->
            when {
                item1.category == item2.category -> 0
                item1.category == category -> -1
                else -> +1
            }
        })
        adapter.notifyDataSetChanged()
    }

    fun sortItemsByOrder() {
        adapter.items.sortWith(Comparator { item1, item2 ->
            if (item1.isUrgent != item2.isUrgent) if (item1.isUrgent) -1 else +1
            else item1.position - item2.position
        })
    }
}
