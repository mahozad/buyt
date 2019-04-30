package com.pleon.buyt.database.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StoresViewModel.Sort
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.*

@Dao
interface StoreDao {

    @Transaction
    fun getStoreDetails(sort: Sort): List<StoreDetail> {
        val sqlSortColumn = when (sort) {
            TOTAL_SPENDING -> "totalSpending"
            PURCHASE_COUNT -> "purchaseCount"
            STORE_CATEGORY -> "category"
            STORE_NAME -> "name"
        }
        val query = SimpleSQLiteQuery("SELECT Store.*, SUM(cost) AS totalSpending, COUNT(purchaseId) AS purchaseCount " +
                "FROM Store JOIN (SELECT SUM(totalPrice) AS cost, purchaseId, purchase.storeId FROM Item NATURAL JOIN Purchase GROUP BY purchaseId) AS ip ON Store.storeId = ip.storeId " +
                "WHERE Store.isFlaggedForDeletion = 0 " +
                "GROUP BY Store.storeId " +
                "ORDER BY $sqlSortColumn DESC")
        return getDetails(query)
    }

    /**
     * @RawQuery is used because dynamic parameters cannot be used in ORDER BY clauses
     */
    @RawQuery
    fun getDetails(query: SupportSQLiteQuery): List<StoreDetail>

    @Query("SELECT * FROM Store")
    fun getAllList(): List<Store>

    @Query("SELECT * FROM Store WHERE :sinLat * sinLat + :cosLat * cosLat * (cosLng * :cosLng + sinLng * :sinLng) > :maxDistance")
    fun getNearStores(sinLat: Double, cosLat: Double, sinLng: Double, cosLng: Double, maxDistance: Double): List<Store>

    @Insert
    fun insert(store: Store): Long

    @Update
    fun updateAll(stores: Collection<Store>)

    @Delete
    fun delete(store: Store)
}
