package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myhatd.data.model.DriverInfoRequest
import com.example.myhatd.data.model.DriverResponse
import com.example.myhatd.repository.DriverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class DriverViewModel(private val driverRepository: DriverRepository) : ViewModel() {

    // --- State Observable ---
    private val _driverInfo = MutableStateFlow<DriverResponse?>(null)
    val driverInfo: StateFlow<DriverResponse?> = _driverInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // --- Fetch driver info ---
    fun fetchDriver(phoneNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // SỬA LỖI: Gọi hàm của Repository, KHÔNG gọi thẳng API Service
                val response: Response<DriverResponse> = driverRepository.getDriverByPhone(phoneNumber)

                if (response.isSuccessful) {
                    _driverInfo.value = response.body()
                } else {
                    _errorMessage.value = "Lỗi từ server: ${response.code()}"
                    _driverInfo.value = null
                }
            } catch (e: Exception) {
                // ...
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- Save driver info ---
    suspend fun saveDriverInfo(request: DriverInfoRequest): Response<Unit> {
        return driverRepository.saveDriverInfo(request)
    }

    // --- ViewModel Factory ---
    class Factory(private val driverRepository: DriverRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DriverViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DriverViewModel(driverRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
