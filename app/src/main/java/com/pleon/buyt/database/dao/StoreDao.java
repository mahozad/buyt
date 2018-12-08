package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
public interface StoreDao {

    @Insert
    long insert(Store store);

//    @Query("SELECT * FROM Store")
//    LiveData<List<Store>> getAll();
}
