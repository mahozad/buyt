package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.pleon.buyt.database.converter.DateConverter
import com.pleon.buyt.database.dto.*
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.viewmodel.StatsViewModel.Filter
import java.text.DateFormat
import java.util.*

// NOTE that BETWEEN is inclusive in both ends
private const val PERIOD_CLAUSE = " date BETWEEN STRFTIME('%s', 'now', 'localtime', 'start of day', -:period || ' days') AND STRFTIME('%s', 'now', 'localtime') "
private const val FILTER_CLAUSE = " (:filter = 'NoFilter' OR category = :filter) "
private const val PERIOD_AND_FILTER_CLAUSE = "$PERIOD_CLAUSE AND $FILTER_CLAUSE"

@Dao
abstract class PurchaseDao {

    @Insert
    abstract suspend fun insert(purchase: Purchase): Long

    /**
     * Annotating a method with @Transaction makes sure that all database operations you’re
     * executing in that method will be run inside one transaction.
     * The transaction will fail when an exception is thrown in the method body.
     */
    @Transaction
    open suspend fun getStats(period: Int, filter: Filter): Stats {
        val adjustedPeriod = period - 1 // The queries return one extra day so subtract 1
        val stats = Stats()

        // NOTE: Use the Item::Category everywhere you should filter on Category
        //  because it is the single source of truth that we consider as Category

        stats.dailyCosts = getDailyCosts(adjustedPeriod, filter.criterion)
        stats.totalPurchaseCount = getTotalPurchaseCount(adjustedPeriod, filter.criterion)
        stats.totalPurchaseCost = getTotalPurchaseCost(adjustedPeriod, filter.criterion) ?: 0
        stats.maxPurchaseCost = getMaxPurchaseCost(adjustedPeriod, filter.criterion) ?: 0
        stats.minPurchaseCost = getMinPurchaseCost(adjustedPeriod, filter.criterion) ?: 0
        stats.averagePurchaseCost = getAveragePurchaseCost(adjustedPeriod, filter.criterion) ?: 0
        stats.weekdayWithMaxPurchaseCount = getWeekdayWithMaxPurchaseCount(adjustedPeriod, filter.criterion)
        stats.storeWithMaxPurchaseCount = getStoreWithMaxPurchaseCount(adjustedPeriod, filter.criterion)
        stats.mostPurchasedCategories = getMostPurchasedCategories(adjustedPeriod, filter.criterion)
        stats.mostPurchasedItem = getMostPurchasedItem(adjustedPeriod, filter.criterion)

        return stats
    }

    /**
     * The Sqlite functions SUM, AVG, MAX, and MIN return NULL when they do not
     * get any value to begin with.
     *
     * Instead of returning a nullable type, we could have used the Sqlite function
     * `IFNULL` to make it return 0 rather than `NULL` like so:
     *
     * ```SQL
     * SELECT IFNULL(SUM(totalPrice), 0) FROM Purchase
     * ```
     */
    @Query("""SELECT SUM(totalPrice) FROM Purchase NATURAL JOIN Item WHERE $PERIOD_AND_FILTER_CLAUSE""")
    protected abstract suspend fun getTotalPurchaseCost(period: Int, filter: String): Long?

    @Query("""SELECT COUNT(DISTINCT purchaseId) FROM Purchase NATURAL JOIN Item WHERE $PERIOD_AND_FILTER_CLAUSE""")
    protected abstract suspend fun getTotalPurchaseCount(period: Int, filter: String): Long

    @Query("""
        SELECT STRFTIME('%w', date, 'unixepoch', 'localtime') AS weekday, SUM(totalPrice) AS sum
        FROM Purchase NATURAL JOIN Item
        WHERE $PERIOD_AND_FILTER_CLAUSE
        GROUP BY weekday
        ORDER BY sum DESC
        LIMIT 1;""")
    protected abstract fun getWeekdayWithMaxPurchaseCost(period: Int, filter: String): Int

    @Query("""
        SELECT STRFTIME('%w', date, 'unixepoch', 'localtime') AS weekday, COUNT(DISTINCT purchaseId) as purchaseCount
        FROM Purchase NATURAL JOIN Item
        WHERE $PERIOD_AND_FILTER_CLAUSE
        GROUP BY weekday
        ORDER BY purchaseCount DESC
        LIMIT 1;""")
    protected abstract suspend fun getWeekdayWithMaxPurchaseCount(period: Int, filter: String): WeekdayWithMostPurchaseCountDto

