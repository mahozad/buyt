package com.pleon.buyt.ui.activity

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.ui.dialog.SelectDialogFragment
import com.pleon.buyt.ui.dialog.SelectionDialogRow
import com.pleon.buyt.ui.fragment.StatesFragment

class StatesActivity : BaseActivity(), SelectDialogFragment.Callback {

    companion object {
        @Suppress("unused")
        private const val TAG = "STATES"
    }

    private lateinit var statesFragment: StatesFragment
    private var filterList: ArrayList<SelectionDialogRow> = ArrayList()
    private var filterMenuItem: MenuItem? = null

    override fun layoutResource() = R.layout.activity_states

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statesFragment = supportFragmentManager.findFragmentById(R.id.statesFragment) as StatesFragment

        val noFilterEntry = SelectionDialogRow(getString(R.string.no_filter), R.drawable.ic_filter)
        filterList.add(noFilterEntry)
        for (category in Category.values()) {
            filterList.add(SelectionDialogRow(category.name, category.imageRes))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_bottom_states, menu)

        filterMenuItem = menu.findItem(R.id.action_filter)
        filterMenuItem!!.setIcon(if (statesFragment.filter == null)
            R.drawable.ic_filter else statesFragment.filter!!.imageRes)

        menu.findItem(R.id.action_toggle_period).setIcon(statesFragment.period.imageRes)
        (menu.getItem(0).icon as Animatable).start() // Animate icon to get its final shape

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
                item.setIcon(statesFragment.period.imageRes)
                (item.icon as Animatable).start()
            }

            android.R.id.home -> finish()
        }
        return true
    }

    override fun onSelected(index: Int) {
        val selection = filterList[index]
        filterMenuItem!!.setIcon(selection.image)
        statesFragment.filter = if (selection.name == getString(R.string.no_filter))
            null else Category.valueOf(selection.name!!)
    }
}
