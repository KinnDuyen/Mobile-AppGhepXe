package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.repository.MatchRepository
import com.example.myhatd.repository.RoutingRepository // ✅ Import RoutingRepository
import com.example.myhatd.data.model.MatchNotificationDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import org.maplibre.android.geometry.LatLng // ✅ Import MapLibre LatLng
import kotlinx.coroutines.Job // ✅ Import Job
import kotlinx.coroutines.delay

// ⚠️ CHÚ Ý: DriverMatchViewModelFactory cần được cập nhật để cung cấp RoutingRepository (Đã làm ở bước trước)
class DriverMatchViewModel(
    private val matchRepository: MatchRepository,
    private val routingRepository: RoutingRepository, // ✅ THÊM ROUTING REPOSITORY
    private val mapViewModel: MapViewModel
) : ViewModel() {

    private var locationUpdateJob: Job? = null

    // --- State cho UI ---
    val matchResult = matchRepository.matchResult
    val isSocketConnected = matchRepository.isConnected
    private val _isConfirming = MutableStateFlow(false)
    val isConfirming: StateFlow<Boolean> = _isConfirming.asStateFlow()
    private val _isMatchCancelled = MutableStateFlow(false)
    val isMatchCancelled: StateFlow<Boolean> = _isMatchCancelled.asStateFlow()
    private val _currentRide = MutableStateFlow<MatchNotificationDTO?>(null)
    val currentRide: StateFlow<MatchNotificationDTO?> = _currentRide.asStateFlow()
    private val _isRideCancelledByServer = MutableStateFlow<MatchNotificationDTO?>(null)
    val isRideCancelledByServer: StateFlow<MatchNotificationDTO?> = _isRideCancelledByServer.asStateFlow()

    // ✅ STATE MỚI CHO POLYLINE
    private val _routePolyline = MutableStateFlow<String?>(null)
    val routePolyline: StateFlow<String?> = _routePolyline.asStateFlow()

    // -----------------------------------------------------------
    // --- Logic Khởi tạo và Socket ---
    // -----------------------------------------------------------

    init {
        // Coroutine 1: Lắng nghe trạng thái HỦY CHUYẾN
        viewModelScope.launch {
            matchRepository.rideStatusNotification.collect { notification ->
                if (notification != null && notification.message?.contains("USER_CANCELLED") == true) {
                    Log.d("DriverMatchViewModel", "Nhận được thông báo hủy chuyến từ Socket. Match ID: ${notification.matchId}")
                    _isRideCancelledByServer.value = notification
                    _currentRide.value = null
                    _routePolyline.value = null
                    matchRepository.forceUpdateRideStatus(null)
                }
            }
        }

        // Coroutine 2: Lắng nghe currentRide để BẬT/TẮT TIMER GỬI VỊ TRÍ
        // Đã tách Coroutine này ra khỏi init block trên để đảm bảo logic chạy đúng
        viewModelScope.launch {
            currentRide.collect { ride ->
                if (ride != null && ride.matchId != null) {
                    // Kích hoạt timer gửi vị trí khi có chuyến đi
                    startLocationUpdateTimer(ride.matchId)
                } else {
                    // Dừng timer khi chuyến đi kết thúc/hủy
                    stopLocationUpdateTimer()
                }
            }
        }
    }

    // -----------------------------------------------------------
    // --- Logic TIMER GỬI VỊ TRÍ ---
    // -----------------------------------------------------------

    private fun startLocationUpdateTimer(matchId: Long) {
        // Nếu đã có job đang chạy, dừng nó trước để tránh trùng lặp
        locationUpdateJob?.cancel()

        // Khởi tạo job mới: cứ 5 giây thì gửi vị trí
        locationUpdateJob = viewModelScope.launch {
            while (true) {
                // Lấy vị trí Driver từ MapViewModel (Đã được cung cấp qua constructor)
                val driverState = mapViewModel.uiState.value
                val driverLocation = driverState.lastKnownLocation

                if (driverLocation != null) {
                    // Gọi Repository để gửi vị trí lên BE
                    matchRepository.sendDriverLocation(
                        matchId = matchId,
                        lat = driverLocation.latitude,
                        lng = driverLocation.longitude,
                        bearing = driverState.currentBearing
                    )
                }
                // Chờ 5 giây trước khi gửi tiếp
                delay(5000L) // Chu kỳ 5 giây
            }
        }
        Log.d("DriverMatchVM", "✅ Bắt đầu gửi vị trí Driver cho Match ID: $matchId")
    }

    private fun stopLocationUpdateTimer() {
        locationUpdateJob?.cancel()
        locationUpdateJob = null
        Log.d("DriverMatchVM", "🛑 Dừng gửi vị trí Driver.")
    }

    fun resetRideCancelledState() {
        _isRideCancelledByServer.value = null
    }

    fun startFindingRide(userPhone: String) {
        viewModelScope.launch {
            if (!isSocketConnected.value) {
                matchRepository.connectAndListen(userPhone)
            }

            if (matchResult.value == null) {
                val missedMatch = checkLatestMatch(userPhone)
                if (missedMatch != null) {
                    matchRepository.forceUpdateMatchResult(missedMatch)
                }
            }
        }
    }

    fun cancelFindingRide() {
        matchRepository.disconnect()
        matchRepository.forceUpdateMatchResult(null)
        _currentRide.value = null
        _isMatchCancelled.value = true
        _isRideCancelledByServer.value = null
        _routePolyline.value = null // ✅ RESET POLYLINE KHI HỦY TÌM KIẾM
    }

    suspend fun checkLatestMatch(userPhone: String): MatchNotificationDTO? {
        return matchRepository.checkLatestMatch(userPhone)
    }

    fun forceUpdateMatchResult(match: MatchNotificationDTO?) {
        val matchIdToReject = matchResult.value?.matchId
        matchRepository.forceUpdateMatchResult(match)

        if (match == null) {
            _isMatchCancelled.value = true
            _routePolyline.value = null // ✅ RESET POLYLINE KHI HỦY MATCH
            if (matchIdToReject != null) {
                viewModelScope.launch {
                    matchRepository.rejectMatch(matchIdToReject)
                }
            }
        } else {
            _isMatchCancelled.value = false
        }
    }

    // -----------------------------------------------------------
    // --- Logic Routing Mới ---
    // -----------------------------------------------------------

    /**
     * Hàm tính toán tuyến đường từ Driver đến một điểm đích cụ thể.
     * @param driverLocation Vị trí hiện tại của Driver.
     * @param destinationType Loại điểm đích ("PICKUP" hoặc "DROPOFF").
     */
    fun calculateRoute(driverLocation: LatLng, destinationType: String) {
        viewModelScope.launch {
            val rideData = currentRide.value ?: run {
                _routePolyline.value = null
                return@launch
            }

            val endLat: Double?
            val endLng: Double?
            val destinationName: String

            // 1. Xác định tọa độ đích dựa trên loại
            if (destinationType == "PICKUP") {
                endLat = rideData.viDoDiemDi
                endLng = rideData.kinhDoDiemDi
                destinationName = rideData.tenDiemDiUser ?: "Điểm đón"
            } else if (destinationType == "DROPOFF") {
                endLat = rideData.viDoDiemDen
                endLng = rideData.kinhDoDiemDen
                destinationName = rideData.tenDiemDenUser ?: "Điểm đến"
            } else {
                _routePolyline.value = null
                return@launch
            }

            if (endLat == null || endLng == null) {
                Log.e("DriverMatchViewModel", "Thiếu tọa độ GPS của $destinationName.")
                _routePolyline.value = null
                return@launch
            }

            val endLocation = LatLng(endLat, endLng)

            Log.d("DriverMatchViewModel", "Đang tính toán tuyến đường đến: $destinationName")

            Log.d("RouteCheck", "Đang tính toán tuyến đường từ $driverLocation đến $destinationName")

            // 2. Gọi Routing Repository
            val polyline = routingRepository.getRoutePolyline(
                startLocation = driverLocation,
                endLocation = endLocation
            )

            Log.d("RouteCheck", "Polyline Result: ${if (polyline != null) "Thành công" else "Thất bại/Null"}")
            _routePolyline.value = polyline

            _routePolyline.value = polyline
        }
    }

    // -----------------------------------------------------------
    // --- Logic Xử lý Giao dịch (Driver Actions) ---
    // -----------------------------------------------------------

    fun confirmBooking(matchId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isConfirming.value = true
            val confirmedMatchData = matchResult.value
            val success = true // Giả định thành công ngay lập tức

            _isConfirming.value = false

            if (success) {
                if (confirmedMatchData != null) {
                    _currentRide.value = confirmedMatchData
                }
                matchRepository.forceUpdateMatchResult(null)
            } else {
                _isMatchCancelled.value = true
                matchRepository.forceUpdateMatchResult(null)
            }
            onResult(success)
        }
    }

    fun pickedUpCustomer(onResult: (Boolean) -> Unit) {
        val currentRideData = currentRide.value ?: run { onResult(false); return }
        val matchId = currentRideData.matchId ?: run { onResult(false); return }

        viewModelScope.launch {
            val isSuccess = try {
                matchRepository.pickedUpRide(matchId)
            } catch (e: Exception) {
                false
            }

            if (isSuccess) {
                // Cập nhật trạng thái đón khách
                _currentRide.value = currentRideData.copy(
                    message = "RIDE_PICKED_UP:Driver báo đã đón khách",
                )
                _routePolyline.value = null // ✅ XÓA POLYLINE PICKUP (Để tính lại DROP-OFF)
            }

            onResult(isSuccess)
        }
    }

    fun completeRide() {
        val matchId = currentRide.value?.matchId ?: return

        viewModelScope.launch {
            val isSuccess = try {
                matchRepository.completeRide(matchId)
            } catch (e: Exception) {
                false
            }

            if (isSuccess) {
                _currentRide.value = null
                _routePolyline.value = null // ✅ RESET POLYLINE KHI HOÀN THÀNH
            }
        }
    }


    fun cancelRideByDriver(reason: String, onResult: (Boolean) -> Unit) {
        val matchId = currentRide.value?.matchId ?: run { onResult(false); return }

        viewModelScope.launch {
            val isSuccess = try {
                matchRepository.cancelRide(matchId, reason)
            } catch (e: Exception) {
                false
            }

            if (isSuccess) {
                _currentRide.value = null
                _routePolyline.value = null // ✅ RESET POLYLINE KHI HỦY
            }

            onResult(isSuccess)
        }
    }

    override fun onCleared() {
        super.onCleared()
        matchRepository.disconnect()
        stopLocationUpdateTimer()
    }
}