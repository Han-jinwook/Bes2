package com.bes2.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_clusters")
data class ImageClusterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "best_image_uri")
    val bestImageUri: String? = null, // 1등 이미지 URI
    @ColumnInfo(name = "second_best_image_uri")
    val secondBestImageUri: String? = null, // 2등 이미지 URI
    @ColumnInfo(name = "creation_time")
    val creationTime: Long,
    @ColumnInfo(name = "review_status")
    val reviewStatus: String = "PENDING_REVIEW"
)
