package com.example.myhatd.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.MapViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
// ✅ THÊM IMPORTS THIẾU CHO DELEGATE
import androidx.compose.runtime.setValue
import com.example.myhatd.ui.driver.ClickableSearchBox
import com.example.myhatd.ui.driver.dashedBorder
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
// XÓA CÁC IMPORT GOOGLE MAPS CŨ

// -----------------------------------------------------------
// 2. Màn hình chính: HomeUserScreen (Đã sửa lỗi)
// -----------------------------------------------------------

@Composable
fun HomeUserScreen(
    navController: NavController,
    mapViewModel: MapViewModel
) {
    val context = LocalContext.current

    // ✅ SỬA LỖI DELEGATE BẰNG CÁCH THÊM import androidx.compose.runtime.setValue
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }

    // SỬ DỤNG TRẠNG THÁI VỊ TRÍ TỪ VIEWMODEL
    val mapUiState by mapViewModel.uiState.collectAsState()

    // ✅ KHÔI PHỤC KHAI BÁO BIẾN userLocation (Sửa lỗi Unresolved reference)
    val userLocation = mapUiState.lastKnownLocation // Đã là MapLibre LatLng

    // Logic xin cấp quyền (Giữ nguyên)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        mapViewModel.setLocationPermission(isGranted)
        if (isGranted) {
            mapViewModel.startLocationUpdates(context)
        }
    }

    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                mapViewModel.setLocationPermission(true)
                mapViewModel.startLocationUpdates(context)
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // ✅ LOGIC HIỂN THỊ VỊ TRÍ VÀ MARKER (Giữ nguyên)
    LaunchedEffect(mapLibreMap, userLocation) {
        if (mapLibreMap != null && userLocation != null) {
            addOrUpdateMarker(
                map = mapLibreMap,
                currentMarker = currentMarker,
                onMarkerUpdate = { marker -> currentMarker = marker },
                latLng = userLocation,
                name = "Vị trí của bạn"
            )
        }
    }
    // -----------------------------------------------------------

    // 🌟 KHUNG CHÍNH: Column
    Column(modifier = Modifier.fillMaxSize()) {

        // 1. Header (Giữ nguyên)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Giảm chiều cao Header để bản đồ có thêm không gian
                .height(175.dp) // Điều chỉnh từ 200.dp
        ) {
            // Background Header Image
            Image(
                painter = painterResource(id = R.drawable.header),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 1.dp, y = 10.dp)
            )

            // 🚀 Thanh tìm kiếm (Giữ nguyên)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF3085E0),
                        shape = RoundedCornerShape(25.dp)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 8f
                        shape = RoundedCornerShape(25.dp)
                        clip = true
                    }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.position),
                    contentDescription = "Định vị",
                    modifier = Modifier.size(30.dp)
                )

                ClickableSearchBox(
                    onFieldClick = {
                        navController.navigate("tim_dia_chi")
                    }
                )

                Image(
                    painter = painterResource(id = R.drawable.glass),
                    contentDescription = "Tìm kiếm",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        // 2. VỊ TRÍ CỦA MAP & Nội dung cuộn được
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✨ MAPLIBRE COMPOSABLE
            MapLibreComposable(
                modifier = Modifier
                    .fillMaxWidth() // ✅ MAX WIDTH (đã xóa padding horizontal)
                    .height(200.dp),
                // ✅ ĐÃ XÓA .clip(RoundedCornerShape(8.dp)) để bỏ bo góc
                userLocation = userLocation,
                onMapReady = { mapLibreMap = it } // ✅ Nhận tham chiếu MapLibreMap
            )

            // ✅ THÊM KHOẢNG CÁCH SAU MAP
            Spacer(modifier = Modifier.height(16.dp))

            // Box Chế độ Driver
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .padding(horizontal = 16.dp), // Thêm padding lại cho Box
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (Code Box Chế độ Driver giữ nguyên) ...
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                        .dashedBorder(
                            strokeWidth = 4.dp,
                            color = Color(0xFF00BCD4),
                            cornerRadius = 16.dp
                        )
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navController.navigate("dang_ky_driver")
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.drivermode),
                            contentDescription = "Chế độ Driver",
                            modifier = Modifier
                                .size(60.dp)
                                .offset(x = (-5).dp, y = 0.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Chế độ Driver",
                            color = Color(0xFF787B79),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            modifier = Modifier.offset(x = (-8).dp, y = 0.dp)
                        )
                    }
                }
            }

            // ✅ THÊM KHOẢNG CÁCH SAU CHẾ ĐỘ DRIVER
            Spacer(modifier = Modifier.height(16.dp))

            // Carousel và các phần tử khác
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // Tăng padding top để tách biệt
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ... (Code Carousel và Indicator giữ nguyên) ...
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            Image(
                                painter = painterResource(
                                    id = when (page) {
                                        0 -> R.drawable.contentsale
                                        1 -> R.drawable.contentsale2
                                        else -> R.drawable.contentsale
                                    }
                                ),
                                contentDescription = "Ưu đãi",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = when (page) {
                                    0 -> "Ưu đãi cực lớn lên đến 50%"
                                    1 -> "Tiết kiệm-Thời gian-Chi phí-Môi trường"
                                    else -> "Ưu đãi cực lớn lên đến 50%"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFF000000),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = when (page) {
                                    0 -> "Từ 3/10-10/10"
                                    1 -> "Từ 6/10-15/10"
                                    else -> "Từ 3/10-10/10"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(2) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (pagerState.currentPage == index) Color(0xFF00BCD4)
                                    else Color(0xFFBDBDBD)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.footer),
                        contentDescription = "Banner độc lập",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // 3. Footer Navigation (Giữ nguyên)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    shadowElevation = 12f
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                    clip = true
                }
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                )
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon 1 - Notification
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bell),
                    contentDescription = "Thông báo",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            navController.navigate("thong_bao_user")
                        }
                )
            }

            // Icon 2 - History
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clocki),
                    contentDescription = "Lịch sử",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            navController.navigate("hen_gio_user")
                        }
                )
            }

            // Icon 3 - Profile
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Hồ sơ",
                    modifier = Modifier
                        .size(70.dp)
                        .clickable {
                            navController.navigate("ho_so_user")
                        }
                )
            }
        }
    }
}