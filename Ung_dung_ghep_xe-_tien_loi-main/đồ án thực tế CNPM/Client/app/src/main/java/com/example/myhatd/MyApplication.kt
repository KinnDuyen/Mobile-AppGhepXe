package com.example.myhatd

import android.app.Application
import com.example.myhatd.repository.MatchRepository
import com.google.gson.Gson
import com.example.myhatd.data.network.RetrofitClient
import com.example.myhatd.data.network.ApiService // ✅ Cần import ApiService
import org.maplibre.android.MapLibre

/**
 * Lớp Application để khởi tạo và giữ các dependencies singleton.
 */
class MyApplication : Application() {

    // 1. Dependency: Gson
    private val gsonInstance: Gson by lazy {
        Gson()
    }

    // ✅ 2. Dependency: ApiService (Lấy từ RetrofitClient singleton)
    private val apiService: ApiService by lazy {
        RetrofitClient.apiService
    }

    // 3. Singleton: MatchRepository (Truyền cả Gson và ApiService vào)
    val matchRepository: MatchRepository by lazy {
        // ✅ Khởi tạo Repository với đầy đủ dependencies
        MatchRepository(gsonInstance, apiService)
    }

    override fun onCreate() {
        super.onCreate()
        // Các logic khởi tạo khác (nếu có)
        MapLibre.getInstance(applicationContext)
    }
}