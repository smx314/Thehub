package com.example.unihub.data.repository

import com.example.unihub.data.local.dao.ExpenseDao
import com.example.unihub.data.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepository(private val expenseDao: ExpenseDao) {
    fun getAllExpenses(userId: String): Flow<List<Expense>> = expenseDao.getAllExpenses(userId)
    
    fun getCategorySummary(userId: String): Flow<Map<String, Double>> = expenseDao.getCategorySummary(userId).map { list ->
        list.associate { it.category to it.total }
    }

    fun getExpensesInRange(userId: String, start: Long, end: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesInRange(userId, start, end)
    }

    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }
}
