//package com.example.myhatd.ui.customer
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Clear
////import androidx.compose.material.icons.filled.MyLocation
//import androidx.compose.material.icons.filled.Place
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.myhatd.data.model.NominatimResult
//import com.example.myhatd.viewmodel.LocationSearchViewModel
//import com.example.myhatd.viewmodel.MapViewModel
//import com.example.myhatd.viewmodel.TripRequestState
//// Giả sử R.drawable nằm trong package này, nếu không hãy thay đổi cho phù hợp
//import com.example.myhatd.R
//import java.net.URLEncoder
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.myhatd.MyApplication
//import com.example.myhatd.viewmodel.FindingRideViewModel
//import com.example.myhatd.viewmodel.FindingRideViewModelFactory
//import org.maplibre.android.geometry.LatLng
//
//// Enum từ File 1
//enum class SearchField { ORIGIN, DESTINATION, NONE }
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LocationSearchScreen(
//    navController: NavController,
//    phoneNumber: String,
//    role: String,
//    viewModel: LocationSearchViewModel = viewModel(),
//    mapViewModel: MapViewModel = viewModel()
//) {
//    val context = LocalContext.current
//    val application = context.applicationContext as MyApplication
//    val findingRideFactory = FindingRideViewModelFactory(application.matchRepository)
//    // Lấy instance của FindingRideViewModel
//    val findingRideViewModel: FindingRideViewModel = viewModel(factory = findingRideFactory)
//
//
//    // Toàn bộ State logic từ File 1
//    val mapUiState by mapViewModel.uiState.collectAsState()
//    val userLatLng = mapUiState.lastKnownLocation
//
//    val originSearchText by viewModel.originSearchText.collectAsState()
//    val destinationSearchText by viewModel.destinationSearchText.collectAsState()
//    val searchResults by viewModel.searchResults.collectAsState()
//    val isLoading by viewModel.isLoading.collectAsState()
//    val isLoadingLocation by viewModel.isLoadingLocation.collectAsState()
//    val activeSearchField by viewModel.activeSearchField.collectAsState()
//    val searchError by viewModel.searchError.collectAsState()
//
//    val selectedOriginLatLng by viewModel.selectedOriginLatLng.collectAsState()
//    val selectedDestinationLatLng by viewModel.selectedDestinationLatLng.collectAsState()
//
//    val tripRequestStatus by viewModel.tripRequestStatus.collectAsState()
//
//    val currentSearchText = when (activeSearchField) {
//        SearchField.ORIGIN -> originSearchText
//        SearchField.DESTINATION -> destinationSearchText
//        SearchField.NONE -> ""
//    }
//
//    val phoneNumberUI = phoneNumber
//    val roleUI = role
//
//    Box(
//        modifier = Modifier.fillMaxSize()
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.nentaoyeucauchuyendi),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
//                .alpha(0.9f),
//            contentScale = ContentScale.Crop
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp)
//        ) {
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(top = 26.dp, bottom = 16.dp)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.backicon),
//                    contentDescription = "Quay lại",
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .size(40.dp)
//                        .clickable {
//                            navController.popBackStack()
//                        },
//                    contentScale = ContentScale.Fit
//                )
//
//                Text(
//                    text = "Tìm kiếm Tuyến đường",
//                    color = Color(0xFF1976D2),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 22.sp,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//
//            // =======================================================
//            // 1. Ô NHẬP LIỆU ĐIỂM ĐI
//            // =======================================================
//            OutlinedTextField(
//                value = originSearchText,
//                onValueChange = { viewModel.onSearchTextChange(it, SearchField.ORIGIN) },
//                label = { Text("Điểm đi") },
//                singleLine = true,
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đi") },
//                trailingIcon = {
//                    if (activeSearchField == SearchField.ORIGIN && isLoading) {
//                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
//                    } else if (originSearchText.isNotEmpty() && activeSearchField == SearchField.ORIGIN && !isLoadingLocation) {
//                        IconButton(onClick = { viewModel.onSearchTextChange("", SearchField.ORIGIN) }) {
//                            Icon(Icons.Filled.Clear, contentDescription = "Xóa")
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    // ✅ KHẮC PHỤC 1: Đã xóa .height(56.dp) để chữ không bị khuất
//                    .clickable { viewModel.setActiveSearchField(SearchField.ORIGIN) },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF3085E0),
//                    unfocusedBorderColor = Color(0xFF3085E0),
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White
//                ),
//                shape = RoundedCornerShape(30.dp)
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // =======================================================
//            // ✅ KHẮC PHỤC 2: SỬ DỤNG VỊ TRÍ HIỆN TẠI
//            // Trả về dạng Row đơn giản như File 1, không dùng style TextField
//            // =======================================================
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clickable(
//                        onClick = { viewModel.useCurrentLocation(context, userLatLng) },
//                        enabled = !isLoadingLocation
//                    )
//                    // Thêm padding để dễ nhấn hơn và hài hòa
//                    .padding(vertical = 12.dp, horizontal = 8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                if (isLoadingLocation) {
//                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
//                } else {
//                    // Sử dụng màu xanh cho đồng bộ
////                    Icon(Icons.Filled.MyLocation, contentDescription = "Vị trí hiện tại", tint = Color(0xFF1976D2))
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Text(
//                    if (isLoadingLocation) "Đang lấy địa chỉ..." else "Sử dụng vị trí hiện tại",
//                    style = MaterialTheme.typography.bodyLarge,
//                    // Dùng màu xanh cho đồng bộ, và in đậm
//                    color = if (isLoadingLocation) Color.Gray else Color(0xFF1976D2),
//                    fontWeight = FontWeight.Bold
//                )
//            }
//            // Thêm đường kẻ ngang như File 1
//            HorizontalDivider(color = Color(0xFF3085E0).copy(alpha = 0.5f))
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // =======================================================
//            // 2. Ô NHẬP LIỆU ĐIỂM ĐẾN
//            // =======================================================
//            OutlinedTextField(
//                value = destinationSearchText,
//                onValueChange = { viewModel.onSearchTextChange(it, SearchField.DESTINATION) },
//                label = { Text("Điểm đến") },
//                singleLine = true,
//                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đến") },
//                trailingIcon = {
//                    if (activeSearchField == SearchField.DESTINATION && isLoading) {
//                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
//                    } else if (destinationSearchText.isNotEmpty() && activeSearchField == SearchField.DESTINATION) {
//                        IconButton(onClick = { viewModel.onSearchTextChange("", SearchField.DESTINATION) }) {
//                            Icon(Icons.Filled.Clear, contentDescription = "Xóa")
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    // ✅ KHẮC PHỤC 1: Đã xóa .height(56.dp) để chữ không bị khuất
//                    .clickable { viewModel.setActiveSearchField(SearchField.DESTINATION) },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = Color(0xFF3085E0),
//                    unfocusedBorderColor = Color(0xFF3085E0),
//                    focusedContainerColor = Color.White,
//                    unfocusedContainerColor = Color.White
//                ),
//                shape = RoundedCornerShape(30.dp)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // =======================================================
//            // NÚT XÁC NHẬN YÊU CẦU CHUYẾN ĐI
//            // =======================================================
//            Button(
//                onClick = {
//                    // 1. Gửi API yêu cầu tìm chuyến
//                    viewModel.sendTripRequest(phoneNumberUI, roleUI)
//
//                    findingRideViewModel.resetMatchState()
//
//                    // 2. Mã hóa địa chỉ điểm đi để truyền qua Navigation
//                    // Đảm bảo rằng originSearchText không phải là null hoặc rỗng
//                    val originToEncode = originSearchText
//                    val encodedOrigin = URLEncoder.encode(originToEncode, "UTF-8")
//
//                    // 3. Điều hướng: Gửi cả Địa chỉ và SĐT
//                    navController.navigate("xac_nhan_diem_don/$encodedOrigin/$phoneNumberUI")
//                },
//                enabled = selectedOriginLatLng != null && selectedDestinationLatLng != null && tripRequestStatus != TripRequestState.Loading,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                shape = RoundedCornerShape(30.dp)
//            ) {
//                Text(
//                    text = if (tripRequestStatus == TripRequestState.Loading) "Đang gửi Yêu cầu..." else "Xác nhận Yêu cầu Chuyến đi"
//                )
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // =======================================================
//            // HIỂN THỊ KẾT QUẢ TÌM KIẾM
//            // =======================================================
//            if (activeSearchField != SearchField.NONE && searchResults.isNotEmpty() && !isLoadingLocation) {
//                Text(
//                    text = "Gợi ý cho ${if (activeSearchField == SearchField.ORIGIN) "Điểm đi" else "Điểm đến"}:",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = Color.Black,
//                    fontWeight = FontWeight.Bold
//                )
//                HorizontalDivider(Modifier.padding(vertical = 4.dp), color = Color.Gray)
//
//                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
//                    items(searchResults, key = { it.place_id }) { result ->
//                        LocationResultItem(
//                            result = result,
//                            onClick = {
//                                viewModel.selectLocation(result, activeSearchField)
//                            }
//                        )
//                    }
//                }
//            } else if (searchError != null) {
//                Text(
//                    text = searchError!!,
//                    modifier = Modifier.padding(16.dp),
//                    color = MaterialTheme.colorScheme.error
//                )
//            } else if (activeSearchField != SearchField.NONE && !isLoading && currentSearchText.length > 2) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(Color(0xFFE3F2FD))
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Không tìm thấy địa điểm nào khớp.",
//                        color = Color.DarkGray
//                    )
//                }
//            } else if (activeSearchField == SearchField.NONE) {
//                Text(
//                    text = "Điểm đến gần đây",
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                    fontSize = 20.sp,
//                    modifier = Modifier.padding(start = 20.dp, top = 20.dp)
//                )
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(60.dp)
//                        .clip(RoundedCornerShape(12.dp))
//                        .background(Color(0xFFE3F2FD)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text(
//                        text = "Chọn Điểm đi/đến để bắt đầu tìm kiếm",
//                        color = Color.Gray,
//                        fontSize = 15.sp
//                    )
//                }
//            }
//        }
//    }
//
//    // =======================================================
//    // XỬ LÝ PHẢN HỒI (Alert Dialogs) - Giữ nguyên từ File 1
//    // =======================================================
//    when (val status = tripRequestStatus) {
//        is TripRequestState.Loading -> {
//            AlertDialog(
//                onDismissRequest = { /* Không cho dismiss khi đang loading */ },
//                title = { Text("Đang xử lý...") },
//                text = { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) },
//                confirmButton = {}
//            )
//        }
////        is TripRequestState.Success -> {
////            AlertDialog(
////                onDismissRequest = { viewModel.resetTripRequestStatus() },
////                title = { Text("Thành công! 🎉") },
////                text = {
////                    val id = status.response?.requestId ?: "N/A"
////                    Text("Yêu cầu của bạn (ID: $id) đã được gửi thành công. Vui lòng chờ tài xế xác nhận.")
////                },
////                confirmButton = {
////                    Button(onClick = {
////                        viewModel.resetTripRequestStatus()
////                        navController.popBackStack()
////                    }) {
////                        Text("Đóng")
////                    }
////                }
////            )
////        }
//        is TripRequestState.Error -> {
//            AlertDialog(
//                onDismissRequest = { viewModel.resetTripRequestStatus() },
//                title = { Text("Lỗi Gửi Yêu Cầu ⚠️") },
//                text = { Text(status.error) },
//                confirmButton = {
//                    Button(onClick = { viewModel.resetTripRequestStatus() }) {
//                        Text("Thử lại")
//                    }
//                }
//            )
//        }
//        else -> Unit // Idle: Không làm gì cả
//    }
//}
//
//// Giữ nguyên Composable này
//@Composable
//fun LocationResultItem(result: NominatimResult, onClick: () -> Unit) {
//    Card(
//        shape = RoundedCornerShape(12.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clickable(onClick = onClick),
//        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            modifier = Modifier.padding(12.dp)
//        ) {
//            Icon(
//                Icons.Default.Place,
//                contentDescription = null,
//                tint = Color(0xFF1976D2),
//                modifier = Modifier.size(28.dp)
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Text(
//                text = result.display_name,
//                style = MaterialTheme.typography.bodyLarge,
//                maxLines = 2,
//                color = Color.Black
//            )
//        }
//    }
//}

package com.example.myhatd.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn // ✅ Giữ lại import này
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
// Giả sử R.drawable nằm trong package này, nếu không hãy thay đổi cho phù hợp
import com.example.myhatd.R
import java.net.URLEncoder
// import androidx.lifecycle.viewmodel.compose.viewModel // Đã có ở trên, không cần import lại
import com.example.myhatd.MyApplication
import com.example.myhatd.viewmodel.FindingRideViewModel
import com.example.myhatd.viewmodel.FindingRideViewModelFactory
import org.maplibre.android.geometry.LatLng

// Enum từ File 1
enum class SearchField { ORIGIN, DESTINATION, NONE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchScreen(
    navController: NavController,
    phoneNumber: String,
    role: String,
    viewModel: LocationSearchViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as MyApplication
    val findingRideFactory = FindingRideViewModelFactory(application.matchRepository)
    // Lấy instance của FindingRideViewModel
    val findingRideViewModel: FindingRideViewModel = viewModel(factory = findingRideFactory)


    // Toàn bộ State logic từ File 1
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

    val tripRequestStatus by viewModel.tripRequestStatus.collectAsState()

    val currentSearchText = when (activeSearchField) {
        SearchField.ORIGIN -> originSearchText
        SearchField.DESTINATION -> destinationSearchText
        SearchField.NONE -> ""
    }

    val phoneNumberUI = phoneNumber
    val roleUI = role

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
                            navController.popBackStack()
                        },
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Tìm kiếm Tuyến đường",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // =======================================================
            // 1. Ô NHẬP LIỆU ĐIỂM ĐI
            // =======================================================
            OutlinedTextField(
                value = originSearchText,
                onValueChange = { viewModel.onSearchTextChange(it, SearchField.ORIGIN) },
                label = { Text("Điểm đi") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đi") },
                trailingIcon = {
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // =======================================================
            // SỬ DỤNG VỊ TRÍ HIỆN TẠI
            // =======================================================
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
                    // ✅ THAY THẾ BẰNG ICON TỒN TẠI
                    Icon(Icons.Default.LocationOn, contentDescription = "Vị trí hiện tại", tint = Color(0xFF1976D2))
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

            // =======================================================
            // 2. Ô NHẬP LIỆU ĐIỂM ĐẾN
            // =======================================================
            OutlinedTextField(
                value = destinationSearchText,
                onValueChange = { viewModel.onSearchTextChange(it, SearchField.DESTINATION) },
                label = { Text("Điểm đến") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Tìm điểm đến") },
                trailingIcon = {
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // =======================================================
            // NÚT XÁC NHẬN YÊU CẦU CHUYẾN ĐI
            // =======================================================
            Button(
                onClick = {
                    // 1. Gửi API yêu cầu tìm chuyến
                    viewModel.sendTripRequest(phoneNumberUI, roleUI)

                    findingRideViewModel.resetMatchState()

                    // 2. Mã hóa địa chỉ điểm đi để truyền qua Navigation
                    val originToEncode = originSearchText
                    val encodedOrigin = URLEncoder.encode(originToEncode, "UTF-8")

                    // 3. Điều hướng: Gửi cả Địa chỉ và SĐT
                    navController.navigate("xac_nhan_diem_don/$encodedOrigin/$phoneNumberUI")
                },
                enabled = selectedOriginLatLng != null && selectedDestinationLatLng != null && tripRequestStatus != TripRequestState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text(
                    text = if (tripRequestStatus == TripRequestState.Loading) "Đang gửi Yêu cầu..." else "Xác nhận Yêu cầu Chuyến đi"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =======================================================
            // HIỂN THỊ KẾT QUẢ TÌM KIẾM
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
                        text = "Chọn Điểm đi/đến để bắt đầu tìm kiếm",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // =======================================================
    // XỬ LÝ PHẢN HỒI (Alert Dialogs) - Giữ nguyên từ File 1
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
        else -> Unit // Idle: Không làm gì cả
    }
}

// Giữ nguyên Composable này
@Composable
fun LocationResultItem(result: NominatimResult, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = result.display_name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                color = Color.Black
            )
        }
    }
}