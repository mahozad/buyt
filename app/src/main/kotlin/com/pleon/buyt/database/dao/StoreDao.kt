package com.pleon.buyt.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pleon.buyt.model.Store

@Dao
interface StoreDao {

    @Query("SELECT store.*, sum(cost) as totalSpending, count(purchaseId) as purchaseCount  " +
            "FROM Store join (select sum(totalPrice) as cost, purchaseId, purchase.storeId from item natural join purchase group by purchaseId) as ip on store.storeId=ip.storeId " +
            "group by store.storeId")
    fun getStoreDetails(): LiveData<List<StoreDetail>>

    class StoreDetail {
        @Embedded
        lateinit var store: Store
        var purchaseCount: Int = 0
        var totalSpending: Int = 0
    }

    @Query("SELECT * FROM Store")
    fun getAllList(): List<Store>

    @Query("SELECT * FROM Store WHERE :sinLat * sinLat + :cosLat * cosLat * (cosLng * :cosLng + sinLng * :sinLng) > :maxDistance")
    fun getNearStores(sinLat: Double, cosLat: Double, sinLng: Double, cosLng: Double, maxDistance: Double): List<Store>

    @Insert
    fun insert(store: Store): Long

    @Update
    fun update(store: Store)

    @Delete
    fun delete(store: Store)
}
