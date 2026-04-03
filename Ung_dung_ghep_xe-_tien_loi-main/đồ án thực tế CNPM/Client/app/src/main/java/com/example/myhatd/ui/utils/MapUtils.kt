package com.example.myhatd.ui.utils

import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory  // ← ĐÚNG
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

// Nếu bạn chuyển sang SymbolManager (khuyến nghị)
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions

/**
 * Cách cũ: dùng Marker (không xoay được nữa trong MapLibre mới)
 * → Chỉ để lại nếu bạn chấp nhận không xoay marker
 */
fun addOrUpdateMarker(
    map: MapLibreMap?,
    currentMarker: Marker?,
    onMarkerUpdate: (Marker?) -> Unit,
    latLng: LatLng,
    name: String?,
    bearing: Double = 0.0
) {
    map?.let { mapLibreMap ->
        if (currentMarker == null) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(name ?: "Địa điểm")
            // .rotation(bearing.toFloat()) ← KHÔNG TỒN TẠI trong MapLibre

            val newMarker = mapLibreMap.addMarker(markerOptions)
            onMarkerUpdate(newMarker)
        } else {
            currentMarker.position = latLng
            currentMarker.title = name ?: "Địa điểm"
            // currentMarker.setRotation(...) ← CŨNG KHÔNG CÓ
        }

        // DI CHUYỂN CAMERA – DÙNG MapLibre class
        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(16.0)
            .bearing(bearing)   // Xoay bản đồ theo hướng người dùng (rất đẹp)
            .tilt(30.0)
            .build()

        mapLibreMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition), // ← MapLibre
            1000
        )
    }
}