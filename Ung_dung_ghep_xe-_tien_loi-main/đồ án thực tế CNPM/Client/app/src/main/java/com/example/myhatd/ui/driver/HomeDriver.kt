package com.example.myhatd.ui.driver

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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myhatd.R
import com.google.android.gms.location.LocationServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.collectAsState
import com.example.myhatd.viewmodel.DriverMatchViewModel
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.data.model.MatchNotificationDTO
import androidx.compose.material3.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// ✅ THÊM CÁC IMPORTS MAPLIBRE VÀ VIEWMODEL
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import com.example.myhatd.viewmodel.MapViewModel
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
import androidx.lifecycle.viewmodel.compose.viewModel


/**
 * 1. Composable Tái sử dụng: Ô tìm kiếm chỉ đọc có thể nhấp
 */
@Composable
fun RowScope.ClickableSearchBox(
    onFieldClick: () -> Unit
) {
    // ... (Code ClickableSearchBox không đổi) ...
    val displayValue = ""

    Box(
        modifier = Modifier
            .weight(1f)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = displayValue,
            onValueChange = { /* Bỏ qua vì nó là chỉ đọc */ },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = Color(0xFF333333),
                fontSize = 15.sp
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (displayValue.isEmpty()) {
                        Text(
                            text = "Bạn muốn đi đâu",
                            color = Color(0xFF000000),
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    onFieldClick()
                }
        )
    }
}


// -----------------------------------------------------------
// 2. Màn hình chính: HomeDriverScreen
// -----------------------------------------------------------

@Composable
fun HomeDriverScreen(
    navController: NavController,
    viewModel: DriverMatchViewModel,
    // ✅ THÊM MapViewModel cho MapLibre
    mapViewModel: MapViewModel = viewModel()
) {

    // --- Logic Bản đồ và Vị trí ---
    val context = LocalContext.current
    val fLpc = remember { LocationServices.getFusedLocationProviderClient(context) }

    // ✅ TRẠNG THÁI MAPLIBRE
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLocation = mapUiState.lastKnownLocation // MapLibre LatLng

    // Logic lấy vị trí cũ (Đã sửa để gọi ViewModel)
    val fetchLastLocation: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu ViewModel cập nhật vị trí
            mapViewModel.startLocationUpdates(context)
        }
    }

    // Logic xin quyền (Đã sửa để gọi ViewModel)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        mapViewModel.setLocationPermission(isGranted)
        if (isGranted) fetchLastLocation()
    }

    val driverPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber
        ?: ""

    val matchResult by viewModel.matchResult.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()

    // 1. Logic Khởi tạo và Vị trí
    LaunchedEffect(driverPhoneNumber) {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                mapViewModel.setLocationPermission(true)
                fetchLastLocation()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        // 2. Khởi động tìm chuyến (chỉ chạy nếu có SĐT) (LOGIC KHÔNG ĐỔI)
        if (driverPhoneNumber.isNotEmpty()) {
            viewModel.startFindingRide(driverPhoneNumber)
        }
    }

    // ✅ LOGIC HIỂN THỊ MARKER CHO MAPLIBRE
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

    // --- Giao diện Chính ---

    Column(modifier = Modifier.fillMaxSize()) {

        // 1. Header (Banner và Logo) (GIỮ NGUYÊN)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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

            // 🚀 Thanh tìm kiếm (GIỮ NGUYÊN)
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
                        navController.navigate(NavigationRoutes.TIM_DIA_CHI_DRIVER)
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

            // ✨ MAPLIBRE COMPOSABLE (Thay thế Placeholder)
            MapLibreComposable(
                modifier = Modifier
                    .fillMaxWidth() // ✅ MAX WIDTH
                    .height(205.dp) // Giữ chiều cao cũ
                    .offset(y = (-25).dp), // Giữ offset cũ
                // BỎ padding horizontal và clip để khớp với yêu cầu UI User
                userLocation = userLocation,
                onMapReady = { mapLibreMap = it } // Nhận tham chiếu MapLibreMap
            )

            // Box Chế độ Người đi (GIỮ NGUYÊN)
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .offset(y = (-25).dp), // Giữ offset cũ để khớp với Map
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        .padding(4.dp)
                        .padding(horizontal = 16.dp), // Thêm padding ngang cho Box
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navController.navigate(NavigationRoutes.HOME)
                            }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.usermode),
                            contentDescription = "Chế độ Người đi",
                            modifier = Modifier
                                .size(60.dp)
                                .offset(x = (-5).dp, y = 0.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Chế độ Người đi",
                            color = Color(0xFF787B79),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            modifier = Modifier.offset(x = (-8).dp, y = 0.dp)
                        )
                    }
                }
            }

            // Carousel (HorizontalPager) (GIỮ NGUYÊN)
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp) // Thêm padding trên
                    .offset(y = (-25).dp), // Tiếp tục offset lên để giữ khoảng cách đều
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                        // Column chứa tiêu đề và mô tả
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

                // Indicator (các chấm tròn)
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

            // Spacer để đẩy banner dưới xuống
            Spacer(modifier = Modifier.height(16.dp))

            // Box ảnh độc lập phía dưới carousel (Banner Footer) (GIỮ NGUYÊN)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-25).dp) // Tiếp tục offset lên
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

        // 3. Footer Navigation (Cố định ở dưới cùng) (GIỮ NGUYÊN)
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
                    modifier = Modifier.size(70.dp)
                        .clickable {
                            navController.navigate(NavigationRoutes.THONG_BAO_USER)
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
                    modifier = Modifier.size(70.dp)
                        .clickable {
                            navController.navigate(NavigationRoutes.DU_DOAN_DRIVER)
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
                    modifier = Modifier.size(70.dp)
                        .clickable {
                            navController.navigate(NavigationRoutes.HO_SO_DRIVER)
                        }
                )
            }
        }

        // --- HỘP THOẠI THÔNG BÁO GHÉP ĐÔI (Match Notification Dialog) (GIỮ NGUYÊN LOGIC) ---
        if (matchResult != null) {
            val currentMatch = matchResult!!
            MatchNotificationDialog(
                matchData = currentMatch,
                isConfirming = isConfirming,
                onConfirmClick = { matchId ->
                    val route = NavigationRoutes.createDriverRideDetailRoute(matchId)
                    navController.navigate(route)
                },
                onDismiss = {
                    viewModel.forceUpdateMatchResult(null)
                }
            )
        }
    }
}


