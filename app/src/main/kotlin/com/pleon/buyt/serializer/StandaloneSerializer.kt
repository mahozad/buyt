package com.pleon.buyt.serializer

/**
 * A serializer that requires creating the output file directly itself.
 */
interface StandaloneSerializer<T> : Serializer<T> {
    val defaultOutputFileName: String
}
