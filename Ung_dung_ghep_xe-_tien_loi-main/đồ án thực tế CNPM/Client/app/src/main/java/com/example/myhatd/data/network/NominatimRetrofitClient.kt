package com.example.myhatd.data.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NominatimRetrofitClient {

    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    // ⚠️ BẮT BUỘC: Đặt User Agent định danh ứng dụng của bạn
    // Thay thế bằng tên ứng dụng của bạn và email liên hệ
    private const val APP_USER_AGENT = "MyHatdApp/1.0 (contact: nguyentrungchihao8809@gmail.com)"

    // Tái sử dụng OkHttpClient với cấu hình chung
    private val okHttpClient = OkHttpClient.Builder()

        // ✅ THÊM INTERCEPTOR ĐỂ ĐẶT USER-AGENT TRONG HEADER
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", APP_USER_AGENT)
                .build()
            chain.proceed(request)
        }

        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Phương thức tạo ra đối tượng NominatimApiService
    val nominatimService: NominatimApiService by lazy {
        retrofit.create(NominatimApiService::class.java)
    }
}