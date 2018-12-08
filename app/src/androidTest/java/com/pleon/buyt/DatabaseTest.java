package com.pleon.buyt;

import android.content.Context;

import com.pleon.buyt.database.AppDatabase;
import com.pleon.buyt.database.dao.ItemDao;
import com.pleon.buyt.model.Item;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;


import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.*;

// Instrumented test, which will execute on an Android device.
@RunWith(AndroidJUnit4.class)
public class DatabaseTest {

    private AppDatabase database;
    private ItemDao itemDao;

    @Before
    public void setUp() {
        // Context of the app under test.
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        itemDao = database.itemDao();
    }

    public void addOneItem() {
        Item item = new Item("Chocolate", "1225", 1, "None");

        itemDao.insert(item);

        long itemCount = itemDao.getCount();
        assertEquals(1, itemCount);
    }

    @After
    public void tearDown() {
        database.close();
    }
}
