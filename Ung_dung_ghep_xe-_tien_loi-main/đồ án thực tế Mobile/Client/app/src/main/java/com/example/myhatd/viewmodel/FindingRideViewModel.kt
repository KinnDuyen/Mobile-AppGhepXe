package com.example.myhatd.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.repository.MatchRepository
import com.example.myhatd.data.model.MatchNotificationDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers


class FindingRideViewModel(
    private val matchRepository: MatchRepository
) : ViewModel() {

    // --- State cho UI ---
    val matchResult = matchRepository.matchResult // Dữ liệu Match (null hoặc MatchNotificationDTO)
    val isSocketConnected = matchRepository.isConnected // Trạng thái kết nối Socket

    val userStatusNotification = matchRepository.userStatusNotification

    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()

    private val _isSearchAttemptComplete = MutableStateFlow(false)
    val isSearchAttemptComplete: StateFlow<Boolean> = _isSearchAttemptComplete.asStateFlow()


    // ✅ THÊM STATE ĐỂ QUẢN LÝ VIỆC MATCH BỊ HỦY/TIMEOUT RÕ RÀNG
    private val _isMatchCancelled = MutableStateFlow(false)
    val isMatchCancelled: StateFlow<Boolean> = _isMatchCancelled.asStateFlow()

    private val _currentRide = MutableStateFlow<MatchNotificationDTO?>(null)
    val currentRide: StateFlow<MatchNotificationDTO?> = _currentRide.asStateFlow()

    private val _currentRideStatus = MutableStateFlow<String?>(null)
    val currentRideStatus: StateFlow<String?> = _currentRideStatus.asStateFlow()

    // ✅ STATE VỊ TRÍ VÀ HƯỚNG CỦA DRIVER
    private val _driverLocation = MutableStateFlow<LatLng?>(null)
    val driverLocation: StateFlow<LatLng?> = _driverLocation.asStateFlow()

    private val _driverBearing = MutableStateFlow(0.0)
    val driverBearing: StateFlow<Double> = _driverBearing.asStateFlow()
    // END STATE VỊ TRÍ VÀ HƯỚNG CỦA DRIVER

    private val _routePolyline = MutableStateFlow<String?>(null)
    val routePolyline: StateFlow<String?> = _routePolyline.asStateFlow()



    // --- Logic Quản lý Trạng thái ---


    init {
        // ✅ LẮNG NGHE KÊNH MATCH MỚI (chỉ MatchResult)
        viewModelScope.launch {
            matchResult.collect { notification ->
                if (notification != null) {
                    handleMatchNotification(notification)
                }
            }
        }

        // ✅ BỔ SUNG: LẮNG NGHE KÊNH STATUS (Hoàn thành, User Cancel, v.v.)
        viewModelScope.launch {
            matchRepository.userStatusNotification.collect { notification ->
                if (notification != null) {
                    handleStatusNotification(notification)
                }
            }
        }

        // ✅ BỔ SUNG: LẮNG NGHE KÊNH VỊ TRÍ DRIVER ĐỂ CẬP NHẬT UI VÀ TÍNH ROUTE
        viewModelScope.launch {
            matchRepository.driverCurrentLocation.collect { locationUpdate ->
                val ride = _currentRide.value
                val status = _currentRideStatus.value

                if (locationUpdate != null && ride != null) {
                    // 1. Cập nhật vị trí và hướng
                    _driverLocation.value = LatLng(locationUpdate.lat, locationUpdate.lng)
                    _driverBearing.value = locationUpdate.bearing

                    // 2. Tính toán Route dựa trên trạng thái
                    val startLoc = LatLng(locationUpdate.lat, locationUpdate.lng)

                    // ✅ ĐÃ SỬA: Thay thế tên thuộc tính bằng tên đúng trong MatchNotificationDTO
                    val endLoc: LatLng? = when (status) {
                        // Driver đang đến đón User (Route: Driver -> User Pickup)
                        "DRIVER_ACCEPTED" -> if (ride.viDoDiemDi != null && ride.kinhDoDiemDi != null) {
                            LatLng(ride.viDoDiemDi, ride.kinhDoDiemDi)
                        } else null
                        // Driver đã đón và đang đến điểm đến (Route: Driver -> Destination)
                        "PICKED_UP" -> if (ride.viDoDiemDen != null && ride.kinhDoDiemDen != null) {
                            LatLng(ride.viDoDiemDen, ride.kinhDoDiemDen)
                        } else null
                        else -> null
                    }

                    if (endLoc != null) {
                        // Tính toán tuyến đường mới (Reactive)
                        calculateRoute(startLoc, endLoc)
                    } else {
                        _routePolyline.value = null
                    }
                } else {
                    // Nếu chuyến đi null hoặc location null, reset route
                    _routePolyline.value = null
                    _driverLocation.value = null
                    _driverBearing.value = 0.0
                }
            }
        }

    }

    fun updateDriverLocation(latLng: LatLng?) {
        _driverLocation.value = latLng
    }

    fun calculateRoute(start: LatLng, end: LatLng) {
        viewModelScope.launch {
            if (start == null || end == null) {
                Log.e("FindingRideVM", "Không thể tính route: Thiếu điểm bắt đầu/kết thúc.")
                _routePolyline.value = null
                return@launch
            }

            // ✅ SỬ DỤNG withContext(Dispatchers.IO) ĐỂ GỌI HÀM SUSPEND TRONG REPOSITORY
            val polyline = withContext(Dispatchers.IO) {
                // Giả định MatchRepository có hàm calculateRoute
                matchRepository.calculateRoute(start, end)
            }

            if (polyline != null) {
                _routePolyline.value = polyline
                Log.d("FindingRideVM", "Đã tính toán tuyến đường thành công.")
            } else {
                _routePolyline.value = null
                Log.e("FindingRideVM", "Lỗi tính toán tuyến đường.")
            }
        }
    }

    fun resetMatchState() {
        Log.d("FindingRideVM", "Resetting match state for new search.")
        // Đặt lại các cờ điều hướng và kết quả
        matchRepository.forceUpdateMatchResult(null) // Đặt lại _matchResult trong Repository
        _isMatchCancelled.value = false
        _isSearchAttemptComplete.value = false
        // ✅ Reset các state theo dõi
        _routePolyline.value = null
        _driverLocation.value = null
        _driverBearing.value = 0.0
        // Đảm bảo ngắt kết nối socket nếu nó đang mở
        matchRepository.disconnect()
    }

    private fun handleMatchNotification(notification: MatchNotificationDTO) {
        // Giữ nguyên phần này
    }

    // ✅ THÊM HÀM MỚI: Xử lý thông báo trạng thái chuyến đi
    private fun handleStatusNotification(notification: MatchNotificationDTO) {
        // Phân tích Message để tìm tiền tố
        val messageType = notification.message?.split(":")?.getOrNull(0)

        when (messageType) {
            "COMPLETED_RIDE" -> {
                Log.d("FindingRideViewModel", "🔔 Socket nhận thông báo: CHUYẾN ĐI ĐÃ HOÀN THÀNH. ID: ${notification.matchId}")

                _currentRide.value = notification
                _currentRideStatus.value = "COMPLETED"

                matchRepository.forceUpdateUserStatus(null)
                matchRepository.forceUpdateMatchResult(null)
            }

            "DRIVER_ACCEPTED" -> {
                // Xử lý khi Driver chấp nhận (chuyển từ màn hình chờ sang màn hình theo dõi)
                _currentRide.value = notification
                _currentRideStatus.value = "DRIVER_ACCEPTED" // ✅ Thêm trạng thái DRIVER_ACCEPTED
                matchRepository.forceUpdateUserStatus(null)
            }

            "RIDE_PICKED_UP" -> {
                Log.d("FindingRideViewModel", "🔔 Socket nhận thông báo: DRIVER ĐÃ ĐÓN KHÁCH. ID: ${notification.matchId}")
                _currentRide.value = notification
                _currentRideStatus.value = "PICKED_UP" // ✅ Lưu trạng thái PICKED_UP
                matchRepository.forceUpdateUserStatus(null)
            }

            "DRIVER_REJECTED" -> {
                Log.d("FindingRideViewModel", "🔔 Socket nhận thông báo: DRIVER ĐÃ TỪ CHỐI/BỎ QUA CHUYẾN. ID: ${notification.matchId}")

                _currentRide.value = null
                _currentRideStatus.value = "DRIVER_REJECTED"

                matchRepository.forceUpdateMatchResult(null)
                _isMatchCancelled.value = true
                matchRepository.forceUpdateUserStatus(null)
                _isConfirming.value = false
                _driverLocation.value = null // ✅ Reset driver location
                _routePolyline.value = null // ✅ Reset polyline
            }

            "USER_CANCELLED" -> {
                matchRepository.forceUpdateUserStatus(null)
            }

            else -> {
                // Xử lý các loại thông báo khác
            }
        }
    }

    fun resetRideStatus() {
        _currentRideStatus.value = null
        _currentRide.value = null // Có thể reset luôn chuyến đi hiện tại
        _driverLocation.value = null
        _routePolyline.value = null
    }

    /**
     * Kích hoạt quá trình tìm chuyến.
     */
    fun startFindingRide(userPhone: String) {
        viewModelScope.launch {
            Log.d("FindingRideVM", "Starting new search, resetting all previous states.")
            matchRepository.forceUpdateMatchResult(null)
            matchRepository.forceUpdateUserStatus(null)
            _isMatchCancelled.value = false
            _currentRide.value = null
            _currentRideStatus.value = null
            _isSearchAttemptComplete.value = false
            _driverLocation.value = null // ✅ Reset driver location
            _routePolyline.value = null // ✅ Reset polyline

            if (!isSocketConnected.value) {
                matchRepository.connectAndListen(userPhone)
            }

            // 3. Chạy API DỰ PHÒNG: Kiểm tra Match bị miss (chặn Socket Race Condition)
            val missedMatch = checkLatestMatch(userPhone)

            if (missedMatch != null) {
                matchRepository.forceUpdateMatchResult(missedMatch)
            }

            // 4. ĐÁNH DẤU HOÀN TẤT
            _isSearchAttemptComplete.value = true
        }
    }

    /**
     * Gọi API HTTP để kiểm tra Match bị bỏ lỡ (missed match) qua Database.
     */
    suspend fun checkLatestMatch(userPhone: String): MatchNotificationDTO? {
        return matchRepository.checkLatestMatch(userPhone)
    }

    /**
     * Cập nhật MatchResult trực tiếp.
     */
    fun forceUpdateMatchResult(match: MatchNotificationDTO?) {
        matchRepository.forceUpdateMatchResult(match)
        // ✅ Nếu cập nhật thành NULL từ ngoài (do Server báo hủy),
        //    thì kích hoạt cờ báo hủy.
        if (match == null) {
            _isMatchCancelled.value = true
        }else {
            // ✅ BỔ SUNG: Nếu có Match, đảm bảo cờ hủy được reset
            _isMatchCancelled.value = false
        }
    }

    // --- Logic Xử lý Giao dịch ---

    /**
     * Gọi API xác nhận Match (Đặt xe).
     */
    fun confirmBooking(matchId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isConfirming.value = true

            // Lấy dữ liệu Match hiện tại trước khi gọi API
            val confirmedMatchData = matchResult.value

            val success = matchRepository.confirmRideRequest(matchId)

            _isConfirming.value = false

            if (success) {
                // ✅ BƯỚC 1: LƯU TRỮ DỮ LIỆU VÀO STATE MỚI
                if (confirmedMatchData != null) {
                    _currentRide.value = confirmedMatchData
                    _currentRideStatus.value = "DRIVER_ACCEPTED" // Đặt trạng thái ban đầu

                    // KHÔNG CẦN KÍCH HOẠT TÍNH ROUTE TẠI ĐÂY, VÌ LOGIC ĐÃ ĐƯỢC CHUYỂN SANG
                    // LẮNG NGHE VỊ TRÍ DRIVER (driverCurrentLocation.collect)
                }
                // ✅ BƯỚC 2: RESET matchResult
                matchRepository.forceUpdateMatchResult(null)

            } else {
                // Xử lý lỗi như cũ
                _isMatchCancelled.value = true
                matchRepository.forceUpdateMatchResult(null)
            }

            // Trả về kết quả API cho UI
            onResult(success)
        }
    }

    fun submitReview(
        matchId: Long,
        rating: Int,
        compliments: Set<String>,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            if (rating == 0) {
                onError() // Yêu cầu phải có sao
                return@launch
            }

            val request = MatchRepository.ReviewRequest(
                matchId = matchId,
                rating = rating,
                compliments = compliments.toList(),
                note = null // Hiện tại UI không có ô ghi chú
            )

            // Gửi qua Repository (API)
            val isSuccess = matchRepository.reviewRide(request)

            if (isSuccess) {
                onSuccess()
            } else {
                onError()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Đảm bảo ngắt kết nối khi ViewModel bị hủy.
        matchRepository.disconnect()
    }

    fun cancelFindingProcess() {
        Log.d("FindingRideViewModel", "Hủy quá trình tìm kiếm.")
        matchRepository.disconnect()
        matchRepository.forceUpdateMatchResult(null)
        _isSearchAttemptComplete.value = false
        _isMatchCancelled.value = true
        _driverLocation.value = null
        _routePolyline.value = null
    }
    fun cancelFindingRide(matchId: Long, reason: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            Log.d(
                "FindingRideViewModel",
                "Đang gọi API hủy chuyến cho ID: $matchId, Lý do: $reason"
            )

            // Gọi Repository, hàm này đã được cập nhật để dùng CancelRideRequest
            val success = matchRepository.cancelRide(matchId, reason)

            if (success) {
                // RESET DỮ LIỆU ĐÃ LƯU TRONG VIEWMODEL
                matchRepository.disconnect()
                matchRepository.forceUpdateMatchResult(null)
                matchRepository.forceUpdateUserStatus(null)
                _currentRide.value = null
                _currentRideStatus.value = null
                _isMatchCancelled.value = true
                _driverLocation.value = null // ✅ Reset driver location
                _routePolyline.value = null // ✅ Reset polyline
            }

            onComplete(success) // Trả về kết quả cho UI
        }
    }
}
