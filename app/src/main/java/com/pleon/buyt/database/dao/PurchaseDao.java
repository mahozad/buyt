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
     * Gets List of total costs per each <b>day of week</b> for the given period.
     * <p>
     * <code>strftime()</code> gets date in seconds (<code>date/1000</code>), adapts it to user time
     * (<code>'unixepoch', 'localtime'</code>) and then formats it as day of week (<code>'%w'</code>
     * resulting in numbers from 0 to 6, with 0 meaning Sunday and so on).
     * <p>
     * Do NOT change the order of 'unixepoch' and 'localtime'. Also note that since we save the date
     * as milliseconds in the database and epoch is in seconds, we divide the 'date' column by 1000.
     * <p>
     * See sqlite strftime() docs <a href="https://www.sqlite.org/lang_datefunc.html">here</a>.
     *
     * @param from the starting date in milliseconds
     * @param to   the ending date in milliseconds
     * @return List of total costs per weekday
     */
    @Query("SELECT strftime('%w', date/1000, 'unixepoch', 'localtime') as day, SUM(totalcost) as cost " +
            "FROM purchase " +
            "WHERE date BETWEEN :from AND :to " +
            "GROUP BY strftime('%w', date/1000, 'unixepoch', 'localtime')" +
            "ORDER BY day")
    List<WeekdayCost> getCost(long from, long to);

    @Query("SELECT * FROM purchase")
    LiveData<List<Purchase>> getAll();
}
