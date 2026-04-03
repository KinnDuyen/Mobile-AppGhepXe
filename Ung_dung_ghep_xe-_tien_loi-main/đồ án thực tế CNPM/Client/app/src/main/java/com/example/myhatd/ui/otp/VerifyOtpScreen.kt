package com.example.myhatd.ui.otp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.myhatd.R
import com.example.myhatd.viewmodel.AuthViewModel
import androidx.navigation.NavController
import com.example.myhatd.ui.navigation.NavigationRoutes
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import bổ sung nếu dùng Lifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun VerifyOtpScreen( navController: NavController,
                     viewModel: AuthViewModel) {

    // ✅ TRUY CẬP STATE ĐÚNG CÁCH CHO StateFlow
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // 1. Xử lý điều hướng sau khi xác thực Firebase thành công
    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            // Chuyển sang màn hình nhập thông tin người dùng
            navController.navigate(NavigationRoutes.THONG_TIN_USER) {
                // Xóa màn hình PhoneAuth khỏi back stack
                popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Ảnh nền (chú mèo) - Điều chỉnh kích thước và vị trí cho phù hợp với màn hình
        Image(
            painter = painterResource(R.drawable.cat_chubby),
            contentDescription = null,
            contentScale = ContentScale.Crop, // Dùng Crop thay vì FillBounds để tránh bị méo quá nhiều
            modifier = Modifier
                .align(Alignment.BottomEnd) // Đặt ở góc dưới bên phải
                .offset(x = 100.dp, y = 100.dp) // Dịch chuyển để làm hiệu ứng nền
                .width(400.dp)
                .height(350.dp)
                .alpha(0.3f) // Giảm độ mờ để nó trông giống ảnh nền hơn
        )

        // Nút quay lại
        Icon(
            painter = painterResource(id = R.drawable.back),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 27.dp, start = 16.dp)
                .size(50.dp)
                .clickable {
                    viewModel.resetState()
                    navController.popBackStack()
                },
            tint = Color.Black
        )

        // Toàn bộ nội dung chính, cho phép cuộn trên màn hình nhỏ
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 80.dp)
                .verticalScroll(scrollState), // Thêm khả năng cuộn
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ... (Phần giao diện tiêu đề BoxDecor) ...
            BoxDecor(viền = 5.dp, màu = Color(0xFF00BCD4), bo = 24.dp, pad = 10.dp) {
                BoxDecor(
                    viền = 4.dp,
                    màu = Color(0xFFBAB7B7),
                    bo = 16.dp,
                    nền = Color(0xFFE9E9E9),
                    pad = 16.dp
                ) {
                    Text("Mã xác thực OTP", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(40.dp))

            // Nội dung khung OTP
            BoxDecor(
                viền = 5.dp,
                bo = 24.dp,
                màu = Color(0xFF00BCD4),
                nền = Color.White,
                pad = 24.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        // Sửa để hiển thị đúng state.phoneNumber
                        "Nhập mã gồm 6 chữ số được gửi đến\n${state.phoneNumber}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(24.dp))

                    // ✅ OTP Input Field
                    OTPInputField(
                        otp = state.otpCode,
                        onOtpChange = {
                            // Chỉ cho phép nhập số và tối đa 6 ký tự
                            if (it.all(Char::isDigit) && it.length <= 6) {
                                viewModel.onOtpCodeChange(it)
                            }
                        }
                    )

                    Spacer(Modifier.height(32.dp))

                    // ✅ Nút xác minh
                    Button(
                        onClick = viewModel::verifyOtp,
                        enabled = !state.isLoading && state.otpCode.length == 6, // Kiểm tra length state.otpCode
                        modifier = Modifier
                            .fillMaxWidth(0.8f) // Dùng 80% chiều rộng thay vì 70%
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00BCD4)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Xác minh", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Hiển thị lỗi
                    if (state.error != null) {
                        Text(
                            text = "Lỗi: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ... (Phần gửi lại mã) ...
            BoxDecor(
                viền = 3.dp,
                màu = Color(0xFF00BCD4),
                bo = 12.dp,
                pad = 8.dp,
                clickable = { /* viewModel.resendOtp() nếu cần */ },
                modifier = Modifier
                    .width(130.dp) // Tăng nhẹ kích thước
            ) {
                Text("Gửi lại mã", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(50.dp)) // Thêm khoảng cách ở cuối để cuộn lên được
        }
    }
}

// -------------------- HỖ TRỢ GIAO DIỆN -------------------- //
// Chỉnh sửa OTPInputField để các ô co giãn (dùng weight)

@Composable
fun OTPInputField(otp: String, onOtpChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        // Hàng 6 ô OTP
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween // Dùng SpaceBetween để phân bố đều
        ) {
            repeat(6) { i ->
                val c = otp.getOrNull(i)?.toString().orEmpty()
                val chọn = i == otp.length
                Box(
                    // Sử dụng Modifier.weight(1f) để các ô chia đều không gian
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f) // Giữ tỷ lệ 1:1 cho ô vuông
                        .padding(horizontal = 4.dp) // Thêm padding ngang nhỏ
                        .VienChamCham(
                            2.dp,
                            if (chọn) Color(0xFF00BCD4) else Color(0xFFCCCCCC),
                            12.dp
                        )
                        .background(
                            if (c.isNotEmpty()) Color(0xFFE0F7FA) else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        c,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (c.isNotEmpty()) Color(0xFF00BCD4) else Color.Gray
                    )
                }
            }
        }

        // BasicTextField ẩn để xử lý input
        BasicTextField(
            value = otp,
            onValueChange = onOtpChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .alpha(0.01f) // Làm cho TextField gần như vô hình
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent)
        )
    }
}

// Giữ nguyên BoxDecor và VienChamCham
@Composable
fun BoxDecor(
    modifier: Modifier = Modifier,
    viền: Dp = 4.dp,
    màu: Color = Color(0xFF00BCD4),
    bo: Dp = 24.dp,
    nền: Color = Color.White,
    pad: Dp = 24.dp,
    height: Dp? = null,
    clickable: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier)
            .VienChamCham(viền, màu, bo)
            .background(nền, RoundedCornerShape(bo))
            .then(if (clickable != null) Modifier.clickable { clickable() } else Modifier)
            .padding(pad),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

fun Modifier.VienChamCham(độDày: Dp, màu: Color, bo: Dp) = drawBehind {
    drawRoundRect(
        color = màu,
        style = Stroke(
            width = độDày.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 10f))
        ),
        cornerRadius = CornerRadius(bo.toPx())
    )
}