package com.pleon.buyt.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.pleon.buyt.model.Item

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
interface ItemDao {

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT * FROM Item " +
            "WHERE isBought = 0 AND isFlaggedForDeletion = 0 " +
            "ORDER BY isUrgent DESC, position ASC")
    fun getAll(): LiveData<List<Item>>

    @Query("SELECT count(*) FROM Item")
    fun getCount(): Long

    @Transaction
    fun getItemNamesAndCats() = getNameCats().associateBy({ it.name }, { it.category })

    // PRIVATE
    @Query("SELECT name, category from item group by name")
    fun getNameCats(): Array<NameCat>

    // PRIVATE
    class NameCat(val name: String, val category: String)

    @Transaction
    fun insertItem(item: Item) {
        val itemId = insert(item)
        if (!item.isBought) updateItemPosition(itemId) // no need to update position of purchased item
    }

    /**
     * Do NOT call this function; It is a private member of the interface. Call [insertItem] instead.
     */
    @Insert(onConflict = REPLACE)
    fun insert(item: Item): Long

    @Query("UPDATE Item SET position = (SELECT MAX(position) + 1 FROM Item) WHERE itemId = :itemId")
    fun updateItemPosition(itemId: Long)

    // FIXME: very heavy operation. @Update method, updates all fields of an entity
    // so this method updates all fields of all of the given items!
    @Update
    fun updateAll(items: Collection<Item>)

    @Transaction
    fun deleteItem(item: Item) {
        delete(item)
        updateBelowItemsPosition(item.itemId)
    }

    /**
     * Do NOT call this function; It is a private member of the interface. Call [deleteItem] instead.
     */
    @Delete
    fun delete(item: Item)

    @Query("UPDATE Item SET position = position - 1 WHERE itemId > :itemId")
    fun updateBelowItemsPosition(itemId: Long)
}
