package com.example.myhatd.repository

import android.util.Log
import com.example.myhatd.data.model.MatchNotificationDTO
import com.example.myhatd.data.network.ApiService
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage
import com.example.myhatd.data.model.CancelRideRequest
import com.example.myhatd.data.model.DriverLocationUpdate
import org.maplibre.android.geometry.LatLng

data class DriverLocationDTO(
    val lat: Double,
    val lng: Double,
    val matchId: Long
    // Thêm các trường khác như bearing nếu cần
)

// KHÔNG CẦN @Singleton HAY @Inject vì ta tự quản lý trong Application
class MatchRepository(
    private val gson: Gson,
    private val apiService: ApiService
) {
    // --- Khai báo ---
    private var mStompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private val webSocketUrl = "ws://192.168.1.250:8089/ws"

    // --- StateFlow để UI lắng nghe ---
    private val _matchResult = MutableStateFlow<MatchNotificationDTO?>(null)
    val matchResult: StateFlow<MatchNotificationDTO?> = _matchResult

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // ✅ BỔ SUNG: Flow cho các thông báo Trạng thái chuyến đi của Driver (Hủy chuyến, v.v.)
    private val _rideStatusNotification = MutableStateFlow<MatchNotificationDTO?>(null)
    val rideStatusNotification: StateFlow<MatchNotificationDTO?> = _rideStatusNotification.asStateFlow()

    // ✅ BỔ SUNG: Flow cho các thông báo Trạng thái của User (Giữ lại kênh User)
    private val _userStatusNotification = MutableStateFlow<MatchNotificationDTO?>(null)
    val userStatusNotification: StateFlow<MatchNotificationDTO?> = _userStatusNotification.asStateFlow()

    // ✅ BỔ SUNG: Flow cho Vị trí tài xế theo thời gian thực
    private val _driverCurrentLocation = MutableStateFlow<DriverLocationUpdate?>(null)
    val driverCurrentLocation: StateFlow<DriverLocationUpdate?> = _driverCurrentLocation.asStateFlow()


    data class ReviewRequest(
        val matchId: Long,
        val rating: Int,
        val compliments: List<String>,
        val note: String? = null // Thêm trường ghi chú nếu UI có
    )


    suspend fun sendDriverLocation(
        matchId: Long,
        lat: Double,
        lng: Double,
        bearing: Double
    ): Boolean {
        if (matchId == -1L) return false // Bảo vệ: Không gửi nếu không có matchId

        val request = DriverLocationUpdate(matchId, lat, lng, bearing)

        return try {
            val response = apiService.updateDriverLocation(request)

            if (response.isSuccessful) {
                // Log.d("MatchRepository", "Đã gửi vị trí: $lat, $lng, MatchID: $matchId")
                true
            } else {
                Log.e("MatchRepository", "API gửi vị trí thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi gửi vị trí: $e")
            false
        }
    }

    // ✅ IMPLEMENT HÀM calculateRoute GỌI OSRM API
    suspend fun calculateRoute(start: LatLng, end: LatLng): String? {
        // Địa chỉ OSRM mặc định
        val osrmBaseUrl = "http://router.project-osrm.org/route/v1/driving"

        // Chuẩn bị tọa độ theo định dạng OSRM (long,lat;long,lat)
        val coordinates = "${start.longitude},${start.latitude};${end.longitude},${end.latitude}"

        // Tạo URL đầy đủ. overview=full để lấy polyline.
        val fullUrl = "$osrmBaseUrl/$coordinates?overview=full&geometries=polyline"

        return try {
            Log.d("MatchRepository", "Đang gọi OSRM API: $fullUrl")
            val response = apiService.getRawRoutingData(fullUrl) // Gọi API với @Url

            if (response.isSuccessful) {
                val geometry = response.body()?.routes?.firstOrNull()?.geometry
                if (geometry != null) {
                    Log.d("MatchRepository", "Đã nhận được Polyline từ OSRM.")
                    geometry
                } else {
                    Log.e("MatchRepository", "Phản hồi OSRM không có geometry.")
                    null
                }
            } else {
                Log.e("MatchRepository", "API OSRM thất bại. Code: ${response.code()}")
                null
            }

        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi tính toán tuyến đường: ${e.message}")
            null
        }
    }

    suspend fun reviewRide(request: ReviewRequest): Boolean {
        Log.d("MatchRepository", "Đang gửi đánh giá cho Match ID: ${request.matchId}")

        return try {
            val response = apiService.postRideReview(request.matchId, request)

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Đánh giá chuyến đi thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API đánh giá thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi đánh giá chuyến đi: $e")
            false
        }
    }

    /**
     * Hàm chính: Kết nối và Đăng ký kênh (Socket logic)
     */
    fun connectAndListen(soDienThoai: String) {
        val client = mStompClient
        if (client != null && client.isConnected) {
            Log.d("MatchRepository", "Đã kết nối rồi, không cần kết nối lại.")
            return
        }

        val topicMatch = "/topic/match/$soDienThoai" // Match Mới
        val topicUserStatus = "/topic/user/status/$soDienThoai" // Status User
        val topicDriverStatus = "/topic/driver/status/$soDienThoai" // Status Driver
        val topicDriverLocation = "/topic/driver/location/$soDienThoai" // ✅ Kênh Vị trí Driver

        Log.d("MatchRepository", "Đang tạo StompClient...")

        val newStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, webSocketUrl)
        mStompClient = newStompClient

        // Consumer 1: Chỉ dành cho Match MỚI (Cập nhật _matchResult)
        val onMatchMessage = Consumer<StompMessage> { stompMessage ->
            Log.d("MatchRepository", "✅ MATCH MỚI: ${stompMessage.payload}")
            try {
                val notification = gson.fromJson(
                    stompMessage.payload,
                    MatchNotificationDTO::class.java
                )
                _matchResult.value = notification
            } catch (e: Exception) {
                Log.e("MatchRepository", "Lỗi Parsing Match JSON: ", e)
            }
        }

        // ✅ Consumer 2: Dành cho các Kênh Status (Cập nhật _rideStatusNotification và _userStatusNotification)
        val onStatusMessage = Consumer<StompMessage> { stompMessage ->
            Log.d("MatchRepository", "✅ THÔNG BÁO STATUS: ${stompMessage.payload}")
            try {
                val notification = gson.fromJson(
                    stompMessage.payload,
                    MatchNotificationDTO::class.java
                )
                // Cập nhật Flow Trạng thái Driver (Dùng cho Hủy chuyến)
                _rideStatusNotification.value = notification
                // Cập nhật Flow Trạng thái User (Dùng cho Hoàn thành chuyến)
                _userStatusNotification.value = notification
            } catch (e: Exception) {
                Log.e("MatchRepository", "Lỗi Parsing Status JSON: ", e)
            }
        }

        // ✅ Consumer 3: Dành cho Vị trí Tài xế theo thời gian thực
        val onDriverLocationMessage = Consumer<StompMessage> { stompMessage ->
            // Log.d("MatchRepository", "✅ VỊ TRÍ DRIVER: ${stompMessage.payload}")
            try {
                // Giả định Server gửi DriverLocationUpdate (vị trí + matchId + bearing)
                val locationUpdate = gson.fromJson(
                    stompMessage.payload,
                    DriverLocationUpdate::class.java
                )
                _driverCurrentLocation.value = locationUpdate
            } catch (e: Exception) {
                Log.e("MatchRepository", "Lỗi Parsing Driver Location JSON: ", e)
            }
        }


        val onTopicError = Consumer<Throwable> { error ->
            Log.e("MatchRepository", "Lỗi nhận tin nhắn: ", error)
        }

        val onConnectSuccess = Consumer<LifecycleEvent> { lifecycleEvent ->
            when (lifecycleEvent.type) {
                LifecycleEvent.Type.OPENED -> {
                    Log.d("MatchRepository", "✅ KẾT NỐI SERVER THÀNH CÔNG!")
                    _isConnected.value = true

                    // 1. Đăng ký kênh MATCH MỚI
                    Log.d("MatchRepository", "Đang đăng ký kênh MATCH: $topicMatch")
                    val matchDisposable = newStompClient.topic(topicMatch)
                        .subscribe(onMatchMessage, onTopicError)
                    compositeDisposable.add(matchDisposable)

                    // 2. Đăng ký kênh STATUS USER
                    Log.d("MatchRepository", "Đang đăng ký kênh USER STATUS: $topicUserStatus")
                    val userStatusDisposable = newStompClient.topic(topicUserStatus)
                        .subscribe(onStatusMessage, onTopicError)
                    compositeDisposable.add(userStatusDisposable)

                    // 3. Đăng ký kênh STATUS DRIVER
                    Log.d("MatchRepository", "Đang đăng ký kênh DRIVER STATUS: $topicDriverStatus")
                    val driverStatusDisposable = newStompClient.topic(topicDriverStatus)
                        .subscribe(onStatusMessage, onTopicError)
                    compositeDisposable.add(driverStatusDisposable)

                    // ✅ 4. Đăng ký kênh VỊ TRÍ DRIVER
                    Log.d("MatchRepository", "Đang đăng ký kênh DRIVER LOCATION: $topicDriverLocation")
                    val locationDisposable = newStompClient.topic(topicDriverLocation)
                        .subscribe(onDriverLocationMessage, onTopicError)
                    compositeDisposable.add(locationDisposable)
                }
                LifecycleEvent.Type.CLOSED -> {
                    Log.d("MatchRepository", "Kết nối đã đóng.")
                    _isConnected.value = false
                }
                LifecycleEvent.Type.ERROR -> {
                    Log.e("MatchRepository", "Lỗi kết nối: ", lifecycleEvent.exception)
                    _isConnected.value = false
                }
                else -> {}
            }
        }

        val onConnectError = Consumer<Throwable> { error ->
            Log.e("MatchRepository", "Lỗi đăng ký kết nối: ", error)
        }

        val connectDisposable = newStompClient.lifecycle()
            .subscribe(onConnectSuccess, onConnectError)

        compositeDisposable.add(connectDisposable)

        newStompClient.connect()
    }


    fun disconnect() {
        Log.d("MatchRepository", "Ngắt kết nối Stomp...")
        mStompClient?.disconnect()
        compositeDisposable.clear()
        _isConnected.value = false
        _matchResult.value = null
        // Đặt tất cả các Flow về null khi ngắt kết nối
        _rideStatusNotification.value = null
        _userStatusNotification.value = null
        _driverCurrentLocation.value = null // ✅ Reset driver location flow
        mStompClient = null
    }

    // =======================================================
    // 1. LOGIC DỰ PHÒNG HTTP (Fix lỗi Timing)
    // =======================================================

    /**
     * Gọi API HTTP để kiểm tra Match bị bỏ lỡ (missed match) qua Database.
     */
    suspend fun checkLatestMatch(userPhone: String): MatchNotificationDTO? {
        Log.d("MatchRepository", "Kiểm tra Match bị miss qua API cho SĐT: $userPhone")

        return try {
            val response = apiService.getLatestMatch(userPhone) // ✅ Gọi API thực tế

            // Xử lý mã 200/201 (có dữ liệu) hoặc 204 (No Content - không có Match)
            if (response.isSuccessful && response.code() != 204 && response.body() != null) {
                Log.d("MatchRepository", "Đã tìm thấy Match bị miss qua HTTP.")
                response.body()
            } else {
                if (response.code() != 404 && response.code() != 204) {
                    Log.e("MatchRepository", "Lỗi API checkLatestMatch: Code ${response.code()}")
                }
                null
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi checkLatestMatch: $e")
            null
        }
    }

    /**
     * Cập nhật MatchResult trực tiếp (dùng khi bắt được Match bị miss qua HTTP)
     */
    fun forceUpdateMatchResult(match: MatchNotificationDTO?) {
        _matchResult.value = match
        if (match != null) {
            Log.d("MatchRepository", "Cập nhật Match bị miss thành công qua HTTP.")
        }
    }

    fun forceUpdateRideStatus(notification: MatchNotificationDTO?) {
        _rideStatusNotification.value = notification
    }

    // ✅ BỔ SUNG: Hoàn thiện hàm này (vì bạn đã khai báo _userStatusNotification)
    fun forceUpdateUserStatus(notification: MatchNotificationDTO?) {
        _userStatusNotification.value = notification
    }


    // =======================================================
    // 2. LOGIC XÁC NHẬN ĐẶT XE (confirmRideRequest)
    // =======================================================

    /**
     * Gửi API HTTP để xác nhận việc đặt xe (sau khi User chấp nhận giá/thời gian).
     */
    suspend fun confirmRideRequest(matchId: Long): Boolean {
        Log.d("MatchRepository", "Đang gửi xác nhận đặt xe cho Match ID: $matchId")

        return try {
            val response = apiService.confirmRide(matchId) // ✅ Gọi API thực tế

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Xác nhận đặt xe thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API xác nhận thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi xác nhận đặt xe: $e")
            false
        }
    }

    suspend fun completeRide(matchId: Long): Boolean {
        Log.d("MatchRepository", "Đang gửi yêu cầu kết thúc chuyến đi cho Match ID: $matchId")

        return try {
            val response = apiService.completeRide(matchId) // ✅ Gọi API thực tế

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Kết thúc chuyến đi thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API kết thúc chuyến đi thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi kết thúc chuyến đi: $e")
            false
        }
    }

    suspend fun pickedUpRide(matchId: Long): Boolean {
        Log.d("MatchRepository", "Đang gửi yêu cầu Driver đã đón khách cho Match ID: $matchId")

        return try {
            // 🛑 SỬ DỤNG API THỰC TẾ ĐÃ ĐƯỢC KHAI BÁO
            val response = apiService.pickedUp(matchId)

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Báo đã đón khách thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API báo đã đón khách thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi báo đã đón khách: $e")
            false
        }
    }

    suspend fun cancelRide(matchId: Long, reason: String): Boolean {
        Log.d("MatchRepository", "Đang gửi yêu cầu hủy chuyến đi cho Match ID: $matchId, Lý do: $reason")

        return try {
            // Tạo body request chứa cả matchId và reason
            val requestBody = CancelRideRequest(matchId = matchId, reason = reason)

            // Gọi API chỉ với requestBody (không cần Path Variable)
            val response = apiService.cancelRide(requestBody)

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Hủy chuyến đi thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API hủy chuyến đi thất bại. Code: ${response.code()}, Error: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi hủy chuyến đi: $e")
            false
        }
    }

    suspend fun rejectMatch(matchId: Long): Boolean {
        Log.d("MatchRepository", "Đang gửi yêu cầu Driver từ chối Match ID: $matchId")

        return try {
            val response = apiService.rejectMatch(matchId) // <--- Gọi API mới

            if (response.isSuccessful) {
                Log.d("MatchRepository", "Từ chối Match thành công (2xx).")
                true
            } else {
                Log.e("MatchRepository", "API từ chối Match thất bại. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("MatchRepository", "Lỗi kết nối khi từ chối Match: $e")
            false
        }
    }
}