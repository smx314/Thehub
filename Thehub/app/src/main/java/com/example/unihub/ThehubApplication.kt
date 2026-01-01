package com.example.unihub

import android.app.Application
import com.example.unihub.data.local.AppDatabase
import com.example.unihub.data.remote.ApiService
import com.example.unihub.data.repository.ExpenseRepository
import com.example.unihub.data.repository.FocusRepository
import com.example.unihub.data.repository.ScheduleRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ThehubApplication : Application() {
    // 使用延迟初始化，仅在需要时创建数据库和存储库
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // 通用数据同步服务 (示例 URL，你可以修改为真实的服务器地址)
    private val apiRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thehub.com/") // TODO: 替换为真实的服务器地址
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val apiService by lazy { apiRetrofit.create(ApiService::class.java) }

    val expenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val scheduleRepository by lazy { ScheduleRepository(database.scheduleDao(), apiService) }
    val focusRepository by lazy { FocusRepository(database.focusDao()) }
}
