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
    void update(Item item);

    @Delete
    void delete(Item item);

    /* Annotating a method with @Transaction makes sure that all database operations you’re
    executing in that method will be run inside one transaction.
    The transaction will fail when an exception is thrown in the method body. */
   /* @Transaction
    void updateData(List<Item> items) {
        deleteAllUsers();
        insertAll(users);
    } */
}
