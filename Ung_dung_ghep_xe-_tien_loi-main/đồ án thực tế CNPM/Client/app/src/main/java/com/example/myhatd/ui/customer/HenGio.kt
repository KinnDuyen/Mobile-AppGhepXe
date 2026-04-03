package com.example.myhatd.ui.customer
// Import các thư viện cần thiết
import androidx.compose.foundation.Image // Hiển thị ảnh
import androidx.compose.foundation.background // Vẽ nền cho Composable
import androidx.compose.foundation.clickable // Cho phép Composable có thể click
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.* // Layout: Box, Column, Row, Spacer...
import androidx.compose.foundation.lazy.LazyColumn // Danh sách cuộn theo chiều dọc
import androidx.compose.foundation.lazy.itemsIndexed // Hiển thị các item theo index
import androidx.compose.foundation.lazy.rememberLazyListState // Lưu trạng thái scroll của LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape // Bo tròn góc
import androidx.compose.material3.* // Material Design components (Button, Text...)
import androidx.compose.runtime.* // State management trong Compose
import androidx.compose.ui.Alignment // Căn chỉnh nội dung
import androidx.compose.ui.Modifier // Thêm các thuộc tính cho Composable
import androidx.compose.ui.draw.drawBehind // Vẽ trực tiếp phía sau Composable
import androidx.compose.ui.geometry.CornerRadius // Góc bo khi vẽ
import androidx.compose.ui.graphics.Color // Màu sắc
import androidx.compose.ui.graphics.PathEffect // Hiệu ứng path (dashed line)
import androidx.compose.ui.graphics.drawscope.Stroke // Vẽ viền
import androidx.compose.ui.res.painterResource // Load ảnh từ resource
import androidx.compose.ui.text.font.FontWeight // Độ đậm chữ
import androidx.compose.ui.text.style.TextAlign // Căn chỉnh chữ
import androidx.compose.ui.unit.Dp // Đơn vị chiều dài DP
import androidx.compose.ui.unit.dp // DP
import androidx.compose.ui.unit.sp // SP
import androidx.navigation.NavController
import com.example.myhatd.R // Resource file
import java.text.SimpleDateFormat // Định dạng ngày giờ
import java.util.* // Calendar, Date
import kotlinx.coroutines.launch // Coroutine để chạy async

