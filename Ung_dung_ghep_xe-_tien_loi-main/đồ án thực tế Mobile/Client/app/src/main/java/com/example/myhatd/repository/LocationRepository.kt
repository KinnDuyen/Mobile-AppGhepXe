package com.example.myhatd.repository

import com.example.myhatd.data.network.NominatimRetrofitClient
import com.example.myhatd.data.model.NominatimResult
import com.example.myhatd.data.network.NominatimApiService

class LocationRepository(
    private val apiService: NominatimApiService = NominatimRetrofitClient.nominatimService
) {
    suspend fun searchLocation(query: String): List<NominatimResult> {
        return apiService.searchLocation(query)
    }
}