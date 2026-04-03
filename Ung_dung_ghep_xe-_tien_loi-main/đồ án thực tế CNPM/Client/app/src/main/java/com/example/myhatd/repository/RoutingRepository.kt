package com.example.myhatd.repository

import android.util.Log
import com.example.myhatd.data.network.ApiService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.maplibre.android.geometry.LatLng
import retrofit2.Response

// DTO phải khớp với cấu trúc phản hồi JSON của OSRM
data class OSRMRoutingResponse(
    @SerializedName("code") val code: String,
    @SerializedName("routes") val routes: List<Route>
) {
    data class Route(
        @SerializedName("geometry") val geometry: String // Chuỗi Encoded Polyline (Level 5)
    )
}

/**
 * Repository chuyên xử lý các tác vụ định tuyến (Routing) bên ngoài Backend chính,
 * ví dụ: gọi Open Source Routing Machine (OSRM).
 */
class RoutingRepository(
    private val apiService: ApiService
) {
    // ⚠️ CHÚ Ý: Đổi URL này sang OSRM Server của bạn (hoặc GraphHopper, v.v.)
    private val baseRoutingUrl = "http://router.project-osrm.org/route/v1/driving/"

    /**
     * Gọi API OSRM để lấy chuỗi Polyline đã được mã hóa cho đường đi.
     */
    suspend fun getRoutePolyline(
        startLocation: LatLng,
        endLocation: LatLng
    ): String? {
        val startLng = startLocation.longitude
        val startLat = startLocation.latitude
        val endLng = endLocation.longitude
        val endLat = endLocation.latitude

        val coordinates = "$startLng,$startLat;$endLng,$endLat"
        // Yêu cầu Polyline để dễ dàng giải mã và vẽ
        val fullUrl = "${baseRoutingUrl}$coordinates?geometries=polyline&overview=full"

        Log.d("RoutingRepository", "Đang gọi Routing API: $fullUrl")

        return try {
            val response: Response<OSRMRoutingResponse> = apiService.getRawRoutingData(fullUrl)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()
                if (result?.code == "Ok" && result.routes.isNotEmpty()) {
                    Log.d("RoutingRepository", "Đã nhận được chuỗi Polyline.")
                    result.routes.first().geometry
                } else {
                    Log.e("RoutingRepository", "Phản hồi Routing không hợp lệ. Code: ${result?.code}")
                    null
                }
            } else {
                Log.e("RoutingRepository", "API Routing thất bại. Code: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("RoutingRepository", "Lỗi kết nối khi gọi API Routing: $e")
            null
        }
    }
}