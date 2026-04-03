package com.example.myhatd.ui.driver

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.data.model.DriverInfoRequest
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.viewmodel.DriverViewModel
import kotlinx.coroutines.launch

@Composable
fun DangKyHatdScreen(
    navController: NavController,
    driverViewModel: DriverViewModel,
    phoneNumber: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- State Management ---
    var hangXe by remember { mutableStateOf("") }
    var bienSo by remember { mutableStateOf("") }
    var gioiTinh by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var anhChanDungMoRong by remember { mutableStateOf(false) }
    var gioiTinhMoRong by remember { mutableStateOf(false) }

    // ✅ STATE MỚI: Kiểm soát Dialog thành công
    var showSuccessDialog by remember { mutableStateOf(false) }

    // --- Logic Xử lý Đăng ký (Local Function) ---
    fun xuLyDangKy() {
        if (hangXe.isBlank() || bienSo.isBlank() || gioiTinh.isBlank()) {
            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val request = DriverInfoRequest(
            phoneNumber = phoneNumber,
            hangXe = hangXe,
            bienSo = bienSo,
            gioiTinh = gioiTinh
        )

        Log.d("DriverRequest", "Sending request: $request")

        scope.launch {
            isLoading = true
            try {
                val response = driverViewModel.saveDriverInfo(request)
                Log.d("DriverRequest", "Response: ${response.code()}, success: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    // ✅ THAY THẾ TOAST BẰNG VIỆC BẬT DIALOG
                    showSuccessDialog = true

                    // Logic điều hướng sẽ được chuyển vào Dialog
                } else {
                    Toast.makeText(
                        context,
                        "Lỗi từ server: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("DriverRequest", "Exception sending request", e)
                Toast.makeText(
                    context,
                    "Không gửi được request: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                isLoading = false
            }
        }
    }

    // --- UI Layout ---
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.nendangkyhatd),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tiêu đề
            Text(
                text = "Quy trình đăng ký HATDBIKE\n(SĐT: $phoneNumber)",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = (35).dp)
                    .border(3.dp, Color(0xFF4ABDE0), RoundedCornerShape(12.dp))
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))

            // Box chứa Card và Ảnh chồng lên nhau
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Card trắng chính
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                        .padding(top = 125.dp)
                        .border(3.dp, Color(0xFF4ABDE0), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .padding(top = 70.dp, bottom = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Thông báo chào mừng
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFD4D4D4), RoundedCornerShape(12.dp))
                                .border(2.dp, Color(0xFF4CDFE6), RoundedCornerShape(12.dp))
                                .padding(5.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Chào mừng bạn trở thành tài xế của HATD",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF333333),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Hãy đảm bảo thông tin xe chính xác, tuân thủ luật giao thông và luôn giữ thái độ thân thiện với hành khách. Cùng HATD mang đến những chuyến đi an toàn và đáng tin cậy.",
                                    fontSize = 14.sp,
                                    color = Color(0xFF000000),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Ảnh Chân Dung
                        MucMoRong(
                            tieuDe = "Ảnh Chân Dung",
                            daMoRong = anhChanDungMoRong,
                            khiBamVao = { anhChanDungMoRong = !anhChanDungMoRong },
                            chieuDaiGachXanh = 135f
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Biển số xe (bienSo)
                        OutlinedTextField(
                            value = bienSo,
                            onValueChange = { bienSo = it },
                            label = { Text("Biển số xe") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4ABDE0),
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedLabelColor = Color(0xFF000000),
                                unfocusedLabelColor = Color(0xFF666666),
                                cursorColor = Color(0xFF000000),
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Giới tính Dropdown (gioiTinh)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = { gioiTinhMoRong = !gioiTinhMoRong },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        ) {
                            OutlinedTextField(
                                value = gioiTinh,
                                onValueChange = { },
                                label = { Text("Giới tính") },
                                readOnly = true,
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFF333333),
                                    disabledLabelColor = if (gioiTinh.isEmpty()) Color(0xFF666666) else Color(0xFF000000),
                                    disabledTextColor = Color(0xFF000000)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    Text(
                                        text = if (gioiTinhMoRong) "▲" else "▼",
                                        fontSize = 16.sp,
                                        color = Color(0xFF666666),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            )
                            DropdownMenu(
                                expanded = gioiTinhMoRong,
                                onDismissRequest = { gioiTinhMoRong = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(2.dp, Color(0xFF4ABDE0), RoundedCornerShape(12.dp))
                            ) {
                                val genderOptions = listOf(
                                    "Nam" to "♂",
                                    "Nữ" to "♀",
                                    "Khác" to "⚥"
                                )

                                genderOptions.forEachIndexed { index, (gTinh, icon) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Start
                                            ) {
                                                Text(
                                                    text = icon,
                                                    fontSize = 20.sp,
                                                    color = Color(0xFF4ABDE0),
                                                    modifier = Modifier.padding(end = 12.dp)
                                                )
                                                Text(
                                                    text = gTinh,
                                                    fontSize = 16.sp,
                                                    fontWeight = if (gioiTinh == gTinh) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (gioiTinh == gTinh) Color(0xFF4ABDE0) else Color(0xFF333333)
                                                )
                                            }
                                        },
                                        onClick = {
                                            gioiTinh = gTinh
                                            gioiTinhMoRong = false
                                        },
                                        modifier = Modifier
                                            .background(
                                                if (gioiTinh == gTinh) Color(0xFFE8F8FA) else Color.Transparent
                                            )
                                            .padding(vertical = 4.dp)
                                    )
                                    if (index < genderOptions.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            thickness = 1.dp,
                                            color = Color(0xFFE0E0E0)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hãng xe (hangXe)
                        OutlinedTextField(
                            value = hangXe,
                            onValueChange = { hangXe = it },
                            label = { Text("Hãng xe") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4ABDE0),
                                unfocusedBorderColor = Color(0xFF333333),
                                focusedLabelColor = Color(0xFF000000),
                                unfocusedLabelColor = Color(0xFF666666),
                                cursorColor = Color(0xFF000000),
                                focusedTextColor = Color(0xFF000000),
                                unfocusedTextColor = Color(0xFF000000)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Nút Đăng ký (Gọi hàm cục bộ)
                        Button(
                            onClick = { xuLyDangKy() }, // ✅ Gọi hàm cục bộ
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = 20.dp)
                                .height(90.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25C8CD)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Text(
                                    text = "Đăng ký",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Ảnh minh họa - đè lên Card
                Image(
                    painter = painterResource(id = R.drawable.dangkyhatd),
                    contentDescription = "HATD Illustration",
                    modifier = Modifier
                        .width(362.dp)
                        .height(250.dp)
                        .offset(x = 0.dp, y = -80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.TopCenter),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ✅ DIALOG THÔNG BÁO ĐĂNG KÝ THÀNH CÔNG
    if (showSuccessDialog) {
        HopThoaiDangKyThanhCong(
            onSuccessAction = {
                showSuccessDialog = false
                // ✅ LOGIC ĐIỀU HƯỚNG VÀ XÓA BACK STACK
                navController.navigate(NavigationRoutes.HOME_DRIVER) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }
        )
    }
}

// ----------------------------------------------------------------------------------
// ✅ COMPOSABLE DIALOG MỚI (CHUYÊN NGHIỆP)
// ----------------------------------------------------------------------------------

@Composable
fun HopThoaiDangKyThanhCong(onSuccessAction: () -> Unit) {
    Dialog(onDismissRequest = onSuccessAction) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                // Thêm đổ bóng nhẹ
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(20.dp), ambientColor = Color(0xFF4CAF50).copy(alpha = 0.5f)),
            // Dùng màu xanh lá cây nhạt làm nền để thể hiện sự thành công
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 30.dp, horizontal = 24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon thành công lớn (Màu xanh lá cây)
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00796B)), // Màu xanh lá đậm hơn
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.ThumbUp, // Icon ngón tay cái, thân thiện
                        contentDescription = "Thành công",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tiêu đề: Lớn, đậm, màu xanh lá cây
                Text(
                    text = "Đăng Ký Thành Công!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color(0xFF00796B), // Màu xanh đậm nổi bật
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hồ sơ của bạn đã được ghi nhận. Chào mừng bạn gia nhập đội ngũ Tài xế HATD!",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = onSuccessAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    // Màu nút là màu xanh lá chính
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Text("Bắt đầu", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------------
// CÁC COMPOSABLE PHỤ TRỢ (GIỮ NGUYÊN)
// ----------------------------------------------------------------------------------

@Composable
fun MucMoRong(
    tieuDe: String,
    daMoRong: Boolean,
    khiBamVao: () -> Unit,
    chieuDaiGachXanh: Float = 160f
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = khiBamVao,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tieuDe,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier
                    .offset(x = 8.dp)
                    .drawBehind {
                        val doLeY = -7.dp.toPx()
                        val doLeX = 0.dp.toPx()
                        val banKinhGoc = 8.dp.toPx()

                        val chieuDaiGachXanhPx = chieuDaiGachXanh.dp.toPx()
                        val chieuDaiGachDen = 330.dp.toPx()

                        // Gạch ngang xanh với bo góc
                        drawRoundRect(
                            color = Color(0xFF01A6AA),
                            topLeft = Offset(doLeX, size.height + doLeY - 2.dp.toPx()),
                            size = Size(chieuDaiGachXanhPx, 6.dp.toPx()),
                            cornerRadius = CornerRadius(banKinhGoc, banKinhGoc)
                        )

                        // Gạch ngang đen phía dưới
                        drawRoundRect(
                            color = Color(0xFF000000),
                            topLeft = Offset(doLeX, size.height + doLeY + 6.dp.toPx()),
                            size = Size(chieuDaiGachDen, 2.dp.toPx()),
                            cornerRadius = CornerRadius(banKinhGoc / 2, banKinhGoc / 2)
                        )
                    }
            )

            // Icon ảnh thay thế dấu cộng
            Image(
                painter = painterResource(id = R.drawable.cong),
                contentDescription = if (daMoRong) "Thu gọn" else "Mở rộng",
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
        }
    }
}