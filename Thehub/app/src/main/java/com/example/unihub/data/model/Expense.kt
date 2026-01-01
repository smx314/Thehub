package com.example.unihub.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 账目模型
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest",
    val amount: Double,
    val category: String,
    val note: String,
    val type: Int = 0, // 0: 支出, 1: 收入
    val timestamp: Long = System.currentTimeMillis()
)
