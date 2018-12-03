package com.pleon.buyt.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface ShopDao {

    @Insert
    long insertStore(Shop shop);

    @Query("SELECT * FROM Shop")
    List<Shop> getStores();
}
