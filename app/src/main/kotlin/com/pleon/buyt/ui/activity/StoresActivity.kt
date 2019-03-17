package com.pleon.buyt.ui.activity

import android.view.Menu
import android.view.MenuItem
import com.pleon.buyt.R

class StoresActivity : BaseActivity() {

    override fun layout() = R.layout.activity_stores

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_stores, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                // TODO: add function
            }

            android.R.id.home -> finish()
        }
        return true
    }
}
