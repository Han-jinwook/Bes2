package com.bes2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// THIS IS A TEMPORARY FILE TO FIX BUILD ERRORS.
// IT IS NOT PART OF THE DATABASE SCHEMA ANYMORE.
// WILL BE REMOVED AFTER REFACTORING.
@Entity(
    tableName = "image_items",
    indices = [
        Index(value = ["uri"], unique = true),
    ]
)
data class ImageItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String = "",
    val filePath: String = "",
    val timestamp: Long = 0,
    val status: String = "NEW",
    val category: String? = null,
    val pHash: String? = null,
    val nimaScore: Double? = null,
    val musiqScore: Float? = null,
    val blurScore: Float? = null,
    val exposureScore: Float? = null,
    val areEyesClosed: Boolean? = null,
    val smilingProbability: Float? = null,
    val faceEmbedding: ByteArray? = null,
    val embedding: ByteArray? = null,
    val clusterId: String? = null,
    val isUploaded: Boolean = false,
    val isSelectedByUser: Boolean = false
)
