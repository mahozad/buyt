package com.pleon.buyt.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Purchase(val date: Date) {

    @PrimaryKey(autoGenerate = true)
    var purchaseId: Long = 0

    @ForeignKey(entity = Store::class, parentColumns = ["storeId"], childColumns = ["storeId"])
    var storeId: Long = 0
}
