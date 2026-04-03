package com.example.myhatd.data.model

data class DriverLocationUpdate( // ✅ Data class mới cho Body Request
    val matchId: Long,
    val lat: Double,
    val lng: Double,
    val bearing: Double
)

