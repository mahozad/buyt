package com.pleon.buyt.model

import androidx.room.*

@Entity
class Item(val name: String, @Embedded val quantity: Quantity, var category: Category,
           val isUrgent: Boolean, var isBought: Boolean) {

    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0 // TODO: change type of id here to int?
    @ForeignKey(entity = Purchase::class, parentColumns = ["purchaseId"], childColumns = ["purchaseId"])
    var purchaseId: Long = 0
    var description: String? = null
    var totalPrice: Long = 0

    // For display purposes
    @Ignore
    var isExpanded: Boolean = false
    var position: Int = 0
    // To fix the bug that happens when two items are deleted in row (the first appears again)
    var isFlaggedForDeletion = false
}
