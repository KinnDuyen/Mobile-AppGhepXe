// Đặt tên file là MarkerUtils.kt hoặc sửa file addOrUpdateMarker.kt
package com.example.myhatd.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.annotation.DrawableRes
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

// ✅ Định nghĩa kích thước cố định cho Icon (ví dụ 48x48 dp)
// MapLibre sử dụng pixel, nên cần chuyển đổi dp sang pixel
private const val ICON_SIZE_DP = 28

fun addOrUpdateMarkerWithCustomIcon(
    map: MapLibreMap?,
    currentMarker: Marker?,
    onMarkerUpdate: (Marker?) -> Unit,
    latLng: LatLng,
    name: String?,
    context: Context,
    @DrawableRes iconResId: Int? = null,
    bearing: Double = 0.0
) {
    map?.let { mapLibreMap ->
        if (currentMarker == null) {
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(name ?: "Địa điểm")

            // 1. Xử lý Icon Tùy chỉnh và Resize
            if (iconResId != null) {
                try {
                    val iconFactory = IconFactory.getInstance(context)
                    val drawable = context.resources.getDrawable(iconResId, context.theme)

                    // ✅ TÍNH TOÁN KÍCH THƯỚC PIXEL THỰC TẾ
                    val density = context.resources.displayMetrics.density
                    val sizePx = (ICON_SIZE_DP * density).toInt()

                    // Chuyển Drawable sang Bitmap và Resize
                    val originalBitmap = (drawable as BitmapDrawable).bitmap
                    val resizedBitmap = Bitmap.createScaledBitmap(
                        originalBitmap,
                        sizePx, // Chiều rộng mới
                        sizePx, // Chiều cao mới
                        false
                    )

                    val icon: Icon = iconFactory.fromBitmap(resizedBitmap)
                    markerOptions.icon(icon)
                } catch (e: Exception) {
                    Log.e("MapLibreUtil", "Lỗi tạo hoặc resize icon: ${e.message}")
                }
            }

            // 2. Thêm Marker mới
            val newMarker = mapLibreMap.addMarker(markerOptions)
            onMarkerUpdate(newMarker)
        } else {
            // 3. Cập nhật vị trí và tiêu đề
            currentMarker.position = latLng
            currentMarker.title = name ?: "Địa điểm"
        }

        // 4. DI CHUYỂN CAMERA (Vẫn giữ lại phần xoay Map)
        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(16.0)
            .bearing(bearing)
            .tilt(30.0)
            .build()

        mapLibreMap.animateCamera(
            CameraUpdateFactory.newCameraPosition(cameraPosition),
            1000
        )
    }
}