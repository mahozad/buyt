package com.pleon.buyt.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.R

class IntroPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = arrayOf(
            Fragment(R.layout.fragment_intro_1),
            Fragment(R.layout.fragment_intro_2),
            Fragment(R.layout.fragment_intro_3)
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}
