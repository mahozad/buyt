package com.pleon.buyt.model

import androidx.room.*
import com.pleon.buyt.R

@Entity
class Item(val name: String, @Embedded val quantity: Quantity, var category: Category,
           val isUrgent: Boolean, var isBought: Boolean) {

    class Quantity(val quantity: Long, val unit: Unit) {
        enum class Unit(val nameRes: Int) {
            // FIXME: due to a bug in add item fragment the units are arranges in reverse order
            KILOGRAM(R.string.qty_kilogram), GRAM(R.string.qty_gram), UNIT(R.string.qty_unit)
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
    @Ignore
    var isExpanded = false
    var position: Int = 0
    // To fix the bug that happens when two items are deleted in row (the first appears again)
    var isFlaggedForDeletion = false
}
