package com.bes2.photos_integration.model

import com.google.gson.annotations.SerializedName

// Model for mediaItems:batchCreate request
data class BatchCreateMediaItemsRequest(
    @SerializedName("newMediaItems") val newMediaItems: List<NewMediaItem>
)

data class NewMediaItem(
    @SerializedName("description") val description: String,
    @SerializedName("simpleMediaItem") val simpleMediaItem: SimpleMediaItem
)

data class SimpleMediaItem(
    @SerializedName("uploadToken") val uploadToken: String,
    @SerializedName("fileName") val fileName: String
)

// Model for mediaItems:batchCreate response
data class BatchCreateMediaItemsResponse(
    @SerializedName("newMediaItemResults") val newMediaItemResults: List<NewMediaItemResult>
)

data class NewMediaItemResult(
    @SerializedName("uploadToken") val uploadToken: String,
    @SerializedName("status") val status: Status,
    @SerializedName("mediaItem") val mediaItem: MediaItem?
)

data class Status(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

// Model for the created MediaItem in the response
data class MediaItem(
    @SerializedName("id") val id: String,
    @SerializedName("productUrl") val productUrl: String,
    @SerializedName("mimeType") val mimeType: String,
    @SerializedName("mediaMetadata") val mediaMetadata: MediaMetadata
)

data class MediaMetadata(
    @SerializedName("creationTime") val creationTime: String,
    @SerializedName("width") val width: String,
    @SerializedName("height") val height: String
)

// Models for albums:list response
data class ListAlbumsResponse(
    @SerializedName("albums") val albums: List<Album>?,
    @SerializedName("nextPageToken") val nextPageToken: String?
)

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("productUrl") val productUrl: String,
    @SerializedName("mediaItemsCount") val mediaItemsCount: String,
    @SerializedName("coverPhotoBaseUrl") val coverPhotoBaseUrl: String
)
