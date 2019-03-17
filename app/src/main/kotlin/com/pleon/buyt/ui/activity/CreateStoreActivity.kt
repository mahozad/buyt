package com.pleon.buyt.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.ui.fragment.CreateStoreFragment
import com.pleon.buyt.viewmodel.CreateStoreViewModel
import kotlinx.android.synthetic.main.activity_create_store.*

class CreateStoreActivity : BaseActivity(), CreateStoreFragment.Callback {

    private lateinit var viewModel: CreateStoreViewModel
    private lateinit var createStoreFrag: CreateStoreFragment

    override fun layout() = R.layout.activity_create_store

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        viewModel = ViewModelProviders.of(this).get(CreateStoreViewModel::class.java)
        createStoreFrag = supportFragmentManager.findFragmentById(R.id.fragment_create_store) as CreateStoreFragment
        fab.setOnClickListener { createStoreFrag.onDonePressed() }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true
    }

    /**
     * Calling finish() in this method is safe because insertion of store is run in an
     * {@link android.os.AsyncTask AsyncTask} which is responsible for finishing its job in
     * any case (even if the activity is destroyed).
     *
     * @param store
     */
    override fun onSubmit(store: Store) {
        viewModel.addStore(store).observe(this, Observer {
            setResult(RESULT_OK, Intent().putExtra("STORE", it))
            finish()
        })
    }
}
