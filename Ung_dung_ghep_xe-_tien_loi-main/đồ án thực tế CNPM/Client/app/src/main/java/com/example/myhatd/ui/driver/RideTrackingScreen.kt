package com.example.myhatd.ui.driver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.DriverMatchViewModel
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth

// ✅ CÁC IMPORTS MỚI CHO MAPLIBRE
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import com.example.myhatd.ui.utils.addOrUpdateMarkerWithCustomIcon
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
import com.example.myhatd.viewmodel.MapViewModel // Cần ViewModel quản lý vị trí Driver
import com.example.myhatd.ui.utils.drawRoutePolyline
import org.maplibre.android.annotations.Polyline // Cần import này
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLngBounds

// --- 1. MÀN HÌNH CHÍNH (RideTrackingScreen - Hiển thị thông tin Khách hàng cho Driver) ---
@Composable
fun DriverRideTrackingScreen(
    navController: NavController,
    viewModel: DriverMatchViewModel,
    mapViewModel: MapViewModel // ✅ THÊM MAPVIEWMODEL ĐỂ LẤY VỊ TRÍ DRIVER
) {
    // 1. Lấy dữ liệu động từ ViewModel
    val rideInfo by viewModel.currentRide.collectAsState()
    val context = LocalContext.current

    val routePolyline by viewModel.routePolyline.collectAsState() // Chuỗi Polyline từ ViewModel
    var routeMapPolyline by remember { mutableStateOf<Polyline?>(null) } // Đối tượng Polyline trên Map


    val cancelledNotification by viewModel.isRideCancelledByServer.collectAsState()
    var showCancellationDialog by remember { mutableStateOf(false) }

    // ✅ TRẠNG THÁI MAPLIBRE VÀ VỊ TRÍ DRIVER
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }
    val mapUiState by mapViewModel.uiState.collectAsState()
    val driverLocation = mapUiState.lastKnownLocation // MapLibre LatLng
    val driverBearing = mapUiState.currentBearing

    var destinationMarker by remember { mutableStateOf<org.maplibre.android.annotations.Marker?>(null) }

    val isCustomerPickedUp = remember(rideInfo) {
        // Kiểm tra nếu trường message chứa "RIDE_PICKED_UP"
        rideInfo?.message?.contains("RIDE_PICKED_UP") == true
    }

    // ✅ LOGIC CẬP NHẬT MARKER VỊ TRÍ DRIVER TRÊN MAP
    LaunchedEffect(mapLibreMap, driverLocation) {
        if (mapLibreMap != null && driverLocation != null) {
            // 🚨 SỬ DỤNG HÀM MỚI VÀ TRUYỀN ICON XE MÁY
            addOrUpdateMarkerWithCustomIcon( // <-- HÀM MỚI
                map = mapLibreMap,
                currentMarker = currentMarker,
                onMarkerUpdate = { marker -> currentMarker = marker },
                latLng = driverLocation,
                name = "Vị trí của bạn",
                context = context, // <-- TRUYỀN CONTEXT
                iconResId = R.drawable.xegocduoiphai, // <-- TRUYỀN ICON XE MÁY CỦA BẠN
                bearing = driverBearing // <-- Vẫn truyền bearing (sẽ dùng để xoay Map)
            )
            // Tùy chọn: Di chuyển camera đến vị trí Driver
            // mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(driverLocation, 14.0))
        }
    }


    LaunchedEffect(cancelledNotification) {
        if (cancelledNotification != null) {
            showCancellationDialog = true

            // Đợi 10 giây
            kotlinx.coroutines.delay(10000L)

            showCancellationDialog = false

            // ✅ QUAY VỀ HOME VÀ RESET STATE
            viewModel.resetRideCancelledState()
            navController.popBackStack("home_driver", inclusive = false) // Quay về Home Driver

            // Tùy chọn: Khởi động lại tìm chuyến để nghe socket mới
            val driverPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
            if (driverPhoneNumber.isNotEmpty()) {
                viewModel.startFindingRide(driverPhoneNumber)
            }
        }
    }

    // ✅ LOGIC TÍNH TOÁN TUYẾN ĐƯỜNG VÀ VẼ MARKER ĐIỂM ĐÓN/ĐẾN
