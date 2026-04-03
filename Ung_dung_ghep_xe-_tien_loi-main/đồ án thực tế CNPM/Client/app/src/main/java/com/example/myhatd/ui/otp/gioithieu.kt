package com.example.myhatd.ui.otp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.myhatd.R

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*

import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun GioiThieuScreen(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("intro") {
            popUpTo("gioithieu") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // ảnh trong res/drawable/
            contentDescription = "Hình giới thiệu",
            modifier = Modifier
                .size(550.dp)
                .padding(top = 200.dp,),
            contentScale = ContentScale.Fit
        )
        Text("Form",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 150.dp,top = 800.dp)
        )
        Text("HATD",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF2A5EE1),
            modifier = Modifier
                .padding(start = 195.dp, top = 795.dp )
        )
    }
}