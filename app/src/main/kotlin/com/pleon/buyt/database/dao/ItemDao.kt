package com.pleon.buyt.database.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.pleon.buyt.database.dto.ItemNameCat
import com.pleon.buyt.model.Item
import kotlinx.coroutines.flow.Flow

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
abstract class ItemDao {

    /**
     * Because we return a flow object (and the calculations and updates to its
     * value occur later aka asynchronously), there is no need to use suspend
     * modifier for the function or even the flowOn to run the flow calculation
     * on another dispatcher like IO because the Room library itself does that
     * (i.e. it is main-safe).
     *
     * How Flow is implemented in Room?
     *
     * When collected, the flow initially emits the first result for the query.
     * Once that result is processed, the flow suspends until one of the tables
     * (in this query, only the Item table) changes. At this point, nothing is
     * happening in the system until one of the tables changes and the flow resumes.
     * When the flow resumes, it makes another main-safe query and emits the results.
     * (main-safe means it doesn't block the UI—aka main— thread; in other words,
     * the main-safe function runs in another thread by using a dispatcher other
     * than the MAIN like this:
     * ```kotlin
     * fun query() = withContext(Dispatchers.IO) {
     *   doTheQuery()
     * }
     * ```
     * This whole process continues forever in an infinite loop.
     */
    @Query("""
        SELECT * FROM Item
        WHERE isBought = 0 AND isFlaggedForDeletion = 0
        ORDER BY isUrgent DESC, position ASC""")
    abstract fun getAll(): Flow<List<Item>>

    @Query("""SELECT COUNT(*) FROM Item""")
    abstract fun getCount(): Long

    @Query("""SELECT name, category FROM Item GROUP BY name""")
    abstract fun getItemNameCats(): Flow<List<ItemNameCat>>

    @Transaction
    open suspend fun insert(item: Item): Long {
        val itemId = insertItem(item)
        if (!item.isBought) updatePosition(itemId) // no need to update position of purchased item
        return itemId
    }

    @Insert(onConflict = REPLACE)
    protected abstract suspend fun insertItem(item: Item): Long

    @Query("""UPDATE Item SET position = (SELECT MAX(position) + 1 FROM Item) WHERE itemId = :itemId""")
    protected abstract suspend fun updatePosition(itemId: Long)

    @Update
    abstract suspend fun updateItem(item: Item)

    /**
     * NOTE: This is a very heavy operation. @Update method, updates all fields of an entity
     *  so this method updates all fields of all of the given items!
     */
    @Update
    abstract suspend fun updateAll(items: Collection<Item>)

    @Transaction
    open suspend fun delete(item: Item) {
        deleteItem(item)
        updateOtherPositions(item.position)
    }

    @Delete
    protected abstract suspend fun deleteItem(item: Item)

    @Query("""UPDATE Item SET position = position - 1 WHERE position > :itemPosition""")
    protected abstract suspend fun updateOtherPositions(itemPosition: Int)
}
