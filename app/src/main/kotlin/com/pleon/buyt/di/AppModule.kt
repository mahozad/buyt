package com.pleon.buyt.di

import android.app.Application
import android.app.NotificationManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.billing.IabHelper
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

    @Provides
    @Singleton
    internal fun provideLocalBroadcastManager(app: Application): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(app)
    }

    @Provides
    @Singleton
    internal fun provideIabHelper(app: Application): IabHelper {
        // FIXME: It is recommended to add more security than just pasting it in your source code;
        val base64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDW/+Cgaba85mg16U2qNlPChs" +
                "7LrqiEnfwZX1odxiY1mO9SPNM2uE8B8kAND9OuXENeYQVLtXISJ9sjdJ2a3WW6ZWGLMUzDKuVSRBSnGM632" +
                "hvWLh9xye/WsFP2Q9zZH2xi5/dbQ/mix1VcdxycWCgHtCJ7lFGfq9yVvJ+ZHoIivIMEWy5NbksQziTgwHK0" +
                "fDh1kIN6qDB8zJIH2ak0kENK6Mk0r75hI6MkPHz8f/sCAwEAAQ=="
        return IabHelper(app, base64EncodedPublicKey)
    }

    @Provides
    internal fun provideArgbEvaluator(): ArgbEvaluatorCompat {
        return ArgbEvaluatorCompat()
    }
}
