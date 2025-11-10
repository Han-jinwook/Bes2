package com.bes2.photos_integration.repository

import com.bes2.photos_integration.auth.GooglePhotosAuthManager
import com.bes2.photos_integration.model.Album
import com.bes2.photos_integration.network.GooglePhotosApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePhotosRepository @Inject constructor(
    private val authManager: GooglePhotosAuthManager,
    private val apiService: GooglePhotosApiService
) {

    suspend fun listAlbums(): Result<List<Album>> = withContext(Dispatchers.IO) {
        val accessToken = authManager.getAccessToken()
            ?: return@withContext Result.failure(IllegalStateException("User not authenticated."))

        val authToken = "Bearer $accessToken"

        try {
            val response = apiService.getAlbums(authToken)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.albums ?: emptyList())
            } else {
                val errorBody = response.errorBody()?.string() ?: "Failed to list albums"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
