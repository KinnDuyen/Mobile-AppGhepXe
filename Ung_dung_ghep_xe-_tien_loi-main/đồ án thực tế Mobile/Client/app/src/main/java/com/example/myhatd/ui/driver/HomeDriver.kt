package com.example.myhatd.ui.driver

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myhatd.viewmodel.DriverMatchViewModel
import com.example.myhatd.viewmodel.MapViewModel
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeDriverScreen(
    navController: NavController,
    viewModel: DriverMatchViewModel,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }
    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLocation = mapUiState.lastKnownLocation
    val driverPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        mapViewModel.setLocationPermission(isGranted)
        if (isGranted) mapViewModel.startLocationUpdates(context)
    }

    LaunchedEffect(driverPhoneNumber) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapViewModel.setLocationPermission(true)
            mapViewModel.startLocationUpdates(context)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (driverPhoneNumber.isNotEmpty()) viewModel.startFindingRide(driverPhoneNumber)
    }

    LaunchedEffect(mapLibreMap, userLocation) {
        if (mapLibreMap != null && userLocation != null) {
            addOrUpdateMarker(map = mapLibreMap, currentMarker = currentMarker, onMarkerUpdate = { currentMarker = it }, latLng = userLocation, name = "Vị trí của bạn")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MapLibreComposable(
            modifier = Modifier.fillMaxWidth().height(250.dp),
            userLocation = userLocation,
            onMapReady = { mapLibreMap = it }
        )
    }
}