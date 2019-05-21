package com.pleon.buyt.di

import android.app.Application
import androidx.room.Room
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.DB_NAME
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(app, AppDatabase::class.java, DB_NAME).build()
    }

    @Provides
    @Singleton
    fun provideItemDao(database: AppDatabase) = database.itemDao()

    @Provides
    @Singleton
    fun providePurchaseDao(database: AppDatabase) = database.purchaseDao()

    @Provides
    @Singleton
    fun provideStoreDao(database: AppDatabase) = database.storeDao()
}
