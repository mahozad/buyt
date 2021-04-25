package com.pleon.buyt.serializer

interface Serializer<T> {
    val mimeType: String
    val fileExtension: String
    fun serialize(entities: List<T>)
    fun setUpdateListener(listener: (progress: Int, fragment: String) -> Unit)
    fun setFinishListener(listener: () -> Unit)
}
