package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.pleon.buyt.model.Subscription

@Dao
interface SubscriptionDao {

    @Insert(onConflict = REPLACE)
    fun insertSubscription(subscription: Subscription)

    @Query("SELECT isPremium FROM Subscription")
    fun hasSubscription(): Boolean
}
