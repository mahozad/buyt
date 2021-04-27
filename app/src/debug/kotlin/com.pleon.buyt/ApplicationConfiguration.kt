package com.pleon.buyt

import android.content.Context
import com.facebook.stetho.Stetho
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication

class ApplicationConfiguration {

    fun setPremiumStatus() {
        isPremium = true
    }

    fun setupStetho(context: Context) {
        Stetho.initializeWithDefaults(context)
    }

    fun setupKoinLogger(koinApplication: KoinApplication) {
        // Use koin android logger
        koinApplication.androidLogger()
    }

    fun setupSubscription() {
        // Do nothing for debug variant
    }
}