    @Query("""
        SELECT Store.name, COUNT(DISTINCT Purchase.purchaseId) AS purchaseCount
        FROM Purchase NATURAL JOIN Store JOIN Item ON Item.purchaseId = Purchase.purchaseId
        WHERE $PERIOD_CLAUSE AND (:filter = 'NoFilter' OR Item.category = :filter)
        GROUP BY storeId
        ORDER BY purchaseCount DESC
        LIMIT 1;""")
    protected abstract suspend fun getStoreWithMaxPurchaseCount(period: Int, filter: String): StoreWithMostPurchaseCountDto

    @Query("""
        SELECT Item.name, COUNT(Item.name) AS purchaseCount 
        FROM Item NATURAL JOIN Purchase
        WHERE $PERIOD_AND_FILTER_CLAUSE
        GROUP BY Item.name
        ORDER BY purchaseCount DESC
        LIMIT 1;""")
    protected abstract suspend fun getMostPurchasedItem(period: Int, filter: String): MostPurchasedItemDto

    @Query("""
        SELECT MAX(cost) FROM (SELECT SUM(totalPrice) AS cost
                               FROM Purchase NATURAL JOIN Item
                               WHERE $PERIOD_AND_FILTER_CLAUSE
                               GROUP BY purchaseId)""")
    protected abstract suspend fun getMaxPurchaseCost(period: Int, filter: String): Long?

    @Query("""
        SELECT AVG(cost) FROM (SELECT SUM(totalPrice) AS cost
                               FROM Purchase NATURAL JOIN Item
                               WHERE $PERIOD_AND_FILTER_CLAUSE
                               GROUP BY purchaseId)""")
    protected abstract suspend fun getAveragePurchaseCost(period: Int, filter: String): Long?

    @Query("""
        SELECT MIN(cost) FROM (SELECT SUM(totalPrice) AS cost
                               FROM Purchase NATURAL JOIN Item
                               WHERE $PERIOD_AND_FILTER_CLAUSE
                               GROUP BY purchaseId)""")
    protected abstract suspend fun getMinPurchaseCost(period: Int, filter: String): Long?

    /**
     * Returns total costs per day that are within the specified period and match the filter.
     *
     * This query also includes dates that did not have any purchase in them (so it
     * returns 0 as the total cost for those dates). For more information, see
     * [the official documentation of
     * WITH clause in sqlite](https://www.sqlite.org/lang_with.html).
     *
     * If you want to return the date as a [java Date][Date] object, then change
     * type of the field in [DailyCost] from [String] to [Date] and
     * modify the [DateConverter] to convert from String to Date
     * (using a [DateFormat] or any other approaches).
     *
     * @param period number of days to return their costs
     * @param filter the [category][Category] to filter by
     * @return list of [daily costs][DailyCost]
     */
    @Query("""
        WITH RECURSIVE AllDates(date) AS (SELECT DATE('now', 'localtime', -:period||' days')
                                          UNION ALL SELECT DATE(date, '+1 days') FROM AllDates
                                          WHERE date < DATE('now', 'localtime'))
        SELECT AllDates.date, SUM(totalPrice) AS totalCost
        FROM AllDates LEFT JOIN (SELECT DATE(date, 'unixepoch', 'localtime') AS date, totalPrice
                                 FROM Purchase NATURAL JOIN Item
                                 WHERE $PERIOD_AND_FILTER_CLAUSE) AS DailyCosts
        ON AllDates.date = DailyCosts.date
        GROUP BY AllDates.date""")
    protected abstract suspend fun getDailyCosts(period: Int, filter: String): List<DailyCost>

    @Query("""
        SELECT category AS name, SUM(totalPrice) AS value
        FROM Item NATURAL JOIN Purchase
        WHERE $PERIOD_AND_FILTER_CLAUSE
        GROUP BY category
        ORDER BY value DESC""")
    protected abstract suspend fun getMostPurchasedCategories(period: Int, filter: String): List<CategorySum>

    @Query("""
        SELECT *
        FROM Purchase NATURAL JOIN Item
        WHERE $PERIOD_AND_FILTER_CLAUSE
        GROUP BY purchaseId
        ORDER BY date DESC""")
    abstract suspend fun getPurchaseDetails(period: Int, filter: String): List<PurchaseDetail>

    @Query("""
        SELECT *
        FROM Purchase NATURAL JOIN Item
        GROUP BY purchaseId
        ORDER BY date DESC""")
    abstract suspend fun getAllPurchaseDetails(): List<PurchaseDetail>

    @Transaction
    open suspend fun getPurchaseCountInPeriod(period: Int): Int {
        val adjustedPeriod = period - 1 // The queries return one extra day so subtract 1
        return getCountInPeriod(adjustedPeriod)
    }

    @Query("""SELECT Count(*) FROM Purchase WHERE $PERIOD_CLAUSE""")
    protected abstract suspend fun getCountInPeriod(period: Int): Int
}
