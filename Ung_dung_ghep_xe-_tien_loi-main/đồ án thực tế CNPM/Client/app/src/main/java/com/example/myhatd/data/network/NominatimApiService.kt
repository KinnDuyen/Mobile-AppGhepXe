// data/network/NominatimApiService.kt
package com.example.myhatd.data.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.example.myhatd.data.model.NominatimResult

interface NominatimApiService {

    @GET("search") // API endpoint cho tìm kiếm
    suspend fun searchLocation(
        @Query("q") query: String, // Tên địa điểm cần tìm
        @Query("format") format: String = "json", // Yêu cầu trả về JSON
        @Query("addressdetails") details: Int = 1, // Để có thêm chi tiết địa chỉ
        @Query("limit") limit: Int = 5 // Giới hạn số lượng kết quả
    ): List<NominatimResult>
}