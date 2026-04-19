plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.kiran.movie.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        val baseUrl = "https://api.themoviedb.org/3/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val accessToken = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZTZlYjBkNTE3OWY3MDMxYWNmMzE0ZGI4ZTQxMTJhOSIsIm5iZiI6MTcxOTI1ODUwMi42NTkxMzIsInN1YiI6IjVjYTFiMmNkOTI1MTQxMWExODA4ZDEyZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xjTYKsio_c1M2zxwtfdpOgSyGthyjnsvzRh3ifbBFYg"
        buildConfigField("String", "API_READ_ACCESS_TOKEN", "\"$accessToken\"")
    }
    buildFeatures {
        buildConfig = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(project(":core:common"))
    api(project(":domain"))
    implementation(project(":domain"))
}