//    LaunchedEffect(mapLibreMap, driverLocation, rideInfo, isCustomerPickedUp) {
//        Log.d("RouteCheck", "Kích hoạt tính toán tuyến đường.")
//        Log.d("RouteCheck", "MapReady: ${mapLibreMap != null}")
//        Log.d("RouteCheck", "DriverLoc: $driverLocation")
//        Log.d("RouteCheck", "RideInfo: ${rideInfo != null}")
//
//        if (driverLocation != null && rideInfo != null && mapLibreMap != null) {
//            val destinationLatLng: LatLng?
//            val destinationName: String
//            val destinationType: String
//
//            if (!isCustomerPickedUp) {
//                // TRẠNG THÁI 1: Driver -> Điểm đón (Pickup)
//                if (rideInfo?.viDoDiemDi != null && rideInfo?.kinhDoDiemDi != null) {
//                    destinationLatLng = LatLng(rideInfo!!.viDoDiemDi!!, rideInfo!!.kinhDoDiemDi!!)
//                    destinationName = rideInfo!!.tenDiemDiUser ?: "Điểm đón"
//                    destinationType = "PICKUP"
//                } else return@LaunchedEffect
//            } else {
//                // TRẠNG THÁI 2: Pickup -> Điểm đến (Dropoff)
//                if (rideInfo?.viDoDiemDen != null && rideInfo?.kinhDoDiemDen != null) {
//                    destinationLatLng = LatLng(rideInfo!!.viDoDiemDen!!, rideInfo!!.kinhDoDiemDen!!)
//                    destinationName = rideInfo!!.tenDiemDenUser ?: "Điểm đến"
//                    destinationType = "DROPOFF"
//                } else return@LaunchedEffect
//            }
//
//            // CHỈ GỌI MỘT LẦN DUY NHẤT
//            Log.d("RouteCheck", "Gọi calculateRoute đến: $destinationType")
//            viewModel.calculateRoute(driverLocation, destinationType)
//            addOrUpdateMarker(
//                map = mapLibreMap,
//                currentMarker = destinationMarker,
//                onMarkerUpdate = { marker -> destinationMarker = marker },
//                latLng = destinationLatLng, // Đã được xác định trong if-else
//                name = destinationName      // Đã được xác định trong if-else
//            )
//
//            // TODO: Cập nhật Marker cho Điểm Đón/Đến (sử dụng destinationLatLng và destinationName)
//            // Ví dụ: addOrUpdateMarker(mapLibreMap, destinationMarker, { destinationMarker = it }, destinationLatLng, destinationName)
//
//        } else {
//            Log.d("RouteCheck", "Thiếu điều kiện, chưa gọi calculateRoute.")
//        }
//    }
    LaunchedEffect(mapLibreMap, rideInfo, isCustomerPickedUp) {
        Log.d("RouteCheck", "Kích hoạt tính toán tuyến đường.")

        // Đảm bảo driverLocation đã có trước khi gọi
        val driverLoc = driverLocation ?: return@LaunchedEffect

        if (rideInfo != null && mapLibreMap != null) {
            val destinationLatLng: LatLng?
            val destinationName: String
            val destinationType: String

            if (!isCustomerPickedUp) {
                // TRẠNG THÁI 1: Driver -> Điểm đón (Pickup)
                if (rideInfo?.viDoDiemDi != null && rideInfo?.kinhDoDiemDi != null) {
                    destinationLatLng = LatLng(rideInfo!!.viDoDiemDi!!, rideInfo!!.kinhDoDiemDi!!)
                    destinationName = rideInfo!!.tenDiemDiUser ?: "Điểm đón"
                    destinationType = "PICKUP"
                } else return@LaunchedEffect
            } else {
                // TRẠNG THÁI 2: Pickup -> Điểm đến (Dropoff)
                if (rideInfo?.viDoDiemDen != null && rideInfo?.kinhDoDiemDen != null) {
                    destinationLatLng = LatLng(rideInfo!!.viDoDiemDen!!, rideInfo!!.kinhDoDiemDen!!)
                    destinationName = rideInfo!!.tenDiemDenUser ?: "Điểm đến"
                    destinationType = "DROPOFF"
                } else return@LaunchedEffect
            }

            // ✅ GỌI CALCULATE ROUTE CHỈ KHI TRẠNG THÁI THAY ĐỔI
            Log.d("RouteCheck", "Gọi calculateRoute đến: $destinationType")
            // NOTE: Viewmodel cần dùng driverLocation TỪ mapUiState.lastKnownLocation
            viewModel.calculateRoute(driverLoc, destinationType)

            // Cập nhật Marker Điểm Đích
            addOrUpdateMarker(
                map = mapLibreMap,
                currentMarker = destinationMarker,
                onMarkerUpdate = { marker -> destinationMarker = marker },
                latLng = destinationLatLng,
                name = destinationName
            )
        } else {
            Log.d("RouteCheck", "Thiếu điều kiện, chưa gọi calculateRoute.")
            // Nếu chuyến đi null, đảm bảo xóa marker
            destinationMarker?.remove()
            destinationMarker = null
        }
    }
    LaunchedEffect(mapLibreMap, driverLocation, destinationMarker) {
        val destinationLoc = destinationMarker?.position
        if (mapLibreMap != null && driverLocation != null && destinationLoc != null) {
            // 1. Tạo LatLngBounds bao gồm vị trí Driver và Điểm đích
            val bounds = LatLngBounds.Builder()
                .include(driverLocation)
                .include(destinationLoc)
                .build()

            // 2. Di chuyển camera để fit vào bounds
            mapLibreMap!!.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    100, // padding (ví dụ 100 pixels)
                    100,
                    100,
                    300 // Padding lớn hơn ở dưới để tránh bị che bởi Bottom Sheet
                )
            )
        }
    }


    // ✅ LOGIC VẼ POLYLINE LÊN MAP (Dựa trên kết quả từ ViewModel)
    LaunchedEffect(mapLibreMap, routePolyline) {
        if (mapLibreMap != null && routePolyline != null) {
            drawRoutePolyline( // <-- Gọi hàm tiện ích đã tạo
                map = mapLibreMap,
                encodedPolyline = routePolyline!!,
                currentPolyline = routeMapPolyline,
                onPolylineUpdate = { routeMapPolyline = it }
            )
        } else if (mapLibreMap != null && routePolyline == null) {
            // Xóa đường đi nếu Polyline là null (ví dụ: sau khi reset/hoàn thành chuyến)
            routeMapPolyline?.remove()
            routeMapPolyline = null
        }
    }

