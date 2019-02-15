package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Category;
import com.pleon.buyt.model.DailyCost;
import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.Statistics;
import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import static androidx.room.RoomWarnings.CURSOR_MISMATCH;

@Dao
public abstract class PurchaseDao {

    private static final String PERIOD_CLAUSE =
            " date >= strftime('%s', 'now', 'localtime', 'start of day', -:period || ' days') ";

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
        statistics.setDailyCosts(getDailyCosts(period, filter));
        statistics.setAveragePurchaseCost(getAveragePurchaseCost(period, filter));
        statistics.setCategory(getMostPurchasedCategory(period));
        statistics.setTotalPurchaseCost(getTotalPurchaseCost(period, filter));

        return statistics;
    }

    @Query("select category from purchase natural join item where" + PERIOD_CLAUSE +
            "group by category " +
            "order by count(category) desc " +
            "limit 1;")
    abstract Category getMostPurchasedCategory(int period);

    @Query("select sum(totalCost) from purchase natural join item where" + PERIOD_CLAUSE +
            "      and (:filter is null or category = :filter)")
    abstract long getTotalPurchaseCost(int period, Category filter);

    @Query("select strftime('%w', date, 'unixepoch', 'localtime') AS day " +
            "from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter) " +
            "group by day " +
            "limit 1;")
    abstract int getWeekdayWithMaxPurchaseCount(int period, Category filter);

    @Query("select strftime('%w', date, 'unixepoch', 'localtime') AS day " +
            "from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter) " +
            "group by day " +
            "limit 1;")
    abstract int getWeekdayWithMinPurchaseCount(int period, Category filter);

    @Query("select avg(totalCost) from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter)")
    abstract long getAveragePurchaseCost(int period, Category filter);

    @SuppressWarnings(CURSOR_MISMATCH)
    @Query("select store.*, count(purchase.storeId) from purchase natural join store " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter) " +
            "group by purchase.storeId " +
            "order by count(purchase.storeId) desc " +
            "limit 1;")
    abstract Store getStoreIdWithMaxPurchases(int period, Category filter);

    @Query("select max(totalCost) from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter)")
    abstract long getMaxPurchaseCost(int period, Category filter);

    @Query("select min(totalCost) from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter)")
    abstract long getMinPurchaseCost(int period, Category filter);

    @Query("select strftime('%j', date, 'unixepoch', 'localtime') AS day, avg(totalCost) " +
            "from purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter)" +
            "group by day")
    abstract long getAverageDailyPurchaseCost(int period, Category filter);

    @Query("SELECT strftime('%j', date, 'unixepoch', 'localtime') AS day, SUM(totalCost) AS cost " +
            "FROM purchase natural join item " +
            "where" + PERIOD_CLAUSE + "and (:filter is null or category = :filter)" +
            "GROUP BY day " +
            "UNION ALL " +
            "SELECT strftime('%j', 'now', 'localtime') AS day, -1 AS cost")
    abstract List<DailyCost> getDailyCosts(int period, Category filter);
}
