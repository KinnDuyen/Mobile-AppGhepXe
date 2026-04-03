//package com.example.myhatd.viewmodel
//
//import android.content.Context
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.myhatd.repository.LocationRepository
//import com.example.myhatd.data.model.NominatimResult
//import com.example.myhatd.data.model.TripRequestResponse
//import com.example.myhatd.data.model.YeuCauChuyenDi
//import com.example.myhatd.data.network.RetrofitClient
//import com.example.myhatd.ui.customer.SearchField
//import com.google.android.gms.maps.model.LatLng
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import retrofit2.HttpException
//import java.io.IOException
//
//// Sealed Class để quản lý trạng thái yêu cầu API
//sealed class TripRequestState {
//    object Idle : TripRequestState()
//    object Loading : TripRequestState()
//    data class Success(val response: TripRequestResponse?, val message: String) : TripRequestState()
//    data class Error(val error: String) : TripRequestState()
//}
//
//class LocationSearchViewModel(
//    private val repository: LocationRepository = LocationRepository()
//) : ViewModel() {
//
//    // =======================================================
//    // 1. SEARCH & LOCATION STATES
//    // =======================================================
//    private val _searchResults = MutableStateFlow<List<NominatimResult>>(emptyList())
//    val searchResults: StateFlow<List<NominatimResult>> = _searchResults
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _isLoadingLocation = MutableStateFlow(false)
//    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation
//
//    private val _searchError = MutableStateFlow<String?>(null)
//    val searchError: StateFlow<String?> = _searchError
//    // -----------------------------------------------------
//
//    private var searchJob: Job? = null
//    private val DEBOUNCE_DELAY_MS = 300L
//
//    // =======================================================
//    // 2. INPUT & SELECTED LOCATION STATES
//    // =======================================================
//    private val _originSearchText = MutableStateFlow("")
//    val originSearchText: StateFlow<String> = _originSearchText
//
//    private val _destinationSearchText = MutableStateFlow("")
//    val destinationSearchText: StateFlow<String> = _destinationSearchText
//
//    private val _activeSearchField = MutableStateFlow(SearchField.NONE)
//    val activeSearchField: StateFlow<SearchField> = _activeSearchField
//
//    private val _selectedOriginLatLng = MutableStateFlow<LatLng?>(null)
//    val selectedOriginLatLng: StateFlow<LatLng?> = _selectedOriginLatLng
//
//    private val _selectedDestinationLatLng = MutableStateFlow<LatLng?>(null)
//    val selectedDestinationLatLng: StateFlow<LatLng?> = _selectedDestinationLatLng
//
//    // =======================================================
//    // 3. API CALL: TRIP REQUEST STATUS
//    // =======================================================
//    private val _tripRequestStatus = MutableStateFlow<TripRequestState>(TripRequestState.Idle)
//    val tripRequestStatus: StateFlow<TripRequestState> = _tripRequestStatus
//
//    fun resetTripRequestStatus() {
//        _tripRequestStatus.value = TripRequestState.Idle
//    }
//    // =======================================================
//    // 4. API CALL: SEND TRIP REQUEST
//    // =======================================================
//
//    fun sendTripRequest(phoneNumber: String, role: String) {
//        val origin = _selectedOriginLatLng.value
//        val destination = _selectedDestinationLatLng.value
//
//        if (origin == null || destination == null) {
//            _tripRequestStatus.value = TripRequestState.Error("Lỗi: Vui lòng chọn đầy đủ Điểm đi và Điểm đến.")
//            return
//        }
//
//        _tripRequestStatus.value = TripRequestState.Loading
//
//        val request = YeuCauChuyenDi(
//            // Dùng tên địa điểm đã được chọn
//            tenDiemDi = originSearchText.value,
//            tenDiemDen = destinationSearchText.value,
//            viDoDiemDi = origin.latitude,
//            kinhDoDiemDi = origin.longitude,
//            viDoDiemDen = destination.latitude,
//            kinhDoDiemDen = destination.longitude,
//            soDienThoai = phoneNumber,
//            vaiTro = role
//        )
//
//        viewModelScope.launch {
//            try {
//                val response = RetrofitClient.apiService.taoYeuCauChuyenDi(request)
//
//                if (response.isSuccessful) {
//                    val responseBody = response.body()
//                    val id = responseBody?.requestId ?: 0L
//                    val message = "Yêu cầu chuyến đi thành công! ID: $id"
//
//                    _tripRequestStatus.value = TripRequestState.Success(responseBody, message)
//
//                } else {
//                    val errorBody = response.errorBody()?.string()
//                    val errorMessage = "Lỗi Server ${response.code()}: ${errorBody ?: "Không có thông báo lỗi."}"
//                    _tripRequestStatus.value = TripRequestState.Error(errorMessage)
//                }
//            } catch (e: HttpException) {
//                _tripRequestStatus.value = TripRequestState.Error("Lỗi HTTP ${e.code()}: Kiểm tra kết nối Server.")
//            } catch (e: IOException) {
//                _tripRequestStatus.value = TripRequestState.Error("Lỗi kết nối mạng: Vui lòng kiểm tra Internet.")
//            } catch (e: Exception) {
//                _tripRequestStatus.value = TripRequestState.Error("Lỗi không xác định: ${e.message}")
//            }
//        }
//    }
//
//    // =======================================================
//    // 5. LOCATION AND SEARCH LOGIC (Đã sửa lỗi gợi ý)
//    // =======================================================
//
//    fun useCurrentLocation(context: Context, latLng: LatLng?) {
//        if (latLng == null) {
//            _originSearchText.value = "GPS chưa hoạt động hoặc quyền bị từ chối."
//            _isLoadingLocation.value = false
//            return
//        }
//
//        _isLoadingLocation.value = true
//        _originSearchText.value = "Fetching address..."
//        _activeSearchField.value = SearchField.NONE
//        searchJob?.cancel()
//        _searchError.value = null
//
//        viewModelScope.launch {
//            try {
//                val address = repository.reverseGeocode(context, latLng.latitude, latLng.longitude)
//
//                _selectedOriginLatLng.value = latLng
//                _originSearchText.value = address ?: "Unknown location"
//
//            } catch (e: Exception) {
//                _originSearchText.value = "Geocoding Error: ${e.message}"
//            } finally {
//                _isLoadingLocation.value = false
//            }
//        }
//    }
//
//    fun onSearchTextChange(text: String, field: SearchField) {
//        when (field) {
//            SearchField.ORIGIN -> _originSearchText.value = text
//            SearchField.DESTINATION -> _destinationSearchText.value = text
//            SearchField.NONE -> { /* Skip */ }
//        }
//
//        _activeSearchField.value = field
//
//        startAutoSearch(query = text)
//    }
//
//    private fun startAutoSearch(query: String) {
//        searchJob?.cancel()
//        _searchResults.value = emptyList()
//        _searchError.value = null
//
//        if (query.length > 2) {
//            searchJob = viewModelScope.launch {
//                delay(DEBOUNCE_DELAY_MS)
//
//                _isLoading.value = true
//                try {
//                    val results = repository.searchLocation(query)
//                    _searchResults.value = results
//                } catch (e: Exception) {
//                    val errorMessage = when (e) {
//                        is HttpException -> "Lỗi HTTP: ${e.code()}. Vui lòng thử lại sau."
//                        is IOException -> "Lỗi kết nối mạng: Kiểm tra Internet."
//                        else -> "Lỗi không xác định: ${e.message}"
//                    }
//                    _searchError.value = errorMessage
//                } finally {
//                    _isLoading.value = false
//                }
//            }
//        }
//    }
//
//    fun setActiveSearchField(field: SearchField) {
//        searchJob?.cancel()
//        if (_activeSearchField.value != field) {
//            _activeSearchField.value = field
//            // Nếu chuyển trường, ta clear search results cũ
//            _searchResults.value = emptyList()
//            _searchError.value = null
//        }
//        // Kích hoạt tìm kiếm lại nếu có text sẵn
//        val currentText = when(field) {
//            SearchField.ORIGIN -> _originSearchText.value
//            SearchField.DESTINATION -> _destinationSearchText.value
//            SearchField.NONE -> ""
//        }
//        if (currentText.length > 2) {
//            startAutoSearch(currentText)
//        }
//    }
//
//    fun selectLocation(result: NominatimResult, field: SearchField) {
//        when (field) {
//            SearchField.ORIGIN -> {
//                _originSearchText.value = result.display_name
//                try {
//                    val lat = result.lat.toDouble()
//                    val lon = result.lon.toDouble()
//                    _selectedOriginLatLng.value = LatLng(lat, lon)
//                } catch (e: Exception) { /* Bỏ qua lỗi chuyển đổi */ }
//            }
//            SearchField.DESTINATION -> {
//                _destinationSearchText.value = result.display_name
//                try {
//                    val lat = result.lat.toDouble()
//                    val lon = result.lon.toDouble()
//                    _selectedDestinationLatLng.value = LatLng(lat, lon)
//                } catch (e: Exception) { /* Bỏ qua lỗi chuyển đổi */ }
//            }
//            SearchField.NONE -> { /* Skip */ }
//        }
//
//        // HÀNH ĐỘNG SỬA: Ẩn danh sách gợi ý sau khi chọn
//        _activeSearchField.value = SearchField.NONE
//        _searchResults.value = emptyList()
//        searchJob?.cancel()
//        _searchError.value = null
//    }
//}
package com.example.myhatd.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.repository.LocationRepository
import com.example.myhatd.data.model.NominatimResult
import com.example.myhatd.data.model.TripRequestResponse
import com.example.myhatd.data.model.YeuCauChuyenDi
import com.example.myhatd.data.network.RetrofitClient
import com.example.myhatd.ui.customer.SearchField
// XÓA: import com.google.android.gms.maps.model.LatLng
// ✅ THAY THẾ BẰNG MAPLIBRE LATLNG
import org.maplibre.android.geometry.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Sealed Class để quản lý trạng thái yêu cầu API (Giữ nguyên)
sealed class TripRequestState {
    object Idle : TripRequestState()
    object Loading : TripRequestState()
    data class Success(val response: TripRequestResponse?, val message: String) : TripRequestState()
    data class Error(val error: String) : TripRequestState()
}

