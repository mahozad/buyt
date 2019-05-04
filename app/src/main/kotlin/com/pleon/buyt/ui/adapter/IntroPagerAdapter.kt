package com.pleon.buyt.ui.adapter

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.ui.fragment.intro.IntroFragment1

class IntroPagerAdapter(fragMgr: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragMgr, lifecycle) {

    override fun getItemCount() = 3

    override fun getItem(position: Int) = when (position) {
        0 -> IntroFragment1()
        1 -> IntroFragment1()
        else -> IntroFragment1()
    }
}
