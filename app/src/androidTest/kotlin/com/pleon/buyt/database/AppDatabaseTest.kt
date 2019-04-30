package com.pleon.buyt.database

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Item
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Instrumentation tests execute only on API level 26 (Android 8.0) and above.
 * This is because JUnit 5 requires an environment built on top of Java 8.
 * Therefore these tests are just ignored on older API levels.
 */
class AppDatabaseTest {

    private var database: AppDatabase? = null
    private var itemDao: ItemDao? = null

    @BeforeEach
    fun setUp() {
        // Context of the app under test.
        val context = InstrumentationRegistry.getInstrumentation().context
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        itemDao = database!!.itemDao()
    }

    @Test
    fun addOneItem() {
        val item = Item("Chocolate", Item.Quantity(1, Item.Quantity.Unit.UNIT), Category.GROCERY, isUrgent = false, isBought = false)

        itemDao!!.insert(item)

        val itemCount = itemDao!!.getCount()
        Assert.assertEquals(2, itemCount)
    }

    @AfterEach
    fun tearDown() {
        database!!.close()
    }
}
