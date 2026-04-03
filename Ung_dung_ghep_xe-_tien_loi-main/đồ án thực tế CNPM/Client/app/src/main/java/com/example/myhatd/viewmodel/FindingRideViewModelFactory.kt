package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myhatd.repository.MatchRepository

/**
 * Factory tùy chỉnh để tạo FindingRideViewModel và cung cấp MatchRepository.
 */
class FindingRideViewModelFactory(
    private val matchRepository: MatchRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Kiểm tra đúng ViewModel cần tạo
        if (modelClass.isAssignableFrom(FindingRideViewModel::class.java)) {
            // Trả về instance của ViewModel với dependency đã được cung cấp
            return FindingRideViewModel(matchRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}