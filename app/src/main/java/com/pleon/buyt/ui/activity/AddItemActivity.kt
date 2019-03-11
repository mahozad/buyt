package com.pleon.buyt.ui.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import com.pleon.buyt.R
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.viewmodel.AddItemViewModel
import kotlinx.android.synthetic.main.activity_add_item.*
import java.util.*

class AddItemActivity : BaseActivity(), AddItemFragment.Callback {

    private lateinit var addItemFragment: AddItemFragment
    private lateinit var viewModel: AddItemViewModel

    override fun layoutResource() = R.layout.activity_add_item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AddItemViewModel::class.java)
        addItemFragment = supportFragmentManager.findFragmentById(R.id.fragment_add_item) as AddItemFragment
        fab.setOnClickListener { addItemFragment.onDonePressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    /**
     * Called for adding regular (not purchased) item.
     *
     *
     * Calling finish() in this method is safe because insertion of item is run in an
     * [AsyncTask][android.os.AsyncTask] which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param item
     */
    override fun onSubmit(item: Item) {
        viewModel.addItem(item)
        finish()
    }

    /**
     * Called for adding purchased item.
     *
     *
     * Calling finish() in this method is safe because database operations are run in an
     * [AsyncTask][android.os.AsyncTask] which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param item
     * @param store
     */
    override fun onSubmitPurchasedItem(item: Item, store: Store, purchaseDate: Date) {
        viewModel.addPurchasedItem(item, store, purchaseDate)
        finish()
    }
}
