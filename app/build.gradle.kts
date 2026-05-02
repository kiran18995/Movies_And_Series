import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kiran.movie"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kiran.movie"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val baseUrl = extra.properties["BASE_URL"] as String? ?: "https://api.themoviedb.org/3/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val baseImageUrl = extra.properties["BASE_IMAGE_URL"] as String? ?: "https://image.tmdb.org/t/p/w500"
        buildConfigField("String", "BASE_IMAGE_URL", "\"$baseImageUrl\"")
        val accessToken = extra.properties["API_READ_ACCESS_TOKEN"] as String? ?: "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZTZlYjBkNTE3OWY3MDMxYWNmMzE0ZGI4ZTQxMTJhOSIsIm5iZiI6MTcxOTI1ODUwMi42NTkxMzIsInN1YiI6IjVjYTFiMmNkOTI1MTQxMWExODA4ZDEyZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xjTYKsio_c1M2zxwtfdpOgSyGthyjnsvzRh3ifbBFYg"
        buildConfigField("String", "API_READ_ACCESS_TOKEN", "\"$accessToken\"")
    }

    signingConfigs {
        create("release") {
            // NOTE: Add these values to your local.properties file:
            // RELEASE_STORE_FILE=/path/to/your/keystore.jks
            // RELEASE_STORE_PASSWORD=your_store_password
            // RELEASE_KEY_ALIAS=your_key_alias
            // RELEASE_KEY_PASSWORD=your_key_password
            
            val keystorePropertiesFile = rootProject.file("local.properties")
            if (keystorePropertiesFile.exists()) {
                val properties = Properties()
                properties.load(FileInputStream(keystorePropertiesFile))
                
                val storeFilePath = properties.getProperty("RELEASE_STORE_FILE")
                if (storeFilePath != null) {
                    storeFile = rootProject.file(storeFilePath)
                    storePassword = properties.getProperty("RELEASE_STORE_PASSWORD")
                    keyAlias = properties.getProperty("RELEASE_KEY_ALIAS")
                    keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD")
                }
            }
        }
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
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
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

    // Project Modules
    implementation(project(":core:common"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:movies"))
    implementation(project(":feature:tvshows"))
    implementation(project(":feature:saved"))

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

    // Compose dependencies
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.androidx.paging.compose)
    debugImplementation(libs.androidx.ui.tooling)

    //leak canary
    debugImplementation(libs.leakcanary.android)
}