package com.example.myhatd.ui.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myhatd.data.network.RetrofitClient
import com.example.myhatd.data.storage.TokenManager
import com.example.myhatd.repository.AuthRepository
import com.example.myhatd.repository.DriverRepository
import com.example.myhatd.ui.driver.DangKyHatdScreen
import com.example.myhatd.ui.driver.HoSoDriverScreen
import com.example.myhatd.ui.driver.HomeDriverScreen
import com.example.myhatd.ui.home.HomeUserScreen
import com.example.myhatd.ui.otp.*
import com.example.myhatd.ui.otp.intro.IntroScreen
import com.example.myhatd.ui.customer.*
import com.example.myhatd.viewmodel.AuthViewModel
import com.example.myhatd.viewmodel.ChuyenDiViewModel
import com.example.myhatd.viewmodel.DriverViewModel
import com.example.myhatd.viewmodel.MapViewModel
import com.example.myhatd.viewmodel.UserViewModel
import com.example.myhatd.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.myhatd.ui.driver.DriverHenGioScreen
import com.example.myhatd.ui.customer.LocationSearchScreen
import com.example.myhatd.viewmodel.LocationSearchViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myhatd.ui.driver.DriverLocationSearchScreen
import java.net.URLDecoder
import com.example.myhatd.ui.driver.TripData
import com.example.myhatd.viewmodel.FindingRideViewModel
import androidx.compose.ui.platform.LocalContext // <-- Cần
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Cần
import com.example.myhatd.MyApplication // <-- Cần
import com.example.myhatd.ui.driver.ChiTietChuyenDiScreen
import com.example.myhatd.ui.driver.DriverRideTrackingScreen
import com.example.myhatd.ui.driver.DuDoanScreen
import com.example.myhatd.viewmodel.FindingRideViewModelFactory // <-- Cần
import com.example.myhatd.viewmodel.DriverMatchViewModel
import com.example.myhatd.viewmodel.DriverMatchViewModelFactory
import com.example.myhatd.repository.RoutingRepository // ✅ IMPORT MỚI


fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

    val application = context.applicationContext as MyApplication
    val apiService = RetrofitClient.apiService

    // --- KHỞI TẠO REPOSITORIES ---
    val matchRepository = application.matchRepository
    val authRepository = remember { AuthRepository(apiService = apiService) }
    val driverRepository = remember { DriverRepository(apiService = apiService) }
    val tokenManager = remember { TokenManager(context = context) }

    // ✅ 1. KHỞI TẠO ROUTING REPOSITORY
    // RoutingRepository chỉ cần ApiService
    val routingRepository = remember {
        RoutingRepository(apiService = apiService)
    }

    val mapViewModel: MapViewModel = viewModel()

    // --- KHỞI TẠO FACTORIES VÀ VIEWMODEL CHUNG ---
    val findingRideFactory = remember { FindingRideViewModelFactory(matchRepository) }

    // ✅ 2. CẬP NHẬT FACTORY ĐỂ TRUYỀN routingRepository
    val driverMatchFactory = remember {
        DriverMatchViewModelFactory(
            matchRepository = matchRepository,
            routingRepository = routingRepository,// <-- ĐÃ THÊM
            mapViewModel = mapViewModel
        )
    }

    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(tokenManager = tokenManager)
    )
    val isLoggedIn by mainViewModel.isUserLoggedIn

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            activity = activity,
            authRepository = authRepository,
            tokenManager = tokenManager
        )
    )

    val authState by authViewModel.state.collectAsStateWithLifecycle()

    val userViewModel: UserViewModel = viewModel<UserViewModel>(
        factory = UserViewModel.Factory(authRepository)
    )
    val currentPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

    // Tải dữ liệu người dùng khi navigation khởi tạo
    LaunchedEffect(currentPhoneNumber) {
        if (currentPhoneNumber.isNotEmpty()) {
            userViewModel.loadUser(currentPhoneNumber)
        }
    }
    val user by userViewModel.userData

    // ✅ DÙNG ROUTE CƠ BẢN CHO START DESTINATION
    val startDestination = if (isLoggedIn) {
        NavigationRoutes.GIOI_THIEU
    } else {
        NavigationRoutes.GIOI_THIEU
    }

    val findingRideViewModel: FindingRideViewModel = viewModel(factory = findingRideFactory)
    val driverMatchViewModel: DriverMatchViewModel = viewModel(factory = driverMatchFactory)
    // ✅ KHAI BÁO MAPVIEWMODEL CHUNG
