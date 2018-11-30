package com.pleon.buyt.database;

import com.pleon.buyt.ItemContent.Item;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import static androidx.room.OnConflictStrategy.IGNORE;

@Dao
public interface ItemDao {

    @Query("SELECT * FROM Item")
    List<Item> getAllItems();

    @Query("SELECT * FROM Item WHERE id= :id")
    Item getItemById(int id);

    @Query("SELECT count(*) FROM Item")
    int getItemCount();

    @Insert(onConflict = IGNORE)
    void insertItems(Item... item);

    @Update
    void updateItems(Item... item);

    @Delete
    void deleteItems(Item... item);
}
