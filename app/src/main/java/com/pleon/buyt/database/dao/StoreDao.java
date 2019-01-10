package com.pleon.buyt.database.dao;

import com.pleon.buyt.model.Store;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// In DAOs, we specify SQL queries and associate them with method calls
@Dao
public interface StoreDao {

    @Query("SELECT * FROM Store")
    List<Store> getAll();

    @Query("SELECT * FROM Store WHERE :curSinLat * sinLat + :curCosLat * cosLat * (cosLng * :curCosLng + sinLng * :curSinLng) > :maxDistance")
    List<Store> findNearStores(double curSinLat, double curCosLat, double curSinLng, double curCosLng, double maxDistance);

    @Insert
    long insert(Store store);
}
