package com.pleon.buyt.database;

import android.content.Context;

import com.pleon.buyt.ItemContent;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = ItemContent.Item.class, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "buyt-database";
    private static AppDatabase INSTANCE;

    public abstract ItemDao itemModel();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            context = context.getApplicationContext();
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, DATABASE_NAME).build();
        }
        return INSTANCE;
    }

    // TODO: call this method when appropriate
    public static void destroyInstance() {
        INSTANCE = null;
    }
}
