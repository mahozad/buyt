package com.pleon.buyt.serializer

/* sealed */ interface Serializer<T> {
    val mimeType: String
    val fileExtension: String
    var updateListener: (suspend (progress: Int, fragment: String) -> Unit)?
    var finishListener: (suspend () -> Unit)?
    suspend fun serialize(entities: List<T>)
}

/**
 * A serializer that requires creating the output file directly itself.
 */
interface StandaloneSerializer<T> : Serializer<T> {
    val defaultOutputFileName: String
}

/**
 * A serializer that does not require creating the output file itself.
 */
interface InteractiveSerializer<T> : Serializer<T>
