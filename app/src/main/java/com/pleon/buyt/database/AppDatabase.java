package com.pleon.buyt.database;

import android.content.Context;

import com.pleon.buyt.database.dao.ItemDao;
import com.pleon.buyt.database.dao.PurchaseDao;
import com.pleon.buyt.database.dao.StoreDao;
import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Store;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

// Usually, you only need one instance of the Room database for the whole app
@Database(entities = {Item.class, Store.class, Purchase.class}, version = 1)
@TypeConverters({DateConverter.class, QuantityUnitConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "buyt-database.db";

    private static volatile AppDatabase instance;

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) { // double-checked locking
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    context = context.getApplicationContext();
                    instance = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
//                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    // TODO: call this method when appropriate
    public static void destroyInstance() {
        instance = null;
    }

    public abstract ItemDao itemDao();

    public abstract PurchaseDao purchaseDao();

    public abstract StoreDao storeDao();
}
