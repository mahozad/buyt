package com.pleon.buyt.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pleon.buyt.database.converter.CategoryConverter
import com.pleon.buyt.database.converter.DateConverter
import com.pleon.buyt.database.converter.QuantityUnitConverter
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store

@Volatile private var INSTANCE: AppDatabase? = null
private const val DB_NAME = "buyt-database.db"

fun getDatabase(context: Context): AppDatabase {
    return INSTANCE ?: synchronized(AppDatabase::class) {
        INSTANCE = Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, DB_NAME).build()
        return INSTANCE as AppDatabase
    }
}

fun destroyDatabase() {
    INSTANCE = null
}

// Usually, you only need one instance of the Room database for the whole app
@Database(entities = [Item::class, Store::class, Purchase::class], version = 1)
@TypeConverters(DateConverter::class, QuantityUnitConverter::class, CategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    abstract fun storeDao(): StoreDao

    abstract fun purchaseDao(): PurchaseDao
}
