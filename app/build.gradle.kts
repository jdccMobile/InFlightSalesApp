plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jdmobile.inflightsalesapp"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.jdmobile.inflightsalesapp"
        minSdk = 31
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // Koin
    implementation (libs.koin.android)
    implementation (libs.koin.androidx.navigation)
    implementation (libs.koin.androidx.compose)
    // Navigation
    implementation (libs.androidx.navigation.compose)
    // Material Icons
    implementation(libs.material.icons)
    implementation(libs.material.icons.extended)
    // Coil
    implementation(libs.coil.compose)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    // Arrow
    implementation(libs.arrow.core)
    // Room
    implementation(libs.room.common)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
}