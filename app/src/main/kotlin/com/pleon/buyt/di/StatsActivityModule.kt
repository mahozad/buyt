package com.pleon.buyt.di

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.ui.activity.StatsActivity
import com.pleon.buyt.ui.adapter.StatsPagerAdapter
import dagger.Module
import dagger.Provides

@Module
class StatsActivityModule {

    @Provides
    internal fun provideFragmentStateAdapter(activity: StatsActivity): FragmentStateAdapter {
        return StatsPagerAdapter(activity)
    }
}
