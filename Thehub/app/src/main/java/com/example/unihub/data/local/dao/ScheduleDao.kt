package com.example.unihub.data.local.dao

import androidx.room.*
import com.example.unihub.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE userId = :userId ORDER BY startTime ASC")
    fun getAllSchedules(userId: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE userId = :userId AND isCountdown = 1 ORDER BY startTime ASC")
    fun getCountdowns(userId: String): Flow<List<Schedule>>

    @Delete
    suspend fun delete(schedule: Schedule)
}
