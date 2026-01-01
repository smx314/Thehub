package com.example.unihub.data.remote

import com.example.unihub.data.model.Schedule
import retrofit2.http.GET
import retrofit2.http.Url

interface ApiService {
    /**
     * 从用户指定的 URL 获取最新的日程列表
     */
    @GET
    suspend fun getSchedules(@Url url: String): List<Schedule>
}
