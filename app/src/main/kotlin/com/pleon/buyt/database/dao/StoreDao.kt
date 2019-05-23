package com.pleon.buyt.database.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StoresViewModel.Sort
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.*

@Dao
abstract class StoreDao {

    @Transaction
    open fun getDetails(sort: Sort): List<StoreDetail> {
        val sqlSortColumn = when (sort) {
            TOTAL_SPENDING -> "totalSpending"
            PURCHASE_COUNT -> "purchaseCount"
            STORE_CATEGORY -> "category"
            STORE_NAME -> "name"
        }
        val query = SimpleSQLiteQuery("SELECT Store.*, SUM(cost) AS totalSpending, COUNT(purchaseId) AS purchaseCount " +
                "FROM Store LEFT JOIN (SELECT SUM(totalPrice) AS cost, purchaseId, purchase.storeId FROM Item NATURAL JOIN Purchase GROUP BY purchaseId) AS ip ON Store.storeId = ip.storeId " +
                "WHERE Store.isFlaggedForDeletion = 0 " +
                "GROUP BY Store.storeId " +
                "ORDER BY $sqlSortColumn DESC")
        return getStoreDetails(query)
    }

    /**
     * @RawQuery is used because dynamic parameters cannot be used in ORDER BY clauses
     */
    @RawQuery
    protected abstract fun getStoreDetails(query: SupportSQLiteQuery): List<StoreDetail>

    @Query("SELECT * FROM Store")
    abstract fun getAllSync(): List<Store>

    @Query("SELECT * FROM Store WHERE :sinLat * sinLat + :cosLat * cosLat * (cosLng * :cosLng + sinLng * :sinLng) > :maxDistance")
    abstract fun getNearStores(sinLat: Double, cosLat: Double, sinLng: Double, cosLng: Double, maxDistance: Double): List<Store>

    @Insert
    abstract fun insert(store: Store): Long

    @Update
    abstract fun updateAll(stores: Collection<Store>)

    @Delete
    abstract fun delete(store: Store)
}
