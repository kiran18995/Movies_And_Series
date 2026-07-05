plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.kiran.movie.core.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        val baseImageUrl = "https://image.tmdb.org/t/p/w500"
        buildConfigField("String", "BASE_IMAGE_URL", "\"$baseImageUrl\"")
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.recyclerview)
    api(libs.androidx.paging.runtime.ktx)
    api(libs.coil)
    api(libs.shimmer)
    api(project(":domain"))
    api(project(":shared"))

    val composeBom = platform(libs.compose.bom)
    api(composeBom)
    api(libs.androidx.ui)
    api(libs.androidx.ui.graphics)
    api(libs.androidx.ui.tooling.preview)
    api(libs.androidx.material3)
    api(libs.androidx.activity.compose)
    api("androidx.compose.material:material-icons-extended:1.6.8")
    api(libs.coil.compose)
    api(libs.androidx.paging.compose)
    debugApi(libs.androidx.ui.tooling)
}
