package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.pleon.buyt.R

class SettingsActivity : BaseActivity() {

    override fun layout() = R.layout.activity_settings

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                (item.icon as Animatable).start()
                val preferences = PreferenceManager.getDefaultSharedPreferences(this)
                preferences.edit().clear().apply()
                PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
            }
            android.R.id.home -> finish()
        }
        return true
    }
}
