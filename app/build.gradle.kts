plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kiran.movie"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kiran.movie"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val baseUrl = extra.properties["BASE_URL"] as String? ?: "https://api.themoviedb.org/3/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val baseImageUrl = extra.properties["BASE_IMAGE_URL"] as String? ?: "https://image.tmdb.org/t/p/w500"
        buildConfigField("String", "BASE_IMAGE_URL", "\"$baseImageUrl\"")
        val accessToken = extra.properties["API_READ_ACCESS_TOKEN"] as String? ?: "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZTZlYjBkNTE3OWY3MDMxYWNmMzE0ZGI4ZTQxMTJhOSIsIm5iZiI6MTcxOTI1ODUwMi42NTkxMzIsInN1YiI6IjVjYTFiMmNkOTI1MTQxMWExODA4ZDEyZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xjTYKsio_c1M2zxwtfdpOgSyGthyjnsvzRh3ifbBFYg"
        buildConfigField("String", "API_READ_ACCESS_TOKEN", "\"$accessToken\"")
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dagger Hilt dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit + Gson dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    // Paging 3
    implementation(libs.androidx.paging.runtime.ktx)

    // Shimmer
    implementation(libs.shimmer)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Swipe Refresh Layout
    implementation(libs.androidx.swiperefreshlayout)

    // Toasty
    implementation(libs.toasty)

    // Coil
    implementation(libs.coil)

    //leak canary
    debugImplementation(libs.leakcanary.android)

}