class LocationSearchViewModel(
    private val repository: LocationRepository = LocationRepository()
) : ViewModel() {

    // =======================================================
    // 1. SEARCH & LOCATION STATES
    // =======================================================
    private val _searchResults = MutableStateFlow<List<NominatimResult>>(emptyList())
    val searchResults: StateFlow<List<NominatimResult>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError
    // -----------------------------------------------------

    private var searchJob: Job? = null
    private val DEBOUNCE_DELAY_MS = 300L

    // =======================================================
    // 2. INPUT & SELECTED LOCATION STATES
    // =======================================================
    private val _originSearchText = MutableStateFlow("")
    val originSearchText: StateFlow<String> = _originSearchText

    private val _destinationSearchText = MutableStateFlow("")
    val destinationSearchText: StateFlow<String> = _destinationSearchText

    private val _activeSearchField = MutableStateFlow(SearchField.NONE)
    val activeSearchField: StateFlow<SearchField> = _activeSearchField

    // ✅ SỬA LỖI 1: Thay LatLng Google Maps bằng LatLng MapLibre
    private val _selectedOriginLatLng = MutableStateFlow<LatLng?>(null)
    val selectedOriginLatLng: StateFlow<LatLng?> = _selectedOriginLatLng

    // ✅ SỬA LỖI 2: Thay LatLng Google Maps bằng LatLng MapLibre
    private val _selectedDestinationLatLng = MutableStateFlow<LatLng?>(null)
    val selectedDestinationLatLng: StateFlow<LatLng?> = _selectedDestinationLatLng

    // =======================================================
    // 3. API CALL: TRIP REQUEST STATUS (Giữ nguyên)
    // =======================================================
    private val _tripRequestStatus = MutableStateFlow<TripRequestState>(TripRequestState.Idle)
    val tripRequestStatus: StateFlow<TripRequestState> = _tripRequestStatus

    fun resetTripRequestStatus() {
        _tripRequestStatus.value = TripRequestState.Idle
    }
    // =======================================================
    // 4. API CALL: SEND TRIP REQUEST (Giữ nguyên logic)
    // =======================================================

    fun sendTripRequest(phoneNumber: String, role: String) {
        val origin = _selectedOriginLatLng.value
        val destination = _selectedDestinationLatLng.value

        if (origin == null || destination == null) {
            _tripRequestStatus.value = TripRequestState.Error("Lỗi: Vui lòng chọn đầy đủ Điểm đi và Điểm đến.")
            return
        }

        _tripRequestStatus.value = TripRequestState.Loading

        val request = YeuCauChuyenDi(
            // Dùng tên địa điểm đã được chọn
            tenDiemDi = originSearchText.value,
            tenDiemDen = destinationSearchText.value,
            // LatLng MapLibre vẫn có thuộc tính latitude và longitude
            viDoDiemDi = origin.latitude,
            kinhDoDiemDi = origin.longitude,
            viDoDiemDen = destination.latitude,
            kinhDoDiemDen = destination.longitude,
            soDienThoai = phoneNumber,
            vaiTro = role
        )

        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.taoYeuCauChuyenDi(request)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val id = responseBody?.requestId ?: 0L
                    val message = "Yêu cầu chuyến đi thành công! ID: $id"

                    _tripRequestStatus.value = TripRequestState.Success(responseBody, message)

                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = "Lỗi Server ${response.code()}: ${errorBody ?: "Không có thông báo lỗi."}"
                    _tripRequestStatus.value = TripRequestState.Error(errorMessage)
                }
            } catch (e: HttpException) {
                _tripRequestStatus.value = TripRequestState.Error("Lỗi HTTP ${e.code()}: Kiểm tra kết nối Server.")
            } catch (e: IOException) {
                _tripRequestStatus.value = TripRequestState.Error("Lỗi kết nối mạng: Vui lòng kiểm tra Internet.")
            } catch (e: Exception) {
                _tripRequestStatus.value = TripRequestState.Error("Lỗi không xác định: ${e.message}")
            }
        }
    }

    // =======================================================
    // 5. LOCATION AND SEARCH LOGIC
    // =======================================================

    // ✅ SỬA LỖI 3: Thay LatLng Google Maps bằng LatLng MapLibre
    fun useCurrentLocation(context: Context, latLng: LatLng?) {
        if (latLng == null) {
            _originSearchText.value = "GPS chưa hoạt động hoặc quyền bị từ chối."
            _isLoadingLocation.value = false
            return
        }

        _isLoadingLocation.value = true
        _originSearchText.value = "Fetching address..."
        _activeSearchField.value = SearchField.NONE
        searchJob?.cancel()
        _searchError.value = null

        viewModelScope.launch {
            try {
                // LatLng MapLibre vẫn có thuộc tính latitude và longitude
                val address = repository.reverseGeocode(context, latLng.latitude, latLng.longitude)

                _selectedOriginLatLng.value = latLng
                _originSearchText.value = address ?: "Unknown location"

            } catch (e: Exception) {
                _originSearchText.value = "Geocoding Error: ${e.message}"
            } finally {
                _isLoadingLocation.value = false
            }
        }
    }

    fun onSearchTextChange(text: String, field: SearchField) {
        when (field) {
            SearchField.ORIGIN -> _originSearchText.value = text
            SearchField.DESTINATION -> _destinationSearchText.value = text
            SearchField.NONE -> { /* Skip */ }
        }

        _activeSearchField.value = field

        startAutoSearch(query = text)
    }

    private fun startAutoSearch(query: String) {
        searchJob?.cancel()
        _searchResults.value = emptyList()
        _searchError.value = null

        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(DEBOUNCE_DELAY_MS)

                _isLoading.value = true
                try {
                    val results = repository.searchLocation(query)
                    _searchResults.value = results
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is HttpException -> "Lỗi HTTP: ${e.code()}. Vui lòng thử lại sau."
                        is IOException -> "Lỗi kết nối mạng: Kiểm tra Internet."
                        else -> "Lỗi không xác định: ${e.message}"
                    }
                    _searchError.value = errorMessage
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun setActiveSearchField(field: SearchField) {
        searchJob?.cancel()
        if (_activeSearchField.value != field) {
            _activeSearchField.value = field
            // Nếu chuyển trường, ta clear search results cũ
            _searchResults.value = emptyList()
            _searchError.value = null
        }
        // Kích hoạt tìm kiếm lại nếu có text sẵn
        val currentText = when(field) {
            SearchField.ORIGIN -> _originSearchText.value
            SearchField.DESTINATION -> _destinationSearchText.value
            SearchField.NONE -> ""
        }
        if (currentText.length > 2) {
            startAutoSearch(currentText)
        }
    }

    fun selectLocation(result: NominatimResult, field: SearchField) {
        when (field) {
            SearchField.ORIGIN -> {
                _originSearchText.value = result.display_name
                try {
                    val lat = result.lat.toDouble()
                    val lon = result.lon.toDouble()
                    // ✅ SỬA LỖI 4: Thay LatLng Google Maps bằng LatLng MapLibre
                    _selectedOriginLatLng.value = LatLng(lat, lon)
                } catch (e: Exception) { /* Bỏ qua lỗi chuyển đổi */ }
            }
            SearchField.DESTINATION -> {
                _destinationSearchText.value = result.display_name
                try {
                    val lat = result.lat.toDouble()
                    val lon = result.lon.toDouble()
                    // ✅ SỬA LỖI 4: Thay LatLng Google Maps bằng LatLng MapLibre
                    _selectedDestinationLatLng.value = LatLng(lat, lon)
                } catch (e: Exception) { /* Bỏ qua lỗi chuyển đổi */ }
            }
            SearchField.NONE -> { /* Skip */ }
        }

        // HÀNH ĐỘNG SỬA: Ẩn danh sách gợi ý sau khi chọn
        _activeSearchField.value = SearchField.NONE
        _searchResults.value = emptyList()
        searchJob?.cancel()
        _searchError.value = null
    }
}