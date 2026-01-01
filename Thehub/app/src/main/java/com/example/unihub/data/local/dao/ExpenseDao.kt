package com.example.unihub.data.local.dao

import androidx.room.*
import com.example.unihub.data.model.CategorySummary
import com.example.unihub.data.model.Expense
import kotlinx.coroutines.flow.Flow

/**
 * 账目数据访问接口
 */
@Dao
interface ExpenseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllExpenses(userId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getExpensesInRange(userId: String, start: Long, end: Long): Flow<List<Expense>>

    @Query("SELECT category, SUM(amount) as total FROM expenses WHERE userId = :userId GROUP BY category")
    fun getCategorySummary(userId: String): Flow<List<CategorySummary>>

    @Delete
    suspend fun delete(expense: Expense)
}
