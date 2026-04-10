package com.example.myhatd.repository

import com.example.myhatd.data.network.ApiService
import com.google.gson.annotations.SerializedName
import org.maplibre.android.geometry.LatLng

// DTO đơn giản ban đầu
data class OSRMRoutingResponse(
    @SerializedName("code") val code: String,
    @SerializedName("routes") val routes: List<Route>
) {
    data class Route(
        @SerializedName("geometry") val geometry: String
    )
}

class RoutingRepository(private val apiService: ApiService) {
    private val baseRoutingUrl = "http://router.project-osrm.org/route/v1/driving/"

    suspend fun getRoutePolyline(startLocation: LatLng, endLocation: LatLng): String? {
        val coordinates = "${startLocation.longitude},${startLocation.latitude};${endLocation.longitude},${endLocation.latitude}"
        val fullUrl = "${baseRoutingUrl}$coordinates?geometries=polyline"

        val response = apiService.getRawRoutingData(fullUrl)
        return if (response.isSuccessful) {
            response.body()?.routes?.firstOrNull()?.geometry
        } else {
            null
        }
    }
}