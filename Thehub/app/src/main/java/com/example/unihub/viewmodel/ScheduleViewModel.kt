package com.example.unihub.viewmodel

import androidx.lifecycle.*
import com.example.unihub.data.model.Schedule
import com.example.unihub.data.repository.ScheduleRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleViewModel(private val repository: ScheduleRepository) : ViewModel() {
    private val _userId = MutableLiveData<String>()

    val allSchedules: LiveData<List<Schedule>> = _userId.switchMap { uid ->
        repository.getAllSchedules(uid).asLiveData()
    }
    
    val countdowns: LiveData<List<Schedule>> = _userId.switchMap { uid ->
        repository.getCountdowns(uid).asLiveData()
    }

    private val _selectedYear = MutableLiveData<Int>(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: LiveData<Int> = _selectedYear

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.value = year
    }

    /**
     * 调用 Repository 同步用户指定 URL 的云端数据
     */
    fun refreshSchedules(url: String, onSuccess: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        try {
            val uid = _userId.value ?: "guest"
            repository.refreshSchedules(uid, url)
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "未知错误")
        }
    }

    fun insert(schedule: Schedule) = viewModelScope.launch {
        repository.insert(schedule)
    }

    fun delete(schedule: Schedule) = viewModelScope.launch {
        repository.delete(schedule)
    }
}

class ScheduleViewModelFactory(private val repository: ScheduleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScheduleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
