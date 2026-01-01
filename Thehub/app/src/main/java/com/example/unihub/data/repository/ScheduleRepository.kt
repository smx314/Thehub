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

    /**
     * 从用户指定的远程 URL 获取数据并同步到本地数据库
     */
    suspend fun refreshSchedules(userId: String, url: String) {
        try {
            // 1. 从用户指定的 URL 获取最新数据
            val remoteSchedules = apiService.getSchedules(url)
            
            // 2. 更新本地数据库
            remoteSchedules.forEach { schedule ->
                // 确保同步过来的数据也绑定当前用户
                val scheduleWithUser = schedule.copy(userId = userId)
                scheduleDao.insert(scheduleWithUser)
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun insert(schedule: Schedule) {
        scheduleDao.insert(schedule)
    }

    suspend fun delete(schedule: Schedule) {
        scheduleDao.delete(schedule)
    }
}
