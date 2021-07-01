package com.pleon.buyt.util

/**
 * Produces hex value without alpha like so: #xxyyzz
 */
fun Int.toHexColor() = "#%06x".format(this and 0xffffff)
