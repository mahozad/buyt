package com.pleon.buyt.di

import com.pleon.buyt.component.GpsService
import com.pleon.buyt.ui.activity.*
import com.pleon.buyt.ui.dialog.CreateStoreDialogFragment
import com.pleon.buyt.ui.fragment.*
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
internal abstract class BindingModule {

    //================== Activities ==================\\

    @ContributesAndroidInjector
    abstract fun bindSplashActivity(): SplashActivity

    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [StatsActivityModule::class])
    abstract fun bindStatsActivity(): StatsActivity

    @ContributesAndroidInjector
    abstract fun bindStoresActivity(): StoresActivity

    @ContributesAndroidInjector
    abstract fun bindSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    abstract fun bindHelpActivity(): HelpActivity

    @ContributesAndroidInjector(modules = [IntroActivityModule::class])
    abstract fun bindIntroActivity(): IntroActivity

    //================== Fragments ==================\\

    @ContributesAndroidInjector(modules = [ItemsFragmentModule::class])
    abstract fun bindItemsFrament(): ItemsFragment

    @ContributesAndroidInjector
    abstract fun bindAddItemFrament(): AddItemFragment

    @ContributesAndroidInjector(modules = [StoresFragmentModule::class])
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
