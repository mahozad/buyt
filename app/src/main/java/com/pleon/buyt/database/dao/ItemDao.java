package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Item;

import java.util.Collection;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.REPLACE;

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
public interface ItemDao {

    @Query("SELECT * FROM Item " +
            "WHERE bought = 0 AND flaggedForDeletion = 0 " +
            "ORDER BY urgent DESC, position ASC")
    LiveData<List<Item>> getAll();

    @Query("SELECT count(*) FROM Item")
    long getCount();

    @Insert(onConflict = REPLACE)
    long insert(Item item);

    // FIXME: very heavy operation. @Update method, updates all fields of an entity
    // so this method updates all fields of all of the given items!
    @Update
    void updateAll(Collection<Item> items);

    @Delete
    void delete(Item item);
}
