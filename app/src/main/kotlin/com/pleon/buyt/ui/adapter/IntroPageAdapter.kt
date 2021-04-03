package com.pleon.buyt.ui.adapter

import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.ui.fragment.IntroFragment1
import com.pleon.buyt.ui.fragment.IntroFragment2
import com.pleon.buyt.ui.fragment.IntroFragment3

class IntroPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = arrayOf(
            // Could have used Fragment(R.layout.fragment_intro_1) instead
            IntroFragment1(),
            IntroFragment2(),
            IntroFragment3()
    )

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]
}
