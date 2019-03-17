package com.pleon.buyt.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.pleon.buyt.R
import com.pleon.buyt.ui.activity.HelpActivity
import com.pleon.buyt.ui.activity.SettingsActivity
import com.pleon.buyt.ui.activity.StatesActivity
import com.pleon.buyt.ui.activity.StoresActivity

class BottomDrawerFragment : BottomSheetDialogFragment(), OnNavigationItemSelectedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottomsheet_menu_fragment, container, false)
        val viewById = view.findViewById<NavigationView>(R.id.navigationView)
        viewById.setNavigationItemSelectedListener(this)
        return view
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.states -> startActivity(Intent(context, StatesActivity::class.java))
            R.id.stores -> startActivity(Intent(context, StoresActivity::class.java))
            R.id.settings -> startActivity(Intent(context, SettingsActivity::class.java))
            R.id.help -> startActivity(Intent(context, HelpActivity::class.java))
        }
        return true
    }
}
