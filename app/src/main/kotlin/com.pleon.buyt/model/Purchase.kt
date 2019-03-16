package com.pleon.buyt.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Purchase(@field:ForeignKey(entity = Store::class, parentColumns = ["id"], childColumns = ["storeId"])
               var storeId: Long, var date: Date?) {

    @PrimaryKey(autoGenerate = true)
    var purchaseId: Long = 0
}
