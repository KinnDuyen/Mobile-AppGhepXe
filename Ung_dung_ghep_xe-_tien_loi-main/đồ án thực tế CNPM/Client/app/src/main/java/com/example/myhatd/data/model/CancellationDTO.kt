// Bạn có thể đặt lớp này trong data.model hoặc nội tuyến nếu nó đơn giản
package com.example.myhatd.data.model
data class CancelRideRequest(
    val matchId: Long,
    val reason: String
)