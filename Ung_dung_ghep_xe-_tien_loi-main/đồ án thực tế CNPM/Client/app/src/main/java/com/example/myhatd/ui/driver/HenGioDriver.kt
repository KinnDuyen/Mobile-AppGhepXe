package com.example.myhatd.ui.driver

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import com.example.myhatd.viewmodel.ChuyenDiViewModel
import com.example.myhatd.viewmodel.UserViewModel
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.window.Dialog
import kotlin.math.abs

// Data Class (Giữ nguyên)
data class TripData(
    val tenDiemDi: String,
    val tenDiemDen: String,
    val viDoDiemDi: Double,
    val kinhDoDiemDi: Double,
    val viDoDiemDen: Double,
    val kinhDoDiemDen: Double
)

@Composable
fun DriverHenGioScreen(
    navController: NavController,
    chuyenDiViewModel: ChuyenDiViewModel,
    userViewModel: UserViewModel,
    phoneNumber: String,
    role: String,
    tripData: TripData
) {
    val context = LocalContext.current
    val state by chuyenDiViewModel.state.collectAsState()
    // val user by userViewModel.userData // Không cần dùng trong phần UI này

    var ViTriNgayDaChon by remember { mutableStateOf(0) }
    var GioDaChon by remember { mutableStateOf("00") }
    var PhutDaChon by remember { mutableStateOf("00") }

    // ✅ STATE MỚI: Kiểm soát Dialog thành công
    var showSuccessDialog by remember { mutableStateOf(false) }

    // --- LOGIC TÍNH TOÁN THỜI GIAN --- (Giữ nguyên)
    val DanhSachNgay = remember {
        val Lich = Calendar.getInstance()
        val DinhDangNgay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val DanhSach = mutableListOf<TuyChonNgay>()
        Lich.set(Calendar.HOUR_OF_DAY, 0); Lich.set(Calendar.MINUTE, 0); Lich.set(Calendar.SECOND, 0); Lich.set(Calendar.MILLISECOND, 0)
        DanhSach.add(TuyChonNgay(0, "Hôm nay", DinhDangNgay.format(Lich.time)))
        Lich.add(Calendar.DAY_OF_MONTH, 1)
        DanhSach.add(TuyChonNgay(1, "Ngày mai", DinhDangNgay.format(Lich.time)))
        for (i in 2..29) {
            Lich.add(Calendar.DAY_OF_MONTH, 1)
            val ChuoiNgay = DinhDangNgay.format(Lich.time)
            DanhSach.add(TuyChonNgay(i, ChuoiNgay, ChuoiNgay))
        }
        DanhSach
    }
    val DanhSachGio = (0..23).map { it.toString().padStart(2, '0') }
    val DanhSachPhut = (0..59).map { it.toString().padStart(2, '0') }
    val GioDenNoi = remember(GioDaChon, PhutDaChon) {
        val TongPhut = GioDaChon.toInt() * 60 + PhutDaChon.toInt() + 23
        val GioDen = (TongPhut / 60) % 24
        val PhutDen = TongPhut % 60
        "${GioDen.toString().padStart(2, '0')}:${PhutDen.toString().padStart(2, '0')}"
    }
    val ThoiGianHenDaChon: String? = remember(ViTriNgayDaChon, GioDaChon, PhutDaChon) {
        val NgayThang = DanhSachNgay.getOrNull(ViTriNgayDaChon)?.NgayThang
        if (NgayThang != null) {
            val ChuoiThoiGian = "$NgayThang $GioDaChon:$PhutDaChon"
            val DinhDangParse = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val DinhDangISO = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            try {
                val dateObject = DinhDangParse.parse(ChuoiThoiGian)
                dateObject?.let { DinhDangISO.format(it) }
            } catch (e: Exception) {
                Log.e("SCHEDULE_TIME", "Lỗi Parse Date: $e")
                null
            }
        } else {
            null
        }
    }
    // --- KẾT THÚC LOGIC TÍNH TOÁN ---

    // --- LOGIC SCROLL VÀ SNAP --- (Giữ nguyên)
    val TrangThaiDanhSachNgay = rememberLazyListState()
    val TrangThaiDanhSachGio = rememberLazyListState()
    val TrangThaiDanhSachPhut = rememberLazyListState()
    val PhamVi = rememberCoroutineScope()
    fun getCentralItemIndex(state: LazyListState): Int {
        val layoutInfo = state.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        if (visibleItems.isEmpty()) return -1
        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
        val centralItem = visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }
        return centralItem?.index ?: -1
    }
    LaunchedEffect(TrangThaiDanhSachNgay.isScrollInProgress) {
        if (!TrangThaiDanhSachNgay.isScrollInProgress) {
            val centralIndex = getCentralItemIndex(TrangThaiDanhSachNgay)
            if (centralIndex != -1 && centralIndex in DanhSachNgay.indices) {
                if (ViTriNgayDaChon != centralIndex) {
                    ViTriNgayDaChon = centralIndex
                }
                PhamVi.launch { TrangThaiDanhSachNgay.animateScrollToItem(centralIndex) }
            }
        }
    }
    LaunchedEffect(TrangThaiDanhSachGio.isScrollInProgress) {
        if (!TrangThaiDanhSachGio.isScrollInProgress) {
            val centralIndex = getCentralItemIndex(TrangThaiDanhSachGio)
            if (centralIndex != -1 && centralIndex in DanhSachGio.indices) {
                if (GioDaChon != DanhSachGio[centralIndex]) {
                    GioDaChon = DanhSachGio[centralIndex]
                }
                PhamVi.launch { TrangThaiDanhSachGio.animateScrollToItem(centralIndex) }
            }
        }
    }
    LaunchedEffect(TrangThaiDanhSachPhut.isScrollInProgress) {
        if (!TrangThaiDanhSachPhut.isScrollInProgress) {
            val centralIndex = getCentralItemIndex(TrangThaiDanhSachPhut)
            if (centralIndex != -1 && centralIndex in DanhSachPhut.indices) {
                if (PhutDaChon != DanhSachPhut[centralIndex]) {
                    PhutDaChon = DanhSachPhut[centralIndex]
                }
                PhamVi.launch { TrangThaiDanhSachPhut.animateScrollToItem(centralIndex) }
            }
        }
    }
    // --- KẾT THÚC LOGIC SCROLL ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 40.dp)
                .size(40.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    navController.popBackStack()
                }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp)
                .VienChamCham(3.dp, Color(0xFF00BCD4), 24.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {

            Column(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.close),
                            contentDescription = "Dong",
                            modifier = Modifier
                                .size(35.dp)
                                .clickable { /* Dong */ }
                        )
                        Text(
                            "Hẹn giờ",
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.size(35.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ================================================
                    // ✅ BOX TÓM TẮT CHUYẾN ĐI (ĐÃ THIẾT KẾ LẠI)
                    // ================================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F8FF), contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // --- CỘT ICON LỘ TRÌNH ---
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                // 💡 Icon tùy chỉnh cho điểm đi (ví dụ: dùng Icon Place màu xanh)
                                Icon(
                                    Icons.Filled.Place,
                                    contentDescription = "Điểm đi",
                                    tint = Color(0xFF00BCD4),
                                    modifier = Modifier.size(20.dp)
                                )
                                // Dùng Divider làm đường nối
                                Divider(
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(30.dp)
                                        .padding(vertical = 4.dp)
                                )
                                Icon(
                                    Icons.Filled.Place,
                                    contentDescription = "Điểm đến",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // --- CỘT THÔNG TIN VĂN BẢN ---
                            Column(modifier = Modifier.weight(1f)) {
                                // 1. ĐIỂM ĐI
                                Text(
                                    "ĐIỂM ĐI",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tripData.tenDiemDi,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // 2. ĐIỂM ĐẾN
                                Text(
                                    "ĐIỂM ĐẾN",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = tripData.tenDiemDen,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                    // ================================================
                    // ✅ KẾT THÚC BOX TÓM TẮT
                    // ================================================


                    Spacer(Modifier.height(16.dp))

                    // --- BỘ CHỌN NGÀY / GIỜ / PHÚT ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimePickerColumn(
                            state = TrangThaiDanhSachNgay,
                            items = DanhSachNgay.map { it.Nhan },
                            modifier = Modifier.weight(1.5f)
                        )
                        TimePickerColumn(
                            state = TrangThaiDanhSachGio,
                            items = DanhSachGio,
                            modifier = Modifier.weight(1f)
                        )
                        TimePickerColumn(
                            state = TrangThaiDanhSachPhut,
                            items = DanhSachPhut,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // --- KẾT THÚC BỘ CHỌN ---

                    Spacer(Modifier.height(16.dp))

                    // Box thông tin xe đón
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .VienChamCham(2.dp, Color(0xFF00BCD4), 16.dp)
                            .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Xe đón bạn lúc $GioDaChon:$PhutDaChon - ${DanhSachNgay.getOrNull(ViTriNgayDaChon)?.NgayThang ?: ""}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Đến nơi lúc $GioDenNoi",
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )
                            Text(
                                "di chuyển khoảng 23 phút",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                } // Kết thúc Column cuộn (weight 1f)


                // Nút xác nhận
                Button(
                    onClick = {
                        val scheduleTime = ThoiGianHenDaChon
                        if (phoneNumber.isNotEmpty()) {
                            if (role == "DRIVER" && scheduleTime == null) {
                                Log.e("SCHEDULE_SEND", "Tài xế phải chọn thời gian khởi hành.")
                                return@Button
                            }
                            chuyenDiViewModel.sendChuyenDi(
                                phoneNumber = phoneNumber,
                                role = role,
                                tenDiemDi = tripData.tenDiemDi,
                                tenDiemDen = tripData.tenDiemDen,
                                viDoDiemDi = tripData.viDoDiemDi,
                                kinhDoDiemDi = tripData.kinhDoDiemDi,
                                viDoDiemDen = tripData.viDoDiemDen,
                                kinhDoDiemDen = tripData.kinhDoDiemDen,
                                scheduleTime = scheduleTime
                            )
                        } else {
                            Log.e("SCHEDULE_SEND", "Thiếu thông tin user (số điện thoại).")
                        }
                    },
                    enabled = ThoiGianHenDaChon != null && phoneNumber.isNotEmpty() && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3E5FC)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = if (state.isLoading) "Đang gửi yêu cầu..." else "Xác nhận",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4)
                    )
                }
            }
        }
    }

    // ✅ XỬ LÝ ĐIỀU HƯỚNG VÀ HIỂN THỊ DIALOG THÀNH CÔNG
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            // Thay thế Toast bằng việc bật Dialog
            showSuccessDialog = true
            // Không cần reset state ở đây, logic reset sẽ được thực hiện khi đóng Dialog
        }
    }
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            Log.e("SCHEDULE_ERROR", state.errorMessage!!)
            Toast.makeText(
                context,
                "Lỗi: ${state.errorMessage}",
                Toast.LENGTH_LONG
            ).show()
            chuyenDiViewModel.resetState()
        }
    }

    // ✅ DIALOG THÔNG BÁO THÀNH CÔNG CHUYÊN NGHIỆP
    if (showSuccessDialog) {
        HopThoaiThanhCong(
            onClose = {
                showSuccessDialog = false
                // ✅ LOGIC NGHIỆP VỤ: Điều hướng và reset state
                navController.popBackStack("home_driver", inclusive = false)
                chuyenDiViewModel.resetState()
            }
        )
    }
}

