package com.example.myhatd.ui.otp

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.AuthViewModel
import com.example.myhatd.ui.navigation.NavigationRoutes
import androidx.compose.foundation.BorderStroke


fun Any.findActivity(): Any? = null // Giả lập để code biên dịch

@Composable
fun PhoneAuthScreen(
    viewModel: AuthViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    if (activity == null) {
        // Thông báo lỗi UI thay vì Text đơn thuần
        ErrorBox(message = "Lỗi hệ thống: Không thể tìm thấy Activity cho Firebase Auth.")
        // Log lỗi
        LaunchedEffect(Unit) {
            println("FATAL_ERROR: Không thể tìm thấy Activity cho Firebase Auth.") // Thay bằng Log.e
        }
        return
    }

    // ✅ TRUY CẬP STATEFLOW ĐÚNG CÁCH
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 2. LOGIC ĐIỀU HƯỚNG
    LaunchedEffect(state.isOtpSent) {
        if (state.isOtpSent) {
            println("AuthFlow: OTP đã gửi thành công. Chuyển sang màn hình Verify OTP.") // Thay bằng Log.d
            navController.navigate(NavigationRoutes.VERIFY_OTP)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Ảnh nền full màn hình
        Image(
            painter = painterResource(id = R.drawable.bgsingup),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Nội dung chính
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center)
                .offset(y = (-230).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nút quay lại
            IconButton(
                onClick = {
                    println("UserAction: Nhấn nút Quay Lại.") // Thay bằng Log.d
                    navController.popBackStack()
                },
                Modifier
                    .align(Alignment.Start)
                    .offset(x = (-5).dp, y = (-1).dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tiêu đề
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nhập số điện thoại của bạn",
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.None
                    )

                    Divider(
                        color = Color(0xFF2A5EE1),
                        thickness = 2.dp,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Số điện thoại của bạn sẽ được sử dụng để xác minh và đăng nhập vào tài khoản.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tiêu đề "Số điện thoại" với icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Số điện thoại",
                    fontSize = 15.sp,
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    painter = painterResource(id = R.drawable.phone),
                    contentDescription = "Phone icon",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 4.dp)
                )
            }

            // Input nhập số điện thoại
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vn),
                    contentDescription = "Vietnam flag",
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(10.dp))

                OutlinedTextField(
                    value = state.phoneNumber,
                    onValueChange = {
                        println("Input: Phone number changed to: $it") // Thay bằng Log.d
                        viewModel.onPhoneNumberChange(it)
                    },
                    placeholder = { Text("96691599", color = Color.Gray) },
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        cursorColor = Color.Black,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nút gửi OTP
            Button(
                onClick = {
                    println("UserAction: Nhấn Gửi Mã OTP. Phone: ${state.phoneNumber}") // Thay bằng Log.d
                    viewModel.sendOtp(activity)
                },
                enabled = !state.isLoading && !state.isOtpSent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isLoading || state.isOtpSent) Color(0xFFCCCCCC) else Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(2.dp, Color.Black)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black
                    )
                } else {
                    Text(
                        text = if (state.isOtpSent) "Mã đã gửi (Tiếp tục)" else "Gửi Mã OTP",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị thông báo lỗi hoặc OTP ID
            AnimatedVisibility(
                visible = state.error != null || state.isOtpSent,
                enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(500)) + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (state.error != null) {
                        // HIỂN THỊ LỖI ĐẸP HƠN
                        LaunchedEffect(state.error) {
                            println("Error: Gửi OTP thất bại: ${state.error}") // Thay bằng Log.e
                        }
                        ErrorBox(message = state.error!!)
                    } else if (state.isOtpSent) {
                        // HIỂN THỊ THÀNH CÔNG ĐẸP HƠN
                        SuccessBox(message = "Mã OTP đã gửi thành công. Đang chuyển màn hình...")
                        // Dòng debug ID, chỉ dùng khi cần thiết
                        Text(
                            text = "Verification ID: ${state.verificationId}",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Footer
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-43).dp)
                .padding(bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "From ",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 15.sp
            )
            Text(
                text = "HATD",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2A5EE1)
            )
        }
    }
}

// -------------------- HỖ TRỢ GIAO DIỆN THÔNG BÁO -------------------- //

@Composable
fun ErrorBox(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFFEBEE)) // Nền đỏ nhạt
            .border(1.dp, Color(0xFFD32F2F), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Lỗi",
            tint = Color(0xFFD32F2F), // Màu đỏ đậm
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Lỗi: $message",
            color = Color(0xFFD32F2F),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SuccessBox(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE8F5E9)) // Nền xanh lá nhạt
            .border(1.dp, Color(0xFF388E3C), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Thành công",
            tint = Color(0xFF388E3C), // Màu xanh lá đậm
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            color = Color(0xFF388E3C),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}