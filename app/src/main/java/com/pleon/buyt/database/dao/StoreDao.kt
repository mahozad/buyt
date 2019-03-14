package com.pleon.buyt.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pleon.buyt.model.Store

@Dao
interface StoreDao {

    @Query("SELECT * FROM Store")
    fun getAll(): LiveData<List<Store>>

    @Query("SELECT * FROM Store")
    fun getAllList(): List<Store>

    // language=RoomSql see [https://youtrack.jetbrains.com/issue/KT-13233] if the issue is resolved
    @Query("SELECT *" +
            "FROM Store " +
            "WHERE :sinLat * sinLat + :cosLat * cosLat * (cosLng * :cosLng + sinLng * :sinLng) > :maxDistance")
    fun findNearStores(sinLat: Double, cosLat: Double, sinLng: Double, cosLng: Double, maxDistance: Double): List<Store>

    @Insert
    fun insert(store: Store): Long

    @Update
    fun update(store: Store)

    @Delete
    fun delete(store: Store)
}
