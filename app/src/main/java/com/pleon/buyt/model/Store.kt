package com.pleon.buyt.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(indices = [Index("latitude", "longitude")])
class Store(@field:Embedded
            var location: Coordinates?, var name: String?, var category: Category?) : Serializable {

    @PrimaryKey(autoGenerate = true)
    var storeId: Long = 0
    // To fix the bug that happens when two stores are deleted in a row (the first appears again)
    var isFlaggedForDeletion = false
}
