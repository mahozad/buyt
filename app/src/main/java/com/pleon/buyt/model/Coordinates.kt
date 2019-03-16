package com.pleon.buyt.model

import android.location.Location
import java.io.Serializable
import java.lang.Math.*

class Coordinates(var latitude: Double, var longitude: Double) : Serializable {

    var cosLat = 0.0
    var sinLat = 0.0
    var cosLng = 0.0
    var sinLng = 0.0

    constructor(location: Location) : this(location.latitude, location.longitude)

    init {
        this.cosLat = cos(latitude * PI / 180)
        this.cosLng = cos(longitude * PI / 180)
        this.sinLat = sin(latitude * PI / 180)
        this.sinLng = sin(longitude * PI / 180)
    }
}
