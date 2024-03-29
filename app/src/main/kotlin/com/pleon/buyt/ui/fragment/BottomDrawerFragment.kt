package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.pleon.buyt.R
import com.pleon.buyt.ui.activity.AboutActivity
import com.pleon.buyt.ui.activity.SettingsActivity
import com.pleon.buyt.ui.activity.StatsActivity
import com.pleon.buyt.ui.activity.StoresActivity
import org.jetbrains.anko.startActivity

class BottomDrawerFragment : BottomSheetDialogFragment(), OnNavigationItemSelectedListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedState: Bundle?): View {
        val view = inflater.inflate(R.layout.bottomsheet_menu_fragment, container, false)
        val viewById = view.findViewById<NavigationView>(R.id.navigationView)
        viewById.setNavigationItemSelectedListener(this)
        return view
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val context = requireContext()
        when (menuItem.itemId) {
            R.id.stats -> context.startActivity<StatsActivity>()
            R.id.stores -> context.startActivity<StoresActivity>()
            R.id.settings -> context.startActivity<SettingsActivity>()
            R.id.help -> context.startActivity<AboutActivity>()
        }
        return true
    }
}
