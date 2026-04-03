package com.example.myhatd.ui.driver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.myhatd.data.model.MatchNotificationDTO
import com.example.myhatd.viewmodel.ChuyenDiViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToLong

// --- CÁC IMPORTS THIẾT YẾU KHÁC ---
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
// ------------------------------------

// ⚠️ Nếu hàm VienChamCham được định nghĩa trong DriverHenGioScreen.kt (cùng package),
// Kotlin sẽ tự động thấy nó. Nếu không, bạn cần thêm import cụ thể:
// import com.example.myhatd.ui.driver.VienChamCham
// (Giả định nó đã có sẵn do cùng package hoặc đã được import ngầm)


// ------------------------------------------------------------------------------------------------
// 📌 MÀN HÌNH CHÍNH: DuDoanScreen (Hiển thị chuyến đi đã hẹn giờ của Tài xế)
// ------------------------------------------------------------------------------------------------

@Composable
fun DuDoanScreen(
    navController: NavController,
    // Thu thập StateFlow từ ChuyenDiViewModel
    chuyenDiViewModel: ChuyenDiViewModel = viewModel()
) {
    // Thu thập StateFlow<MatchNotificationDTO?>
//    val scheduledRideState = chuyenDiViewModel.scheduledRide.collectAsState()
//    val scheduledTrip = scheduledRideState.value

    // Tạo danh sách chỉ chứa chuyến đi đã hẹn giờ gần nhất (nếu có)
//    val notifications = remember(scheduledTrip) {
//        if (scheduledTrip != null) listOf(scheduledTrip) else emptyList()
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- Phần Thanh tiêu đề và Nút Quay lại ---
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .clickable { navController.navigateUp() },
            contentScale = ContentScale.Fit
        )

        Text(
            text = "Chuyến Đi Đã Hẹn Giờ",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )

        Divider(
            color = Color(0xFF4ABDE0),
            thickness = 3.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .width(280.dp)
        )
        // -------------------------------------------------------

        // --- Nội dung thông báo chi tiết sử dụng LazyColumn ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, start = 16.dp, end = 16.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 20.dp)
        ) {
//            if (notifications.isEmpty()) {
//                item {
//                    Text(
//                        text = "Bạn chưa có chuyến đi nào được hẹn giờ.",
//                        color = Color.Gray,
//                        fontSize = 16.sp,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 50.dp),
//                        textAlign = TextAlign.Center
//                    )
//                }
//            } else {
//                items(notifications, key = { it.sdtDriver ?: it.hashCode() }) { notification ->
//                    DriverScheduledTripItem(data = notification)
//                    Spacer(modifier = Modifier.height(16.dp))
//                }
//            }
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 💡 COMPONENT PHỤ: DriverScheduledTripItem (Hiển thị chi tiết từng chuyến hẹn)
// ------------------------------------------------------------------------------------------------

@Composable
fun DriverScheduledTripItem(
    data: MatchNotificationDTO
) {
    // 1. Trích xuất và định dạng dữ liệu
    val diemDi = data.tenDiemDiUser ?: "Đang cập nhật"
    val diemDen = data.tenDiemDenUser ?: "Đang cập nhật"
    val tenDriver = data.tenDriver ?: "N/A"
    val sdtDriver = data.sdtDriver ?: "N/A"

    // Định dạng thời gian (Giả sử thoiGianDriverDenUser là ISO 8601)
    val thoiGianDonRaw = data.thoiGianDriverDenUser ?: "N/A"
    val thoiGianDonFormatted = thoiGianDonRaw
        .substringAfter('T')
        .substringBeforeLast(':')
        .substringBefore('.')
        .ifEmpty { "N/A" }

    val statusTitle = if (data.message == "SCHEDULED_SUCCESS") "✅ Chuyến đi ĐÃ ĐƯỢC LÊN LỊCH" else "🔄 Chờ khớp khách hàng"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9) // Nền xanh lá nhạt
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = statusTitle,
                color = Color(0xFF388E3C),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFF4ABDE0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Thông tin hẹn giờ
            NotificationDetailRow("Tài xế:", tenDriver, Color(0xFF4CAF50))
            NotificationDetailRow("SĐT:", sdtDriver, Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // Thông tin chuyến
            NotificationDetailRow("Điểm đi:", diemDi, Color.Black)
            NotificationDetailRow("Điểm đến:", diemDen, Color.Black)
            NotificationDetailRow("Thời gian khởi hành:", thoiGianDonFormatted, Color(0xFFF44336))

            Spacer(modifier = Modifier.height(8.dp))

            // Ghi chú
            Text(
                "Chuyến đi này đang chờ khớp với yêu cầu của khách hàng. Chúng tôi sẽ thông báo khi có Match.",
                fontSize = 13.sp,
                color = Color.DarkGray
            )
        }
    }
}

// ------------------------------------------------------------------------------------------------
// 💡 HÀM PHỤ: NotificationDetailRow
// ------------------------------------------------------------------------------------------------

@Composable
fun NotificationDetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ------------------------------------------------------------------------------------------------
// ⚠️ QUAN TRỌNG: KHÔNG ĐỊNH NGHĨA LẠI HÀM VienChamCham Ở ĐÂY
// ------------------------------------------------------------------------------------------------