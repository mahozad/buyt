package com.pleon.buyt.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(
    /* TODO: Uncomment this and migrate the database */
    // foreignKeys = [
    //     ForeignKey(entity = Store::class, parentColumns = ["storeId"], childColumns = ["storeId"])
    // ]
)
class Purchase(val date: Date) {

    @PrimaryKey(autoGenerate = true)
    var purchaseId: Long = 0
    var storeId: Long = 0
}
