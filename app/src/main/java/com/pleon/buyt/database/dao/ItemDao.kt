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
            "WHERE bought = 0 AND flaggedForDeletion = 0 " +
            "ORDER BY urgent DESC, position ASC")
    fun getAll(): LiveData<List<Item>>

    @Query("SELECT count(*) FROM Item")
    fun getCount(): Long

    @Query("SELECT DISTINCT name from item")
    fun getItemNames(): LiveData<Array<String>>

    @Insert(onConflict = REPLACE)
    fun insert(item: Item): Long

    // FIXME: very heavy operation. @Update method, updates all fields of an entity
    // so this method updates all fields of all of the given items!
    @Update
    fun updateAll(items: Collection<Item>)

    @Delete
    fun delete(item: Item)
}
