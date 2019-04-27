package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.BaseTransientBottomBar.BaseCallback
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_LONG
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.StoreListAdapter
import com.pleon.buyt.viewmodel.StoreListViewModel
import kotlinx.android.synthetic.main.activity_stores.*
import kotlinx.android.synthetic.main.fragment_store_list.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class StoreListFragment : Fragment(R.layout.fragment_store_list), ItemTouchHelperListener {

    private lateinit var viewModel: StoreListViewModel
    private lateinit var adapter: StoreListAdapter
    private lateinit var sortMenuItemView: TextView

    override fun onViewCreated(view: View, savedState: Bundle?) {
        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(StoreListViewModel::class.java)
        updateStoresList()

        // for swipe-to-delete of store
        val touchHelperCallback = TouchHelperCallback(this)
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(recyclerView)
        adapter = StoreListAdapter(context!!).also { recyclerView.adapter = it }
    }

    /**
     * In fragments use getViewLifecycleOwner() as owner argument
     */
    private fun updateStoresList() {
        viewModel.storeDetails.observe(viewLifecycleOwner, Observer { adapter.stores = it })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_bottom_stores, menu)

        // Setting up "Sort" action because it has custom layout
        val sortMenuItem = menu.findItem(R.id.action_sort)
        sortMenuItemView = sortMenuItem.actionView as TextView
        sortMenuItemView.setOnClickListener { onOptionsItemSelected(sortMenuItem) }
        updateSortMenuItemView()
    }

    private fun updateSortMenuItemView() {
        sortMenuItemView.text = getString(R.string.menu_text_sort_prefix, getString(viewModel.sort.nameRes))
        sortMenuItemView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, viewModel.sort.imgRes, 0
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sort) {
            viewModel.toggleSort()
            updateSortMenuItemView()
            updateStoresList()
        }
        return true
    }

    override fun onMoved(oldPosition: Int, newPosition: Int) = Unit // No action needed

    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        // Backup the store for undo purpose
        val storeDetail = adapter.getStore(viewHolder.adapterPosition)

        storeDetail.store.isFlaggedForDeletion = true
        viewModel.updateStore(storeDetail.store)

        // TODO: Use Anko to show snackbar
        showUndoSnackbar(storeDetail.store)
    }

    private fun showUndoSnackbar(store: Store) { // FIXME: Duplicate method
        val snackbar = Snackbar.make(activity!!.snbContainer, getString(com.pleon.buyt.R.string.snackbar_message_item_deleted, store.name), LENGTH_LONG)
        snackbar.setAction(getString(com.pleon.buyt.R.string.snackbar_action_undo)) {
            store.isFlaggedForDeletion = false
            viewModel.updateStore(store)
        }
        snackbar.addCallback(object : BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event != DISMISS_EVENT_ACTION) { // If dismiss wasn't because of "UNDO"...
                    // ... then delete the store from database
                    viewModel.deleteStore(store)
                }
            }
        })
        snackbar.show()
    }
}
