package com.pleon.buyt.database.dao

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.InstantExecutorExtension
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity
import com.pleon.buyt.model.Item.Quantity.Unit.UNIT
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

/**
 * Instrumentation tests execute only on API level 26 (Android 8.0) and above.
 * This is because JUnit 5 requires an environment built on top of Java 8.
 * Therefore these tests are just ignored on older API levels.
 */
@ExtendWith(MockitoExtension::class, InstantExecutorExtension::class)
class ItemDaoTests {

    private lateinit var cxt: Context
    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao

    @Mock
    private lateinit var observer: Observer<List<Item>>

    @BeforeEach
    internal fun setUp() {
        cxt = InstrumentationRegistry.getInstrumentation().context
        database = Room.inMemoryDatabaseBuilder(cxt, AppDatabase::class.java).build()
        itemDao = database.itemDao()
    }

    @Test
    fun getAll_verifyOnChangedIsCalled() {
        itemDao.getAll().observeForever(observer)

        verify(observer, times(1)).onChanged(Collections.emptyList())
    }

    @Test
    fun addOneItem() {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, false, false)

        itemDao.insert(item)

        val itemCount = itemDao.getCount()
        Assert.assertEquals(1, itemCount)
    }

    @Test
    fun deleteOneItem() {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, false, false)

        itemDao.insert(item).also { item.itemId = it }
        itemDao.delete(item)

        val itemCount = itemDao.getCount()
        Assert.assertEquals(0, itemCount)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }
}
