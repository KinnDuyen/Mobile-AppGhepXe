package com.example.myhatd.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhatd.R
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.layout.ContentScale
import com.example.myhatd.viewmodel.FindingRideViewModel // ✅ Import ViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.navigation.NavController // ✅ Import NavController
import com.example.myhatd.ui.navigation.NavigationRoutes // ✅ Import NavigationRoutes

// ✅ Đổi tên Composable và nhận tham số cần thiết
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReviewScreen(
    navController: NavController,
    matchId: Long, // ✅ Nhận Match ID từ Navigation
    viewModel: FindingRideViewModel, // ✅ Nhận ViewModel để gửi API
) {
    // State quản lý số sao người dùng chọn
    var rating by remember { mutableStateOf(5) } // Mặc định 5 sao cho UX tốt hơn
    var selectedCompliments by remember { mutableStateOf(setOf<String>()) }
    var isSending by remember { mutableStateOf(false) } // State gửi loading

    val context = LocalContext.current
    val compliments = listOf("Thân thiện", "Cẩn thận", "Đúng giờ", "Thái độ tốt", "Sạch sẽ", "Đồng phục gọn gàng")

    // --- LOGIC XỬ LÝ GỬI ĐÁNH GIÁ ---
    val onSubmitReview = {
        isSending = true
        viewModel.submitReview(
            matchId = matchId,
            rating = rating,
            compliments = selectedCompliments,
            onSuccess = {
                isSending = false
                Toast.makeText(context, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show()
                // ✅ Điều hướng về màn hình chính (HOME) sau khi đánh giá thành công
                navController.navigate(NavigationRoutes.HOME) {
                    // Xóa tất cả màn hình khỏi stack, bao gồm cả ReviewScreen
                    popUpTo(NavigationRoutes.HOME) { inclusive = true }
                }
            },
            onError = {
                isSending = false
                Toast.makeText(context, "Lỗi: Gửi đánh giá thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5FAFE))
            .verticalScroll(rememberScrollState()) // ✅ Thêm scroll
            .padding(16.dp)
    ) {

        // Thanh tiêu đề có nút “X” đóng
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Nút "X" ở góc trái
            Text(
                text = "X",
                color = Color.Gray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable {
                        // Cho phép đóng và điều hướng về Home nếu người dùng không muốn đánh giá
                        navController.navigate(NavigationRoutes.HOME) {
                            popUpTo(NavigationRoutes.HOME) { inclusive = true }
                        }
                    }
                    .padding(start = 8.dp)
            )

            // Tiêu đề căn giữa
            Text(
                text = "Đánh giá tài xế",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF000000),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        //  Thông tin tài xế
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

                // Ảnh nền trong card tài xế
                Image(
                    painter = painterResource(id = R.drawable.nenthongtindriver),
                    contentDescription = "Background Driver",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Avatar, xe máy và tên tài xế
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box {
                            // Avatar tài xế
                            Image(
                                painter = painterResource(id = R.drawable.avtdriver),
                                contentDescription = "Ảnh tài xế",
                                modifier = Modifier
                                    .size(80.dp)
                                    .offset(x = (-8).dp)
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

                        // Tên tài xế và đánh giá sao (Giả định giá trị cứng)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Hứa Anh Tới Đón", // ⚠️ Cần truyền tên thật
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

                // Biển số xe + tên xe (Giả định giá trị cứng)
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
                            text = "59TA-113.15", // ⚠️ Cần truyền biển số thật
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF0081F1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "EVO.HATD CYAN", // ⚠️ Cần truyền hãng xe thật
                        fontSize = 12.sp,
                        color = Color(0xFF007ACC)
                    )
                }

                // Logo góc dưới phải
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

        // Khung đánh giá tổng thể
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //KHUNG NÉT ĐỨT bao quanh 3 phần: thông điệp, sao và lời khen
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 6f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 14f))
                            )
                            drawRoundRect(
                                color = Color.Black,
                                style = stroke,
                                cornerRadius = CornerRadius(48f, 48f)
                            )
                        }
                        .padding(15.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Hộp thông điệp gợi ý
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF0081F1))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "HATD luôn lắng nghe điều bạn nói.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Có điều gì bạn muốn nhắn gửi đến HATD không?\n" +
                                        "Chia sẻ ngay cảm nghĩ của bạn giúp chúng tôi mang đến\n" +
                                        "những chuyến đi ngày càng tuyệt hơn.",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // HÀNG SAO ĐÁNH GIÁ
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2196F3), RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 1..5) {
                                IconButton(onClick = { rating = i }) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (i <= rating) R.drawable.sao else R.drawable.saorong
                                        ),
                                        contentDescription = "Sao $i",
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // LỜI KHEN
                    Text(
                        text = "Gửi lời khen của bạn",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Hàng chứa các “chip” lời khen
                    FlowRow(modifier = Modifier.fillMaxWidth()) {
                        compliments.forEach { compliment ->
                            val isSelected = compliment in selectedCompliments
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isSelected) Color(0xFF0081F1) else Color(0xFFF0F0F0),
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                                    .clickable {
                                        selectedCompliments =
                                            if (isSelected) selectedCompliments - compliment
                                            else selectedCompliments + compliment
                                    }
                            ) {
                                Text(
                                    text = compliment,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // NÚT GỬI ĐI
                    Button(
                        onClick = onSubmitReview, // ✅ GỌI HÀM SUBMIT
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0081F1)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSending, // Vô hiệu hóa khi đang gửi
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(top = 40.dp)
                    ) {
                        if (isSending) {
                            // ✅ Hiển thị Loading khi đang gửi
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Text(
                                text = "Gửi đi",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}