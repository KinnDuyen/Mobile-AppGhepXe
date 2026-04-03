package com.example.myhatd.ui.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.viewmodel.AuthViewModel

@Composable
fun xacnhanotpScreen(
    navController: NavController,
    // ✅ Nhận ViewModel qua tham số (hoặc tự khởi tạo qua viewModel())
    viewModel: AuthViewModel = viewModel()
) {
    // ✅ Truy cập StateFlow đúng cách
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Các biến State cục bộ cho input
    var name by remember { mutableStateOf("") }
    var cccd by remember { mutableStateOf("") }

    // --- LOGIC ĐIỀU HƯỚNG ---
    LaunchedEffect(state.isInfoSaved) {
        if (state.isInfoSaved) {
            // Chuyển hướng về Home sau khi lưu thông tin Tên/CCCD thành công
            navController.navigate(NavigationRoutes.HOME) {
                // Xóa các màn hình xác thực khỏi back stack
                popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
            }
        }
    }

    // Xử lý lỗi từ server (nếu có)
    LaunchedEffect(state.error) {
        if (state.error != null && !state.isLoading && state.isAuthenticated) {
            // Hiện thị lỗi nếu có (ví dụ: Log hoặc Snackbar)
            println("Server Error on Info Save: ${state.error}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔹 Nút back
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            IconButton(onClick = {
                // Quay lại màn hình OTP (hoặc màn hình trước)
                navController.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
        }

        // 🔹 TextField Tên
        Text(
            text = "Tên",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Bạn muốn được gọi bằng tên gì ?", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp)
        )

        // 🔹 TextField CCCD
        Text(
            text = "CCCD",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        TextField(
            value = cccd,
            onValueChange = { cccd = it },
            placeholder = { Text("Nhập số CCCD/CMND (Không bắt buộc)", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Đoạn mô tả
        Text(
            text = "Bằng việc tiếp tục, bạn xác nhận rằng bạn đã đọc và đồng ý với " +
                    "Điều Khoản Dịch Vụ và Thông Báo Bảo Mật của chúng tôi...",
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Nút Tiếp tục
        Button(
            onClick = {
                // ✅ Gọi hàm ViewModel để gửi Tên và CCCD lên Backend
                if (name.isNotBlank()) {
                    viewModel.finalizeUserInfo(
                        name = name,
                        cccd = cccd.ifBlank { null }
                    )
                }
            },
            // Chỉ cho phép click khi không Loading VÀ đã nhập Tên
            enabled = !state.isLoading && name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Xác nhận và Hoàn tất")
            }
        }
    }
}
