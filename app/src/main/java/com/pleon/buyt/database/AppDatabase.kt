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

// Usually, you only need one instance of the Room database for the whole app
@Database(entities = [Item::class, Store::class, Purchase::class], version = 1)
@TypeConverters(DateConverter::class, QuantityUnitConverter::class, CategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao

    abstract fun purchaseDao(): PurchaseDao

    abstract fun storeDao(): StoreDao

    companion object {

        private const val DATABASE_NAME = "buyt-database.db"

        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase? {
            // TODO: Make this method thread safe. See the previous java version
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                        // .fallbackToDestructiveMigration()
                        .build()
            }
            return instance
        }

        // TODO: call this method when appropriate
        fun destroyInstance() {
            // instance = null
        }
    }
}
