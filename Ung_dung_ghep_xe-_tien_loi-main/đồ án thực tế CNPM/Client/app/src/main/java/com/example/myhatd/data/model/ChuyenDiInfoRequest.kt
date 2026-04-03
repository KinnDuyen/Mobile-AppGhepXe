package com.example.myhatd.data.model

data class ChuyenDiInfoRequest(
    val phoneNumber: String,      // SĐT người dùng
    // XÓA giá trị mặc định để Gson luôn serialize nó
    val role: String,
    val viDo: Double,
    val kinhDo: Double,
    val thoiGian: String,         // Gửi ISO 8601 string, ví dụ "2025-11-10T15:40:52"
    val diemDi: String? = null,   // Optional
    val diemDen: String? = null,   // Optional

    val thoiGianDriver: String? = null
)