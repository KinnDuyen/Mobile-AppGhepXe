package com.example.myhatd.data.model

data class UserResponse(
    val userId: Long?,
    val name: String?,
    val canCuocCongDan: String?,     // CCCD
    val phoneNumber: String?,       // SĐT
    val role: String,
    val message: String?,// Thông điệp BE trả về

    val sessionToken: String? = null
)
