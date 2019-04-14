package com.pleon.buyt.ui.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.StatDetailsFragment
import com.pleon.buyt.ui.fragment.StatsFragment

class StatsPagerAdapter(private val cxt: Context, fragMgr: FragmentManager)
    : FragmentStatePagerAdapter(fragMgr) {

    override fun getCount(): Int = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> StatsFragment()
            else -> StatDetailsFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> cxt.getString(R.string.tab_title_charts) /*+" and Stats"*/
            else -> /*"Purchase "+*/ cxt.getString(R.string.tab_title_details)
        }
    }
}