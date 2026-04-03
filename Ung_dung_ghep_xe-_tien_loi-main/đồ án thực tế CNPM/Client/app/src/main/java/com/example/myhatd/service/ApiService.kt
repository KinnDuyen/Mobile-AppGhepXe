package com.example.myhatd.service

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// --- Data classes cho Directions API response ---

data class DirectionsResponse(
    val routes: List<Route>,
    val status: String
)

data class Route(
    @SerializedName("overview_polyline")
    val overviewPolyline: OverviewPolyline,
    val legs: List<Leg>
)

data class OverviewPolyline(
    val points: String
)

data class Leg(
    val distance: Distance,
    val duration: Duration,
    // Các trường địa chỉ này không bắt buộc nhưng tốt cho việc debug
    @SerializedName("start_address")
    val startAddress: String,
    @SerializedName("end_address")
    val endAddress: String
)

data class Distance(
    val text: String, // Ví dụ: "100 km"
    val value: Int // Giá trị bằng mét
)

data class Duration(
    val text: String, // Ví dụ: "1 giờ 30 phút"
    val value: Int // Giá trị bằng giây
)

// --- Retrofit API Interface ---

interface DirectionsApiService {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("mode") mode: String = "driving",
        @Query("language") language: String = "vi" // Thêm language để lấy kết quả tiếng Việt
    ): DirectionsResponse
}

// --- Retrofit Instance ---

object RetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/"

    val api: DirectionsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DirectionsApiService::class.java)
    }
}