//    val mapViewModel: MapViewModel = viewModel() // Sử dụng viewModel() để giữ thể hiện (instance)


    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(NavigationRoutes.PHONE_AUTH) {
            PhoneAuthScreen(navController = navController, viewModel = authViewModel)
        }
        composable(NavigationRoutes.VERIFY_OTP) {
            VerifyOtpScreen(navController = navController, viewModel = authViewModel)
        }
        composable(NavigationRoutes.THONG_TIN_USER) {
            xacnhanotpScreen(navController = navController, viewModel = authViewModel)
        }
        composable(NavigationRoutes.HOME) {
            // ✅ DÙNG mapViewModel ĐÃ KHAI BÁO BÊN NGOÀI
            HomeUserScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }

        // --- (Mã cũ đã được comment) ---

        composable(NavigationRoutes.GIOI_THIEU) {
            GioiThieuScreen(navController = navController)
        }
        composable(NavigationRoutes.DU_DOAN_DRIVER) {
            // ✅ LẤY HOẶC TẠO CHUYENDIVIEWMODEL TẠI ĐÂY
            val dedicatedChuyenDiViewModel: ChuyenDiViewModel = viewModel()

            // Nếu bạn đã có ChuyenDiViewModel khởi tạo ở ngoài NavHost, bạn dùng nó.

            // DuDoanScreen cần ChuyenDiViewModel để lấy scheduledRideState
            DuDoanScreen(
                navController = navController,
                chuyenDiViewModel = dedicatedChuyenDiViewModel // TRUYỀN VM CÓ scheduledRide
            )
        }
        composable(NavigationRoutes.INTRO) {
            IntroScreen(navController = navController)
        }
        composable(NavigationRoutes.THONG_BAO_USER) {
            ThongBaoScreen(navController = navController)
        }
        composable(NavigationRoutes.HOME_DRIVER) {
            // ✅ TRUYỀN VM DRIVER ĐÃ CHIA SẺ
            HomeDriverScreen(
                navController = navController,
                viewModel = driverMatchViewModel // Thêm tham số viewModel vào HomeDriverScreen
            )
        }

        composable(NavigationRoutes.DANG_KY_DRIVER) {
            val phoneNumber = authState.phoneNumber
            val driverViewModel: DriverViewModel = viewModel(
                factory = DriverViewModel.Factory(driverRepository = driverRepository)
            )

            DangKyHatdScreen(
                navController = navController,
                phoneNumber = phoneNumber,
                driverViewModel = driverViewModel
            )
        }

        composable(
            route = "xac_nhan_diem_don/{diemDon}/{phoneNumber}",
            arguments = listOf(
                navArgument("diemDon") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val diemDonEncoded = backStackEntry.arguments?.getString("diemDon") ?: "Điểm đón chưa chọn"
            val phoneNumberArg = backStackEntry.arguments?.getString("phoneNumber") ?: currentPhoneNumber

            // ✅ SỬA ĐỔI: Xử lý Decode an toàn hơn
            val diemDonText = try {
                URLDecoder.decode(diemDonEncoded, "UTF-8")
            } catch (e: Exception) {
                // Nếu decode lỗi, dùng chuỗi đã encode hoặc giá trị mặc định
                diemDonEncoded
            }

            // ✅ DÙNG mapViewModel ĐÃ KHAI BÁO BÊN NGOÀI
            XacNhanDiemDonScreen(
                navController = navController,
                mapViewModel = mapViewModel,
                diemDonText = diemDonText, // <-- Dùng chuỗi đã decode an toàn
                currentPhoneNumber = phoneNumberArg
            )
        }

        composable(NavigationRoutes.HO_SO_USER) {
            HoSoUserScreen(
                navController = navController,
                viewModel = userViewModel,
                phoneNumber = currentPhoneNumber
            )
        }

        composable(NavigationRoutes.TIM_DIA_CHI) {
            val phoneNumber = user?.phoneNumber ?: currentPhoneNumber
            val role = user?.role ?: "User"

            val locationSearchViewModel: LocationSearchViewModel = viewModel()
            // ✅ DÙNG mapViewModel ĐÃ KHAI BÁO BÊN NGOÀI

            LocationSearchScreen(
                navController = navController,
                viewModel = locationSearchViewModel,
                mapViewModel = mapViewModel,
                phoneNumber = phoneNumber,
                role = role // Truyền vai trò "User" hoặc giá trị đã load
            )
        }

        composable(NavigationRoutes.TIM_DIA_CHI_DRIVER) {
            val phoneNumber = user?.phoneNumber ?: currentPhoneNumber
            val locationSearchViewModel: LocationSearchViewModel = viewModel()
            // ✅ DÙNG mapViewModel ĐÃ KHAI BÁO BÊN NGOÀI

            DriverLocationSearchScreen(
                navController = navController,
                viewModel = locationSearchViewModel,
                mapViewModel = mapViewModel,
                phoneNumber = phoneNumber,
                role = "DRIVER" // Gán giá trị cứng
            )
        }

        // =======================================================
        // 3. ROUTE HẸN GIỜ (DRIVER) - ĐÃ CẬP NHẬT
        // =======================================================
        composable(
            route = NavigationRoutes.HEN_GIO_DRIVER, // Route này giờ đã có 8 tham số
            arguments = listOf(
                // ✅ BƯỚC 1: BỔ SUNG CÁC ARGUMENTS MỚI
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("role") { type = NavType.StringType },

                // ✅ BƯỚC 2: ĐẢM BẢO TÊN KHỚP VỚI ĐỊNH NGHĨA ROUTE
                navArgument("tenDiemDi") { type = NavType.StringType },
                navArgument("tenDiemDen") { type = NavType.StringType },
                navArgument("viDoDiemDi") { type = NavType.FloatType }, // Sửa tên (bỏ chữ "Di")
                navArgument("kinhDoDiemDi") { type = NavType.FloatType },// Sửa tên
                navArgument("viDoDiemDen") { type = NavType.FloatType }, // Sửa tên (bỏ chữ "Den")
                navArgument("kinhDoDiemDen") { type = NavType.FloatType } // Sửa tên
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments

            // ✅ BƯỚC 3: LẤY CÁC GIÁ TRỊ MỚI
            val phoneNumberArg = args?.getString("phoneNumber") ?: ""
            val roleArg = args?.getString("role") ?: "DRIVER" // Mặc định là DRIVER nếu null

            // Lấy các giá trị cũ (với tên đã sửa)
            val tenDiemDiArg = args?.getString("tenDiemDi") ?: ""
            val tenDiemDenArg = args?.getString("tenDiemDen") ?: ""

            // Lấy tọa độ (nhất quán .getFloat().toDouble())
            val viDoDiemDiArg = args?.getFloat("viDoDiemDi")?.toDouble() ?: 0.0
            val kinhDoDiemDiArg = args?.getFloat("kinhDoDiemDi")?.toDouble() ?: 0.0
            val viDoDiemDenArg = args?.getFloat("viDoDiemDen")?.toDouble() ?: 0.0
            val kinhDoDiemDenArg = args?.getFloat("kinhDoDiemDen")?.toDouble() ?: 0.0

            // Trích xuất và giải mã (decode) tên địa điểm
            val tenDiemDi = URLDecoder.decode(tenDiemDiArg, "UTF-8")
            val tenDiemDen = URLDecoder.decode(tenDiemDenArg, "UTF-8")

            // Tạo đối tượng TripData
            val tripData = TripData(
                tenDiemDi = tenDiemDi,
                tenDiemDen = tenDiemDen,
                viDoDiemDi = viDoDiemDiArg,
                kinhDoDiemDi = kinhDoDiemDiArg,
                viDoDiemDen = viDoDiemDenArg,
                kinhDoDiemDen = kinhDoDiemDenArg
            )

            // Lấy các ViewModel cần thiết
            val chuyenDiViewModel: ChuyenDiViewModel = viewModel()
            // Không cần userViewModelForHenGio, dùng lại userViewModel bên trên

            // ✅ BƯỚC 4: TRUYỀN CÁC THAM SỐ VÀO MÀN HÌNH
            DriverHenGioScreen(
                navController = navController,
                chuyenDiViewModel = chuyenDiViewModel,
                userViewModel = userViewModel, // Dùng lại viewModel đã có
                phoneNumber = phoneNumberArg,  // Truyền số điện thoại
                role = roleArg,                // Truyền vai trò
                tripData = tripData
            )
        }


        composable(NavigationRoutes.HO_SO_DRIVER) {
            val driverViewModel: DriverViewModel = viewModel(
                factory = DriverViewModel.Factory(driverRepository)
            )

            LaunchedEffect(currentPhoneNumber) {
                if (currentPhoneNumber.isNotEmpty()) {
                    driverViewModel.fetchDriver(currentPhoneNumber)
                }
            }

            HoSoDriverScreen(
                navController = navController,
                driverViewModel = driverViewModel,
                userViewModel = userViewModel
            )
        }
        composable(
            // Route phải là DRIVER_RIDE_DETAIL
            route = NavigationRoutes.DRIVER_RIDE_DETAIL,
            arguments = listOf(navArgument("matchId") { type = NavType.LongType })
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId")

            // 🛑 SỬA LỖI: BỎ KHỞI TẠO VM MỚI VÀ DÙNG VM ĐÃ CHIA SẺ CHO DRIVER

            // Xóa/Comment các dòng khởi tạo ViewModel tại đây:
            // val application = context.applicationContext as MyApplication
            // val factory = FindingRideViewModelFactory(application.matchRepository)
            // val findingRideViewModel: FindingRideViewModel = viewModel(factory = factory)

            if (matchId != null) {
                ChiTietChuyenDiScreen(
                    navController = navController,
                    matchId = matchId,
                    // ✅ TRUYỀN VM ĐÃ CHIA SẺ VÀ CÓ DỮ LIỆU
                    viewModel = driverMatchViewModel
                )
            } else {
                Text("Lỗi: Không tìm thấy ID chuyến đi.")
            }
        }


        composable(NavigationRoutes.XAC_NHAN_DAT_XE) {
            // ✅ TRUYỀN VIEWMODEL ĐÃ CÓ
            XacNhanDatXeScreen(
                navController = navController,
                viewModel = findingRideViewModel // <--- TRUYỀN VÀO
            )
        }

        // =======================================================
        // 4. THÊM RIDE_INFO
        // =======================================================
        composable(NavigationRoutes.THEO_DOI_LO_TRINH) {
            // Màn hình hiển thị thông tin Driver/User đã xác nhận
            // Nó cũng sẽ tự lấy ViewModel để truy cập matchResult
            RideTrackingScreen(navController = navController,
                viewModel = findingRideViewModel
            )
        }

        composable(NavigationRoutes.DRIVER_TRACKING) {
            // Màn hình theo dõi lộ trình của Tài xế (hiển thị thông tin Khách hàng)
            DriverRideTrackingScreen(navController = navController,
                viewModel = driverMatchViewModel,
                mapViewModel = mapViewModel
            )
        }

        composable(
            route = NavigationRoutes.REVIEW_SCREEN,
            arguments = listOf(
                navArgument("matchId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val matchId = backStackEntry.arguments?.getLong("matchId")

            if (matchId != null) {
                // ✅ ĐÃ SỬA: TRUYỀN findingRideViewModel VÀO MÀN HÌNH ĐÁNH GIÁ
                ReviewScreen(
                    navController = navController,
                    matchId = matchId,
                    viewModel = findingRideViewModel // <-- ĐÃ THÊM VIEWMDEL
                )
            } else {
                // Hiển thị màn hình lỗi nếu không có Match ID
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Lỗi: Không tìm thấy ID chuyến đi để đánh giá.")
                }
            }
        }

        composable(NavigationRoutes.CHI_TIET_CHUYEN_DI_USER) {
            // Màn hình hiển thị thông tin Driver/User đã xác nhận
            // Nó cũng sẽ tự lấy ViewModel để truy cập matchResult
            ChiTietChuyenDiUserScreen(navController = navController,
                viewModel = findingRideViewModel
            )
        }

        composable(
            route = NavigationRoutes.CHO_SOCKET,
            arguments = listOf(navArgument("userPhone") { type = NavType.StringType })
        ) { backStackEntry ->
            val userPhoneArg = backStackEntry.arguments?.getString("userPhone") ?: currentPhoneNumber

            // ✅ TRUYỀN VIEWMODEL ĐÃ CÓ
            ChoSocketScreen(
                userPhone = userPhoneArg,
                navController = navController,
                viewModel = findingRideViewModel // <--- TRUYỀN VÀO
            )
        }
    }

    // --- LOGIC ĐIỀU HƯỚNG TẬP TRUNG SAU KHI XÁC THỰC THÀNH CÔNG ---
    LaunchedEffect(authState.isOtpSent, authState.isAuthenticated, authState.isInfoSaved) {
        when {
            authState.isInfoSaved -> {
                navController.navigate(NavigationRoutes.HOME) {
                    popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
                }
            }
            authState.isAuthenticated -> {
                navController.navigate(NavigationRoutes.THONG_TIN_USER) {
                    popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
                }
            }
            authState.isOtpSent -> {
                val currentRoute = navController.currentDestination?.route
                if (currentRoute != NavigationRoutes.VERIFY_OTP) {
                    navController.navigate(NavigationRoutes.VERIFY_OTP)
                }
            }
        }
    }
}