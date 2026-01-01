package com.example.unihub.viewmodel

import androidx.lifecycle.*
import com.example.unihub.data.model.Expense
import com.example.unihub.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {
    private val _userId = MutableLiveData<String>()
    
    private val _dateRange = MutableLiveData<Pair<Long, Long>>(getDefaultDateRange())
    val dateRange: LiveData<Pair<Long, Long>> = _dateRange

    private val _userIdAndRange = MediatorLiveData<Pair<String, Pair<Long, Long>>>().apply {
        addSource(_userId) { uid ->
            val range = _dateRange.value
            if (uid != null && range != null) value = Pair(uid, range)
        }
        addSource(_dateRange) { range ->
            val uid = _userId.value
            if (uid != null && range != null) value = Pair(uid, range)
        }
    }

    val allExpenses: LiveData<List<Expense>> = _userIdAndRange.switchMap { (uid, range) ->
        repository.getExpensesInRange(uid, range.first, range.second).asLiveData()
    }
    
    val categorySummary: LiveData<Map<String, Double>> = _userId.switchMap { uid ->
        repository.getCategorySummary(uid).asLiveData()
    }

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun setDateRange(start: Long, end: Long) {
        _dateRange.value = Pair(start, end)
    }

    private fun getDefaultDateRange(): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        
        calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        
        return Pair(start, end)
    }

    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
}

class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
