// Thư mục: ui
// File: Utils.kt

package com.example.myhatd.ui.otp

import android.content.Context
import android.content.ContextWrapper
import android.app.Activity

/**
 * Hàm mở rộng để tìm Activity từ Context.
 * Giải quyết cảnh báo và lỗi casting LocalContext sang Activity.
 */
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}