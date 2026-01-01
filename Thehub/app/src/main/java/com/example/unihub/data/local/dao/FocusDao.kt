package com.example.unihub.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.unihub.data.model.FocusRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusDao {
    @Insert
    suspend fun insert(record: FocusRecord)

    @Query("SELECT * FROM focus_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllRecords(userId: String): Flow<List<FocusRecord>>

    @Query("SELECT SUM(durationSeconds) FROM focus_records WHERE userId = :userId AND timestamp >= :startOfDay")
    fun getTotalDurationToday(userId: String, startOfDay: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM focus_records WHERE userId = :userId AND timestamp >= :startOfDay")
    fun getCountToday(userId: String, startOfDay: Long): Flow<Int>

    @Query("SELECT SUM(durationSeconds) FROM focus_records WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getDurationInRange(userId: String, startTime: Long, endTime: Long): Int?

    @Query("SELECT COUNT(*) FROM focus_records WHERE userId = :userId AND timestamp BETWEEN :startTime AND :endTime")
    suspend fun getCountInRange(userId: String, startTime: Long, endTime: Long): Int
}
