package com.example.myhatd.repository

import com.example.myhatd.data.model.UserInfoRequest
import com.example.myhatd.data.network.ApiService
import com.example.myhatd.data.model.UserResponse
import retrofit2.Response

/**
 * Hàm tiện ích nội tuyến (inline) để đóng gói logic xử lý Retrofit Response
 * và try-catch chung cho các cuộc gọi API.
 */
private inline fun <T> apiCall(call: () -> Response<T>): Result<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                // Thành công và Body có dữ liệu (Ví dụ: UserResponse chứa sessionToken)
                Result.success(body)
            } else {
                // Xử lý trường hợp 204 No Content hoặc Body là null
                Result.failure(Exception("Lỗi định dạng phản hồi: Body rỗng hoặc No Content."))
            }
        } else {
            // Lỗi HTTP (4xx, 5xx)
            // Cố gắng đọc lỗi chi tiết hơn từ errorBody
            val errorBody = response.errorBody()?.string() ?: response.message()
            val errorMessage = "Lỗi HTTP ${response.code()}: $errorBody"
            Result.failure(Exception(errorMessage))
        }
    } catch (e: Exception) {
        // Lỗi mạng, Timeout, Serialization, v.v.
        // Sử dụng localizedMessage để cung cấp thông tin lỗi thân thiện hơn
        Result.failure(Exception("Lỗi kết nối hoặc xử lý dữ liệu: ${e.localizedMessage}"))
    }
}

/**
 * Repository chịu trách nhiệm gọi API liên quan đến Xác thực (Auth)
 * và lưu thông tin người dùng.
 */
class AuthRepository(private val apiService: ApiService) {

    /**
     * Gửi thông tin bổ sung của người dùng lên Backend sau khi xác thực OTP.
     * Backend trả về UserResponse, được giả định có chứa sessionToken.
     */
    suspend fun saveUserInfo(request: UserInfoRequest): Result<UserResponse> {
        // Sử dụng apiCall để thực hiện cuộc gọi và xử lý kết quả
        return apiCall { apiService.saveUserInfo(request) }
    }

    /**
     * Lấy thông tin hiện tại của người dùng bằng số điện thoại.
     */
    suspend fun getCurrentUser(phone: String): Result<UserResponse> {
        // Sử dụng apiCall để thực hiện cuộc gọi và xử lý kết quả
        return apiCall { apiService.getCurrentUser(phone) }
    }
}