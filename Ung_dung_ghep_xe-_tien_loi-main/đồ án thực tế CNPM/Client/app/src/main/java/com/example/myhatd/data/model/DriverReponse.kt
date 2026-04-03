package com.example.myhatd.data.model

data class DriverResponse(
    val idDriver: Long?,         // ✅ chữ thường (theo chuẩn JSON Jackson của Spring)
    val phoneNumber: String?,
    val gioiTinh: String?,
    val bienSo: String?,
    val hangXe: String?,
    val role: String?,
    val message: String?         // ✅ bỏ dấu phẩy cuối
)
