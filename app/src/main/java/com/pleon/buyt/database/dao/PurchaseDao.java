package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Purchase;

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

//    @Query("SELECT * FROM purchase")
//    LiveData<List<Purchase>> getAll();
}
