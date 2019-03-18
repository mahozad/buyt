package com.pleon.buyt.model

import android.location.Location
import java.io.Serializable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Coordinates(val latitude: Double, val longitude: Double) : Serializable {

    constructor(location: Location) : this(location.latitude, location.longitude)

    var cosLat: Double = cos(latitude * PI / 180)
    var cosLng: Double = cos(longitude * PI / 180)
    var sinLat: Double = sin(latitude * PI / 180)
    var sinLng: Double = sin(longitude * PI / 180)
}
