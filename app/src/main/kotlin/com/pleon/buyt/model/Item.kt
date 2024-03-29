package com.pleon.buyt.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pleon.buyt.R
import java.util.*

/**
 * [isFlaggedForDeletion] was added to fix the bug that happened when two items were deleted
 * in row (which caused the first item to appear again)
 */
@Entity(
    /* TODO: Uncomment this and migrate the database */
    // foreignKeys = [
    //     ForeignKey(
    //         entity = Purchase::class,
    //         parentColumns = ["purchaseId"],
    //         childColumns = ["purchaseId"]
    //     )
    // ]
)
class Item(val name: String,
           @Embedded val quantity: Quantity,
           var category: Category,
           val isUrgent: Boolean,
           var isBought: Boolean) {

    data class Quantity(
        // What happens if we restore from old database file in app (where column name is value)?
        // TODO: @ColumnInfo(name = "quantity")
        val value: Long,
        val unit: Unit
    ) {

        enum class Unit(val nameRes: Int) { UNIT(R.string.qty_unit), KILOGRAM(R.string.qty_kilogram), GRAM(R.string.qty_gram) }

        override fun toString() = "$value ${unit.toString().toLowerCase(Locale.US)}"
    }

    @PrimaryKey(autoGenerate = true)
    var itemId: Long = 0
    var purchaseId: Long = 0
    var totalPrice: Long = 0
    var description: String? = null

    // For display purposes
    var position: Int = 0
    var isFlaggedForDeletion = false
}
