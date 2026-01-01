package com.example.unihub.util

import android.content.Context
import android.content.SharedPreferences

/**
 * 用户管理类，负责保存当前登录用户的标识
 */
object UserManager {
    private const val PREF_NAME = "unihub_user_prefs"
    private const val KEY_USER_ID = "current_user_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserId(context: Context, userId: String) {
        getPrefs(context).edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(context: Context): String {
        return getPrefs(context).getString(KEY_USER_ID, "guest") ?: "guest"
    }

    fun clearUserId(context: Context) {
        getPrefs(context).edit().remove(KEY_USER_ID).apply()
    }
}
