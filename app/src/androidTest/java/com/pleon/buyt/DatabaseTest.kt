package com.pleon.buyt


import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.model.Item
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.runner.RunWith

// Instrumented test, which will execute on an Android device.
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private var database: AppDatabase? = null
    private var itemDao: ItemDao? = null

    @Before
    fun setUp() {
        // Context of the app under test.
        val context = InstrumentationRegistry.getInstrumentation().context
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemDao = database!!.itemDao()
    }

    fun addOneItem() {
        val item = Item("Chocolate", "1225", 1, "None")

        itemDao!!.insert(item)

        val itemCount = itemDao!!.getCount()
        assertEquals(1, itemCount)
    }

    @After
    fun tearDown() {
        database!!.close()
    }
}
