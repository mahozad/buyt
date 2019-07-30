package com.pleon.buyt.di

import android.app.NotificationManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.room.Room.databaseBuilder
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.component.LocationReceiver
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.DB_NAME
import com.pleon.buyt.repository.*
import com.pleon.buyt.ui.TouchHelperCallback
import com.pleon.buyt.ui.TouchHelperCallback.ItemTouchHelperListener
import com.pleon.buyt.ui.adapter.*
import com.pleon.buyt.ui.state.IdleState
import com.pleon.buyt.ui.state.State
import com.pleon.buyt.viewmodel.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { ArgbEvaluatorCompat() }
    single { getDefaultSharedPreferences(androidApplication()) }
    single<State> { IdleState }
    single {
        // FIXME: It is recommended to add more security than just pasting it in your source code;
        val base64EncodedPublicKey = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDW/+Cgaba85mg16U2qNlPChs" +
                "7LrqiEnfwZX1odxiY1mO9SPNM2uE8B8kAND9OuXENeYQVLtXISJ9sjdJ2a3WW6ZWGLMUzDKuVSRBSnGM632" +
                "hvWLh9xye/WsFP2Q9zZH2xi5/dbQ/mix1VcdxycWCgHtCJ7lFGfq9yVvJ+ZHoIivIMEWy5NbksQziTgwHK0" +
                "fDh1kIN6qDB8zJIH2ak0kENK6Mk0r75hI6MkPHz8f/sCAwEAAQ=="
        IabHelper(androidApplication(), base64EncodedPublicKey)
    }
}

val uiModule = module {
    factory { ItemsAdapter(androidApplication()) }
    factory { PurchaseDetailAdapter(androidApplication()) }
    factory { (fragment: Fragment) -> StoresAdapter(fragment, get()) }
    factory { (activity: FragmentActivity) -> IntroPageAdapter(activity) }
    factory { (activity: FragmentActivity) -> StatsPagerAdapter(activity) }
    factory { (listener: ItemTouchHelperListener) -> TouchHelperCallback(androidContext(), listener) }
}

val repositoryModule = module {
    single { MainRepository(get(), get(), get()) }
    single { AddItemRepository(get(), get(), get()) }
    single { StatsRepository(get()) }
    single { StoreRepository(get()) }
    single { SubscriptionRepository(get()) }
}

val viewModelModule = module {
    viewModel { MainViewModel(androidApplication(), get(), get(), get()) }
    viewModel { CreateStoreViewModel(androidApplication(), get()) }
    viewModel { AddItemViewModel(androidApplication(), get()) }
    viewModel { StoresViewModel(androidApplication(), get()) }
    viewModel { StatsViewModel(androidApplication(), get()) }
}

val serviceModule = module {
    single { getSystemService(androidContext(), NotificationManager::class.java) as NotificationManager }
    single { getSystemService(androidContext(), LocationManager::class.java) as LocationManager }
    single { LocalBroadcastManager.getInstance(androidContext()) }
    single { LocationReceiver() }
}

val databaseModule = module {
    single { databaseBuilder(androidApplication(), AppDatabase::class.java, DB_NAME).build() }
    single { get<AppDatabase>().itemDao() }
    single { get<AppDatabase>().storeDao() }
    single { get<AppDatabase>().purchaseDao() }
    single { get<AppDatabase>().subscriptionDao() }
}
