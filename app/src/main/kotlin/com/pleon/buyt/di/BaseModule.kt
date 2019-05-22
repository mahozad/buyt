package com.pleon.buyt.di

import com.pleon.buyt.service.GpsService
import com.pleon.buyt.ui.activity.*
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.fragment.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class BaseModule {

    //================== Activities ==================\\

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

    //================== Fragments ==================\\

    @ContributesAndroidInjector
    abstract fun bindItemsFrament(): ItemsFragment

    @ContributesAndroidInjector
    abstract fun bindAddItemFrament(): AddItemFragment

    @ContributesAndroidInjector
    abstract fun bindStoresFrament(): StoresFragment

    @ContributesAndroidInjector
    abstract fun bindStatsFrament(): StatsFragment

    @ContributesAndroidInjector
    abstract fun bindStatDetailsFrament(): StatDetailsFragment

    @ContributesAndroidInjector
    abstract fun bindCreateStoreDialogFragment(): CreateStoreDialogFragment

    //================== Services ==================\\

    @ContributesAndroidInjector
    abstract fun bindGpsService(): GpsService
}