// -----------------------------------------------------------
// 3. Function tạo viền đứt nét (Không đổi)
// -----------------------------------------------------------
fun Modifier.dashedBorder(
    strokeWidth: Dp = 4.dp,
    color: Color = Color(0xFF00BCD4),
    cornerRadius: Dp = 16.dp
) = this.drawBehind {
    val stroke = strokeWidth.toPx()
    val dashWidth = 20f
    val dashGap = 5f
    val radius = cornerRadius.toPx()

    drawRoundRect(
        color = color,
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashWidth, dashGap), 0f
            )
        ),
        cornerRadius = CornerRadius(radius)
    )
}


// -----------------------------------------------------------
// 4. Match Notification Dialog (Không đổi)
// -----------------------------------------------------------
@Composable
fun MatchNotificationDialog(
    matchData: MatchNotificationDTO,
    onConfirmClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    isConfirming: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = if (isConfirming) "Đang xử lý..." else "CHUYẾN ĐI MỚI! 🔔",
                fontSize = 20.sp
            )
        },

        text = {
            if (isConfirming) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Text("Đang xác nhận chuyến đi...")
                }
            } else {
                Text(
                    text = "Bạn có một yêu cầu chuyến xe mới đang chờ. Hãy xem chi tiết để xác nhận hoặc hủy chuyến.",
                    fontSize = 16.sp
                )
            }
        },

        // Nút HÀNH ĐỘNG CHÍNH (Xem chi tiết)
        confirmButton = {
            Button(
                onClick = { onConfirmClick(matchData.matchId!!) },
                enabled = !isConfirming
            ) {
                Text("Xem chi tiết")
            }
        },

        // Nút HÀNH ĐỘNG PHỤ (Bỏ qua/Từ chối)
        dismissButton = {
            Button(
                onClick = onDismiss,
                enabled = !isConfirming,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0))
            ) {
                Text("Bỏ qua", color = Color.Black)
            }
        }
    )
}