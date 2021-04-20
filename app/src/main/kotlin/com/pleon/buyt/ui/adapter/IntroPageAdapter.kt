package com.pleon.buyt.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 3

    /**
     * Use reflection to instantiate the appropriate class.
     * Could have also used a simple if-else statement.
     * The classes should be specified in *proguard-rules.pro*.
     */
    override fun createFragment(position: Int) =
            Class.forName("com.pleon.buyt.ui.fragment.IntroFragment${position + 1}")
                    .newInstance() as Fragment

}
