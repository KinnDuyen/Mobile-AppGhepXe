package com.example.myhatd.repository

import android.content.Context
import com.example.myhatd.data.network.NominatimRetrofitClient
import com.example.myhatd.data.model.NominatimResult
import com.example.myhatd.data.network.NominatimApiService
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(
    private val apiService: NominatimApiService = NominatimRetrofitClient.nominatimService
) {

    suspend fun searchLocation(query: String): List<NominatimResult> {
        // Xử lý lỗi (try-catch) là cần thiết trong thực tế
        return apiService.searchLocation(query)
    }

    // ✅ HÀM MỚI: Reverse Geocoding sử dụng Android Geocoder
    suspend fun reverseGeocode(context: Context, lat: Double, lon: Double): String? {
        // Chạy tác vụ Geocoding trên IO Dispatcher vì nó là I/O block
        return withContext(Dispatchers.IO) {
            try {
                // Sử dụng Android Geocoder để chuyển đổi tọa độ thành địa chỉ
                val geocoder = android.location.Geocoder(context, Locale.getDefault())

                // Lấy tối đa 1 địa chỉ
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                if (!addresses.isNullOrEmpty()) {
                    // Trả về địa chỉ đầy đủ nhất
                    addresses[0].getAddressLine(0)
                } else {
                    null
                }
            } catch (e: Exception) {
                println("Lỗi Reverse Geocoding: ${e.message}")
                // Trả về null nếu có lỗi
                null
            }
        }
    }
}