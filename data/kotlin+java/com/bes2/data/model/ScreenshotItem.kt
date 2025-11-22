package com.bes2.data.model

import android.net.Uri

data class ScreenshotItem(
    val id: Long,
    val uri: Uri,
    val dateTaken: Long,
    val size: Long,
    var isSelected: Boolean = true // Default selected for deletion
)
