package com.example.unihub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 专注记录模型
 */
@Entity(tableName = "focus_records")
data class FocusRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest",
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)
