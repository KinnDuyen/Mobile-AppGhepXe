package com.example.myhatd.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myhatd.MyApplication
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.viewmodel.FindingRideViewModel
import com.example.myhatd.viewmodel.FindingRideViewModelFactory

@Composable
fun ChoSocketScreen(
    userPhone: String, // SĐT lấy từ Route
    navController: NavController,
    viewModel: FindingRideViewModel
) {

//    val context = LocalContext.current
//    val application = context.applicationContext as MyApplication
//    val factory = FindingRideViewModelFactory(application.matchRepository)
//
//    // ✅ SỬA ĐỔI QUAN TRỌNG: KHÔNG GÁN viewModelStoreOwner CỤ THỂ
//    // ViewModel sẽ được scope tới Activity/NavHost, cho phép XacNhanDatXeScreen
//    // truy cập cùng instance chứa dữ liệu Match.
//    val viewModel: FindingRideViewModel = viewModel(
//        factory = factory
//    )

    // 2. LẤY TRẠNG THÁI
    val isSearchAttemptComplete by viewModel.isSearchAttemptComplete.collectAsState()
    val matchResult by viewModel.matchResult.collectAsState()
    // Lắng nghe trạng thái hủy (mặc dù không dùng để điều hướng ở đây, nhưng tốt cho UI)
    val isMatchCancelled by viewModel.isMatchCancelled.collectAsState()

    // 3. KÍCH HOẠT TÌM KIẾM
    LaunchedEffect(Unit) {
        // ✅ THAY ĐỔI: Thêm lệnh Reset dự phòng và Delay ngắn.
        viewModel.resetMatchState() // Đảm bảo mọi thứ sạch sẽ trước khi tìm kiếm
        kotlinx.coroutines.delay(50) // Chờ một chút để trạng thái reset ổn định
        viewModel.startFindingRide(userPhone)
    }

    // 4. ✅ LOGIC ĐIỀU HƯỚNG KHI MATCH TÌM THẤY
    LaunchedEffect(matchResult) {
        if (matchResult != null) {
            val currentRoute = navController.currentDestination?.route
            // CHỈ navigate nếu chưa ở XAC_NHAN_DAT_XE (ngăn vòng lặp)
            if (currentRoute != NavigationRoutes.XAC_NHAN_DAT_XE) {
                navController.navigate(NavigationRoutes.XAC_NHAN_DAT_XE) {
                    // Xóa màn hình chờ (ChoSocket) ra khỏi stack
                    popUpTo(NavigationRoutes.CHO_SOCKET) { inclusive = true }
                }
            }
        }
    }

    // 5. LOGIC ĐIỀU HƯỚNG KHI BỊ HỦY SAU KHI TÌM KIẾM HOÀN TẤT
//    LaunchedEffect(isMatchCancelled) {
//        if (isMatchCancelled && isSearchAttemptComplete && matchResult == null) {
//            // Nếu bị hủy (từ socket) và không có match nào được tìm thấy
//            navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false)
//        }
//    }

    // --- UI màn hình chờ ---
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Chỉ hiển thị UI này nếu matchResult vẫn là null
            if (matchResult == null) {

                if (!isSearchAttemptComplete) {
                    // Đang Loading/Kiểm tra Match cũ lần đầu
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Đang kiểm tra Match cũ và tìm tài xế...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (isMatchCancelled) {
                    // Đã bị hủy sau quá trình tìm kiếm (có thể do socket thông báo hết hạn/hủy ngay)
                    // UI này chỉ chớp nhoáng vì LaunchedEffect(isMatchCancelled) sẽ điều hướng ngay.
                    Text(
                        text = "Tìm kiếm đã bị hủy hoặc hết hạn. Đang quay về...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                else {
                    // Đã hoàn tất check lần đầu, hiện đang chờ Match thực tế đến
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Đang tìm tài xế...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    // Button hủy
                    Button(onClick = {
                        viewModel.cancelFindingProcess() // Ngắt kết nối
                        navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false) // Quay về tìm kiếm
                    }) {
                        Text("Hủy tìm kiếm")
                    }
                }
            }
        }
    }
}