package com.pleon.buyt.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.R

class IntroPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int) = when (position) {
        0 -> Fragment(R.layout.fragment_intro_1)
        1 -> Fragment(R.layout.fragment_intro_2)
        else -> Fragment(R.layout.fragment_intro_3)
    }
}
