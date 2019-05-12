package com.pleon.buyt.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import com.pleon.buyt.R
import com.pleon.buyt.ui.TabLayoutMediator
import com.pleon.buyt.ui.TabLayoutMediator.OnConfigureTabCallback
import com.pleon.buyt.ui.adapter.StatsPagerAdapter
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import java.util.*

class StatsActivity : BaseActivity(), SelectDialogFragment.Callback {

    private lateinit var viewModel: StatsViewModel
    private lateinit var filterMenuItem: MenuItem
    private lateinit var periodMenuItemView: TextView

    // Update the stats when date changes (e.g. time changes from 23:59 to 00:00)
    private var today = Date()
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(cxt: Context, intent: Intent) {
            if (Date().date != today.date) {
                viewModel.triggerUpdate()
                today = Date()
            }
        }
    }

    override fun layout() = R.layout.activity_stats

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        viewModel = ViewModelProviders.of(this).get(StatsViewModel::class.java)

        registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

        val pagerAdapter = StatsPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        // FIXME: Use ViewPager2 native tab layout if introduced in new releases
        TabLayoutMediator(tabLayout, viewPager, object : OnConfigureTabCallback {
            override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                viewPager.setCurrentItem(tab.position, true)
                tab.text = when (position) {
                    0 -> getString(R.string.tab_title_charts)
                    else -> getString(R.string.tab_title_details)
                }
            }
        }).attach()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_stats, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        filterMenuItem.setIcon(viewModel.filter.imgRes)

        // Setting up "change period" menu item because it has custom layout
        val periodMenuItem = menu.findItem(R.id.action_toggle_period)
        periodMenuItemView = periodMenuItem.actionView as TextView
        periodMenuItemView.setOnClickListener { onOptionsItemSelected(periodMenuItem) }
        updatePeriodMenuItemView()

        return true
    }

    private fun updatePeriodMenuItemView() {
        periodMenuItemView.text = getString(R.string.menu_text_period, viewModel.period.length)
        periodMenuItemView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, viewModel.period.imageRes, 0
        )
        (periodMenuItemView.compoundDrawablesRelative[2] as Animatable).start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> onFilterMenuItemClick()
            R.id.action_toggle_period -> onTogglePeriodClick()
            android.R.id.home -> finish()
        }
        return true
    }

    private fun onFilterMenuItemClick() {
        val dialog = SelectDialogFragment.newInstance(this, R.string.dialog_title_select_filter, viewModel.filterList)
        dialog.show(supportFragmentManager, "SELECTION_DIALOG")
    }

    private fun onTogglePeriodClick() {
        viewModel.togglePeriod()
        updatePeriodMenuItemView()
    }

    override fun onSelected(index: Int) {
        viewModel.setFilter(index)
        filterMenuItem.setIcon(viewModel.filter.imgRes)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timeReceiver)
    }
}
