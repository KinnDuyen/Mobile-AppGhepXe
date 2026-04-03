package com.example.myhatd.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myhatd.data.model.UserInfoRequest
import com.example.myhatd.data.storage.TokenManager // ✅ IMPORT MỚI
import com.example.myhatd.repository.AuthRepository
import com.example.myhatd.service.AuthCallback
import com.example.myhatd.service.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- A. DATA CLASS CHO STATE ---
data class AuthState(
    val phoneNumber: String = "+84",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val verificationId: String? = null,
    val error: String? = null,
    val isOtpSent: Boolean = false,
    val isAuthenticated: Boolean = false, // Xác thực Firebase thành công
    val isInfoSaved: Boolean = false // Lưu thông tin user (Tên, CCCD) & Token thành công
)

class AuthViewModel(
    private val authService: FirebaseAuthService,
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager // ✅ THAM SỐ MỚI CHO VIỆC LƯU PHIÊN
) : ViewModel(), AuthCallback {

    private val auth = FirebaseAuth.getInstance()

    // ✅ STATE FLOW
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    init {
        authService.callback = this
    }

    // --- B. HÀM CẬP NHẬT STATE VÀ LOGIC FIREBASE ---

    fun resetState() {
        _state.value = AuthState()
        // Xóa token khi reset trạng thái (chuẩn bị đăng nhập mới)
        tokenManager.clearToken()
    }

    fun onPhoneNumberChange(newNumber: String) {
        _state.update { it.copy(phoneNumber = newNumber, error = null) }
    }

    fun onOtpCodeChange(newCode: String) {
        _state.update { it.copy(otpCode = newCode, error = null) }
    }

    /**
     * Gửi mã OTP. Lưu ý: Cần truyền Activity vào hàm sendVerificationCode
     * trong FirebaseAuthService (giả định)
     */
    fun sendOtp(activity: Activity) { // ✅ Yêu cầu Activity để xử lý Phone Auth
        if (state.value.phoneNumber.length < 10 || !state.value.phoneNumber.startsWith("+")) {
            _state.update { it.copy(error = "Vui lòng nhập số điện thoại hợp lệ (+Mã quốc gia).") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        authService.sendVerificationCode(state.value.phoneNumber, activity)
    }

    fun verifyOtp() {
        val currentState = state.value
        val vId = currentState.verificationId
        val code = currentState.otpCode

        if (vId == null || code.length < 6) {
            _state.update { it.copy(error = "Mã OTP không hợp lệ.", isLoading = false) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        val credential = PhoneAuthProvider.getCredential(vId, code)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.update { it.copy(
                        isLoading = false,
                        isAuthenticated = true, // ✅ Xác thực thành công -> Chuyển sang nhập thông tin
                        error = null
                    ) }
                } else {
                    _state.update { it.copy(
                        isLoading = false,
                        error = task.exception?.message ?: "Xác minh OTP thất bại."
                    ) }
                }
            }
    }

    // --- C. HÀM GỬI DỮ LIỆU USER LÊN BACKEND (TÊN + CCCD) ---

    /**
     * Gửi thông tin bổ sung (Tên, CCCD) lên Backend và LƯU PHIÊN (Token).
     */
    fun finalizeUserInfo(name: String, cccd: String? = null) {
        val currentPhoneNumber = state.value.phoneNumber
        if (currentPhoneNumber.isBlank()) {
            _state.update { it.copy(error = "Lỗi hệ thống: Không tìm thấy số điện thoại.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val request = UserInfoRequest(
                phoneNumber = currentPhoneNumber,
                name = name,
                cccd = cccd?.ifBlank { null }
            )
            // AuthRepository.saveUserInfo trả về UserResponse (chứa sessionToken)
            val result = authRepository.saveUserInfo(request)

            result.fold(
                onSuccess = { userResponse ->
                    // ✅ LƯU TOKEN VÀO BỘ NHỚ CỤC BỘ ĐỂ LƯU PHIÊN
                    userResponse.sessionToken?.let { token ->
                        tokenManager.saveToken(token)
                    }

                    _state.update { it.copy(
                        isLoading = false,
                        isInfoSaved = true, // ✅ Thành công, chuyển hướng về Home
                        error = null
                    ) }
                },
                onFailure = { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Lỗi khi lưu thông tin người dùng lên server."
                    ) }
                }
            )
        }
    }

    // --- D. AUTH CALLBACKS ---

    override fun onCodeSent(verificationId: String) {
        _state.update { it.copy(
            isLoading = false,
            verificationId = verificationId,
            isOtpSent = true,
            error = null
        ) }
    }

    override fun onVerificationFailed(message: String) {
        _state.update { it.copy(
            isLoading = false,
            error = message
        ) }
    }

    // --- E. FACTORY ---
    class Factory(
        private val activity: Activity,
        private val authRepository: AuthRepository,
        private val tokenManager: TokenManager // ✅ THAM SỐ MỚI
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val authService = FirebaseAuthService(activity)
                @Suppress("UNCHECKED_CAST")
                // ✅ TRUYỀN TOKEN MANAGER VÀO VIEWMODEL
                return AuthViewModel(authService, authRepository, tokenManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}