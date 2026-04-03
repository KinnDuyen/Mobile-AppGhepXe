package com.example.myhatd.ui.utils

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.annotations.Polyline
import org.maplibre.android.annotations.PolylineOptions
import org.maplibre.android.geometry.LatLng
// Cần đảm bảo dependency Mapbox GeoJSON được thêm vào build.gradle
import com.mapbox.geojson.utils.PolylineUtils
import com.mapbox.geojson.Point


/**
 * Giải mã chuỗi Encoded Polyline và vẽ đường đi lên bản đồ MapLibre.
 *
 * @param map Tham chiếu đến MapLibreMap.
 * @param encodedPolyline Chuỗi Polyline (Level 5) từ dịch vụ định tuyến (OSRM).
 * @param currentPolyline Đối tượng Polyline hiện tại cần xóa (để cập nhật).
 * @param onPolylineUpdate Callback để cập nhật đối tượng Polyline mới vào State.
 */
fun drawRoutePolyline(
    map: MapLibreMap?,
    encodedPolyline: String,
    currentPolyline: Polyline?,
    onPolylineUpdate: (Polyline?) -> Unit
) {
    if (map == null || encodedPolyline.isEmpty()) return

    // 1. Giải mã chuỗi Polyline thành danh sách Mapbox Point
    // Độ chính xác (precision) 5 được sử dụng phổ biến với OSRM.
    val points: List<Point> = PolylineUtils.decode(encodedPolyline, 5)

    // 2. Chuyển đổi Mapbox Point sang MapLibre LatLng
    val latLngs = points.map { LatLng(it.latitude(), it.longitude()) }

    // 3. Xóa Polyline cũ
    currentPolyline?.remove()

    // 4. Tạo và Thêm Polyline mới vào bản đồ
    val polylineOptions = PolylineOptions()
        .addAll(latLngs)
        .color(android.graphics.Color.BLUE) // Màu xanh (mặc định) cho đường đi
        .width(6f)
        .alpha(0.9f)

    val newPolyline = map.addPolyline(polylineOptions)

    // 5. Cập nhật State
    onPolylineUpdate(newPolyline)
}