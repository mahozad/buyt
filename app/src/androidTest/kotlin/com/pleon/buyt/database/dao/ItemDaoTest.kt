package com.pleon.buyt.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.pleon.buyt.InstantExecutorExtension
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
import org.mockito.junit.jupiter.MockitoExtension
import java.util.concurrent.Executors

/**
 * Instrumentation tests execute only on API level 26 (Android 8.0) and above.
 * This is because JUnit 5 requires an environment built on top of Java 8.
 * Therefore these tests are just ignored on older API levels.
 */
@ExtendWith(MockitoExtension::class, InstantExecutorExtension::class)
class ItemDaoTests {

    private lateinit var database: AppDatabase
    private lateinit var itemDao: ItemDao

    @BeforeEach internal fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        database = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // See https://stackoverflow.com/a/61663349 for why
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        itemDao = database.itemDao()
    }

    @Test fun getAll_verifyEmptyDatabaseReturnsEmptyList() = runBlocking<Unit> {
        val items = itemDao.getAll().first()
        assertThat(items).isEqualTo(emptyList<Item>())
        // verify(collector, times(2)).invoke(anyList())
    }

    @Test fun getAll_withNoUrgentItemSortByPositionAscending() = runBlocking<Unit> {
        val item1 = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item2 = Item("Cheese", Quantity(8, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item3 = Item("Apple", Quantity(5, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item4 = Item("Orange", Quantity(2, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)

        val items = itemDao.getAll().first()

        assertThat(items.map { it.position }).containsExactly(1, 2, 3, 4)
    }

    @Test fun getAll_UrgentItemShouldAppearFirst() = runBlocking<Unit> {
        val item1 = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item2 = Item("Cheese", Quantity(8, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item3 = Item("Apple", Quantity(5, UNIT), GROCERY, isUrgent = true, isBought = false)
        val item4 = Item("Orange", Quantity(2, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)

        val items = itemDao.getAll().first()

        assertThat(items.map { it.itemId }).containsExactly(3, 1, 2, 4)
    }

    @Test fun getAll_addOneItem_newCollectShouldReturnNonEmptyList() = runBlocking<Unit> {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)

        val itemsFlow = itemDao.getAll()
        val items1 = itemsFlow.first()
        itemDao.insert(item)
        val items2 = itemsFlow.first()

        assertThat(items1.size).isEqualTo(0)
        assertThat(items2.size).isEqualTo(1)
    }

    @Test fun getAll_addOneItem() = runBlocking<Unit> {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item).also {
            item.itemId = it
            item.position = 1
        }

        val items = itemDao.getAll().first()

        assertThat(items.single()).usingRecursiveComparison().isEqualTo(item)
    }

    @Test fun getAll_addOneItem_itemPositionShouldBe1() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))

        val item = itemDao.getAll().first().single()

        assertThat(item.position).isEqualTo(1)
    }

    @Test fun getAll_addTwoItems_itemPositionsShouldBeCorrect() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))
        itemDao.insert(Item("Cheese", Quantity(3, UNIT), GROCERY, isUrgent = false, isBought = false))

        val items = itemDao.getAll().first()

        assertThat(items[0].position).isEqualTo(1)
        assertThat(items[1].position).isEqualTo(2)
    }

    @Test fun getAll_addOneBoughtItem_itemShouldNotBeInGetAll() = runBlocking<Unit> {
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = true)
        itemDao.insert(item)

        val items = itemDao.getAll().first()

        assertThat(items.size).isEqualTo(0)
    }

    @Test fun getAll_addTwoItems() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))
        itemDao.insert(Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val items = itemDao.getAll().first()

        assertThat(items.size).isEqualTo(2)
    }

    @Test fun getCount_emptyDatabase() = runBlocking<Unit> {
        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(0)
    }

    @Test fun getCount_addOneItem() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))

        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(1)
    }

    @Test fun getCount_addTwoItems() = runBlocking<Unit> {
        itemDao.insert(Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false))
        itemDao.insert(Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(2)
    }

    @Test fun getCount_addTwoItemsThenDeleteOne() = runBlocking<Unit> {
        itemDao.insert(Item("Apple", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))
        val item = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item).also { item.itemId = it }
        itemDao.delete(item)

        val itemCount = itemDao.getCount()

        assertThat(itemCount).isEqualTo(1)
    }

    @Test fun getItemNameCats_addOneNewNameCat() = runBlocking<Unit> {
        itemDao.insert(Item("Something", Quantity(500, GRAM), FRUIT, isUrgent = true, isBought = false))

        val itemNameCats = itemDao.getItemNameCats().first()

        assertThat(itemNameCats.size).isEqualTo(1)
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

    /**
     * To pass this test, ItemDao::delete function should first update other items
     * and then delete the item. By first deleting the item and then trying to
     * update other items, we no longer have the reference item to get its position etc.
     */
    @Test fun delete_oneItem_positionOfItemsWithGreaterPositionShouldBeUpdated() = runBlocking<Unit> {
        val item1 = Item("Chocolate", Quantity(1, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item2 = Item("Cheese", Quantity(8, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item3 = Item("Apple", Quantity(5, UNIT), GROCERY, isUrgent = false, isBought = false)
        val item4 = Item("Orange", Quantity(2, UNIT), GROCERY, isUrgent = false, isBought = false)
        itemDao.insert(item1)
        itemDao.insert(item2).also { item2.itemId = it }
        itemDao.insert(item3)
        itemDao.insert(item4)

        itemDao.delete(item2)
        val items = itemDao.getAll().first()

        assertThat(items[0].position).isEqualTo(1)
        assertThat(items[1].position).isEqualTo(2)
        assertThat(items[2].position).isEqualTo(3)
    }

    @AfterEach fun tearDown() {
        database.close()
    }
}
