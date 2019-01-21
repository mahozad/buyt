package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Purchase;
import com.pleon.buyt.model.WeekdayCost;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
public interface PurchaseDao {

    @Insert
    long insert(Purchase purchase);

    /**
     * Gets List of total costs per each <b>day of the week</b> between the given period.
     * <p>
     * The %w in GROUP BY clause, groups the dates by day of the week (it results in numbers
     * from 0 to 6, with 0 meaning sunday and so on).
     *
     * @param from the starting date in milliseconds
     * @param to   the ending date in milliseconds
     * @return List of total costs per weekday
     */
    @Query("SELECT strftime('%w', date/1000, 'unixepoch') as day, SUM(totalcost) as cost " +
            "FROM purchase " +
            "WHERE date BETWEEN :from AND :to " +
            "GROUP BY strftime('%w', date/1000, 'unixepoch')" +
            "ORDER BY day")
    List<WeekdayCost> getCost(long from, long to);

    @Query("SELECT * FROM purchase")
    LiveData<List<Purchase>> getAll();
}
