package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.pleon.buyt.database.DailyCost
import com.pleon.buyt.database.PieSlice
import com.pleon.buyt.database.converter.DateConverter
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Statistics
import com.pleon.buyt.model.Store
import java.text.DateFormat
import java.util.*

private const val PERIOD_CLAUSE = " date >= STRFTIME('%s', 'now', 'localtime', 'start of day', -:period || ' days') "
private const val FILTER_CLAUSE = " (:filter IS NULL OR category = :filter) "
private const val PERIOD_AND_FILTER_CLAUSE = "$PERIOD_CLAUSE AND $FILTER_CLAUSE"

@Dao
interface PurchaseDao {

    @Insert
    fun insert(purchase: Purchase): Long

    /**
     * Annotating a method with @Transaction makes sure that all database operations you’re
     * executing in that method will be run inside one transaction.
     * The transaction will fail when an exception is thrown in the method body.
     */
    @Transaction
    fun getStatistics(period: Int, filter: Category?): Statistics {
        val statistics = Statistics()

        // This query returns one extra day so do period-1
        statistics.dailyCosts = getDailyCosts(period - 1, filter)
        statistics.totalPurchaseCost = (getTotalPurchaseCost(period, filter))
        statistics.averagePurchaseCost = (getAveragePurchaseCost(period, filter))
        statistics.numberOfPurchases = (getNumberOfPurchases(period, filter))
        statistics.maxPurchaseCost = (getMaxPurchaseCost(period, filter))
        statistics.minPurchaseCost = (getMinPurchaseCost(period, filter))
        statistics.setWeekdayWithMaxPurchases(getWeekdayWithMaxPurchaseCount(period, filter))
        statistics.setStoreWithMaxPurchaseCount(getStoreWithMaxPurchaseCount(period, filter))
        statistics.mostPurchasedCategories = getMostPurchasedCategories(period)

        return statistics
    }

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT sum(totalPrice) from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE")
    fun getTotalPurchaseCost(period: Int, filter: Category?): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select count(Distinct purchaseId) from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE")
    fun getNumberOfPurchases(period: Int, filter: Category?): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select strftime('%w', date, 'unixepoch', 'localtime') AS day, sum(totalPrice)" +
            "from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by day " +
            "order by sum(totalPrice) desc " +
            "limit 1;")
    fun getWeekdayWithMaxPurchaseCount(period: Int, filter: Category?): Int

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select avg(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    fun getAveragePurchaseCost(period: Int, filter: Category?): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select store.* from purchase natural join store join item on purchase.purchaseId=item.purchaseId " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or item.category = :filter) " +
            "group by purchase.storeId " +
            "order by count(purchase.storeId) desc " +
            "limit 1;")
    fun getStoreWithMaxPurchaseCount(period: Int, filter: Category?): Store

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select max(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    fun getMaxPurchaseCost(period: Int, filter: Category?): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select min(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by purchaseId)")
    fun getMinPurchaseCost(period: Int, filter: Category?): Long

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("select strftime('%j', date, 'unixepoch', 'localtime') AS day, avg(totalPrice) " +
            "from purchase natural join item " +
            "where $PERIOD_AND_FILTER_CLAUSE" +
            "group by day")
    fun getAverageDailyPurchaseCost(period: Int, filter: Category): Long

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
    fun getDailyCosts(period: Int, filter: Category?): List<DailyCost>

    @Query("SELECT category as name, sum(totalPrice) as value FROM Item natural join purchase " +
            "WHERE $PERIOD_CLAUSE group by category order by value desc")
    fun getMostPurchasedCategories(period: Int): List<PieSlice>
}
