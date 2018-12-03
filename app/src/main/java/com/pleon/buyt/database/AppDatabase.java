package com.pleon.buyt.database;

import android.content.Context;

import com.pleon.buyt.ItemContent;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ItemContent.Item.class, Shop.class, Purchase.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "buyt-database";
    private static AppDatabase INSTANCE;

    public abstract ItemDao itemDao();

    public abstract PurchaseDao purchaseDao();

    public abstract ShopDao shopDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            context = context.getApplicationContext();
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).fallbackToDestructiveMigration().build();
        }
        return INSTANCE;
    }

    // TODO: call this method when appropriate
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
