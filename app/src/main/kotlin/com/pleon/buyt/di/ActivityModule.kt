package com.pleon.buyt.di

import com.pleon.buyt.ui.activity.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun bindStatsActivity(): StatsActivity

    @ContributesAndroidInjector
    abstract fun bindStoresActivity(): StoresActivity

    @ContributesAndroidInjector
    abstract fun bindSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    abstract fun bindHelpActivity(): HelpActivity
}
