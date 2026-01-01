package com.example.unihub.viewmodel

import androidx.lifecycle.*
import com.example.unihub.data.model.FocusRecord
import com.example.unihub.data.repository.FocusRepository
import kotlinx.coroutines.launch

class FocusViewModel(private val repository: FocusRepository) : ViewModel() {
    private val _userId = MutableLiveData<String>()

    val totalCountToday: LiveData<Int> = _userId.switchMap { uid ->
        repository.getCountToday(uid).asLiveData()
    }
    
    val totalDurationToday: LiveData<Int?> = _userId.switchMap { uid ->
        repository.getTotalDurationToday(uid).asLiveData()
    }

    private val _exportStats = MutableLiveData<Pair<Int, Int>>()
    val exportStats: LiveData<Pair<Int, Int>> = _exportStats

    fun setUserId(userId: String) {
        _userId.value = userId
    }

    fun addFocusRecord(durationSeconds: Int) = viewModelScope.launch {
        val uid = _userId.value ?: "guest"
        repository.insert(FocusRecord(userId = uid, durationSeconds = durationSeconds))
    }

    fun fetchStatsForExport(startTime: Long, endTime: Long) = viewModelScope.launch {
        val uid = _userId.value ?: "guest"
        val stats = repository.getStatsInRange(uid, startTime, endTime)
        _exportStats.value = stats
    }
}

class FocusViewModelFactory(private val repository: FocusRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FocusViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
