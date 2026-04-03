// File: com.example.myhatd.data.storage/TokenManager.kt

package com.example.myhatd.data.storage

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * Quản lý lưu trữ và truy xuất Session Token an toàn (dùng SharedPreferences).
 */
class TokenManager(context: Context) {
    // Sử dụng applicationContext để tránh memory leak
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private val TOKEN_KEY = "session_token"

    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        prefs.edit().remove(TOKEN_KEY).apply()
    }
}