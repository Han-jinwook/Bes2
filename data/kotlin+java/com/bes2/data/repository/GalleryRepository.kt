package com.bes2.data.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.bes2.data.model.ScreenshotItem
import com.bes2.model.ImageItem
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

    fun getScreenshotCount(): Int {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        
        // Expanded selection for screenshots
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Capture%", "%Screenshot%")
        
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

    fun getScreenshots(): List<ScreenshotItem> {
        val screenshotList = mutableListOf<ScreenshotItem>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE
        )
        
        // Expanded selection for screenshots
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Capture%", "%Screenshot%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val dateTaken = cursor.getLong(dateColumn)
                    val size = cursor.getLong(sizeColumn)
                    val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                    screenshotList.add(ScreenshotItem(id, contentUri, dateTaken, size))
                }
            }
        } catch (e: Exception) {
            // Handle exception or log error
        }
        return screenshotList
    }

    fun getRecentImages(limit: Int, offset: Int): List<ImageItem> {
        val imageList = mutableListOf<ImageItem>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN
        )

        // Filter out screenshots based on path or bucket name
        // Fixed selection arguments to match the placeholders
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Screenshot%")
        
        // Correct sort order query
        // Used only in fallback path now
        // val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            // We need to apply LIMIT and OFFSET manually or via Bundle if API level allows, 
            // but for compatibility, standard query with sort order is safer, then manual limit.
            // However, since we want efficiency, let's stick to the sortOrder string hack 
            // which works on many devices but isn't standard Android API.
            // Actually, the previous code injected LIMIT into sortOrder which is valid in SQLite but
            // ContentResolver might sanitize it.
            // Let's try a safer approach for pagination if possible, or stick to the hack if it works.
            // Given the user reported it's stuck, maybe the query failed silently.
            
            // Let's try to construct the query strictly.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val bundle = android.os.Bundle().apply {
                    putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                    putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
                    putString(android.content.ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_TAKEN).joinToString(", "))
                    putInt(android.content.ContentResolver.QUERY_ARG_SORT_DIRECTION, android.content.ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
                    putInt(android.content.ContentResolver.QUERY_ARG_LIMIT, limit)
                    putInt(android.content.ContentResolver.QUERY_ARG_OFFSET, offset)
                }
                context.contentResolver.query(uri, projection, bundle, null)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val filePath = cursor.getString(dataColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        imageList.add(
                            ImageItem(
                                id = id,
                                uri = contentUri.toString(),
                                filePath = filePath,
                                timestamp = dateTaken,
                                status = "NEW"
                            )
                        )
                    }
                }
            } else {
                // Fallback for older versions (using the hack)
                val sortOrderWithLimit = "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT $limit OFFSET $offset"
                context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrderWithLimit
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val filePath = cursor.getString(dataColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                        imageList.add(
                            ImageItem(
                                id = id,
                                uri = contentUri.toString(),
                                filePath = filePath,
                                timestamp = dateTaken,
                                status = "NEW"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
             // Log the error to understand why it fails
             e.printStackTrace()
        }
        return imageList
    }
}
