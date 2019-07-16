package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.pleon.buyt.database.converter.DateConverter
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.database.dto.PieSlice
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StatsViewModel.Filter
import java.text.DateFormat
import java.util.*

private const val PERIOD_CLAUSE = " date >= STRFTIME('%s', 'now', 'localtime', 'start of day', -:period || ' days') "
private const val FILTER_CLAUSE = " (:filter = 'NoFilter' OR category = :filter) "
private const val PERIOD_AND_FILTER_CLAUSE = "$PERIOD_CLAUSE AND $FILTER_CLAUSE"

@Dao
abstract class PurchaseDao {

    @Insert
    abstract fun insert(purchase: Purchase): Long

    /**
     * Annotating a method with @Transaction makes sure that all database operations you’re
     * executing in that method will be run inside one transaction.
     * The transaction will fail when an exception is thrown in the method body.
     */
    @Transaction
    open fun getStats(period: Int, filter: Filter): Stats {
        val period = period - 1 // The queries return one extra day so do period-1
        val filter = filter.criterion
        val stats = Stats()

        stats.dailyCosts = getDailyCosts(period, filter)
        stats.totalPurchaseCost = getTotalPurchaseCost(period, filter)
        stats.averagePurchaseCost = getAveragePurchaseCost(period, filter)
        stats.numberOfPurchases = getNumberOfPurchases(period, filter)
        stats.maxPurchaseCost = getMaxPurchaseCost(period, filter)
        stats.minPurchaseCost = getMinPurchaseCost(period, filter)
        stats.weekdayWithMaxPurchases = getWeekdayWithMaxPurchaseCount(period, filter)
        stats.storeWithMaxPurchaseCount = getStoreWithMaxPurchaseCount(period, filter)
        stats.mostPurchasedCategories = getMostPurchasedCategories(period, filter)
        stats.purchaseDetails = getPurchaseDetails(period, filter)

        return stats
    }

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT SUM(totalPrice) FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE")
    protected abstract fun getTotalPurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT COUNT(DISTINCT purchaseId) FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE")
    protected abstract fun getNumberOfPurchases(period: Int, filter: String): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT STRFTIME('%w', date, 'unixepoch', 'localtime') AS day, SUM(totalPrice)" +
            "FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE" +
            "GROUP BY day " +
            "ORDER BY SUM(totalPrice) DESC " +
            "LIMIT 1;")
    protected abstract fun getWeekdayWithMaxPurchaseCount(period: Int, filter: String): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT Store.* FROM Purchase NATURAL JOIN Store JOIN Item ON Purchase.purchaseId=Item.purchaseId " +
            "WHERE $PERIOD_CLAUSE" + "AND (:filter = 'NoFilter' OR Item.category = :filter) " +
            "GROUP BY Purchase.storeId " +
            "ORDER BY COUNT(purchase.storeId) DESC " +
            "LIMIT 1;")
    protected abstract fun getStoreWithMaxPurchaseCount(period: Int, filter: String): Store

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT MAX(cost) FROM " +
            "(SELECT SUM(totalPrice) AS cost FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE" +
            "GROUP BY purchaseId)")
    protected abstract fun getMaxPurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT AVG(cost) FROM " +
            "(SELECT SUM(totalPrice) AS cost FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE" +
            "GROUP BY purchaseId)")
    protected abstract fun getAveragePurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT MIN(cost) FROM " +
            "(SELECT SUM(totalPrice) AS cost FROM Purchase NATURAL JOIN Item " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE" +
            "GROUP BY purchaseId)")
    protected abstract fun getMinPurchaseCost(period: Int, filter: String): Long

    //language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
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
    @Query(" WITH RECURSIVE AllDates(date)" +
            "AS (SELECT DATE('now', 'localtime', -:period||' days')" +
            "    UNION ALL" +
            "    SELECT DATE(date, '+1 days') FROM AllDates" +
            "    WHERE date < DATE('now', 'localtime')) " +
            "SELECT AllDates.date, SUM(totalPrice) AS totalCost " +
            "FROM AllDates LEFT JOIN " +
            "   (SELECT DATE(date, 'unixepoch', 'localtime') AS date, totalPrice" +
            "    FROM Purchase NATURAL JOIN Item" +
            "    WHERE $PERIOD_AND_FILTER_CLAUSE) AS DailyCosts " +
            "ON AllDates.date = DailyCosts.date " +
            "GROUP BY AllDates.date")
    protected abstract fun getDailyCosts(period: Int, filter: String): List<DailyCost>

    @Query("SELECT category AS name, SUM(totalPrice) AS value FROM Item NATURAL JOIN Purchase " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE GROUP BY category ORDER BY value DESC")
    protected abstract fun getMostPurchasedCategories(period: Int, filter: String): List<PieSlice>

    @Query("SELECT * FROM Purchase NATURAL JOIN Item WHERE $PERIOD_AND_FILTER_CLAUSE GROUP BY purchaseId ORDER BY date DESC")
    protected abstract fun getPurchaseDetails(period: Int, filter: String): List<PurchaseDetail>

    @Transaction
    open fun getPurchaseCountInPeriod(period: Int): Int {
        val period = period - 1 // The queries return one extra day so do period-1
        return getCountInPeriod(period)
    }

    @Query("SELECT Count(*) FROM Purchase WHERE $PERIOD_CLAUSE")
    protected abstract fun getCountInPeriod(period: Int): Int
}
