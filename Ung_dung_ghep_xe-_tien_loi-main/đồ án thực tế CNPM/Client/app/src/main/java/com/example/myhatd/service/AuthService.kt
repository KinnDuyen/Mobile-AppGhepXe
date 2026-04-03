// Thư mục: service
// File: AuthService.kt

package com.example.myhatd.service

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Interface để AuthViewModel nhận kết quả từ Firebase.
 */
interface AuthCallback {
    fun onCodeSent(verificationId: String)
    fun onVerificationFailed(message: String)
}

/**
 * Lớp dịch vụ chịu trách nhiệm giao tiếp với Firebase Phone Authentication.
 * Cần Activity để xử lý reCAPTCHA/xác minh ứng dụng.
 */
class FirebaseAuthService(private val activity: Activity) {

    // Biến để lưu trữ đối tượng gọi ngược (ViewModel)
    var callback: AuthCallback? = null

    /**
     * Gửi yêu cầu xác minh số điện thoại đến Firebase.
     */
    fun sendVerificationCode(phoneNumber: String, activity: Activity) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Xác minh tức thì thành công
                // Xử lý đăng nhập tự động tại đây nếu cần
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Thông báo lỗi tới ViewModel
                callback?.onVerificationFailed(e.message ?: "Lỗi xác minh không xác định.")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // Mã đã gửi. Gửi ID xác minh tới ViewModel để lưu lại
                callback?.onCodeSent(verificationId)
            }
        }

        // Thực hiện lệnh gửi mã của Firebase
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,                                // Timeout 60 giây
            TimeUnit.SECONDS,
            activity,
            callbacks
        )
    }
}