plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")

    // ✅ Áp dụng 3 plugin
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.myhatd"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myhatd"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
}

dependencies {
    // (Tất cả các thư viện Compose, Firebase, Networking... của bạn)
    // --- 1. COMPOSE CƠ BẢN (Quản lý qua BOM) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.foundation:foundation")
    // --- 2. COMPOSE KHÔNG THUỘC BOM ---
    implementation("androidx.navigation:navigation-compose:2.8.0-beta05")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0-beta01")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.8")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    // --- 3. FIREBASE & GOOGLE SERVICES ---
    implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
//    implementation("com.google.android.gms:play-services-maps:18.2.0")
//    implementation("com.google.maps.android:maps-compose:4.4.1")
    implementation("com.google.maps.android:android-maps-utils:3.4.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.libraries.places:places:3.4.0")
    // --- 4. NETWORKING & COROUTINES ---
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("io.ktor:ktor-client-core:2.3.11")
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.maplibre.gl:android-sdk:12.3.0-pre0")
// Các dependency khác (giữ nguyên nếu đã có)
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material:material:1.5.0")
    implementation("androidx.activity:activity-compose:1.7.0")

    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
// Cho FusedLocationProviderClient

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.navigation:navigation-compose:2.7.2")

    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-android:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")

    implementation("com.mapbox.mapboxsdk:mapbox-sdk-geojson:5.8.0")
    implementation("androidx.compose.ui:ui-android:1.5.4")
    // --- 5. TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ===============================================
    // ✅ THƯ VIỆN SOCKET (KHÔNG CÓ RXKOTLIN)
    // ===============================================
    implementation(libs.stomp.android)
    implementation(libs.rxjava2)
    implementation(libs.rxandroid2)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // HILT
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
}

kapt {
    correctErrorTypes = true
}

apply(plugin = "com.google.gms.google-services")