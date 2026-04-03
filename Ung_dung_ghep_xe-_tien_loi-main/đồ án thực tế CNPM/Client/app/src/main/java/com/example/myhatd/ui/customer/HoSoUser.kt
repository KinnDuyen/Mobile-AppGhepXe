package com.example.myhatd.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.data.model.UserResponse
import com.example.myhatd.viewmodel.UserViewModel
// Đã xóa import lỗi: androidx.compose.material3.tokens.value

/**
 * Màn hình Hồ sơ Người dùng (Customer)
 */
@Composable
fun HoSoUserScreen(
    navController: NavController,
    viewModel: UserViewModel,
    phoneNumber: String // SĐT user để lấy dữ liệu
) {
    // Lấy trạng thái dữ liệu từ ViewModel sử dụng DELEGATE (by)
    // Việc sử dụng 'by' giúp truy cập trực tiếp giá trị mà không cần '.value'
    val user by viewModel.userData
    val loading by viewModel.isLoading
    val error by viewModel.errorMessage

    // Tải dữ liệu người dùng khi màn hình được khởi tạo
    LaunchedEffect(Unit) {
        viewModel.loadUser(phoneNumber)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Nút quay lại (Top Left)
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 12.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
                .clickable { navController.navigate("home") }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(60.dp))

            // Xử lý trạng thái tải dữ liệu
            when {
                // ✅ Sửa lỗi: Truy cập trực tiếp 'loading' (Boolean) do dùng cú pháp 'by'
                loading -> CircularProgressIndicator()

                // ✅ Sửa lỗi: Truy cập trực tiếp 'error' (String?) do dùng cú pháp 'by'
                error != null -> Text(
                    text = "Lỗi: $error", // Truy cập trực tiếp 'error'
                    color = Color.Red,
                    fontSize = 16.sp
                )

                user != null -> UserProfileContent(
                    user = user!!,
                    navController = navController
                )
                else -> Text("Không tìm thấy dữ liệu người dùng.")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Nội dung hiển thị Hồ sơ User
 */
@Composable
fun UserProfileContent(user: UserResponse, navController: NavController) {
    Spacer(modifier = Modifier.height(20.dp))

    // ===== KHUNG THÔNG TIN USER (Card) =====
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF4ABDE0), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Lưu ý: R.drawable.anhnensauuser và R.drawable.thatim phải tồn tại
            Image(
                painter = painterResource(id = R.drawable.anhnensauuser),
                contentDescription = "Ảnh nền khung user",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                alpha = 0.4f
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.anhuser),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.name ?: "Chưa cập nhật tên",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = user.phoneNumber ?: "Không rõ SĐT",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // ===== CHI TIẾT NGƯỜI DÙNG =====
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ProfileRow(label = "Họ tên", value = user.name ?: "N/A")
            Divider(color = Color.LightGray)
            ProfileRow(label = "Số căn cước công dân", value = user.canCuocCongDan ?: "N/A")
            Divider(color = Color.LightGray)
            ProfileRow(label = "Số điện thoại", value = user.phoneNumber ?: "N/A")
            Divider(color = Color.LightGray)
            ProfileRow(label = "Vai Trò", value = user.role ?: "N/A")
        }

        // Viền nét đứt
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val stroke = 3.dp.toPx()
                    val dash = 15.dp.toPx()
                    val gap = 10.dp.toPx()

                    val borderColor = Color.LightGray

                    // Top line
                    var x = 0f
                    while (x < size.width) {
                        val xEnd = (x + dash).coerceAtMost(size.width)
                        drawLine(borderColor, Offset(x, 0f), Offset(xEnd, 0f), stroke)
                        x += dash + gap
                    }

                    // Bottom line
                    x = 0f
                    val yBottom = size.height
                    while (x < size.width) {
                        val xEnd = (x + dash).coerceAtMost(size.width)
                        drawLine(borderColor, Offset(x, yBottom), Offset(xEnd, yBottom), stroke)
                        x += dash + gap
                    }

                    // Left line
                    var y = 0f
                    while (y < size.height) {
                        val yEnd = (y + dash).coerceAtMost(size.height)
                        drawLine(borderColor, Offset(0f, y), Offset(0f, yEnd), stroke)
                        y += dash + gap
                    }

                    // Right line
                    y = 0f
                    val xRight = size.width
                    while (y < size.height) {
                        val yEnd = (y + dash).coerceAtMost(size.height)
                        drawLine(borderColor, Offset(xRight, y), Offset(xRight, yEnd), stroke)
                        y += dash + gap
                    }
                }
        )
    }

    Spacer(modifier = Modifier.height(30.dp))

    // ===== HỘP CẢM ƠN =====
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, Color(0xFF4ABDE0), RoundedCornerShape(14.dp))
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFFFEE8E8), Color(0xFFD5F1FF))
                )
            )
            .padding(vertical = 26.dp, horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Cảm ơn bạn đã tin tưởng và đồng hành cùng HATD.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "HATD rất vui khi được cùng bạn chia sẻ những khoảnh khắc đáng nhớ.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Image(
            painter = painterResource(id = R.drawable.thatim),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 25.dp, y = 25.dp)
                .size(60.dp)
        )
    }

    Spacer(modifier = Modifier.height(35.dp))

    // ===== NÚT ĐĂNG XUẤT (An toàn) =====
    TextButton(
        onClick = {
            navController.navigate("gioithieu") {
                // Xóa tất cả các màn hình khỏi stack
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    ) {
        Text(
            text = "Đăng xuất",
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
}

/**
 * Composable tái sử dụng để hiển thị một dòng thông tin
 */
@Composable
fun ProfileRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(text = label, fontSize = 15.sp, color = Color.Gray)
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            fontSize = 17.sp
        )
    }
}