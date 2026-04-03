//// Đặt đoạn code này vào một file riêng hoặc ngay trong file Composable của bạn.
//
//package com.example.myhatd.ui.components
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//import org.maplibre.android.MapLibre
//import org.maplibre.android.camera.CameraPosition
//import org.maplibre.android.geometry.LatLng
//import org.maplibre.android.maps.MapView
//import org.maplibre.android.maps.Style
//
///**
// * Composable hiển thị bản đồ OpenStreetMap sử dụng MapLibre.
// * Lưu ý: MapView cần được quản lý lifecycle (onCreate, onResume, onPause, onDestroy)
// * Tuy nhiên, trong AndroidView, chúng ta chỉ tập trung vào việc tạo và cấu hình.
// * MapLibre đã tự xử lý lifecycle ở một mức độ nhất định khi được nhúng.
// */
//@Composable
//fun OpenStreetMapComposable(
//    modifier: Modifier = Modifier,
//    // Ví dụ: Tọa độ trung tâm và zoom level có thể truyền từ ViewModel
//    center: LatLng = LatLng(10.762622, 106.660172), // Ví dụ: Hồ Chí Minh
//    zoom: Double = 12.0
//) {
//    val context = LocalContext.current
//
//    // Khởi tạo MapLibre một lần
//    // Thường được gọi trong Application class, nhưng ta có thể gọi ở đây để đảm bảo
//    MapLibre.getInstance(context)
//
//    // Sử dụng AndroidView để nhúng MapView vào Composable
//    AndroidView(
//        modifier = modifier.fillMaxSize(),
//        // 1. Khởi tạo MapView
//        factory = {
//            val mapView = MapView(it)
//            // Đảm bảo MapView có kích thước cố định
//            mapView.layoutParams = android.view.ViewGroup.LayoutParams(
//                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
//                android.view.ViewGroup.LayoutParams.MATCH_PARENT
//            )
//            mapView.onCreate(null) // Cần gọi onCreate
//            mapView
//        },
//        // 2. Cập nhật MapView
//        update = { mapView ->
//            mapView.getMapAsync { maplibreMap ->
//                // Thiết lập Style cho OpenStreetMap
//                // Style URL của OSM phổ biến (ví dụ: Light, Dark, Standard)
//                // Bạn có thể tìm các tile server khác nếu cần (ví dụ: OSM Carto)
//                // Đây là một style cơ bản có thể hoạt động:
//                val osmStyleUrl = "https://tiles.stadiamaps.com/styles/osm_bright.json?api_key=YOUR_API_KEY" // Cần đăng ký API Key nếu dùng Stadia Maps
//                // HOẶC sử dụng một tile server OSM chuẩn (cần cấu hình phức tạp hơn để thêm các layer)
//                val basicTileUrl = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
//
//                // Cách đơn giản nhất để hiển thị OSM là sử dụng các style đã định sẵn.
//                // Nếu muốn dùng trực tiếp OSM Tile server, cần tạo Custom Style.
//
//                // *** VÍ DỤ SỬ DỤNG STYLE CÓ SẴN CỦA MAPLIBRE HOẶC TỪ ĐỐI TÁC CUNG CẤP TILE SỞ DỤNG OSM DATA ***
//                // Ở đây ta dùng một style giả định, bạn nên thay bằng một style URL hợp lệ cho OSM:
//                maplibreMap.setStyle(Style.Builder().fromUri("asset://demostyle.json")) { style ->
//                    // Đặt vị trí camera
//                    val cameraPosition = CameraPosition.Builder()
//                        .target(center)
//                        .zoom(zoom)
//                        .build()
//
//                    maplibreMap.cameraPosition = cameraPosition
//
//                    // TODO: Thêm logic vẽ marker/polyline nếu cần
//                    // Ví dụ: maplibreMap.addMarker(MarkerOptions().position(center).title("Vị trí"))
//                }
//            }
//        },
//        // 3. Xử lý khi MapView bị loại bỏ khỏi View Hierarchy (cleanup)
//        onRelease = { mapView ->
//            mapView.onDestroy()
//        }
//    )
//
//    // Lưu ý: Lifecycle của MapView trong AndroidView không hoàn hảo như khi dùng Fragment.
//    // Nếu ứng dụng của bạn gặp lỗi liên quan đến lifecycle, bạn cần dùng DisposableEffect
//    // để quản lý các hàm onResume/onPause/onDestroy một cách thủ công.
//}