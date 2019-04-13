package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.adapter.StatesPagerAdapter
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectDialogRow
import com.pleon.buyt.ui.fragment.StateDetailsFragment
import com.pleon.buyt.ui.fragment.StatesFragment
import kotlinx.android.synthetic.main.activity_states.*

class StatesActivity : BaseActivity(), SelectDialogFragment.Callback {

    private lateinit var statesFragment: StatesFragment
    private lateinit var detailsFragment: StateDetailsFragment
    private var filterList = ArrayList<SelectDialogRow>()
    private var filterMenuItem: MenuItem? = null

    override fun layout() = R.layout.activity_states

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)

        val noFilterEntry = SelectDialogRow(getString(R.string.no_filter), R.drawable.ic_filter)
        filterList.add(noFilterEntry)
        for (category in Category.values()) {
            filterList.add(SelectDialogRow(category.name, category.imageRes))
        }

        val pagerAdapter = StatesPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = pagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        // tabLayout.getTabAt(0)?.setIcon(R.drawable.ic_cart)
        statesFragment = pagerAdapter.instantiateItem(viewPager, 0) as StatesFragment
        detailsFragment = pagerAdapter.instantiateItem(viewPager, 1) as StateDetailsFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_states, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        filterMenuItem!!.setIcon(if (statesFragment.filter == null)
            R.drawable.ic_filter else statesFragment.filter!!.imageRes)

        menu.findItem(R.id.action_toggle_period).setIcon(statesFragment.period.imageRes)
        (menu.getItem(0).icon as Animatable).start() // Animate icon to get its final shape

        // Setting up "change period" action because it has custom layout
        val menuItem = menu.findItem(R.id.action_toggle_period)
        menuItem.actionView.setOnClickListener { onOptionsItemSelected(menuItem) }
        menuItem.actionView.findViewById<TextView>(R.id.view).text =
                getString(R.string.menu_text_period, statesFragment.period.length)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_filter -> {
                val dialog = SelectDialogFragment.newInstance(this, R.string.dialog_title_select_filter, filterList)
                dialog.show(supportFragmentManager, "SELECTION_DIALOG")
            }

            R.id.action_toggle_period -> {
                statesFragment.togglePeriod()
                item.actionView.findViewById<TextView>(R.id.view)
                        .setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, statesFragment.period.imageRes, 0)
                (item.actionView.findViewById<TextView>(R.id.view).compoundDrawablesRelative[2] as Animatable).start()
                item.actionView.findViewById<TextView>(R.id.view).text =
                        getString(R.string.menu_text_period, statesFragment.period.length)
            }

            android.R.id.home -> finish()
        }
        return true
    }

    override fun onSelected(index: Int) {
        val selection = filterList[index]
        filterMenuItem!!.setIcon(selection.image)
        statesFragment.filter = if (selection.name == getString(R.string.no_filter))
            null else Category.valueOf(selection.name)
    }
}
