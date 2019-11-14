package com.pleon.buyt.database.dao

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.InstantExecutorExtension
import com.pleon.buyt.database.blockingObserve
import com.pleon.buyt.database.dto.ItemNameCat
import com.pleon.buyt.model.Category.FRUIT
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity
import com.pleon.buyt.model.Item.Quantity.Unit.GRAM
import com.pleon.buyt.model.Item.Quantity.Unit.UNIT
import org.assertj.core.api.Assertions.assertThat
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
    fun getAll_addOneItem() {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item)

        val list = itemDao.getAll().blockingObserve()

        assertThat(list.size).isEqualTo(1)
    }

    @Test
    fun getAll_addTwoItems() {
        val item1 = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item2 = Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false)

        itemDao.insert(item1)
        itemDao.insert(item2)
        val list = itemDao.getAll().blockingObserve()

        assertThat(list.size).isEqualTo(2)
    }

    @Test
    fun getCount_addOneItem() {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)

        itemDao.insert(item)
        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(1)
    }

    @Test
    fun getCount_addTwoItems() {
        val item1 = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item2 = Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false)

        itemDao.insert(item1)
        itemDao.insert(item2)
        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(2)
    }

    @Test
    fun getItemNameCats_addOneNewNameCat() {
        val item2 = Item("Something", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false)
        itemDao.insert(item2)

        val itemNameCats = itemDao.getItemNameCats().blockingObserve()

        assertThat(itemNameCats[0]).isEqualTo(ItemNameCat("Something", FRUIT))
    }

    @Test
    fun delete_oneItem() {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)

        itemDao.insert(item).also { item.itemId = it }
        itemDao.delete(item)

        assertThat(itemDao.getCount()).isEqualTo(0)
        assertThat(itemDao.getAll().blockingObserve().size).isEqualTo(0)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }
}
