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
    @Query("SELECT sum(totalPrice) from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE")
    protected abstract fun getTotalPurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select count(Distinct purchaseId) from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE")
    protected abstract fun getNumberOfPurchases(period: Int, filter: String): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select strftime('%w', date, 'unixepoch', 'localtime') AS day, sum(totalPrice)" +
            "from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by day " +
            "order by sum(totalPrice) desc " +
            "limit 1;")
    protected abstract fun getWeekdayWithMaxPurchaseCount(period: Int, filter: String): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select avg(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    protected abstract fun getAveragePurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select store.* from purchase natural join store join item on purchase.purchaseId=item.purchaseId " +
            "where" + PERIOD_CLAUSE + "and (:filter = 'NoFilter' or item.category = :filter) " +
            "group by purchase.storeId " +
            "order by count(purchase.storeId) desc " +
            "limit 1;")
    protected abstract fun getStoreWithMaxPurchaseCount(period: Int, filter: String): Store

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select max(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    protected abstract fun getMaxPurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select min(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    protected abstract fun getMinPurchaseCost(period: Int, filter: String): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select strftime('%j', date, 'unixepoch', 'localtime') AS day, avg(totalPrice) " +
            "from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by day")
    protected abstract fun getAverageDailyPurchaseCost(period: Int, filter: String): Long

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

    @Query("SELECT category as name, sum(totalPrice) as value FROM Item natural join purchase " +
            "WHERE $PERIOD_AND_FILTER_CLAUSE group by category order by value desc")
    protected abstract fun getMostPurchasedCategories(period: Int, filter: String): List<PieSlice>

    @Query("SELEct * FROM purchase natural join item where $PERIOD_AND_FILTER_CLAUSE group by purchaseId order by date desc")
    protected abstract fun getPurchaseDetails(period: Int, filter: String): List<PurchaseDetail>
}
