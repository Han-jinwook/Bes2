package com.bes2.model

data class ImageItem(
    val id: Long = 0L,
    val uri: String,
    val filePath: String,
    val timestamp: Long,
    val pHash: String? = null,
    val blurScore: Float? = null,
    val exposureScore: Float? = null,
    val nimaScore: Float? = null,
    val clusterId: Long? = null,
    val status: String,
    val isSelectedByUser: Boolean = false,
    val isUploaded: Boolean = false
)