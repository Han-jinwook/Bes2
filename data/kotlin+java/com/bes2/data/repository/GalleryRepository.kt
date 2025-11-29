package com.bes2.data.repository

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.bes2.data.model.MediaStoreImage
import com.bes2.data.model.ScreenshotItem
import com.bes2.model.ImageItem
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class DateGroup(
    val date: String, // YYYY-MM-DD
    val timestamp: Long,
    val count: Int,
    val representativeUri: String
)

@Singleton
class GalleryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getTotalImageCount(): Int {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Screenshot%")
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { it.count } ?: 0
        } catch (e: Exception) { 0 }
    }

    fun getScreenshotCount(): Int {
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Capture%", "%Screenshot%")
        
        return try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { it.count } ?: 0
        } catch (e: Exception) { 0 }
    }
    
    // [FIXED] Use systemDefault() timezone to match Home screen logic
    fun getImagesForDateString(dateString: String): List<MediaStoreImage> {
        val imageList = mutableListOf<MediaStoreImage>()
        
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(dateString, formatter)
            
            // Reverted to ZoneId.systemDefault()
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN)
            val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} <= ? AND " +
                            "(${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?)"
            val selectionArgs = arrayOf(
                startOfDay.toString(), // No need to divide by 1000, DATE_TAKEN is usually in ms (checking below)
                endOfDay.toString(),
                "%Screenshot%", 
                "%Screenshot%"
            )
            // Note: MediaStore.Images.Media.DATE_TAKEN is in milliseconds since 1970.
            
            val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val filePath = cursor.getString(dataColumn)
                    val timestamp = cursor.getLong(dateColumn)
                    val contentUri = ContentUris.withAppendedId(uri, id)
                    imageList.add(MediaStoreImage(id, contentUri.toString(), filePath, timestamp))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return imageList
    }

    fun getScreenshots(): List<ScreenshotItem> {
        val screenshotList = mutableListOf<ScreenshotItem>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN, MediaStore.Images.Media.SIZE)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} LIKE ? OR ${MediaStore.Images.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Capture%", "%Screenshot%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
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
        } catch (e: Exception) {}
        return screenshotList
    }

    fun getRecentImages(limit: Int, offset: Int): List<ImageItem> {
        val imageList = mutableListOf<ImageItem>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Screenshot%")
        
        try {
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
                        imageList.add(ImageItem(id = id, uri = contentUri.toString(), filePath = filePath, timestamp = dateTaken, status = "NEW"))
                    }
                }
            } else {
                val sortOrderWithLimit = "${MediaStore.Images.Media.DATE_TAKEN} DESC LIMIT $limit OFFSET $offset"
                context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrderWithLimit)?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val filePath = cursor.getString(dataColumn)
                        val dateTaken = cursor.getLong(dateColumn)
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                        imageList.add(ImageItem(id = id, uri = contentUri.toString(), filePath = filePath, timestamp = dateTaken, status = "NEW"))
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return imageList
    }

    fun findLargePhotoGroups(minCount: Int = 20): List<DateGroup> {
        val groups = mutableListOf<DateGroup>()
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_TAKEN)
        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} NOT LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ?"
        val selectionArgs = arrayOf("%Screenshot%", "%Screenshot%")
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        
        try {
            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                
                val tempMap = mutableMapOf<String, MutableList<Long>>()
                val idMap = mutableMapOf<String, Long>()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                var scanCount = 0
                while (cursor.moveToNext() && scanCount < 5000) {
                    val id = cursor.getLong(idColumn)
                    val timestamp = cursor.getLong(dateColumn)
                    val dateString = dateFormat.format(Date(timestamp))
                    
                    tempMap.getOrPut(dateString) { mutableListOf() }.add(timestamp)
                    if (!idMap.containsKey(dateString)) {
                        idMap[dateString] = id
                    }
                    scanCount++
                }

                tempMap.forEach { (date, timestamps) ->
                    if (timestamps.size >= minCount) {
                        val repId = idMap[date] ?: 0L
                        val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, repId)
                        groups.add(DateGroup(date, timestamps.maxOrNull() ?: 0L, timestamps.size, contentUri.toString()))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return groups.sortedByDescending { it.timestamp }
    }
}
