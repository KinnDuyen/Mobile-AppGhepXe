package com.example.myhatd.ui.driver

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhatd.R
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myhatd.viewmodel.DriverViewModel
import com.example.myhatd.viewmodel.UserViewModel

// ĐÃ XÓA: private const val CURRENT_PHONE_NUMBER = "0396691599" // XÓA SĐT CỨNG

@Composable
fun HoSoDriverScreen(
    navController: NavController,
    // ViewModel đã được AppNavigation cung cấp với Factory chính xác
    driverViewModel: DriverViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    // --- Lấy State từ ViewModel ---
    val driverInfoState by driverViewModel.driverInfo.collectAsStateWithLifecycle()
    val driverIsLoading by driverViewModel.isLoading.collectAsStateWithLifecycle()
    val driverError by driverViewModel.errorMessage.collectAsStateWithLifecycle()

    val userData by userViewModel.userData
    val userIsLoading by userViewModel.isLoading
    val userError by userViewModel.errorMessage

    // --- Khởi tạo và Tải dữ liệu ---
    // ĐÃ XÓA KHỐI LaunchedEffect KHỎI ĐÂY.
    // Việc tải dữ liệu (fetchDriver/loadUser) được thực hiện trong AppNavigation.kt.

    // --- Xử lý trạng thái Loading/Error ---
    if (driverIsLoading || userIsLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF0081F1))
        }
        return
    }

    // --- Ánh xạ dữ liệu để hiển thị (bao gồm xử lý giá trị null) ---
    val hoTen = userData?.name ?: "Đang tải..."
    val soCCCD = userData?.canCuocCongDan ?: "Chưa có"
    val soDienThoai = userData?.phoneNumber ?: "039xxxxxxx"

    val bienSo = driverInfoState?.bienSo ?: "XXYY-ZZZ.ZZ"
    val hangXe = driverInfoState?.hangXe ?: "Xe máy"
    val gioiTinh = driverInfoState?.gioiTinh ?: "Chưa chọn"

    // --- UI chính (Giữ nguyên) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        // --- Thanh tiêu đề ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Quay lại",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        // --- Card Thông tin tài xế ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
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
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            Image(
                                painter = painterResource(id = R.drawable.avtdriver),
                                contentDescription = "Ảnh tài xế",
                                modifier = Modifier
                                    .size(80.dp)
                                    .offset(x = (-8).dp)
                                    .background(Color.White, CircleShape)
                                    .border(2.dp, Color(0xFF0081F1), CircleShape)
                            )
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = hoTen, // Dữ liệu động
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "5.0⭐", color = Color.Black, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                }

                // Biển số xe + tên xe
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
                            text = bienSo, // Dữ liệu động
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0081F1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = hangXe, // Dữ liệu động
                        fontSize = 12.sp,
                        color = Color(0xFF007ACC)
                    )
                }

                // Logo
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

        // --- Khung thông tin chi tiết (viền xám) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(BorderStroke(1.dp, Color.Gray), shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Thông tin cá nhân:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ThongTinDong("Họ tên", hoTen)
                ThongTinDong("Giới tính", gioiTinh)
                ThongTinDong("Số căn cước công dân", soCCCD)
                ThongTinDong("Số điện thoại", soDienThoai)

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Thông tin xe:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
                ThongTinDong("Biển số xe", bienSo)
                ThongTinDong("Hãng xe", hangXe)
            }
        }

        // --- Xử lý hiển thị lỗi (nếu có) ---
        if (driverError != null || userError != null) {
            Text(
                "Lỗi: ${driverError ?: userError}",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Ghi chú dưới ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, Color(0xFF4ABDE0)), RoundedCornerShape(20.dp))
                .padding(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0xFF4ABDE0).copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Image(
                    painter = painterResource(id = R.drawable.like),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Cảm ơn bạn đã tin tưởng và đồng hành cùng HATD.\n" +
                            "HATD rất vui khi được cùng bạn chia sẻ những khoảnh khắc đáng nhớ.",
                    fontSize = 13.sp,
                    color = Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(id = R.drawable.tim),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Nút đăng xuất ---
        TextButton(
            onClick = { navController.navigate("gioithieu")  },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "Đăng xuất",
                color = Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Hàm Composable cho từng dòng thông tin (Giữ nguyên)
@Composable
fun ThongTinDong(nhan: String, giaTri: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = nhan,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = giaTri,
            style = TextStyle(fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        )
    }
}