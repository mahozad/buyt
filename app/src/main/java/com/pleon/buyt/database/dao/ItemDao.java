package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Item;

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

    @Query("SELECT * FROM Item")
    LiveData<List<Item>> getAll();

    @Query("SELECT * FROM Item WHERE id= :id")
    Item getById(int id);

    @Query("SELECT count(*) FROM Item")
    long getCount();

    @Insert(onConflict = REPLACE)
    long insert(Item item);

    @Update
    void update(Item items);

    @Delete
    void delete(Item items);
}
