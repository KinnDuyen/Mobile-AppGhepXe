package com.example.myhatd.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext // Thêm LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.FindingRideViewModel
import java.util.Locale
import kotlin.math.roundToLong
import com.example.myhatd.ui.navigation.NavigationRoutes
import kotlinx.coroutines.launch

@Composable
fun ChiTietChuyenDiUserScreen(
    navController: NavController,
    viewModel: FindingRideViewModel
) {
    // Trạng thái dialog
    var showLyDoHuy by remember { mutableStateOf(false) }
    var showXacNhanHuy by remember { mutableStateOf(false) }
    var showHoanTatHuy by remember { mutableStateOf(false) }
    var lyDoChon by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // 1. Lắng nghe dữ liệu chuyến đi
    val rideInfo by viewModel.currentRide.collectAsState()
    val rideStatus by viewModel.currentRideStatus.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()

    // 2. Xử lý dữ liệu động
    val tenDriver = rideInfo?.tenDriver ?: "Đang tải..."
    val context = LocalContext.current // Sử dụng LocalContext
    val driverPhone = rideInfo?.sdtDriver
    val bienSoXe = rideInfo?.bienSoXe ?: "N/A"
    val hangXe = rideInfo?.hangXe ?: "HATD Bike"
    val diemDonTen = rideInfo?.tenDiemDiUser ?: "Điểm đón"
    val diemDenTen = rideInfo?.tenDiemDenUser ?: "Điểm đến"
    val hinhThucThanhToan = rideInfo?.hinhThucThanhToan ?: "Tiền mặt"

    val isCancellable = remember(rideStatus, isConfirming) {
        // CHỈ CHO PHÉP HỦY KHI CHƯA PHẢI LÀ PICKED_UP HOẶC COMPLETED VÀ KHÔNG TRONG QUÁ TRÌNH CONFIRM
        rideStatus != "PICKED_UP" && rideStatus != "COMPLETED" && !isConfirming
    }

    val giaTienInt = rideInfo?.giaTien?.roundToLong() ?: 0L
    val giaTienFormatted = String.format(Locale.getDefault(), "%,dđ", giaTienInt)

    // Xử lý thời gian
    val thoiGian = rideInfo?.thoiGianDriverDenUser?: "Đang tải..."
    val thoiGianFormatted = thoiGian.let {
        if (it != "Đang tải...") {
            try {
                // Giả định chuỗi ISO 8601
                val parts = it.split('T')
                val datePart = parts[0].split('-').reversed().joinToString("/")
                val timePart = parts[1].substringBefore('.').substringBeforeLast(':')
                "$datePart • $timePart"
            } catch (e: Exception) {
                "Không xác định"
            }
        } else {
            it
        }
    }


    // Xử lý khi dữ liệu không tồn tại (Loading)
    if (rideInfo == null) {
        LaunchedEffect(Unit) {
            // Nếu vẫn null sau khi màn hình được tạo, có thể chuyến đi đã bị hủy, quay về Home
            // Lưu ý: Logic này có thể cần điều chỉnh nếu ViewModel có trạng thái Loading rõ ràng
            // hiện tại, ta hiển thị Loading
        }
        return Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            Text("Đang tải chi tiết chuyến đi...", modifier = Modifier.offset(y = 40.dp))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()) // ✅ Đảm bảo toàn bộ nội dung cuộn được
    ) {

        Spacer(modifier = Modifier.height(15.dp))
        // 🔹 Thanh tiêu đề
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Chi tiết chuyến đi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Thông tin tài xế (DRIVER CARD) - Giảm chiều cao và tối ưu căn chỉnh
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp, max = 160.dp) // Giới hạn chiều cao
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF0081F1))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                Image(
                    painter = painterResource(id = R.drawable.nenthongtindriver),
                    contentDescription = "Background Driver",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop // Crop để ảnh không bị bóp méo
                )

                // Avatar, xe máy và Tên/Đánh giá
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Box {
                            // Avatar tài xế
                            Image(
                                painter = painterResource(id = R.drawable.avtdriver),
                                contentDescription = "Ảnh tài xế",
                                modifier = Modifier
                                    .size(80.dp)
                                    .offset(x = (-8).dp)
                                    .clip(CircleShape)
                                    .background(Color.White, CircleShape)
                                    .border(2.dp, Color(0xFF0081F1), CircleShape)
                            )

                            // Ảnh xe máy
                            Image(
                                painter = painterResource(id = R.drawable.xemay),
                                contentDescription = "Xe máy",
                                modifier = Modifier
                                    .size(50.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 6.dp, y = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tên tài xế và đánh giá sao
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = tenDriver,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "5.0⭐",
                                color = Color.Black,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Biển số xe, tên xe
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
                            text = bienSoXe,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0081F1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = hangXe,
                        fontSize = 12.sp,
                        color = Color(0xFF007ACC)
                    )
                }

                // Logo góc dưới phải - Điều chỉnh kích thước/offset để không bị cắt
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo HATD",
                    modifier = Modifier
                        .size(100.dp) // Giảm kích thước
                        .align(Alignment.BottomEnd)
                        .offset(x = 20.dp, y = 20.dp) // Offset hợp lý hơn
                        .padding(bottom = 8.dp, end = 16.dp)
                        .alpha(0.7f) // Giảm độ mờ
                )
            }
        }
        // 🔹 Khung nền trắng bo tròn chứa cả chat + call
        Card(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(4.dp, RoundedCornerShape(30.dp)),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .fillMaxWidth(), // Chiếm hết chiều rộng
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly // Căn giữa đều
            ) {
                // 🔸 Nút Chat với tài xế
                OutlinedButton(
                    onClick = {
                        // CHỨC NĂNG NHẮN TIN (SMS)
                        if (driverPhone != null && driverPhone.isNotEmpty()) {
                            val uri = android.net.Uri.parse("smsto:$driverPhone")
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = "Chat",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp) // Giảm size icon
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Chat với tài xế",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                // 🔸 Nút gọi sát bên phải
                IconButton(
                    onClick = {
                        // CHỨC NĂNG GỌI ĐIỆN
                        if (driverPhone != null && driverPhone.isNotEmpty()) {
                            val uri = android.net.Uri.parse("tel:$driverPhone")
                            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, uri)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.size(50.dp) // Giảm size button
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.call),
                        contentDescription = "Call",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp) // Giảm size icon
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        // 🔹 Thông tin chuyến đi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4ABDE0), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Box {
                // Ảnh nền mờ phía sau phần chi tiết chuyến đi
                Image(
                    painter = painterResource(id = R.drawable.anhnenchitietchuyendi),
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize() // Thay vì fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.1f
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    // 🔹 Loại chuyến + thời gian
                    Text(
                        text = hangXe,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Thời gian:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                        Text(
                            text = thoiGianFormatted,
                            color = Color(0xFF9E9E9E),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // 🔹 Điểm đón (Text Wrap)
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            painter = painterResource(id = R.drawable.diembatdau),
                            contentDescription = null,
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Sử dụng Column để Text có thể wrap
                        Column(modifier = Modifier.weight(1f)) {
                            Text(diemDonTen, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 🔹 Ảnh đường nối
                    Image(
                        painter = painterResource(id = R.drawable.duonggachnoi),
                        contentDescription = "Đường nối giữa điểm đón và điểm đến",
                        modifier = Modifier
                            .padding(start = 2.dp, top = 2.dp, bottom = 2.dp)
                            .height(40.dp)
                    )

                    // 🔹 Điểm đến (Text Wrap)
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            painter = painterResource(id = R.drawable.diemden),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(diemDenTen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // 🔹 Tiền mặt
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.dola),
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(hinhThucThanhToan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(giaTienFormatted, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    // 🔹 Ghi chú
                    Text("Ghi chú", fontWeight = FontWeight.Bold)
                    // Thêm Text hiển thị nội dung ghi chú nếu có trong rideInfo
//                    Text(rideInfo?.ghiChu ?: "Không có ghi chú", fontSize = 14.sp, color = Color.Gray)
                }

                // Hình xe nhỏ góc dưới bên phải - Tối ưu vị trí
                Image(
                    painter = painterResource(id = R.drawable.xegocduoiphai),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(80.dp) // Giảm kích thước
                        .offset(y = 10.dp, x = 10.dp) // Đưa ra ngoài cạnh dưới/phải
                        .alpha(0.5f)
                )

                // Hình xe nhỏ góc dưới bên trái - Tối ưu vị trí
                Image(
                    painter = painterResource(id = R.drawable.xegoctrai),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(80.dp) // Giảm kích thước
                        .offset(y = 10.dp, x = -10.dp) // Đưa ra ngoài cạnh dưới/trái
                        .alpha(0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Nút hủy chuyến
        Button(
            onClick = {
                // bật dialog chọn lý do hủy
                showLyDoHuy = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCancellable) Color(0xFF4ABDE0) else Color.Gray
            ),
            enabled = isCancellable // Logic nghiệp vụ: Chỉ bật khi hủy được
        ) {
            Text("Hủy chuyến xe", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp)) // Thêm padding cuối cùng
    }

    // ===== Dialogs (Giữ nguyên logic và tối ưu padding) =====

    if (showLyDoHuy) {
        HopThoaiLyDoHuy(
            onChon = { lyDo ->
                lyDoChon = lyDo
                showLyDoHuy = false
                showXacNhanHuy = true
            },
            onHuy = { showLyDoHuy = false }
        )
    }

    if (showXacNhanHuy) {
        HopThoaiXacNhanHuy(
            lyDo = lyDoChon,
            onXacNhan = {
                val rideId = rideInfo?.matchId
                showXacNhanHuy = false

                if (rideId != null) {
                    scope.launch {
                        viewModel.cancelFindingRide(
                            matchId = rideId,
                            reason = lyDoChon,
                            onComplete = { success ->
                                if (success) {
                                    showHoanTatHuy = true
                                }
                            }
                        )
                    }
                } else {
                    showHoanTatHuy = true
                }
            },
            onQuayLai = {
                showXacNhanHuy = false
                showLyDoHuy = true
            }
        )
    }

    if (showHoanTatHuy) {
        HopThoaiHoanTatHuy(
            onDatLai = {
                showHoanTatHuy = false
                navController.popBackStack(NavigationRoutes.HOME, false)
            },
            onDong = {
                showHoanTatHuy = false
                navController.popBackStack()
            }
        )
    }
}

/**
 * Dialog: HopThoaiLyDoHuy
 */
@Composable
fun HopThoaiLyDoHuy(onChon: (String) -> Unit, onHuy: () -> Unit) {
    Dialog(onDismissRequest = onHuy) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Giảm padding ngang cho màn hình nhỏ
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Tại sao bạn lại hủy bỏ?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Hãy cho HATD biết chuyện gì xảy ra.", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                val danhSach = listOf(
                    "Thay đổi phương thức thanh toán",
                    "Tôi muốn thay đổi địa điểm",
                    "Các vấn đề ưu đãi",
                    "Tài xế ở quá xa"
                )

                danhSach.forEach { lyDo ->
                    OutlinedButton(
                        onClick = { onChon(lyDo) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp) // Cố định chiều cao nút
                            .padding(vertical = 4.dp), // Giảm padding dọc
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF4ABDE0)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFE8F6FC),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = lyDo, fontSize = 14.sp) // Giảm size text
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onHuy) {
                        Text("Đóng")
                    }
                }
            }
        }
    }
}

/**
 * Dialog: HopThoaiXacNhanHuy
 */
@Composable
fun HopThoaiXacNhanHuy(lyDo: String, onXacNhan: () -> Unit, onQuayLai: () -> Unit) {
    Dialog(onDismissRequest = onQuayLai) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avtdriver),
                    contentDescription = "Icon xác nhận",
                    modifier = Modifier
                        .size(80.dp) // Giảm kích thước icon
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Bạn có chắc muốn hủy chuyến?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Lý do: $lyDo", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onXacNhan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4ABDE0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Xác nhận hủy", color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onQuayLai,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Quay lại")
                }
            }
        }
    }
}

/**
 * Dialog: HopThoaiHoanTatHuy
 */
@Composable
fun HopThoaiHoanTatHuy(onDatLai: () -> Unit, onDong: () -> Unit) {
    Dialog(onDismissRequest = onDong) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Hoàn tất",
                    modifier = Modifier.size(80.dp) // Giảm kích thước icon
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Lần sau vẫn đồng hành cùng HATD nhé!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDatLai) {
                    Text(
                        text = "Vui lòng đặt lại",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ABDE0)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDong) {
                    Text("Đóng")
                }
            }
        }
    }
}