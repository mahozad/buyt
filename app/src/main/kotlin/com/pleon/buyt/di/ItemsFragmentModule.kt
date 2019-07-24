package com.pleon.buyt.di

import android.app.Application
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.fragment.ItemsFragment
import dagger.Module
import dagger.Provides

@Module
class ItemsFragmentModule {

    @Provides
    internal fun provideTouchHelperCallback(app: Application, listener: ItemsFragment): TouchHelperCallback {
        return TouchHelperCallback(app, listener as ItemTouchHelperListener)
    }
}
