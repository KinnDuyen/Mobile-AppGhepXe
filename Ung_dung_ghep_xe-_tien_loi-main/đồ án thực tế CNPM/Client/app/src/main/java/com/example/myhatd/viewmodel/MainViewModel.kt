package com.example.myhatd.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myhatd.data.storage.TokenManager

/**
 * ViewModel chịu trách nhiệm kiểm tra trạng thái đăng nhập (session persistence)
 * ngay khi ứng dụng khởi động bằng cách kiểm tra Session Token.
 */
class MainViewModel(private val tokenManager: TokenManager) : ViewModel() {

    // ✅ State: True nếu tìm thấy Session Token hợp lệ (người dùng đã đăng nhập)
    val isUserLoggedIn = mutableStateOf(false)

    init {
        checkLoginStatus()
    }

    /**
     * Kiểm tra sự tồn tại của Session Token trong bộ nhớ cục bộ.
     */
    private fun checkLoginStatus() {
        val token = tokenManager.getToken()
        // Nếu Token tồn tại và không rỗng, coi như người dùng đã đăng nhập.
        isUserLoggedIn.value = !token.isNullOrEmpty()
    }

    // --- Factory ---

    /**
     * Factory để tạo MainViewModel và truyền TokenManager.
     */
    class Factory(private val tokenManager: TokenManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(tokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}