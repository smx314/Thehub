package com.example.unihub.data.repository

import com.example.unihub.data.local.dao.FocusDao
import com.example.unihub.data.model.FocusRecord
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FocusRepository(private val focusDao: FocusDao) {

    fun getCountToday(userId: String): Flow<Int> {
        return focusDao.getCountToday(userId, getStartOfDay())
    }

    fun getTotalDurationToday(userId: String): Flow<Int?> {
        return focusDao.getTotalDurationToday(userId, getStartOfDay())
    }

    suspend fun insert(record: FocusRecord) {
        focusDao.insert(record)
    }

    suspend fun getStatsInRange(userId: String, startTime: Long, endTime: Long): Pair<Int, Int> {
        val count = focusDao.getCountInRange(userId, startTime, endTime)
        val duration = focusDao.getDurationInRange(userId, startTime, endTime) ?: 0
        return Pair(count, duration)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
