// data/model/NominatimResult.kt
package com.example.myhatd.data.model


data class NominatimResult(
    val place_id: Long,
    val licence: String,
    val lat: String, // Vĩ độ (Latitude)
    val lon: String, // Kinh độ (Longitude)
    val display_name: String, // Tên hiển thị đầy đủ
    val type: String,
    val importance: Double,
    // Bạn có thể thêm các trường khác nếu cần
)

