package com.example.unihub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日程/课表模型
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest",
    val title: String,
    val location: String,
    val startTime: Long,
    val endTime: Long,
    val type: Int, // 0: 课程, 1: 会议, 2: 倒数日
    val isCountdown: Boolean = false
)
