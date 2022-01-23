package com.pleon.buyt.model

import androidx.room.*
import java.io.Serializable

@Entity(indices = [Index("latitude", "longitude")])
class Store(
    @Embedded
    val location: Coordinates,
    val name: String,
    val category: Category
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var storeId: Long = 0

    // To fix the bug that happens when two stores are deleted in a row (the first appears again)
    var isFlaggedForDeletion = false

    @Ignore var order: Int = 0
}
