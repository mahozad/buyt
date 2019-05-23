package com.pleon.buyt.di

import android.app.Application
import android.app.NotificationManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    internal fun providePreferences(app: Application) = getDefaultSharedPreferences(app)

    @Provides
    @Singleton
    internal fun provideLocationManager(app: Application): LocationManager {
        return getSystemService(app, LocationManager::class.java) as LocationManager
    }

    @Provides
    @Singleton
    internal fun provideNotificationManager(app: Application): NotificationManager {
        return getSystemService(app, NotificationManager::class.java) as NotificationManager
    }
}
