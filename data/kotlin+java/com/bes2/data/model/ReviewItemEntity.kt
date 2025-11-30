package com.bes2.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_items",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["source_type"]),
        Index(value = ["status"]),
        Index(value = ["cluster_id"])
    ]
)
data class ReviewItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val filePath: String,
    val timestamp: Long,
    
    // Status: NEW, ANALYZED, KEPT, DELETED, etc.
    val status: String = "NEW",
    
    // [KEY FIELD] Source of this item: DIET, MEMORY, INSTANT
    val source_type: String,

    // Analysis Data
    val pHash: String? = null,
    val nimaScore: Double? = null,
    val musiqScore: Float? = null,
    val blurScore: Float? = null,
    val exposureScore: Float? = null,
    val areEyesClosed: Boolean? = null,
    val smilingProbability: Float? = null,
    val faceEmbedding: ByteArray? = null,
    val embedding: ByteArray? = null,
    
    // Clustering
    val cluster_id: String? = null,
    
    // Upload sync
    val isUploaded: Boolean = false,
    val isSelectedByUser: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReviewItemEntity

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (filePath != other.filePath) return false
        if (timestamp != other.timestamp) return false
        if (status != other.status) return false
        if (source_type != other.source_type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + source_type.hashCode()
        return result
    }
}
