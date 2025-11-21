package com.bes2.data.repository

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getTotalImageCount(): Int {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        
        // Filter out screenshots based on path or bucket name
        // Note: This is a simple filter. Some devices might use different naming.
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Screenshot%")
        
        return try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                cursor.count
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
