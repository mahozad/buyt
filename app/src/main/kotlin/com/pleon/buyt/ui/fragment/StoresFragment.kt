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
import com.pleon.buyt.ui.adapter.StoresAdapter
import com.pleon.buyt.viewmodel.StoresViewModel
import kotlinx.android.synthetic.main.activity_stores.*
import kotlinx.android.synthetic.main.fragment_store_list.*

/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class StoresFragment : Fragment(R.layout.fragment_store_list), ItemTouchHelperListener {

    private lateinit var viewModel: StoresViewModel
    private lateinit var adapter: StoresAdapter
    private lateinit var sortMenuItemView: TextView

    override fun onViewCreated(view: View, savedState: Bundle?) {
        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(this).get(StoresViewModel::class.java)
        // In fragments use getViewLifecycleOwner() as owner argument
        viewModel.storeDetails.observe(viewLifecycleOwner, Observer { adapter.storeDetails = it })

        // for swipe-to-delete of store
        val touchHelperCallback = TouchHelperCallback(this)
        ItemTouchHelper(touchHelperCallback).attachToRecyclerView(recyclerView)
        adapter = StoresAdapter(context!!).also { recyclerView.adapter = it }
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
        // Backup the store for undo purpose
        val store = adapter.getStore(viewHolder.adapterPosition)

        store.isFlaggedForDeletion = true
        viewModel.updateStores(listOf(store))

        // TODO: Use Anko to show snackbar
        showUndoSnackbar(store)
    }

    // FIXME: Duplicate method
    private fun showUndoSnackbar(store: Store) {
        val snackbar = Snackbar.make(activity!!.snbContainer, getString(R.string.snackbar_message_store_deleted, store.name), LENGTH_LONG)
        snackbar.setAction(getString(R.string.snackbar_action_undo)) {
            store.isFlaggedForDeletion = false
            viewModel.updateStores(listOf(store))
        }
        snackbar.addCallback(object : BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                // If dismiss wasn't because of "UNDO" then delete the store from database
                if (event != DISMISS_EVENT_ACTION) viewModel.deleteStore(store)
            }
        })
        snackbar.show()
    }
}
