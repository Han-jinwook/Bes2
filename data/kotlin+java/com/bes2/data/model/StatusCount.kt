package com.bes2.data.model

import androidx.room.ColumnInfo

data class StatusCount(
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "count") val count: Int
)
