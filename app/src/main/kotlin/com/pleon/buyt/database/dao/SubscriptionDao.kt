package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Transaction
import com.pleon.buyt.model.Subscription

@Dao
abstract class SubscriptionDao {

    @Transaction
    open suspend fun insertSubscription(subscription: Subscription) {
        deleteAll()
        insert(subscription)
    }

    @Insert(onConflict = REPLACE)
    protected abstract suspend fun insert(subscription: Subscription)

    @Query("""DELETE FROM Subscription""")
    protected abstract suspend fun deleteAll()

    @Query("""SELECT token FROM Subscription""")
    abstract suspend fun getToken(): String
}
