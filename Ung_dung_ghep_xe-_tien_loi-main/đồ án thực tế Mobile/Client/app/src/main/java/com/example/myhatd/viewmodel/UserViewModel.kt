package com.example.myhatd.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myhatd.repository.AuthRepository
import com.example.myhatd.data.model.UserResponse
import kotlinx.coroutines.launch

class UserViewModel(private val repository: AuthRepository) : ViewModel() {

    // --- State ---
    val userData = mutableStateOf<UserResponse?>(null)
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    // --- Load dữ liệu user từ BE theo phone number ---
    fun loadUser(phone: String) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                val result = repository.getCurrentUser(phone)
                result.fold(
                    onSuccess = { userData.value = it },
                    onFailure = { errorMessage.value = it.message }
                )
            } catch (e: Exception) {
                errorMessage.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    // --- Factory để Compose có thể tạo ViewModel với repository ---
    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
