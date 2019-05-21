package com.pleon.buyt.di

import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.ui.fragment.ItemsFragment
import com.pleon.buyt.ui.fragment.StoresFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun bindItemsFrament(): ItemsFragment

    @ContributesAndroidInjector
    abstract fun bindAddItemFrament(): AddItemFragment

    @ContributesAndroidInjector
    abstract fun bindStoresFrament(): StoresFragment
}
