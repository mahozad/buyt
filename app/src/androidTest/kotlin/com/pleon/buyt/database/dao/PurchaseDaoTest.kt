package com.pleon.buyt.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pleon.buyt.InstantExecutorExtension
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.model.Category.*
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Item.Quantity.Unit.UNIT
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StatsViewModel.NoFilter
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.Executors

@ExtendWith(InstantExecutorExtension::class)
class PurchaseDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var purchaseDao: PurchaseDao
    private lateinit var storeDao: StoreDao
    private lateinit var itemDao: ItemDao

    @BeforeEach
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()
        purchaseDao = database.purchaseDao()
        storeDao = database.storeDao()
        itemDao = database.itemDao()
    }

    @Test fun makeSureSqliteReturns0InsteadOfNullPointerExceptionAsSumWhenDatabaseIsEmpty() = runBlocking<Unit> {
        val stats = purchaseDao.getStats(period = 7, filter = NoFilter)

        Assertions.assertThat(stats.totalPurchaseCost).isEqualTo(0)
    }


    @Test
    fun testGetStoreWithMaxPurchaseCount() = runBlocking<Unit> {
        val item1 = Item("item1", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item2 = Item("item2", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item3 = Item("item3", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item4 = Item("item4", Item.Quantity(1, UNIT), GROCERY, false, isBought = true)

        val store1 = Store(Coordinates(1.0, 2.0), "store", GROCERY)
        val store1Id = storeDao.insert(store1)
        store1.storeId = store1Id

        val purchase1 = Purchase(Date()).apply { storeId = store1Id }
        val purchase2 = Purchase(Date()).apply { storeId = store1Id }
        val purchase3 = Purchase(Date.from(Instant.now().minus(2, ChronoUnit.DAYS))).apply { storeId = store1Id }
        val purchase1Id = purchaseDao.insert(purchase1)
        val purchase2Id = purchaseDao.insert(purchase2)
        val purchase3Id = purchaseDao.insert(purchase3)

        item1.purchaseId = purchase1Id
        item2.purchaseId = purchase2Id
        item3.purchaseId = purchase2Id
        item4.purchaseId = purchase3Id
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)

        val stats = purchaseDao.getStats(period = 7, NoFilter)

        Assertions.assertThat(stats.storeWithMaxPurchaseCount?.purchaseCount).isEqualTo(3)
    }

    @Test
    fun shouldOnlyCountPurchasesInTheSpecifiedPeriod() = runBlocking<Unit> {
        val item1 = Item("item1", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item2 = Item("item2", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item3 = Item("item3", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item4 = Item("item4", Item.Quantity(1, UNIT), GROCERY, false, isBought = true)

        val store1 = Store(Coordinates(1.0, 2.0), "store", GROCERY)
        val store1Id = storeDao.insert(store1).also { store1.storeId = it }

        val purchase1 = Purchase(Date()).apply { storeId = store1Id }
        val purchase2 = Purchase(Date()).apply { storeId = store1Id }
        val purchase3 = Purchase(Date.from(Instant.now().minus(2, ChronoUnit.DAYS))).apply { storeId = store1Id }
        val purchase1Id = purchaseDao.insert(purchase1)
        val purchase2Id = purchaseDao.insert(purchase2)
        val purchase3Id = purchaseDao.insert(purchase3)

        item1.purchaseId = purchase1Id
        item2.purchaseId = purchase2Id
        item3.purchaseId = purchase2Id
        item4.purchaseId = purchase3Id
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)

        val stats = purchaseDao.getStats(period = 1, NoFilter)

        Assertions.assertThat(stats.storeWithMaxPurchaseCount?.purchaseCount).isEqualTo(2)
    }

    /**
     * Because our single source of truth is Item::Category
     */
    @Test
    fun shouldOnlyCountPurchasesWithTheSpecifiedItemCategoryAndNotStoreCategory() = runBlocking<Unit> {
        val item1 = Item("item1", Item.Quantity(1, UNIT), FRUIT, false, isBought = false)
        val item2 = Item("item2", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item3 = Item("item3", Item.Quantity(1, UNIT), BREAD, false, isBought = false)
        val item4 = Item("item4", Item.Quantity(1, UNIT), GROCERY, false, isBought = true)

        val store1 = Store(Coordinates(1.0, 2.0), "store", MEAT)
        val store1Id = storeDao.insert(store1).also { store1.storeId = it }

        val purchase1 = Purchase(Date()).apply { storeId = store1Id }
        val purchase2 = Purchase(Date()).apply { storeId = store1Id }
        val purchase3 = Purchase(Date.from(Instant.now().minus(2, ChronoUnit.DAYS))).apply { storeId = store1Id }
        val purchase1Id = purchaseDao.insert(purchase1)
        val purchase2Id = purchaseDao.insert(purchase2)
        val purchase3Id = purchaseDao.insert(purchase3)

        item1.purchaseId = purchase1Id
        item2.purchaseId = purchase2Id
        item3.purchaseId = purchase2Id
        item4.purchaseId = purchase3Id
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)

        val stats = purchaseDao.getStats(period = 7, GROCERY)

        Assertions.assertThat(stats.storeWithMaxPurchaseCount?.purchaseCount).isEqualTo(2)
    }

    @Test fun shouldReturnCorrectWeekdayWithMaxPurchaseCount() = runBlocking<Unit> {
        val item1 = Item("item1", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item2 = Item("item2", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item3 = Item("item3", Item.Quantity(1, UNIT), GROCERY, false, isBought = false)
        val item4 = Item("item4", Item.Quantity(1, UNIT), GROCERY, false, isBought = true)
        val item5 = Item("item5", Item.Quantity(1, UNIT), GROCERY, false, isBought = true)

        val store1 = Store(Coordinates(1.0, 2.0), "store", GROCERY)
        val store1Id = storeDao.insert(store1).also { store1.storeId = it }

        val dateWithMaxPurchaseCount = Date.from(Instant.now().minus(2, ChronoUnit.DAYS))
        val purchase1 = Purchase(Date()).apply { storeId = store1Id }
        val purchase2 = Purchase(dateWithMaxPurchaseCount).apply { storeId = store1Id }
        val purchase3 = Purchase(dateWithMaxPurchaseCount).apply { storeId = store1Id }
        val purchase1Id = purchaseDao.insert(purchase1)
        val purchase2Id = purchaseDao.insert(purchase2)
        val purchase3Id = purchaseDao.insert(purchase3)

        item1.purchaseId = purchase1Id
        item2.purchaseId = purchase1Id
        item3.purchaseId = purchase1Id
        item4.purchaseId = purchase2Id
        item5.purchaseId = purchase3Id
        itemDao.insert(item1)
        itemDao.insert(item2)
        itemDao.insert(item3)
        itemDao.insert(item4)
        itemDao.insert(item5)

        val stats = purchaseDao.getStats(period = 7, NoFilter)

        Assertions.assertThat(stats.weekdayWithMaxPurchaseCount?.weekday).isEqualTo(dateWithMaxPurchaseCount.day)
        Assertions.assertThat(stats.weekdayWithMaxPurchaseCount?.purchaseCount).isEqualTo(2)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }
}
