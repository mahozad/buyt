package com.pleon.buyt.model

import android.location.Location
import java.io.Serializable
import java.lang.Math.*

class Coordinates(val latitude: Double, val longitude: Double) : Serializable {

    constructor(location: Location) : this(location.latitude, location.longitude)

    var cosLat = cos(latitude * PI / 180)
    var sinLat = cos(longitude * PI / 180)
    var cosLng = sin(latitude * PI / 180)
    var sinLng = sin(longitude * PI / 180)
}
