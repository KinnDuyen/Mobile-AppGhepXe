package com.example.myhatd.ui.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myhatd.MyApplication
import com.example.myhatd.viewmodel.FindingRideViewModel
import com.example.myhatd.viewmodel.FindingRideViewModelFactory

@Composable
fun RideInfoScreen(navController: NavController,
                   viewModel: FindingRideViewModel) {

    val matchResult by viewModel.matchResult.collectAsState()

    // Lấy thông tin cần thiết
    val driverName = matchResult?.tenDriver ?: "Đang tải..."
    val carInfo = "${matchResult?.hangXe ?: ""} (${matchResult?.bienSoXe ?: ""})"

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tài xế đang đến!",
                fontSize = 24.sp
            )
            Text(
                text = "Tài xế: $driverName",
                fontSize = 18.sp
            )
            Text(
                text = "Xe: $carInfo",
                fontSize = 18.sp
            )
            // TODO: Thêm nút Hủy chuyến
        }
    }
}