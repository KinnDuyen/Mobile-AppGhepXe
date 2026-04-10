package com.example.myhatd.data.model

data class UserInfoRequest(
    val phoneNumber: String,
    val name: String, // Tên người dùng
    val cccd: String? = null, // Thêm trường CCCD
)

// UserResponse (Giữ nguyên)
