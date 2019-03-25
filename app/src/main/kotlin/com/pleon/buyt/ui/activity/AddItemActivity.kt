package com.pleon.buyt.ui.activity

import android.os.Bundle
import android.view.MenuItem
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.ui.fragment.AddItemFragment
import kotlinx.android.synthetic.main.activity_add_item.*

class AddItemActivity : BaseActivity(), AddItemFragment.Callback {

    override fun layout() = R.layout.activity_add_item

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_add_item)
        fab.setOnClickListener { (fragment as AddItemFragment).onDonePressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return false
    }

    /**
     * Called when the item was added successfully.
     *
     * Calling finish() in this method is safe because insertion of item is run in an
     * [AsyncTask][android.os.AsyncTask] which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param item
     */
    override fun onItemAdded(item: Item, purchased: Boolean) = finish()
}
