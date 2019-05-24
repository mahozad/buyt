package com.pleon.buyt.di

import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.fragment.ItemsFragment
import dagger.Module
import dagger.Provides

@Module
class ItemsFragmentModule {

    @Provides
    internal fun provideTouchHelperCallback(listener: ItemsFragment): TouchHelperCallback {
        return TouchHelperCallback(listener as TouchHelperCallback.ItemTouchHelperListener)
    }
}
