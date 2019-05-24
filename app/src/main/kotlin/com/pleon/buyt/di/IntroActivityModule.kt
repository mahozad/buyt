package com.pleon.buyt.di

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pleon.buyt.ui.activity.IntroActivity
import com.pleon.buyt.ui.adapter.IntroPageAdapter
import dagger.Module
import dagger.Provides

@Module
class IntroActivityModule {

    @Provides
    internal fun provideFragmentStateAdapter(activity: IntroActivity): FragmentStateAdapter {
        return IntroPageAdapter(activity)
    }
}
