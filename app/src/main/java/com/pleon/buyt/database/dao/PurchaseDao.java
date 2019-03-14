package com.pleon.buyt.database.dao;

import com.pleon.buyt.database.converter.DateConverter;
import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.DailyCost;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Statistics;
import com.pleon.buyt.model.Store;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

@Dao
public abstract class PurchaseDao {

    private static final String PERIOD_CLAUSE = " date >= STRFTIME('%s', 'now', 'localtime', 'start of day', -:period || ' days') ";
    private static final String FILTER_CLAUSE = " (:filter IS NULL OR category = :filter) ";

    @Insert
    public abstract long insert(Purchase purchase);

    /**
     * Annotating a method with @Transaction makes sure that all database operations you’re
     * executing in that method will be run inside one transaction.
     * The transaction will fail when an exception is thrown in the method body.
     */
    @Transaction
    public Statistics getStats(int period, @Nullable Category filter) {
        Statistics statistics = new Statistics();

        // This query returns one extra day so do --period
        statistics.setDailyCosts(getDailyCosts(--period, filter));
        statistics.setTotalPurchaseCost(getTotalPurchaseCost(period, filter));
        statistics.setAveragePurchaseCost(getAveragePurchaseCost(period, filter));
        statistics.setMostPurchasedCategory(getMostPurchasedCategory(period));
        statistics.setNumberOfPurchases(getNumberOfPurchases(period, filter));
        statistics.setMaxPurchaseCost(getMaxPurchaseCost(period, filter));
        statistics.setMinPurchaseCost(getMinPurchaseCost(period, filter));
        statistics.setWeekdayWithMaxPurchases(getWeekdayWithMaxPurchaseCount(period, filter));
        statistics.setStoreWithMaxPurchaseCount(getStoreWithMaxPurchaseCount(period, filter));

        return statistics;
    }

    @Query("select sum(totalPrice) from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "AND" + FILTER_CLAUSE)
    abstract long getTotalPurchaseCost(int period, Category filter);

    @Query("select category from purchase natural join item where" + PERIOD_CLAUSE +
            "group by category " +
            "order by count(category) desc " +
            "limit 1;")
    abstract Category getMostPurchasedCategory(int period);

    @Query("select count(Distinct purchaseId) from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE)
    abstract int getNumberOfPurchases(int period, Category filter);

    @Query("select strftime('%w', date, 'unixepoch', 'localtime') AS day, sum(totalPrice)" +
            "from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE +
            "group by day " +
            "order by sum(totalPrice) desc " +
            "limit 1;")
    abstract int getWeekdayWithMaxPurchaseCount(int period, Category filter);

    @Query("select avg(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE +
            "group by purchaseId)")
    abstract long getAveragePurchaseCost(int period, Category filter);

    @Query("select store.* from purchase natural join store join item on purchase.purchaseId=item.purchaseId " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or item.category = :filter) " +
            "group by purchase.storeId " +
            "order by count(purchase.storeId) desc " +
            "limit 1;")
    abstract Store getStoreWithMaxPurchaseCount(int period, Category filter);

    @Query("select max(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE +
            "group by purchaseId)")
    abstract long getMaxPurchaseCost(int period, Category filter);

    @Query("select min(cost) from " +
            "(select sum(totalPrice) as cost from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE +
            "group by purchaseId)")
    abstract long getMinPurchaseCost(int period, Category filter);

    @Query("select strftime('%j', date, 'unixepoch', 'localtime') AS day, avg(totalPrice) " +
            "from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and" + FILTER_CLAUSE +
            "group by day")
    abstract long getAverageDailyPurchaseCost(int period, Category filter);

    /**
     * Returns total costs per day that are within the specified period and match the filter.
     * <p>
     * This query also includes dates that did not have any purchase in them (so it
     * returns 0 as the total cost for those dates). For more information, see
     * <a href="https://www.sqlite.org/lang_with.html">the official documentation of
     * WITH clause in sqlite</a>.
     * <p>
     * If you want to return the date as a {@link Date java Date} object, then change
     * type of the field in {@link DailyCost} from {@link String} to {@link Date} and
     * modify the {@link DateConverter} to convert from String to Date
     * (using a {@link DateFormat} or any other approaches).
     *
     * @param period number of days to return their costs
     * @param filter the {@link Category category} to filter by
     * @return list of {@link DailyCost daily costs}
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
            "    WHERE" + PERIOD_CLAUSE + "AND" + FILTER_CLAUSE + ") DailyCosts " +
            "ON AllDates.date = DailyCosts.date " +
            "GROUP BY AllDates.date")
    abstract List<DailyCost> getDailyCosts(int period, Category filter);
}