@Composable
fun HenGioScreen(navController: NavController) { // Hàm chính hiển thị màn hình hẹn giờ
    // State lưu vị trí/giờ/phút người dùng chọn
    var ViTriNgayDaChon by remember { mutableStateOf(0) } // Chỉ số ngày được chọn
    var GioDaChon by remember { mutableStateOf("00") } // Giờ được chọn
    var PhutDaChon by remember { mutableStateOf("00") } // Phút được chọn

    // Tạo danh sách các ngày từ hôm nay -> 30 ngày sau
    val DanhSachNgay = remember {
        val Lich = Calendar.getInstance() // Lấy ngày hiện tại
        val DinhDangNgay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Định dạng ngày
        val DanhSach = mutableListOf<TuyChonNgay>() // Danh sách ngày để hiển thị

        // Thêm ngày hôm nay
        DanhSach.add(TuyChonNgay(0, "Hôm nay", DinhDangNgay.format(Lich.time)))

        // Thêm ngày mai
        Lich.add(Calendar.DAY_OF_MONTH, 1)
        DanhSach.add(TuyChonNgay(1, "Ngày mai", DinhDangNgay.format(Lich.time)))

        // Thêm các ngày tiếp theo
        for (i in 2..29) {
            Lich.add(Calendar.DAY_OF_MONTH, 1)
            val ChuoiNgay = DinhDangNgay.format(Lich.time)
            DanhSach.add(TuyChonNgay(i, ChuoiNgay, ChuoiNgay))
        }
        DanhSach
    }

    // Danh sách giờ từ 00 -> 23
    val DanhSachGio = (0..23).map { it.toString().padStart(2, '0') }
    // Danh sách phút từ 00 -> 59
    val DanhSachPhut = (0..59).map { it.toString().padStart(2, '0') }

    // Tính giờ đến nơi (giả sử di chuyển 23 phút)
    val GioDenNoi = remember(GioDaChon, PhutDaChon) {
        val Gio = GioDaChon.toInt()
        val Phut = PhutDaChon.toInt()
        val TongPhut = Gio * 60 + Phut + 23 // Tổng phút +23
        val GioDen = (TongPhut / 60) % 24 // Giờ đến, modulo 24
        val PhutDen = TongPhut % 60 // Phút đến
        "${GioDen.toString().padStart(2, '0')}:${PhutDen.toString().padStart(2, '0')}" // Chuỗi hiển thị
    }

    // Trạng thái scroll cho các danh sách LazyColumn
    val TrangThaiDanhSachNgay = rememberLazyListState()
    val TrangThaiDanhSachGio = rememberLazyListState()
    val TrangThaiDanhSachPhut = rememberLazyListState()
    val PhamVi = rememberCoroutineScope() // Coroutine scope để scroll

    // Cập nhật vị trí ngày được chọn khi scroll danh sách ngày
    LaunchedEffect(TrangThaiDanhSachNgay.firstVisibleItemIndex) {
        ViTriNgayDaChon = TrangThaiDanhSachNgay.firstVisibleItemIndex
    }

    // Cập nhật giờ khi scroll danh sách giờ
    LaunchedEffect(TrangThaiDanhSachGio.firstVisibleItemIndex) {
        snapshotFlow { TrangThaiDanhSachGio.firstVisibleItemIndex }
            .collect { ViTri ->
                if (ViTri in DanhSachGio.indices) {
                    GioDaChon = DanhSachGio[ViTri]
                }
            }
    }

    // Cập nhật phút khi scroll danh sách phút
    LaunchedEffect(TrangThaiDanhSachPhut.firstVisibleItemIndex) {
        snapshotFlow { TrangThaiDanhSachPhut.firstVisibleItemIndex }
            .collect { ViTri ->
                if (ViTri in DanhSachPhut.indices) {
                    PhutDaChon = DanhSachPhut[ViTri]
                }
            }
    }

    // Box cha chứa toàn bộ màn hình
    Box(
        modifier = Modifier
            .fillMaxSize() // Chiếm full màn hình
            .background(Color(0xFFF5F5F5)) // Nền xám nhẹ
    ) {
        // Nút Back ở góc trái trên
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 24.dp, y = 40.dp)
                .size(40.dp)
                .clickable{
                navController.navigate("home")
                    }

        )

        // Box chính chứa nội dung hẹn giờ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
                .offset(y = (-16).dp) // Dịch lên một chút
                .VienChamCham(3.dp, Color(0xFF00BCD4), 24.dp) // Viền chấm
                .background(Color.White, RoundedCornerShape(24.dp)) // Nền trắng bo góc
                .padding(20.dp) // Padding nội dung
        ) {
            Column(modifier = Modifier.fillMaxSize()) { // Sắp xếp nội dung theo cột
                // Row header: icon + tiêu đề + spacer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon đóng
                    Image(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = "Dong",
                        modifier = Modifier
                            .size(35.dp)
                            .offset(x = 10.dp, y = 5.dp)
                            .clickable { /* Dong */ }
                    )
                    // Tiêu đề
                    Text(
                        "Hẹn giờ",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = 15.dp)
                    )
                    Spacer(modifier = Modifier.size(24.dp)) // Khoảng trống
                }

                Spacer(modifier = Modifier.height(24.dp)) // Khoảng cách

                // Box chứa các danh sách ngày, giờ, phút
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .offset(y = -30.dp)
                ) {
                    val KhoangCachGiua = 115.dp // Khoảng cách padding giữa các item

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Khoảng cách giữa 3 cột
                    ) {
                        // ======== Cột chọn ngày ========
                        Box(modifier = Modifier.weight(1f)) {
                            LazyColumn(
                                state = TrangThaiDanhSachNgay,
                                modifier = Modifier.fillMaxSize().offset(y = 29.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = KhoangCachGiua)
                            ) {
                                itemsIndexed(DanhSachNgay) { ViTri, Ngay ->
                                    val DaChon = ViTriNgayDaChon == ViTri
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .background(
                                                if (DaChon) Color(0xFFE0F7FA) else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable(
                                                indication = null, // tắt ripple
                                                interactionSource = remember { MutableInteractionSource() } // quản lý tương tác
                                            ) {
                                                ViTriNgayDaChon = ViTri // cập nhật trạng thái ngay lập tức
                                                PhamVi.launch {
                                                    TrangThaiDanhSachNgay.animateScrollToItem(ViTri)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Ngay.Nhan,
                                            fontSize = if (DaChon) 16.sp else 14.sp,
                                            color = if (DaChon) Color(0xFF00BCD4) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // ======== Cột chọn giờ ========
                        Box(modifier = Modifier.weight(0.6f)) {
                            LazyColumn(
                                state = TrangThaiDanhSachGio,
                                modifier = Modifier.fillMaxSize().offset(y = 29.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = KhoangCachGiua)
                            ) {
                                itemsIndexed(DanhSachGio) { ViTri, Gio ->
                                    val DaChon = GioDaChon == Gio
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .background(
                                                if (DaChon) Color(0xFFE0F7FA) else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                GioDaChon = Gio
                                                PhamVi.launch { TrangThaiDanhSachGio.animateScrollToItem(ViTri)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Gio,
                                            fontSize = if (DaChon) 20.sp else 14.sp,
                                            fontWeight = if (DaChon) FontWeight.Bold else FontWeight.Normal,
                                            color = if (DaChon) Color(0xFF00BCD4) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // ======== Cột chọn phút ========
                        Box(modifier = Modifier.weight(0.6f)) {
                            LazyColumn(
                                state = TrangThaiDanhSachPhut,
                                modifier = Modifier.fillMaxSize().offset(y = 29.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(vertical = KhoangCachGiua)
                            ) {
                                itemsIndexed(DanhSachPhut) { ViTri, Phut ->
                                    val DaChon = PhutDaChon == Phut
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .background(
                                                if (DaChon) Color(0xFFE0F7FA) else Color.Transparent,
                                                RoundedCornerShape(16.dp)
                                            )
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) {
                                                PhutDaChon = Phut
                                                PhamVi.launch { TrangThaiDanhSachPhut.animateScrollToItem(ViTri)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = Phut,
                                            fontSize = if (DaChon) 20.sp else 14.sp,
                                            fontWeight = if (DaChon) FontWeight.Bold else FontWeight.Normal,
                                            color = if (DaChon) Color(0xFF00BCD4) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ===== Viền chọn dạng chấm =====
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .offset(y = 30.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(50.dp)
                                .VienChamCham(2.dp, Color(0xFF00BCD4), 16.dp) // Viền chấm
                        )
                        Box(
                            Modifier
                                .weight(0.6f)
                                .height(50.dp)
                                .VienChamCham(2.dp, Color(0xFF00BCD4), 16.dp)
                        )
                        Box(
                            Modifier
                                .weight(0.6f)
                                .height(50.dp)
                                .VienChamCham(2.dp, Color(0xFF00BCD4), 16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Box thông tin xe đón
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = -50.dp)
                        .VienChamCham(2.dp, Color(0xFF00BCD4), 16.dp)
                        .background(Color(0xFFE0F7FA), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Xe đón bạn lúc $GioDaChon:$PhutDaChon - ${DanhSachNgay[ViTriNgayDaChon].NgayThang}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
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

                Spacer(Modifier.height(16.dp))

                // Thông tin thêm về giá cước và hủy chuyến
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = -40.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.smile),
                            contentDescription = null,
                            Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Giá cước giờ thấp điểm luôn tốt hơn giờ cao điểm.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(R.drawable.calendar),
                            contentDescription = null,
                            Modifier.size(30.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Bạn có thể hủy chuyến đi trước khi HATD đến đón.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Nút xác nhận
                Button(
                    onClick = { /* Xac nhan */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .offset(y = 110.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3E5FC)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "Xác nhận",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BCD4)
                    )
                }
            }
        }
    }
}

// Data class chứa thông tin một ngày
data class TuyChonNgay(val Ma: Int, val Nhan: String, val NgayThang: String)

// Hàm vẽ viền chấm cho Modifier
fun Modifier.VienChamCham(
    DoDay: Dp = 3.dp, // Độ dày viền
    MauSac: Color = Color(0xFF00BCD4), // Màu viền
    BanKinh: Dp = 16.dp // Bo góc
) = this.drawBehind {
    val DoRongVien = DoDay.toPx()
    val DoDaiNet = 30f // Độ dài nét vẽ
    val KhoangCach = 10f // Khoảng cách giữa các nét
    val BanKinhGoc = BanKinh.toPx()

    drawRoundRect(
        color = MauSac,
        style = Stroke(
            width = DoRongVien,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DoDaiNet, KhoangCach), 0f) // Chấm nét
        ),
        cornerRadius = CornerRadius(BanKinhGoc)
    )
}