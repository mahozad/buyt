package com.pleon.buyt.di

import android.app.Application
import com.pleon.buyt.BuytApplication
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

/**
 * We mark this interface with the @Component annotation. This is the injector class.
 * And we also define all the modules (if any) that can be injected.
 * Note that we provide AndroidSupportInjectionModule.class
 * here. This class was not created by us.
 * It is an internal class in Dagger 2.10.
 * Provides our activities and fragments with given module.
 */
@Singleton
@Component(modules = [AndroidInjectionModule::class, AppModule::class,
    BaseModule::class, DatabaseModule::class]
)
interface AppComponent {

    /**
     * We will call this builder interface from our custom Application class.
     * This will set our application object to the AppComponent.
     * So inside the AppComponent the application instance is available.
     * So this application instance can be accessed by our modules
     * such as ViewModelModule when needed.
     */
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: BuytApplication)
}
