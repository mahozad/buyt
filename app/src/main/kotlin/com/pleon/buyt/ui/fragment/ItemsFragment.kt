package com.pleon.buyt.ui.fragment

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.ItemSpacingDecoration
import com.pleon.buyt.ui.SnackbarUtil.showUndoSnackbar
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.ItemListAdapter
import com.pleon.buyt.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*
import java.util.*
import kotlin.Comparator

class ItemsFragment : Fragment(R.layout.fragment_item_list), ItemTouchHelperListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: ItemListAdapter
    private lateinit var touchHelperCallback: TouchHelperCallback
    val isSelectedEmpty get() = adapter.selectedItems.isEmpty()
    val selectedItems get() = adapter.selectedItems
    val isCartEmpty get() = adapter.items.isEmpty()

    override fun onViewCreated(view: View, savedState: Bundle?) {
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // In fragments use getViewLifecycleOwner() as owner argument
        viewModel.allItems.observe(viewLifecycleOwner, Observer { items ->
            adapter.items = items
            showHideEmptyHintIfNeeded(items)
        })

        // for swipe-to-delete and drag-n-drop of item
        touchHelperCallback = TouchHelperCallback(this)
        val touchHelper = ItemTouchHelper(touchHelperCallback)
        val columns = resources.getInteger(R.integer.layout_columns)
        val isRtl = resources.getBoolean(R.bool.isRtl)
        recyclerView.layoutManager = GridLayoutManager(context, columns)
        recyclerView.addItemDecoration(ItemSpacingDecoration(columns, isRtl))
        touchHelper.attachToRecyclerView(recyclerView)
        adapter = ItemListAdapter(context!!, touchHelper).also { recyclerView.adapter = it }
    }

    private fun showHideEmptyHintIfNeeded(items: List<Item>) {
        if (items.isEmpty()) {
            emptyHint.setBackgroundResource(R.drawable.avd_list_empty)
            (emptyHint.background as Animatable).start()
            emptyHint.setText(R.string.placeholder_empty)
        } else if (emptyHint.text.isNotEmpty()) {
            emptyHint.setBackgroundResource(R.drawable.avd_list_filled)
            (emptyHint.background as Animatable).start()
            emptyHint.text = ""
        }
    }

    override fun onMoved(oldPosition: Int, newPosition: Int) {
        adapter.getItem(oldPosition).position = newPosition
        adapter.getItem(newPosition).position = oldPosition
        Collections.swap(adapter.items, newPosition, oldPosition)
        adapter.notifyItemMoved(oldPosition, newPosition)

        viewModel.updateItems(adapter.items)
        // or update items in onStop (causes bugs if before onStop some items are deleted)
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        val item = adapter.getItem(viewHolder.adapterPosition)
        viewModel.flagItemForDeletion(item)

        showUndoSnackbar(snbContainer, getString(R.string.snackbar_message_item_deleted, item.name),
                onUndo = { viewModel.restoreDeletedItem(item) },
                onDismiss = { viewModel.deleteItem(item) })
    }

    ///**
    // * From Android 3.0 Honeycomb on, it is guaranteed that this method is called before
    // * the app process is killed. Also the onPause() method should be kept as
    // * light as possible so this method is the preferred place to update items.
    // */
    // override fun onStop() {
    //     super.onStop()
    //     if (itemsReordered) {
    //         viewModel.updateItems(adapter.items)
    //         itemsReordered = false
    //     }
    // }

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
        adapter.items = adapter.items.sortedWith(Comparator { item1, item2 ->
            when {
                item1.category == item2.category -> 0
                item1.category == category -> -1
                else -> +1
            }
        })
        adapter.notifyDataSetChanged()
    }

    fun sortItemsByOrder() {
        adapter.items = adapter.items.sortedWith(Comparator { item1, item2 ->
            if (item1.isUrgent != item2.isUrgent) if (item1.isUrgent) -1 else +1
            else item1.position - item2.position
        })
    }
}
