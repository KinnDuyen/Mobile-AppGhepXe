package com.example.myhatd.data.network

import com.example.myhatd.data.model.CancelRideRequest
import com.example.myhatd.data.model.ChuyenDiInfoRequest
import com.example.myhatd.data.model.ChuyenDiResponse
import com.example.myhatd.data.model.UserInfoRequest
import com.example.myhatd.data.model.UserResponse
import com.example.myhatd.data.model.DriverInfoRequest
import com.example.myhatd.data.model.DriverLocationUpdate
import com.example.myhatd.data.model.DriverResponse
import com.example.myhatd.data.model.MatchNotificationDTO
import com.example.myhatd.data.model.YeuCauChuyenDi
import com.example.myhatd.data.model.TripRequestResponse
import com.example.myhatd.repository.MatchRepository
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Path
import com.example.myhatd.repository.OSRMRoutingResponse // <-- IMPORT MỚI
import retrofit2.http.Url // <-- IMPORT MỚI để gọi API với URL đầy đủ

interface ApiService {

    // Endpoint để gửi thông tin người dùng sau khi xác thực OTP
    @POST("api/users/save-info")
    suspend fun saveUserInfo(@Body request: UserInfoRequest): Response<UserResponse>

    @POST("api/drivers/save-info")
    suspend fun saveDriverInfo(@Body request: DriverInfoRequest): Response<Unit>

    @GET("api/users/me/{soDienThoai}")
    suspend fun getCurrentUser(@Path("soDienThoai", encoded = true) phone: String): Response<UserResponse>

    @GET("api/drivers/me/{soDienThoai}")
    suspend fun getDriverByPhone(
        @Path("soDienThoai") soDienThoai: String
    ): Response<DriverResponse>


//    @POST("api/chuyen-di/send")
//    suspend fun sendChuyenDiInfo(
//        @Body request: ChuyenDiInfoRequest
//    ): Response<ChuyenDiResponse>


    @POST("api/trips/request")
    suspend fun taoYeuCauChuyenDi(@Body yeuCau: YeuCauChuyenDi): Response<TripRequestResponse>


    @GET("api/matches/latest/{soDienThoai}")
    suspend fun getLatestMatch(
        @Path("soDienThoai") soDienThoai: String
    ): Response<MatchNotificationDTO>

    // ✅ 2. API XÁC NHẬN ĐẶT XE: (POST /api/rides/confirm/{matchId})
    @POST("api/rides/confirm/{matchId}")
    suspend fun confirmRide(
        @Path("matchId") matchId: Long
    ): Response<Unit>


    @POST("api/rides/cancel")
    suspend fun cancelRide(
        @Body request: CancelRideRequest // Nhận MatchId và Reason trong Body
    ): Response<Unit>

    @POST("api/rides/picked-up/{matchId}") // Endpoint ví dụ
    suspend fun pickedUp(@Path("matchId") matchId: Long): Response<Unit>

    @POST("api/rides/complete/{matchId}")
    suspend fun completeRide(
        @Path("matchId") matchId: Long
    ): Response<Unit>

    @POST("api/rides/review/{matchId}")
    suspend fun postRideReview(
        @Path("matchId") matchId: Long,
        @Body reviewRequest: MatchRepository.ReviewRequest
    ): Response<Void>

    @POST("api/rides/reject/{matchId}") // ✅ SỬA ĐỂ KHỚP VỚI CONTROLLER MỚI
    suspend fun rejectMatch(
        @Path("matchId") matchId: Long
    ): Response<Unit>

    @POST("api/rides/location-update") // Endpoint ví dụ: POST /api/rides/location-update
    suspend fun updateDriverLocation(
        @Body request: DriverLocationUpdate
    ): Response<Unit> // Response<Unit> nếu không cần trả về dữ liệu

    @GET
    suspend fun getRawRoutingData(
        @Url fullUrl: String
    ): Response<OSRMRoutingResponse>
}