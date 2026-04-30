import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.hilt)
}

// Load local.properties at the top level so the token is available in defaultConfig
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.kiran.movie.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        val baseUrl = "https://api.themoviedb.org/3/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")

        val accessToken = localProperties.getProperty("API_READ_ACCESS_TOKEN")
            ?: error(
                "API_READ_ACCESS_TOKEN is not set in local.properties. " +
                "Add it as: API_READ_ACCESS_TOKEN=<your_tmdb_read_access_token>"
            )
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
}
