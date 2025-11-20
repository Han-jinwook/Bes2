package com.bes2.photos_integration.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NaverMyBoxApiService {
    // This is a placeholder URL. Naver MyBox API documentation is required for the exact endpoint.
    // Assuming a generic upload structure for now.
    
    @Multipart
    @POST("upload") // Placeholder endpoint
    suspend fun uploadFile(
        @Header("Authorization") authToken: String,
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody
    ): Response<String> 
}
