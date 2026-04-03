package com.example.myhatd.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.data.model.UserInfoRequest
import com.example.myhatd.data.network.RetrofitClient // Giả định RetrofitClient tồn tại
import com.google.android.gms.location.* // LocationServices, LocationRequest, LocationCallback
import org.maplibre.android.geometry.LatLng // Sử dụng LatLng của MapLibre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// -----------------------------------------------------------
// 1. DATA CLASS (State)
// -----------------------------------------------------------

data class MapUiState(
    val lastKnownLocation: LatLng? = null,
    val currentBearing: Double = 0.0, // ✅ Hướng di chuyển (0-360 độ)
    val isLocationPermissionGranted: Boolean = false,
    val isTrackingActive: Boolean = false, // Trạng thái lắng nghe GPS
    val mapType: Int = 0,
    val routePolyline: String? = null
)

// -----------------------------------------------------------
// 2. VIEW MODEL
// -----------------------------------------------------------

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    // Client GPS của Google Play Services
    private var fusedLocationClient: FusedLocationProviderClient? = null

    // Callback để nhận các cập nhật vị trí
    private lateinit var locationCallback: LocationCallback

    fun setLocationPermission(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
    }

    // -----------------------------------------------------------
    // 3. LOGIC TRACKING VỊ TRÍ LIÊN TỤC
    // -----------------------------------------------------------

    /**
     * Bắt đầu yêu cầu cập nhật vị trí liên tục (Real-time Tracking).
     * Hàm này NÊN được gọi trong LaunchedEffect của DriverRideTrackingScreen.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context) {
        // 1. Kiểm tra quyền và trạng thái tracking
        if (!_uiState.value.isLocationPermissionGranted || _uiState.value.isTrackingActive) return

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 2. Cấu hình LocationRequest (Ví dụ: cập nhật mỗi 5 giây)
        val locationRequest = LocationRequest.create().apply {
            interval = 5000L // 5 giây (Thời gian cập nhật ưu tiên)
            fastestInterval = 2000L // 2 giây (Thời gian cập nhật nhanh nhất)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Độ chính xác cao
        }

        // 3. Định nghĩa LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { loc ->
                    // ✅ LẤY VỊ TRÍ VÀ HƯỚNG DI CHUYỂN
                    val latLng = LatLng(loc.latitude, loc.longitude)
                    // Sử dụng bearing mới, nếu không có thì giữ lại bearing cũ
                    val bearing = if (loc.hasBearing()) loc.bearing.toDouble() else _uiState.value.currentBearing

                    viewModelScope.launch {
                        _uiState.update { currentState ->
                            currentState.copy(
                                lastKnownLocation = latLng,
                                currentBearing = bearing // ✅ CẬP NHẬT HƯỚNG
                            )
                        }
                    }
                }
            }
        }

        // 4. Bắt đầu nhận cập nhật vị trí
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() // Chạy trên Main Thread
        )?.addOnSuccessListener {
            _uiState.update { it.copy(isTrackingActive = true) }
        }?.addOnFailureListener { e ->
            // Xử lý lỗi (ví dụ: GPS bị tắt)
            Log.e("MapViewModel", "Lỗi startLocationUpdates: ${e.message}")
        }
    }

    /**
     * Ngừng cập nhật vị trí để tiết kiệm pin.
     * Hàm này NÊN được gọi khi màn hình tracking bị hủy (onCleared).
     */
    fun stopLocationUpdates() {
        if (!_uiState.value.isTrackingActive) return

        fusedLocationClient?.removeLocationUpdates(locationCallback)?.addOnCompleteListener {
            _uiState.update { it.copy(isTrackingActive = false) }
        }
    }

    // -----------------------------------------------------------
    // 4. CÁC HÀM CŨ (Đã sửa để dùng MapLibre LatLng và StateFlow update)
    // -----------------------------------------------------------

    fun setRoute(polyline: String) {
        _uiState.update { it.copy(routePolyline = polyline) }
    }

    fun setMapType(type: Int) {
        _uiState.update { it.copy(mapType = type) }
    }

    fun sendUserInfoToServer(
        soDienThoai: String,
        ten: String,
        cccd: String?,
        role: String
    ) {
        val request = UserInfoRequest(
            phoneNumber = soDienThoai,
            name = ten,
            cccd = cccd,
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.saveUserInfo(request)
                if (response.isSuccessful) {
                    // TODO: xử lý thành công
                } else {
                    // TODO: xử lý lỗi server
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // TODO: xử lý lỗi mạng
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Đảm bảo dừng cập nhật vị trí khi ViewModel bị hủy
        stopLocationUpdates()
    }
}