package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders.of
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pleon.buyt.R
import com.pleon.buyt.ui.ItemSpacingDecoration
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.StoresAdapter
import com.pleon.buyt.util.SnackbarUtil.showUndoSnackbar
import com.pleon.buyt.viewmodel.StoresViewModel
import com.pleon.buyt.viewmodel.ViewModelFactory
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_store_list.*
import javax.inject.Inject

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class StoresFragment : Fragment(R.layout.fragment_store_list), ItemTouchHelperListener {

    @Inject internal lateinit var viewModelFactory: ViewModelFactory<StoresViewModel>
    private lateinit var viewModel: StoresViewModel
    private lateinit var adapter: StoresAdapter
    private lateinit var sortMenuItemView: TextView

    override fun onViewCreated(view: View, savedState: Bundle?) {
        setHasOptionsMenu(true) // for onCreateOptionsMenu() to be called

        AndroidSupportInjection.inject(this)
        viewModel = of(this, viewModelFactory).get(StoresViewModel::class.java)
        viewModel.storeDetails.observe(viewLifecycleOwner, Observer { adapter.storeDetails = it })
        adapter = StoresAdapter(context!!).also { recyclerView.adapter = it }
        val columns = resources.getInteger(R.integer.layout_columns)
        val isRtl = resources.getBoolean(R.bool.isRtl)
        recyclerView.layoutManager = GridLayoutManager(context, columns)
        recyclerView.addItemDecoration(ItemSpacingDecoration(columns, isRtl))

        // for swipe-to-delete of store
        val touchHelperCallback = TouchHelperCallback(this)
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(recyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bottom_stores, menu)

        // Setting up "Sort" menu item because it has custom layout
        val sortMenuItem = menu.findItem(R.id.action_sort)
        sortMenuItemView = sortMenuItem.actionView as TextView
        sortMenuItemView.setOnClickListener { onOptionsItemSelected(sortMenuItem) }
        updateSortMenuItemView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sort) {
            viewModel.toggleSort()
            updateSortMenuItemView()
        }
        return true
    }

    private fun updateSortMenuItemView() {
        sortMenuItemView.text = getString(R.string.menu_text_sort_prefix, getString(viewModel.getSort().nameRes))
        sortMenuItemView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, viewModel.getSort().imgRes, 0
        )
    }

    override fun onMoved(oldPosition: Int, newPosition: Int) = Unit // No action needed

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        val store = adapter.getStore(viewHolder.adapterPosition)
        viewModel.flagStoreForDeletion(store)

        showUndoSnackbar(snbContainer, getString(R.string.snackbar_message_store_deleted, store.name),
                onUndo = { viewModel.restoreDeletedStore(store) },
                onDismiss = { viewModel.deleteStore(store) })
    }
}
