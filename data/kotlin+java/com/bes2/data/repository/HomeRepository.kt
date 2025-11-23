package com.bes2.data.repository

import com.bes2.data.dao.ImageItemDao
import com.bes2.data.model.StatusCount
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val imageItemDao: ImageItemDao
) {
    suspend fun getMonthlyStats(year: Int, month: Int): List<StatusCount> {
        val yearMonth = YearMonth.of(year, month)
        val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return imageItemDao.getStatsByDateRange(startOfMonth, endOfMonth)
    }

    suspend fun getYearlyStats(year: Int): List<StatusCount> {
        val startOfYear = LocalDate.of(year, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfYear = LocalDate.of(year, 12, 31).atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        return imageItemDao.getStatsByDateRange(startOfYear, endOfYear)
    }
}
