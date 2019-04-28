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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.getCategoryByString
import com.pleon.buyt.ui.adapter.StatsPagerAdapter
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.ui.fragment.StatDetailsFragment
import com.pleon.buyt.ui.fragment.StatsFragment
import com.pleon.buyt.viewmodel.StatisticsViewModel
import kotlinx.android.synthetic.main.activity_stats.*
import java.util.*

class StatsActivity : BaseActivity(), SelectDialogFragment.Callback {

    private lateinit var statsFragment: StatsFragment
    private lateinit var detailsFragment: StatDetailsFragment
    private var filterList = ArrayList<SelectDialogRow>()
    private var filterMenuItem: MenuItem? = null
    private lateinit var periodMenuItemView: TextView
    private lateinit var viewModel: StatisticsViewModel

    // Update the statistics when date changes (e.g. time changes from 23:59 to 00:00)
    private var today = Date()
    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Date().date != today.date) {
                updateStats()
                today = Date()
            }
        }
    }

    override fun layout() = R.layout.activity_stats

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        viewModel = ViewModelProviders.of(this).get(StatisticsViewModel::class.java)
        registerReceiver(timeReceiver, IntentFilter(Intent.ACTION_TIME_TICK))

        initializeFilterList()

        val pagerAdapter = StatsPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        // tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_cart)
        statsFragment = pagerAdapter.instantiateItem(viewPager, 0) as StatsFragment
        detailsFragment = pagerAdapter.instantiateItem(viewPager, 1) as StatDetailsFragment

        updateStats()
    }

    private fun initializeFilterList() {
        filterList.add(SelectDialogRow(getString(R.string.no_filter), R.drawable.ic_filter))
        for (category in Category.values()) {
            filterList.add(SelectDialogRow(getString(category.nameRes), category.imageRes))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_stats, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        filterMenuItem!!.setIcon(if (viewModel.filter == null)
            R.drawable.ic_filter else viewModel.filter!!.imageRes)

        // Setting up "change period" action because it has custom layout
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
            R.id.action_filter -> {
                val dialog = SelectDialogFragment.newInstance(this, R.string.dialog_title_select_filter, filterList)
                dialog.show(supportFragmentManager, "SELECTION_DIALOG")
            }
            R.id.action_toggle_period -> {
                viewModel.togglePeriod()
                updatePeriodMenuItemView()
                updateStats()
            }
            android.R.id.home -> finish()
        }
        return true
    }

    private fun updateStats() {
        viewModel.statistics.observe(this, Observer { stats ->
            statsFragment.showStats(stats)
            detailsFragment.showStats(stats.purchaseDetails)
        })
    }

    override fun onSelected(index: Int) {
        val filter = filterList[index]
        filterMenuItem!!.setIcon(filter.imgRes)
        viewModel.filter = getCategoryByString(filter.name, applicationContext)
        updateStats()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(timeReceiver)
    }
}
