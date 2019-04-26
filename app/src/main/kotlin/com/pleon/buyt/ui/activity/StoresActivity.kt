package com.pleon.buyt.ui.activity

import android.view.MenuItem
import com.pleon.buyt.R

class StoresActivity : BaseActivity() {

    override fun layout() = R.layout.activity_stores

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return false
    }
}
