package com.bes2.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_items",
    indices = [Index(value = ["uri"], unique = true)]
)
data class ImageItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val filePath: String,
    val timestamp: Long,
    var status: String,
    val pHash: String?,
    val nimaScore: Float?,
    val blurScore: Float?,
    val exposureScore: Float?,
    val areEyesClosed: Boolean?,
    val smilingProbability: Float?,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val faceEmbedding: ByteArray? = null,
    // DEFINITIVE FIX: Change type to String? to match the worker's UUID logic
    @ColumnInfo(name = "cluster_id") var clusterId: String? = null,
    val isSelectedByUser: Boolean = false,
    val isUploaded: Boolean = false,
    // DEFINITIVE FIX: Add the missing property to resolve the build error
    val isBestInCluster: Boolean = false,
    // [New] Category for content classification (MEMORY vs DOCUMENT)
    @ColumnInfo(name = "category") val category: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageItemEntity

        if (id != other.id) return false
        if (uri != other.uri) return false
        if (filePath != other.filePath) return false
        if (timestamp != other.timestamp) return false
        if (status != other.status) return false
        if (pHash != other.pHash) return false
        if (nimaScore != other.nimaScore) return false
        if (blurScore != other.blurScore) return false
        if (exposureScore != other.exposureScore) return false
        if (areEyesClosed != other.areEyesClosed) return false
        if (smilingProbability != other.smilingProbability) return false
        if (faceEmbedding != null) {
            if (other.faceEmbedding == null) return false
            if (!faceEmbedding.contentEquals(other.faceEmbedding)) return false
        } else if (other.faceEmbedding != null) return false
        if (clusterId != other.clusterId) return false
        if (isSelectedByUser != other.isSelectedByUser) return false
        if (isUploaded != other.isUploaded) return false
        if (isBestInCluster != other.isBestInCluster) return false
        if (category != other.category) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + uri.hashCode()
        result = 31 * result + filePath.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (pHash?.hashCode() ?: 0)
        result = 31 * result + (nimaScore?.hashCode() ?: 0)
        result = 31 * result + (blurScore?.hashCode() ?: 0)
        result = 31 * result + (exposureScore?.hashCode() ?: 0)
        result = 31 * result + (areEyesClosed?.hashCode() ?: 0)
        result = 31 * result + (smilingProbability?.hashCode() ?: 0)
        result = 31 * result + (faceEmbedding?.contentHashCode() ?: 0)
        result = 31 * result + (clusterId?.hashCode() ?: 0)
        result = 31 * result + isSelectedByUser.hashCode()
        result = 31 * result + isUploaded.hashCode()
        result = 31 * result + isBestInCluster.hashCode()
        result = 31 * result + (category?.hashCode() ?: 0)
        return result
    }
}
