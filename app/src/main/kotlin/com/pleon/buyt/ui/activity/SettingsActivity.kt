package com.pleon.buyt.ui.activity

import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_NEWBIE
import com.pleon.buyt.util.AnimationUtil.animateIcon

class SettingsActivity : BaseActivity() {

    private lateinit var resetMenuItemView: TextView

    override fun layout() = R.layout.activity_settings

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_settings, menu)

        // Setting up "Reset" action because it has custom layout
        val resetMenuItem = menu.findItem(R.id.action_reset)
        resetMenuItemView = resetMenuItem.actionView as TextView
        resetMenuItemView.setOnClickListener { onOptionsItemSelected(resetMenuItem) }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                animateIcon(resetMenuItemView.compoundDrawablesRelative[2])
                resetPreferences()
            }
            android.R.id.home -> finish()
        }
        return true
    }

    private fun resetPreferences() {
        prefs.edit().clear().apply()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true)
        prefs.edit().putBoolean(PREF_NEWBIE, false).apply() // We don't want tutorial again!
    }
}
