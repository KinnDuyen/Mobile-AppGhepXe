package com.example.myhatd.data.network

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // ⚠️ ĐÃ XÁC MINH: IP này phải là IP cục bộ của máy tính bạn.
    // Nếu bạn đang dùng Wi-Fi, IP này có thể thay đổi.
    private const val BASE_URL = "http://192.168.1.250:8089/"


    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // --- THÊM LOGGING INTERCEPTOR ---
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Đặt level là BODY để ghi lại toàn bộ header, URL, và nội dung body
        level = HttpLoggingInterceptor.Level.BODY
    }

    // --- CẤU HÌNH OKHTTP CLIENT ---
    private val okHttpClient = OkHttpClient.Builder()
        // Thêm Interceptor đã tạo
        .addInterceptor(loggingInterceptor)
        // Cấu hình Timeout (Tùy chọn, giúp phát hiện lỗi chậm mạng)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    // ------------------------------------

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Gán OkHttpClient đã cấu hình
            .client(okHttpClient)
            // Thêm GsonConverterFactory để tự động chuyển đổi JSON sang Kotlin Data Class
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Phương thức tạo ra một đối tượng Service (ApiService.kt)
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}