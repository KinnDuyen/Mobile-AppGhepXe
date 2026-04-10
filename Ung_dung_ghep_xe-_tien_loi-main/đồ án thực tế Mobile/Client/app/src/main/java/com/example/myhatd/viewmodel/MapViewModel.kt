package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.data.model.UserInfoRequest
import com.example.myhatd.data.network.RetrofitClient
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val lastKnownLocation: LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,
    val mapType: Int = 0,
    val routePolyline: String? = null
)

class MapViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    fun setLocationPermission(isGranted: Boolean) {
        _uiState.update { it.copy(isLocationPermissionGranted = isGranted) }
    }

    fun setRoute(polyline: String) {
        _uiState.update { it.copy(routePolyline = polyline) }
    }

    fun setMapType(type: Int) {
        _uiState.update { it.copy(mapType = type) }
    }

    fun sendUserInfoToServer(soDienThoai: String, ten: String, cccd: String?, role: String) {
        val request = UserInfoRequest(phoneNumber = soDienThoai, name = ten, cccd = cccd)
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.saveUserInfo(request)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}