package com.pleon.buyt.di

import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.fragment.StoresFragment
import dagger.Module
import dagger.Provides

@Module
class StoresFragmentModule {

    @Provides
    internal fun provideTouchHelperCallback(listener: StoresFragment): TouchHelperCallback {
        return TouchHelperCallback(listener as TouchHelperCallback.ItemTouchHelperListener)
    }
}
