package com.pleon.buyt.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.textfield.TextInputLayout
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.ItemSpacingDecoration
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.ItemsAdapter
import com.pleon.buyt.util.animateAlpha
import com.pleon.buyt.util.animateIconInfinitely
import com.pleon.buyt.util.showSnackbar
import com.pleon.buyt.util.showUndoSnackbar
import com.pleon.buyt.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.android.synthetic.main.snackbar_container.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ItemsFragment : BaseFragment(), ItemTouchHelperListener {

    interface ItemListListener {
        fun onItemListChanged(isListEmpty: Boolean)
    }

    private val adapter by inject<ItemsAdapter>()
    private val viewModel by viewModel<MainViewModel>()
    private val touchHelperCallback by inject<TouchHelperCallback> {
        parametersOf(this@ItemsFragment)
    }
    val isSelectedEmpty get() = adapter.selectedItems.isEmpty()
    val selectedItems get() = adapter.selectedItems
    val isListEmpty get() = adapter.currentList.isEmpty()
    private var listener: ItemListListener? = null

    override fun layout() = R.layout.fragment_item_list

    override fun onViewCreated(view: View, savedState: Bundle?) {
        // In fragments use getViewLifecycleOwner() as owner argument

        lifecycleScope.launchWhenStarted {
            viewModel.items.collect { items -> updateItems(items) }
        }

        // OR
        /* lifecycleScope.launch {
           lifecycle.repeatOnLifecycle(STARTED) {
                viewModel.items.collect { items -> updateItems(items) }
            }
        }
        */
        // OR
        // this is shorthand for lifecycleScope.launch() and not lifecycleScope.launchWhenStarted()
        /* viewModel.getItems()
            .onEach { items -> updateItems(items) }
            .launchIn(lifecycleScope)
        */

        animateIconInfinitely(emptyHint.drawable, startDelay = 3000, repeatDelay = 2500)

        val touchHelper = ItemTouchHelper(touchHelperCallback)
        touchHelper.attachToRecyclerView(recyclerView)
        adapter.touchHelper = touchHelper
        val columns = resources.getInteger(R.integer.layout_columns)
        val isRtl = resources.getBoolean(R.bool.isRtl)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, columns)
        recyclerView.addItemDecoration(ItemSpacingDecoration(columns, isRtl))
    }

    private fun updateItems(items: List<Item>) {
        adapter.submitList(items)
        animateAlpha(emptyHint, if (items.isEmpty()) 1f else 0f)
        listener?.onItemListChanged(items.isEmpty())
    }

    // Test this with Espresso: https://stackoverflow.com/a/35331578
    override fun onMoved(oldAdapterPosition: Int, newAdapterPosition: Int) {
        val item1 = adapter.getItem(oldAdapterPosition)
        val item2 = adapter.getItem(newAdapterPosition)
        // Do not allow an urgent item to be replaced be a regular item and vice versa
        val areCompatible = item1.isUrgent == item2.isUrgent
        if (areCompatible) {
            val p1 = item1.position
            val p2 = item2.position
            item2.position = p1
            item1.position = p2
            // Or update the items in onStop (causes bugs if some items are deleted before onStop)
            viewModel.updateItems(adapter.currentList)
        }
    }

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        val item = adapter.getItem(viewHolder.adapterPosition)
        viewModel.flagItemForDeletion(item)
        showUndoSnackbar(snbContainer, getString(R.string.snackbar_message_item_deleted, item.name),
                onUndo = { viewModel.restoreDeletedItem(item) },
                onDismiss = { viewModel.deleteItem(item) }
        )
    }

    override fun onAttach(cxt: Context) {
        super.onAttach(cxt)
        listener = if (cxt is ItemListListener) cxt else null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    // **
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

    fun toggleDragMode() {
        adapter.toggleDragMode()
        touchHelperCallback.toggleDragMode()
    }

    fun clearSelectedItems() = adapter.clearSelectedItems()

    fun toggleItemsCheckbox(isEnabled: Boolean) = adapter.togglePriceInput(isEnabled)

    fun validateSelectedItemsPrice(): Boolean {
        var validated = true
        for (item in adapter.selectedItems) {
            if (item.totalPrice == 0L) {
                val itemIndex = adapter.currentList.indexOf(item) // FIXME: maybe heavy operation
                val itemView = recyclerView.layoutManager!!.findViewByPosition(itemIndex)
                val priceLayout = itemView!!.findViewById<TextInputLayout>(R.id.price_layout)
                priceLayout.error = getString(R.string.input_error_price)
                validated = false
            }
        }
        return validated
    }

    fun sortItemsByCategory(category: Category) {
        val list = adapter.currentList.sortedWith { item1, item2 ->
            when (item1.category) {
                item2.category -> 0
                category -> -1
                else -> +1
            }
        }
        adapter.submitList(list)
        recyclerView.smoothScrollToPosition( 0)
    }

    fun sortItemsByOrder() {
        val list = adapter.currentList.sortedWith { item1, item2 ->
            if (item1.isUrgent != item2.isUrgent) if (item1.isUrgent) -1 else +1
            else item1.position - item2.position
        }
        adapter.submitList(list)
        recyclerView.smoothScrollToPosition( 0)
    }

    fun emphasisEmpty() = showSnackbar(snbContainer, R.string.snackbar_message_list_empty, LENGTH_LONG)
}
