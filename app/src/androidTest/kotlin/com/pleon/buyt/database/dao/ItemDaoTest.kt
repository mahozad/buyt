package com.pleon.buyt.database.dao

import android.content.Context
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.InstantExecutorExtension
import com.pleon.buyt.blockingObserve
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.dto.ItemNameCat
import com.pleon.buyt.model.Category.FRUIT
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity
import com.pleon.buyt.model.Item.Quantity.Unit.GRAM
import com.pleon.buyt.model.Item.Quantity.Unit.UNIT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import java.util.concurrent.Executors

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

    @Mock private lateinit var observer: Observer<List<Item>>

    @BeforeEach internal fun setUp() {
        cxt = InstrumentationRegistry.getInstrumentation().context
        database = Room
            .inMemoryDatabaseBuilder(cxt, AppDatabase::class.java)
            // See https://stackoverflow.com/a/61663349
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        itemDao = database.itemDao()
    }

    @Test fun getAll_verifyOnChangedIsCalled() = runBlocking {
        val items = itemDao.getAll().first()
        verify(observer, times(1)).onChanged(Collections.emptyList())
    }

    @Test fun getAll_addOneItem() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))

        val items = itemDao.getAll().first()

        assertThat(items.size).isEqualTo(1)
    }

    @Test fun getAll_addTwoItems(): Unit = runBlocking {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))
        itemDao.insert(Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val items = itemDao.getAll().first()

        assertThat(items.size).isEqualTo(2)
    }

    @Test fun getCount_addOneItem() {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))

        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(1)
    }

    @Test fun getCount_addTwoItems() {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))
        itemDao.insert(Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(2)
    }

    @Test fun getItemNameCats_addOneNewNameCat() {
        itemDao.insert(Item("Something", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val itemNameCats = itemDao.getItemNameCats().blockingObserve()

        assertThat(itemNameCats[0]).isEqualTo(ItemNameCat("Something", FRUIT))
    }

    /**
     * The delete function in the dao is annotated with @Transaction.
     * See [this post](// See https://stackoverflow.com/a/61663349) for information on how to test it.
     */
    @Test fun delete_oneItem() = runBlocking<Unit> {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item).also { item.itemId = it }

        itemDao.delete(item)

        assertThat(itemDao.getCount()).isEqualTo(0)
        assertThat(itemDao.getAll().first().size).isEqualTo(0)
    }

    @AfterEach fun tearDown() {
        database.close()
    }
}