//    val isCustomerPickedUp = remember(rideInfo) {
//        // Kiểm tra nếu trường message chứa "RIDE_PICKED_UP"
//        // Dựa vào logic cập nhật trong ViewModel ở Bước 1.B.
//        rideInfo?.message?.contains("RIDE_PICKED_UP") == true
//    }

    // 2. Trích xuất DỮ LIỆU KHÁCH HÀNG (USER)
    // -------------------------------------------------------------------
    val tenDiemDi = rideInfo?.tenDiemDiUser ?: "Điểm đón"
    val tenDiemDen = rideInfo?.tenDiemDenUser ?: "Điểm đến"
    val userName = rideInfo?.tenUser ?: "Khách hàng"
    val userPhone = rideInfo?.sdtUser ?: "N/A"
    val rating = "5.0⭐" // Giả định

    // ✅ ĐIỀU CHỈNH CÁC GIÁ TRỊ DỰA TRÊN TRẠNG THÁI (isCustomerPickedUp)
    val currentDestinationName = if (isCustomerPickedUp) tenDiemDen else tenDiemDi
    val pickupDropoffInfo =
        if (isCustomerPickedUp) {
            // Khi đã đón: Chỉ hiển thị điểm đến (Destination)
            "Điểm đến: $tenDiemDen"
        } else {
            // Khi chưa đón: Chỉ hiển thị điểm đón (Pickup)
            "Điểm đón: $tenDiemDi"
        }
    val screenTitle = if (isCustomerPickedUp) "Đang đi đến Điểm đến" else "Đang đón Khách hàng"

    // Giả định thời gian tài xế đến/hoàn thành
    val thoiGianDenUserRaw = rideInfo?.thoiGianDriverDenUser ?: "N/A"
    val thoiGianDenUserFormatted = thoiGianDenUserRaw
        .substringAfter('T')
        .substringBeforeLast(':')
        .substringBefore('.')
        .ifEmpty { "Vừa nhận" }
    // -------------------------------------------------------------------

    // 3. Xử lý trạng thái Loading/Null
    if (rideInfo == null && cancelledNotification == null) { // Thêm điều kiện kiểm tra cancelledNotification
        return Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Đang tải thông tin chuyến đi...", fontSize = 20.sp, color = Color.Gray)
        }
    }

    // 4. UI Chính
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🔹 KHU VỰC NỀN TRÊN (MAP)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.TopCenter)
        ) {
            // ✅ THAY THẾ IMAGE BẰNG MAPLIBRE COMPOSABLE THỰC TẾ
            MapLibreComposable(
                modifier = Modifier.fillMaxSize(),
                userLocation = driverLocation, // Vị trí của DRIVER
                onMapReady = { mapLibreMap = it } // Nhận tham chiếu MapLibreMap
            )


            // 🔹 Nút CHỈ ĐƯỜNG (GOOGLE MAPS) - CẬP NHẬT ĐIỂM ĐẾN
            Button(
                onClick = {
                    // CẬP NHẬT: Mở chỉ đường đến điểm đón (nếu chưa đón) hoặc điểm đến (nếu đã đón)
                    val uri = Uri.parse("google.navigation:q=$currentDestinationName")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val webUri = Uri.parse("http://maps.google.com/maps?q=$currentDestinationName")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Google Maps Direction",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if(isCustomerPickedUp) "Đến nơi" else "Đón khách", // Thay đổi text nút
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        } // END Top Background Box


        // 🔹 Khung thông tin Khách hàng (Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            // ✅ HIỂN THỊ TRẠNG THÁI CHO DRIVER
            Text(
                text = "$screenTitle (${thoiGianDenUserFormatted})", // <--- Dùng screenTitle mới
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = pickupDropoffInfo, // <--- Dùng pickupDropoffInfo mới
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔸 Card thông tin Khách hàng
            DynamicUserInfoCardForDriver(
                userName = userName,
                userPhone = userPhone,
                pickupTime = thoiGianDenUserFormatted,
                rating = rating,
                onCardClick = { /* Do nothing */ }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔸 Hàng nút chat, gọi và Đã đón khách
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 20.dp, y = 0.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔸 Nút Chat
                OutlinedButton(
                    onClick = {
                        // CHỨC NĂNG NHẮN TIN (SMS)
                        if (userPhone != "N/A" && userPhone.isNotEmpty()) {
                            val uri = Uri.parse("smsto:$userPhone")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                println("Không thể mở ứng dụng nhắn tin: ${e.message}")
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = "Chat",
                        tint = Color.Black,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat với Khách hàng", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🔸 Nút gọi (Gọi Khách hàng)
                IconButton(
                    onClick = {
                        val uri = Uri.parse("tel:$userPhone")
                        context.startActivity(Intent(Intent.ACTION_DIAL, uri))
                    },
                    modifier = Modifier
                        .size(55.dp)
                        .padding(start = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.call),
                        contentDescription = "Call",
                        tint = Color.Black,
                        modifier = Modifier.size(45.dp)
                    )
                }

                // ✅ NÚT "ĐÃ ĐÓN KHÁCH" MỚI (Chỉ hiển thị nếu chưa đón)
                if (!isCustomerPickedUp) { // <-- Dùng biến cục bộ đã tính toán
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            viewModel.pickedUpCustomer { success ->
                                // ...
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0081F1)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Đã đón", fontSize = 14.sp, color = Color.White)
                    }
                }

                // ✅ NÚT "KẾT THÚC CHUYẾN ĐI" (Hiển thị nếu đã đón)
                if (isCustomerPickedUp) {
                    Spacer(modifier = Modifier.width(22.dp))
                    Button(
                        onClick = {
                            viewModel.completeRide()
                            navController.popBackStack("home_driver", inclusive = false)
                        },
                        // Màu xanh lá cây đậm
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Kết thúc", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        } // END Bottom Info Column
    } // END Main Box

    if (showCancellationDialog && cancelledNotification != null) {
        // ... (AlertDialog giữ nguyên) ...
        AlertDialog(
            onDismissRequest = { /* Không cho Dismiss khi đang chờ quay về */ },
            title = {
                Text(
                    text = "🚨 CHUYẾN ĐI ĐÃ BỊ HỦY",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = cancelledNotification!!.message ?: "Người dùng đã hủy chuyến đi.",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tự động quay về màn hình chính sau 10 giây...",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                // Cho phép Driver nhấn nút để quay về ngay lập tức
                Button(onClick = {
                    // CÁC BƯỚC QUAY VỀ TỨC THÌ
                    viewModel.resetRideCancelledState()
                    navController.popBackStack("home_driver", inclusive = false)
                    // Tùy chọn: Khởi động lại tìm chuyến
                    val driverPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""
                    if (driverPhoneNumber.isNotEmpty()) {
                        viewModel.startFindingRide(driverPhoneNumber)
                    }
                }) {
                    Text("Quay về ngay")
                }
            }
        )
    }
}

// --- 2. COMPOSABLE CŨ: DynamicUserInfoCardForDriver (Giữ nguyên) ---
@Composable
fun DynamicUserInfoCardForDriver(
    userName: String,
    userPhone: String,
    pickupTime: String,
    rating: String,
    onCardClick: () -> Unit
) {
    // ... (Composable này giữ nguyên) ...
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onCardClick)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF0081F1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Ảnh nền trong card
            Image(
                painter = painterResource(id = R.drawable.nenthongtindriver),
                contentDescription = "Background User",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Avatar + Thời gian đón + tên
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        // Avatar Khách hàng
                        Image(
                            painter = painterResource(id = R.drawable.anhuser),
                            contentDescription = "Ảnh Khách hàng",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color(0xFF0081F1), CircleShape)
                                .clip(CircleShape)
                        )

                        // Thời gian đón Khách hàng
                        Text(
                            text = pickupTime,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp)
                                .background(Color.Yellow, RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tên Khách hàng và sao
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rating,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Số điện thoại Khách hàng
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF0081F1)),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = userPhone, // Số điện thoại
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0081F1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "Khách hàng", // Mô tả
                    fontSize = 12.sp,
                    color = Color(0xFF007ACC)
                )
            }

            // Logo góc phải dưới
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo HATD",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 30.dp)
                    .padding(bottom = 8.dp, end = 16.dp)
            )
        }
    }
}