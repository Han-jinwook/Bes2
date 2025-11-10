package com.bes2.photos_integration.network

import com.bes2.photos_integration.model.BatchCreateMediaItemsRequest
import com.bes2.photos_integration.model.BatchCreateMediaItemsResponse
import com.bes2.photos_integration.model.ListAlbumsResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface GooglePhotosApiService {

    /**
     * Uploads the raw bytes of a media file.
     * This call returns an upload token which is used to create a media item.
     */
    @POST("https://photoslibrary.googleapis.com/v1/uploads")
    suspend fun uploadMedia(
        @Header("Authorization") authToken: String,
        @Header("X-Goog-Upload-Content-Type") mimeType: String,
        @Header("X-Goog-Upload-Protocol") protocol: String = "raw",
        @Body requestBody: RequestBody
    ): Response<String> // The response body is the plain text upload token

    /**
     * Creates a media item in a user's Google Photos library.
     */
    @POST("https://photoslibrary.googleapis.com/v1/mediaItems:batchCreate")
    suspend fun createMediaItem(
        @Header("Authorization") authToken: String,
        @Body request: BatchCreateMediaItemsRequest
    ): Response<BatchCreateMediaItemsResponse>

    /**
     * Lists all albums in a user's Google Photos library.
     */
    @GET("https://photoslibrary.googleapis.com/v1/albums")
    suspend fun getAlbums(
        @Header("Authorization") authToken: String,
        @Query("pageSize") pageSize: Int = 50 // Max 50
    ): Response<ListAlbumsResponse>
}
