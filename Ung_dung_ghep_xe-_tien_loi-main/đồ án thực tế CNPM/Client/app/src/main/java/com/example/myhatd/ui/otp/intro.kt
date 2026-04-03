package com.example.myhatd.ui.otp.intro

import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import kotlinx.coroutines.delay

// ------------------------- DATA MODEL (GIỮ NGUYÊN) -------------------------
data class IntroPageData(
    val imageRes: Int,
    val title: String,
    val desc: String
)

private val introPages = listOf(
    IntroPageData(
        imageRes = R.drawable.bg1,
        title = "Chào Mừng đến với HATD!",
        desc = "Cùng chúng tôi đồng hành, mọi chuyến đi của bạn sẽ trở nên đơn giản hơn, tiết kiệm hơn và tràn đầy kết nối tuyệt vời."
    ),
    IntroPageData(
        imageRes = R.drawable.bg2,
        title = "Cùng đi, cùng chia sẻ",
        desc = "Cùng nhau di chuyển, chia sẻ những khoảnh khắc ý nghĩa và tạo nên những kỷ niệm trên suốt hành trình."
    ),
    IntroPageData(
        imageRes = R.drawable.bg1,
        title = "Đúng giờ",
        desc = "Cùng nhau di chuyển, đến nơi đúng giờ – Mỗi hành trình đều trân trọng thời gian của bạn!"
    ),
    IntroPageData(
        imageRes = R.drawable.bg1,
        title = "Thông minh và Tiết kiệm",
        desc = "Di chuyển thông minh, tiết kiệm hơn – lựa chọn di chuyển thông minh hơn cùng HATD."
    ),
    IntroPageData(
        imageRes = R.drawable.bg1,
        title = "Một chạm",
        desc = "Một chạm để kết nối, một chạm để thanh toán – Cùng nhau chia sẻ mọi hành trình."
    )
)

// ------------------------- TỔNG HỢP PAGER (MAIN SCREEN) -------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IntroScreen(navController: NavController) {
    val pageCount = introPages.size
    val pagerState = rememberPagerState(pageCount = { pageCount })

    // 1. Tự động chuyển trang MƯỢT MÀ hơn
    LaunchedEffect(pagerState.currentPage) {
        delay(3000)
        val nextPage = (pagerState.currentPage + 1) % pageCount
        pagerState.animateScrollToPage(
            page = nextPage,
            // Đặt thời gian chuyển động 800ms để tạo cảm giác mượt mà
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // BACKGROUND CỐ ĐỊNH
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // LOGO CỐ ĐỊNH (Top-Start)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo HATD",
            modifier = Modifier
                .size(120.dp) // Giảm size logo một chút cho không gian
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // ------------------------- CỘT NỘI DUNG CHÍNH (RESPONSIVE) -------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 100.dp, bottom = 10.dp), // Padding tổng thể
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // KHU VỰC ẢNH VÀ MÔ TẢ (SẼ LÀ PAGER)
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f) // Chiếm trọng số lớn nhất
            ) { page ->
                // Nội dung động bên trong Pager
                IntroContent(data = introPages[page])
            }

            // DOTS INDICATOR (Phần nhỏ)
            DotsIndicator(
                totalDots = pageCount,
                selectedIndex = pagerState.currentPage,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // NÚT LOGIN
            OutlinedButton(
                onClick = { navController.navigate("phone_auth") },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF4ABDE0)),
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 40.dp)
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
            ) {
                Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // FOOTER
            Row(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("From", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(4.dp))
                Text("HATD", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF2A5EE1))
            }
        }
    }
}

// ------------------------- NỘI DUNG TRANG ĐỘNG (RESPONSIVE) -------------------------
@Composable
fun IntroContent(data: IntroPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. HÌNH ẢNH (Chiếm 40% không gian còn lại)
        Image(
            painter = painterResource(id = data.imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .weight(0.4f) // Trọng số: 40%
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(32.dp))

        // 2. TIÊU ĐỀ (Chiếm 10% không gian còn lại)
        Text(
            text = data.title,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.1f) // Trọng số: 10%
        )

        Spacer(Modifier.height(16.dp))

        // 3. MÔ TẢ (Chiếm 50% không gian còn lại)
        Text(
            text = data.desc,
            color = Color.Black,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f) // Trọng số: 50%
        )
    }
}

// ------------------------- DOTS INDICATOR (GIỮ NGUYÊN) -------------------------
@Composable
fun DotsIndicator(totalDots: Int, selectedIndex: Int, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(totalDots) { index ->
            val color = if (index == selectedIndex) Color(0xFF4FC3F7) else Color.LightGray
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (index == selectedIndex) 10.dp else 8.dp)
                    .background(color, shape = CircleShape)
            )
        }
    }
}