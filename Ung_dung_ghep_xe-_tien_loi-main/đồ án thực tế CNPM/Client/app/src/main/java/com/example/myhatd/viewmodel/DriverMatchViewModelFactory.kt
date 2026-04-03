package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myhatd.repository.MatchRepository
import com.example.myhatd.repository.RoutingRepository

class DriverMatchViewModelFactory(
    private val matchRepository: MatchRepository,
    private val routingRepository: RoutingRepository,
    private val mapViewModel: MapViewModel // ✅ 1. THÊM THAM SỐ MapViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DriverMatchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // ✅ 2. TRUYỀN TẤT CẢ CÁC DEPENDENCY VÀO DriverMatchViewModel
            return DriverMatchViewModel(
                matchRepository,
                routingRepository,
                mapViewModel // ✅ TRUYỀN MapViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}