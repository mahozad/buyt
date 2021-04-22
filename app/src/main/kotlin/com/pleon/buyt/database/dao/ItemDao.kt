package com.pleon.buyt.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.pleon.buyt.database.dto.ItemNameCat
import com.pleon.buyt.model.Item

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
abstract class ItemDao {

    @Query("""
        SELECT * FROM Item
        WHERE isBought = 0 AND isFlaggedForDeletion = 0
        ORDER BY isUrgent DESC, position ASC""")
    abstract fun getAll(): LiveData<List<Item>>

    @Query("""SELECT COUNT(*) FROM Item""")
    abstract fun getCount(): Long

    @Query("""SELECT name, category FROM Item GROUP BY name""")
    abstract fun getItemNameCats(): LiveData<Array<ItemNameCat>>

    @Transaction
    open fun insert(item: Item): Long {
        val itemId = insertItem(item)
        if (!item.isBought) updatePosition(itemId) // no need to update position of purchased item
        return itemId
    }

    @Insert(onConflict = REPLACE)
    protected abstract fun insertItem(item: Item): Long

    @Query("""UPDATE Item SET position = (SELECT MAX(position) + 1 FROM Item) WHERE itemId = :itemId""")
    protected abstract fun updatePosition(itemId: Long)

    /* FIXME: very heavy operation. @Update method, updates all fields of an entity
     *  so this method updates all fields of all of the given items! */
    @Update
    abstract fun updateAll(items: Collection<Item>)

    @Transaction
    open fun delete(item: Item) {
        deleteItem(item)
        updateOtherPositions(item.position)
    }

    @Delete
    protected abstract fun deleteItem(item: Item)

    @Query("DELETE FROM Item")
    abstract fun deleteAll()

    @Query("""UPDATE Item SET position = position - 1 WHERE position > :itemPosition""")
    protected abstract fun updateOtherPositions(itemPosition: Int)
}
