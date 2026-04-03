package com.example.myhatd.ui.customer

// ✅ CÁC IMPORT MỚI ĐỂ XỬ LÝ QUYỀN VÀ CONTEXT
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.Manifest // Thêm Manifest import nếu thiếu
// ----------------------------------------------------

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.viewmodel.MapViewModel

// ✅ IMPORTS MAPLIBRE
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory


@Composable
fun XacNhanDiemDonScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    diemDonText: String,
    currentPhoneNumber: String
) {

    // ✅ TRẠNG THÁI MAPLIBRE
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }

    // ✅ ------ LOGIC YÊU CẦU VỊ TRÍ (Giữ nguyên) ------
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                mapViewModel.setLocationPermission(true)
                mapViewModel.startLocationUpdates(context)
            } else {
                mapViewModel.setLocationPermission(false)
                // TODO: Hiển thị thông báo cho người dùng biết
            }
        }
    )

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                mapViewModel.setLocationPermission(true)
                mapViewModel.startLocationUpdates(context)
            }
            else -> {
                locationPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }
    // ✅ ------ KẾT THÚC LOGIC YÊU CẦU VỊ TRÍ ------


    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLocation = mapUiState.lastKnownLocation // MapLibre LatLng

    // ✅ LOGIC HIỂN THỊ MARKER CHO MAPLIBRE
    // ✅ LOGIC HIỂN THỊ MARKER CHO MAPLIBRE
    LaunchedEffect(mapLibreMap, userLocation) {
        if (mapLibreMap != null && userLocation != null) {
            // 1. CẬP NHẬT MARKER
            addOrUpdateMarker(
                map = mapLibreMap,
                currentMarker = currentMarker,
                onMarkerUpdate = { marker -> currentMarker = marker },
                latLng = userLocation,
                name = "Điểm đón hiện tại"
            )

            // 2. ✅ LOGIC MỚI: DI CHUYỂN CAMERA ĐẾN VỊ TRÍ NGƯỜI DÙNG
            if (mapLibreMap?.cameraPosition?.target != userLocation) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 16.0) // Zoom 16.0 là tốt cho chi tiết đường phố
                mapLibreMap?.animateCamera(cameraUpdate, 1000) // Di chuyển mượt mà trong 1 giây
            }
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ✨ MAPLIBRE COMPOSABLE (Thay thế Placeholder)
        MapLibreComposable(
            modifier = Modifier
                .fillMaxSize(), // ✅ FILL MAX SIZE (Không padding, không bo góc)
            userLocation = userLocation,
            onMapReady = { mapLibreMap = it } // Nhận tham chiếu MapLibreMap
        )

        // Nút Back (Giữ nguyên)
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .offset(x = 20.dp, y = 55.dp)
                .size(40.dp)
                .clickable { navController.popBackStack() }
        )


        // Tạo Bottom Sheet (Giữ nguyên)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    shadowElevation = 16f
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    clip = true
                }
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
        ) {
            // Ảnh nền cho phần Bottom Sheet (Giữ nguyên)
            Image(
                painter = painterResource(id = R.drawable.xacnhan),
                contentDescription = "Điểm đón",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Crop
            )

            // Phần nội dung text + button (Giữ nguyên)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {

                // Box Hiển thị Điểm đón (Giữ nguyên)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFFEEEEEE),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        // Icon (chấm tròn) cho điểm đón
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(10.dp)
                                .background(
                                    color = Color(0xFF3085E0),
                                    shape = CircleShape
                                )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Cột chứa văn bản
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Điểm đón",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF000000),
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = diemDonText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF333333),
                                lineHeight = 22.sp,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Thêm chi tiết",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF3085E0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 50.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        ) {
                            navController.navigate("GhiChu")
                        }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // TODO: GỌI API HTTP YÊU CẦU TÌM CHUYẾN (POST /ride/request)

                        // ✅ CHUYỂN ĐẾN MÀN HÌNH CHỜ SOCKET
                        val socketRoute = NavigationRoutes.createChoSocketRoute(currentPhoneNumber)
                        navController.navigate(socketRoute)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp)
                        .offset(y = 40.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C9BE3)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Xác nhận điểm đón",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}