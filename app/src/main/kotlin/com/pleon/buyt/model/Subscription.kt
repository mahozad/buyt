package com.pleon.buyt.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Subscription {

    @PrimaryKey
    var isPremium = true
}
