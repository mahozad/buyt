package com.pleon.buyt.ui.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.ui.fragment.StatDetailsFragment
import com.pleon.buyt.ui.fragment.StatsFragment

class StatsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int) = when (position) {
        0 -> StatsFragment()
        else -> StatDetailsFragment()
    }
}
