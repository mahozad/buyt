package com.pleon.buyt.ui.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.tabs.TabLayoutMediator
import com.pleon.buyt.R
import com.pleon.buyt.isPremium
import com.pleon.buyt.ui.adapter.StatsPagerAdapter
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.UpgradePromptDialogFragment
import com.pleon.buyt.util.animateIcon
import com.pleon.buyt.viewmodel.StatsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class StatsActivity : BaseActivity(), SelectDialogFragment.Callback {

    private val adapter by inject<StatsPagerAdapter> { parametersOf(this@StatsActivity) }
    private val viewModel by viewModel<StatsViewModel>()
    private lateinit var filterMenuItem: MenuItem
    private lateinit var periodMenuItemView: TextView

    // Update the stats when date changes (i.e. time changes from 23:59 to 00:00)
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
        registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) getString(R.string.tab_title_charts) else getString(R.string.tab_title_details)
        }.attach()
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
        animateIcon(periodMenuItemView.compoundDrawablesRelative[2])
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> if (isPremium) onFilterMenuItemClick()
            else UpgradePromptDialogFragment.newInstance(getText(R.string.dialog_message_upgrade_to_premium))
                    .show(supportFragmentManager, "UPGRADE_DIALOG")

            R.id.action_toggle_period -> if (isPremium) onTogglePeriodClick()
            else UpgradePromptDialogFragment.newInstance(getText(R.string.dialog_message_upgrade_to_premium))
                    .show(supportFragmentManager, "UPGRADE_DIALOG")

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
