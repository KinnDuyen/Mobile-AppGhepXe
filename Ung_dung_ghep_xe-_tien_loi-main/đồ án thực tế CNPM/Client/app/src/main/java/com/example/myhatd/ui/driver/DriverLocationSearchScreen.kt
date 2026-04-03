package com.example.myhatd.ui.driver // ✅ ĐÚNG PACKAGE

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
//import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myhatd.data.model.NominatimResult
import com.example.myhatd.viewmodel.LocationSearchViewModel
import com.example.myhatd.viewmodel.MapViewModel
import com.example.myhatd.viewmodel.TripRequestState
import com.example.myhatd.ui.customer.SearchField // DÙNG CHUNG ENUM
import com.example.myhatd.R // Giả sử R.drawable
import com.example.myhatd.ui.navigation.NavigationRoutes // ✅ Import logic điều hướng

// ✅ Đổi tên Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLocationSearchScreen(
    navController: NavController,
    phoneNumber: String,
    role: String, // Giá trị này phải là "DRIVER"
    viewModel: LocationSearchViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current

    // Toàn bộ State logic từ File logic
    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLatLng = mapUiState.lastKnownLocation

    val originSearchText by viewModel.originSearchText.collectAsState()
    val destinationSearchText by viewModel.destinationSearchText.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingLocation by viewModel.isLoadingLocation.collectAsState()
    val activeSearchField by viewModel.activeSearchField.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val selectedOriginLatLng by viewModel.selectedOriginLatLng.collectAsState()
    val selectedDestinationLatLng by viewModel.selectedDestinationLatLng.collectAsState()
    val tripRequestStatus by viewModel.tripRequestStatus.collectAsState() // Giữ lại theo logic file

    val currentSearchText = when (activeSearchField) {
        SearchField.ORIGIN -> originSearchText
        SearchField.DESTINATION -> destinationSearchText
        SearchField.NONE -> ""
    }

    val phoneNumberUI = phoneNumber
    val roleUI = role

    // =======================================================
    // ÁP DỤNG UI (TỪ FILE KHÁCH HÀNG)
    // =======================================================
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.nentaoyeucauchuyendi),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // Top bar tùy chỉnh
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp, bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backicon),
                    contentDescription = "Quay lại",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .clickable {
                            navController.popBackStack() // Logic từ file
                        },
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Tìm kiếm Tuyến đường (Tài xế)", // ✅ Tiêu đề mới
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // 1. Ô NHẬP LIỆU ĐIỂM ĐI (Logic file + UI)
            OutlinedTextField(
                value = originSearchText,
                onValueChange = { viewModel.onSearchTextChange(it, SearchField.ORIGIN) },
                label = { Text("Điểm đi") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đi") },
                trailingIcon = { // Logic từ file
                    if (activeSearchField == SearchField.ORIGIN && isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else if (originSearchText.isNotEmpty() && activeSearchField == SearchField.ORIGIN && !isLoadingLocation) {
                        IconButton(onClick = { viewModel.onSearchTextChange("", SearchField.ORIGIN) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setActiveSearchField(SearchField.ORIGIN) },
                colors = OutlinedTextFieldDefaults.colors( // UI
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp) // UI
            )

            Spacer(modifier = Modifier.height(8.dp))

            // SỬ DỤNG VỊ TRÍ HIỆN TẠI (Logic file + UI đã khắc phục)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = { viewModel.useCurrentLocation(context, userLatLng) },
                        enabled = !isLoadingLocation
                    )
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoadingLocation) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
//                    Icon(Icons.Filled.MyLocation, contentDescription = "Vị trí hiện tại", tint = Color(0xFF1976D2))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isLoadingLocation) "Đang lấy địa chỉ..." else "Sử dụng vị trí hiện tại",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isLoadingLocation) Color.Gray else Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold
                )
            }
            HorizontalDivider(color = Color(0xFF3085E0).copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Ô NHẬP LIỆU ĐIỂM ĐẾN (Logic file + UI)
            OutlinedTextField(
                value = destinationSearchText,
                onValueChange = { viewModel.onSearchTextChange(it, SearchField.DESTINATION) },
                label = { Text("Điểm đến") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đến") },
                trailingIcon = { // Logic từ file
                    if (activeSearchField == SearchField.DESTINATION && isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else if (destinationSearchText.isNotEmpty() && activeSearchField == SearchField.DESTINATION) {
                        IconButton(onClick = { viewModel.onSearchTextChange("", SearchField.DESTINATION) }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setActiveSearchField(SearchField.DESTINATION) },
                colors = OutlinedTextFieldDefaults.colors( // UI
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp) // UI
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =======================================================
            // NÚT CHÍNH (LOGIC MỚI TỪ FILE)
            // =======================================================
            Button(
                onClick = {
                    val origin = selectedOriginLatLng
                    val destination = selectedDestinationLatLng

                    // Kiểm tra an toàn: Đảm bảo cả hai đều không null
                    if (origin != null && destination != null) {

                        // ✅ ĐIỀU HƯỚNG SANG MÀN HÌNH HẸN GIỜ
                        navController.navigate(
                            NavigationRoutes.createHenGioDriverRoute(
                                // ⭐ BỔ SUNG THÔNG TIN TÀI XẾ
                                phoneNumber = phoneNumberUI, // Lấy từ tham số
                                role = roleUI,           // Lấy từ tham số

                                // Tên địa điểm
                                tenDiemDi = originSearchText,
                                tenDiemDen = destinationSearchText,
                                // Tọa độ
                                viDoDiemDi = origin.latitude,
                                kinhDoDiemDi = origin.longitude,
                                viDoDiemDen = destination.latitude,
                                kinhDoDiemDen = destination.longitude
                            )
                        )
                    }
                },
                // Điều kiện ENABLE từ file
                enabled = selectedOriginLatLng != null && selectedDestinationLatLng != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp) // UI
            ) {
                Text("Chọn Giờ Khởi hành (Tiếp tục)") // ✅ Text mới
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =======================================================
            // HIỂN THỊ KẾT QUẢ TÌM KIẾM (Logic file + UI)
            // =======================================================
            if (activeSearchField != SearchField.NONE && searchResults.isNotEmpty() && !isLoadingLocation) {
                Text(
                    text = "Gợi ý cho ${if (activeSearchField == SearchField.ORIGIN) "Điểm đi" else "Điểm đến"}:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color.Gray)

                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(searchResults, key = { it.place_id }) { result ->
                        // Sử dụng UI LocationResultItem đã tạo kiểu
                        LocationResultItem(
                            result = result,
                            onClick = {
                                viewModel.selectLocation(result, activeSearchField)
                            }
                        )
                    }
                }
            } else if (searchError != null) {
                Text(
                    text = searchError!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            } else if (activeSearchField != SearchField.NONE && !isLoading && currentSearchText.length > 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy địa điểm nào khớp.",
                        color = Color.DarkGray
                    )
                }
            } else if (activeSearchField == SearchField.NONE) {
                // Giữ nguyên UI phần này
                Text(
                    text = "Điểm đến gần đây",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        // Lấy text logic từ file
                        text = "Chọn Điểm đi hoặc Điểm đến, và nhập địa chỉ để tìm kiếm tự động.",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // =======================================================
    // XỬ LÝ PHẢN HỒI (Alert Dialogs) - Giữ nguyên từ File logic
    // =======================================================
    when (val status = tripRequestStatus) {
        is TripRequestState.Loading -> {
            AlertDialog(
                onDismissRequest = { /* Không cho dismiss khi đang loading */ },
                title = { Text("Đang xử lý...") },
                text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
                confirmButton = {}
            )
        }
        is TripRequestState.Success -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetTripRequestStatus() },
                title = { Text("Thành công! 🎉") },
                text = {
                    val id = status.response?.requestId ?: "N/A"
                    Text("Yêu cầu của bạn (ID: $id) đã được gửi thành công. Vui lòng chờ tài xế xác nhận.")
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.resetTripRequestStatus()
                        navController.popBackStack()
                    }) {
                        Text("Đóng")
                    }
                }
            )
        }
        is TripRequestState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.resetTripRequestStatus() },
                title = { Text("Lỗi Gửi Yêu Cầu ⚠️") },
                text = { Text(status.error) },
                confirmButton = {
                    Button(onClick = { viewModel.resetTripRequestStatus() }) {
                        Text("Thử lại")
                    }
                }
            )
        }
        else -> Unit
    }
}

// =======================================================
// HELPER COMPOSABLE (Sử dụng UI đã tạo kiểu)
// =======================================================
@Composable
fun LocationResultItem(result: NominatimResult, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)) // Style từ UI
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                Icons.Default.Place, // Style từ UI
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = result.display_name, // Data từ logic file
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                color = Color.Black
            )
        }
    }
}