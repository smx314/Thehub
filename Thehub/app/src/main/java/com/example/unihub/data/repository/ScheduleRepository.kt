package com.example.unihub.data.repository

import com.example.unihub.data.local.dao.ScheduleDao
import com.example.unihub.data.model.Schedule
import com.example.unihub.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(
    private val scheduleDao: ScheduleDao,
    private val apiService: ApiService
) {
    fun getAllSchedules(userId: String): Flow<List<Schedule>> = scheduleDao.getAllSchedules(userId)
    fun getCountdowns(userId: String): Flow<List<Schedule>> = scheduleDao.getCountdowns(userId)

    suspend fun insert(schedule: Schedule) {
        scheduleDao.insert(schedule)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.delete(schedule)
    }
}
