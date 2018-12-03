package com.pleon.buyt.database;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface PurchaseDao {

    @Insert
    long insertPurchase(Purchase purchase);
}