// ----------------------------------------------------
// ✅ COMPOSABLE MỚI: DIALOG THÔNG BÁO THÀNH CÔNG
// ----------------------------------------------------

@Composable
fun HopThoaiThanhCong(onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF00BCD4).copy(alpha = 0.5f)), // Thêm đổ bóng
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 30.dp, horizontal = 24.dp) // Tăng padding
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon thành công lớn
                Box(
                    modifier = Modifier
                        .size(90.dp) // Kích thước lớn hơn
                        .clip(CircleShape)
                        .background(Color(0xFFE0F7FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Done,
                        contentDescription = "Thành công",
                        tint = Color(0xFF00BCD4),
                        modifier = Modifier.size(60.dp) // Icon lớn hơn
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ TEXT ĐÃ CHỈNH SỬA: Lớn hơn, đậm hơn, nổi bật hơn
                Text(
                    text = "Đăng Ký Thành Công!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp, // Tăng kích thước
                    color = Color(0xFF00796B), // Dùng màu xanh đậm hơn để nổi bật
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Yêu cầu chuyến xe hẹn giờ của bạn đã được ghi nhận. Chúc bạn một hành trình an toàn!",
                    fontSize = 15.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp), // Kích thước lớn hơn
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4))
                ) {
                    Text("Hoàn tất", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// CÁC COMPOSABLE VÀ DATA CLASS KHÁC (GIỮ NGUYÊN)
// ----------------------------------------------------

/**
 * Composable phụ cho cột chọn thời gian (Ngày/Giờ/Phút)
 */
@Composable
private fun TimePickerColumn(
    state: LazyListState,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    val containerHeight = 150.dp
    val itemHeight = 40.dp
    val padding = (containerHeight / 2) - (itemHeight / 2)

    Box(modifier = modifier.height(containerHeight)) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = padding)
        ) {
            itemsIndexed(items) { index, item ->
                val isSelected = remember {
                    derivedStateOf {
                        val layoutInfo = state.layoutInfo
                        val visibleItems = layoutInfo.visibleItemsInfo
                        if (visibleItems.isEmpty()) return@derivedStateOf false
                        val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportSize.height / 2
                        val centralItem = visibleItems.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }
                        centralItem?.index == index
                    }
                }.value

                Text(
                    text = item,
                    fontSize = 18.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.Gray,
                    modifier = Modifier
                        .height(itemHeight)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(itemHeight)
                .align(Alignment.Center)
                .background(
                    Color(0xFF00BCD4).copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    Color(0xFF00BCD4).copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
        )
    }
}


// Data class
data class TuyChonNgay(val Ma: Int, val Nhan: String, val NgayThang: String)

// Hàm vẽ viền chấm
fun Modifier.VienChamCham(
    DoDay: Dp = 3.dp,
    MauSac: Color = Color(0xFF00BCD4),
    BanKinh: Dp = 16.dp
) = this.drawBehind {
    val DoRongVien = DoDay.toPx()
    val DoDaiNet = 30f
    val KhoangCach = 10f
    val BanKinhGoc = BanKinh.toPx()

    drawRoundRect(
        color = MauSac,
        style = Stroke(
            width = DoRongVien,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DoDaiNet, KhoangCach), 0f)
        ),
        cornerRadius = CornerRadius(BanKinhGoc)
    )
}