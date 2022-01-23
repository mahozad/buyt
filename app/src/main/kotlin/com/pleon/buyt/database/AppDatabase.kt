package com.pleon.buyt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pleon.buyt.database.converter.CategoryConverter
import com.pleon.buyt.database.converter.DateConverter
import com.pleon.buyt.database.converter.QuantityUnitConverter
import com.pleon.buyt.database.dao.*
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.model.Subscription

const val DB_NAME = "buyt-database.db"
const val DB_VERSION = 1

@Database(
    entities = [
        Item::class,
        Store::class,
        Purchase::class,
        Subscription::class
    ],
    version = DB_VERSION,
    // Export database schema info to have the generated SQL code and to keep a history of it
    // The location of the exported files can be set in the app build script
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    CategoryConverter::class,
    QuantityUnitConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun storeDao(): StoreDao

    abstract fun purchaseDao(): PurchaseDao

    abstract fun databaseDao(): DatabaseDao

    abstract fun subscriptionDao(): SubscriptionDao
}
