package com.example.myhatd.ui.common

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf // Đảm bảo đã import
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style

// Thêm hằng số mặc định cho Map Style và vị trí (bạn có thể tạo file riêng)
private const val MAPLIBRE_STYLE_URL = "https://api.maptiler.com/maps/streets/style.json?key=ljUPogf7K5oLiFoiiPuN"
private val DEFAULT_LOCATION = LatLng(21.028511, 105.804817) // Hà Nội

@Composable
fun MapLibreComposable(
    modifier: Modifier = Modifier.fillMaxSize(),
    userLocation: LatLng?, // Vị trí người dùng
    // ✅ THÊM CALLBACK (Đề xuất từ câu trả lời trước)
    onMapReady: (MapLibreMap) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Khởi tạo SDK và tạo MapView
    remember { MapLibre.getInstance(context) }
    val mapView = remember { MapView(context) }

    // ✅ SỬA LỖI CÚ PHÁP KOTLIN: Khai báo rõ kiểu dữ liệu trong mutableStateOf
    var mapLibreMap: MapLibreMap? by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker: Marker? by remember { mutableStateOf<Marker?>(null) }

    // 1. Nhúng MapView vào Compose
    AndroidView(
        factory = {
            mapView.apply {
                mapView.onCreate(null)
                getMapAsync(object : OnMapReadyCallback {
                    override fun onMapReady(map: MapLibreMap) {
                        mapLibreMap = map
                        onMapReady(map) // Báo cho Composable cha biết map đã sẵn sàng
                        map.setStyle(Style.Builder().fromUri(MAPLIBRE_STYLE_URL)) {
                            Log.d("MapLibre", "Map style loaded successfully")
                        }
                    }
                })
            }
        },
        update = {},
        modifier = modifier
    )

    // 2. Logic Cập nhật Camera và Marker
    LaunchedEffect(mapLibreMap, userLocation) {
        mapLibreMap?.let { map ->
            val location = userLocation ?: DEFAULT_LOCATION

//            // Cập nhật Marker
//            currentMarker?.remove()
//            val markerOptions = MarkerOptions()
//                .position(location)
//                .title("Vị trí của bạn")
//                .snippet("Vị trí hiện tại")
//            currentMarker = map.addMarker(markerOptions)

            // Cập nhật Camera
            val position = CameraPosition.Builder()
                .target(location)
                .zoom(if (userLocation != null) 15.0 else 12.0)
                .build()

            map.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(position),
                1500
            )
        }
    }

    // 3. Quản lý MapView Lifecycle
    DisposableEffect(lifecycleOwner, mapView) {
        val mapObserver = object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(mapObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(mapObserver)
        }
    }
}