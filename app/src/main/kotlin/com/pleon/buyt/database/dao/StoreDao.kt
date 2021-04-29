package com.pleon.buyt.database.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StoresViewModel.Sort
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.*
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection.DESCENDING
import org.intellij.lang.annotations.Language

@Dao
abstract class StoreDao {

    fun getStoreDetails(sort: Sort, sortDirection: SortDirection, period: Int): List<StoreDetail> {
        val sqlSortColumn = when (sort) {
            TOTAL_SPENDING -> "totalSpending"
            PURCHASE_COUNT -> "purchaseCount"
            STORE_CATEGORY -> "category"
            STORE_NAME -> "name"
        }
        val sqlSortDirection = if (sortDirection == DESCENDING) "DESC" else "ASC"

        @Language("RoomSql")
        val query = SimpleSQLiteQuery("""
            SELECT Store.*, SUM(cost) AS totalSpending, COUNT(DISTINCT purchaseId) AS purchaseCount
            FROM Store LEFT JOIN (SELECT purchaseId, storeId, SUM(totalPrice) AS cost
                                  FROM Item NATURAL JOIN Purchase
                                  GROUP BY purchaseId) AS ip
                       ON Store.storeId = ip.storeId
            WHERE Store.isFlaggedForDeletion = 0
            GROUP BY Store.storeId
            ORDER BY $sqlSortColumn $sqlSortDirection""")

        val briefs = getStoreBriefs(query)
        val storeDetails = mutableListOf<StoreDetail>()
        for (brief in briefs) {
            val storeId = brief.store.storeId
            val storeDetail = StoreDetail()
            storeDetail.brief = brief
            storeDetail.dailyCosts = getStoreDailyCosts(storeId, period)
            storeDetail.purchaseSummary = getStorePurchaseSummary(storeId)
            storeDetails.add(storeDetail)
        }
        return storeDetails
    }

    /**
     * @RawQuery is used because dynamic parameters cannot be used in ORDER BY clauses
     */
    @RawQuery(observedEntities = [Store::class, Purchase::class, Item::class])
    protected abstract fun getStoreBriefs(query: SupportSQLiteQuery): List<StoreDetail.StoreBrief>

    @Query("""
        WITH RECURSIVE AllDates(date) AS (SELECT DATE('now', 'localtime', -:period || ' days')
                                          UNION ALL SELECT DATE(date, '+1 days') FROM AllDates
                                          WHERE date < DATE('now', 'localtime'))
        SELECT AllDates.date, SUM(totalPrice) AS totalCost
        FROM AllDates LEFT JOIN (SELECT DATE(date, 'unixepoch', 'localtime') AS date, totalPrice
                                 FROM Purchase LEFT JOIN Store ON Store.storeId = Purchase.storeId
                                               LEFT JOIN Item ON Purchase.purchaseId = Item.purchaseId
                                 WHERE Store.storeId = :storeId AND date BETWEEN STRFTIME('%s', 'now', 'localtime', 'start of day', -:period || ' days') AND STRFTIME('%s', 'now', 'localtime')) AS DailyCosts
        ON AllDates.date = DailyCosts.date
        GROUP BY AllDates.date""")
    protected abstract fun getStoreDailyCosts(storeId: Long, period: Int): List<DailyCost>

    @Query("""
        SELECT MAX(totalSpending) AS maxPurchaseCost,
               AVG(totalSpending) AS avgPurchaseCost,
               MIN(totalSpending) AS minPurchaseCost
        FROM (SELECT SUM(totalPrice) AS totalSpending
              FROM Item NATURAL JOIN Purchase JOIN Store ON Purchase.storeId = Store.storeId
              WHERE Store.storeId = :storeId
              GROUP BY purchaseId)""")
    protected abstract fun getStorePurchaseSummary(storeId: Long): StoreDetail.PurchaseSummary

    @Query("""SELECT * FROM Store""")
    abstract fun getAllSynchronous(): List<Store>

    @Query("""SELECT * FROM Store WHERE :sinLat * sinLat + :cosLat * cosLat * (cosLng * :cosLng + sinLng * :sinLng) > :maxDistance""")
    abstract fun getNearStores(sinLat: Double, cosLat: Double, sinLng: Double, cosLng: Double, maxDistance: Double): List<Store>

    @Insert
    abstract fun insert(store: Store): Long

    @Update
    abstract fun update(store: Store)

    @Delete
    abstract fun delete(store: Store)
}
