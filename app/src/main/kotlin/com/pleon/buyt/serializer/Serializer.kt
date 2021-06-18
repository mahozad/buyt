package com.pleon.buyt.serializer

interface Serializer<T> {
    val mimeType: String
    val fileExtension: String
    var updateListener: (suspend (progress: Int, fragment: String) -> Unit)?
    var finishListener: (suspend () -> Unit)?
    suspend fun serialize(entities: List<T>)
}
