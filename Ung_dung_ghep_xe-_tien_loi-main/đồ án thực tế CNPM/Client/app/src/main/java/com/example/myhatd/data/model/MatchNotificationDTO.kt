package com.example.myhatd.data.model

// DTO này phải khớp với dữ liệu JSON bạn nhận từ Backend
data class MatchNotificationDTO(
    val matchId: Long?,
    val message: String?,

    // Thông tin tài xế
    val tenDriver: String?,
    val sdtDriver: String?,
    val bienSoXe: String?,
    val hangXe: String?,

    // Thông tin khách
    val tenUser: String?,
    val sdtUser: String?,

    // Thông tin chuyến đi
    val tenDiemDiUser: String?,
    val tenDiemDenUser: String?,
    val giaTien: Double?,
    val thoiGianDriverDenUser: String?,
    val hinhThucThanhToan: String?,

    val viDoDiemDi: Double?,
    val kinhDoDiemDi: Double?,
    val viDoDiemDen: Double?,
    val kinhDoDiemDen: Double?,
)