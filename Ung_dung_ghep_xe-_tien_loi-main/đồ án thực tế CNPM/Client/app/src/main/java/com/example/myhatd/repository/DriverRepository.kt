package com.example.myhatd.repository

import com.example.myhatd.data.model.DriverInfoRequest
import com.example.myhatd.data.network.ApiService
import retrofit2.Response


class DriverRepository(val apiService: ApiService) {

    suspend fun saveDriverInfo(request: DriverInfoRequest): Response<Unit> {
        return try {
            apiService.saveDriverInfo(request)
        } catch (e: Exception) {
            throw e // FE sẽ xử lý exception này
        }
    }

    suspend fun getDriverByPhone(phoneNumber: String): Response<com.example.myhatd.data.model.DriverResponse> {
        return apiService.getDriverByPhone(phoneNumber)
    }

}

