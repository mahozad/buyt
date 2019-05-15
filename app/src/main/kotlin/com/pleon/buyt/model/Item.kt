package com.pleon.buyt.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.pleon.buyt.R

@Entity
class Item(val name: String, @Embedded val quantity: Quantity, var category: Category,
           val isUrgent: Boolean, var isBought: Boolean) {

    class Quantity(val quantity: Long, val unit: Unit) {
        enum class Unit(val nameRes: Int) {
            UNIT(R.string.qty_unit), KILOGRAM(R.string.qty_kilogram), GRAM(R.string.qty_gram)
        }

        override fun toString() = "$quantity ${unit.toString().toLowerCase()}"
    }

    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0
    @ForeignKey(entity = Purchase::class, parentColumns = ["purchaseId"], childColumns = ["purchaseId"])
    var purchaseId: Long = 0
    var description: String? = null
    var totalPrice: Long = 0

    // For display purposes
    var position: Int = 0
    // To fix the bug that happens when two items are deleted in row (the first appears again)
    var isFlaggedForDeletion = false
}
