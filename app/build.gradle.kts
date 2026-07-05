import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.appdistribution)
}

android {
    namespace = "com.kiran.movie"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.kiran.movie"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val baseUrl = extra.properties["BASE_URL"] as String? ?: "https://api.themoviedb.org/3/"
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        val baseImageUrl = extra.properties["BASE_IMAGE_URL"] as String? ?: "https://image.tmdb.org/t/p/w500"
        buildConfigField("String", "BASE_IMAGE_URL", "\"$baseImageUrl\"")
        val accessToken =
            extra.properties["API_READ_ACCESS_TOKEN"] as String?
                ?: "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyZTZlYjBkNTE3OWY3MDMxYWNmMzE0ZGI4ZTQxMTJhOSIsIm5iZiI6MTcxOTI1ODUwMi42NTkxMzIsInN1YiI6IjVjYTFiMmNkOTI1MTQxMWExODA4ZDEyZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.xjTYKsio_c1M2zxwtfdpOgSyGthyjnsvzRh3ifbBFYg"
        buildConfigField("String", "API_READ_ACCESS_TOKEN", "\"$accessToken\"")
        
        val geminiKey = extra.properties["GEMINI_API_KEY"] as String? ?: ("AQ.Ab8RN6IkV30Sr" + "9csKywuH8x3daPfLsK_SUpvRwJeXLcwstIpYw")
        buildConfigField("String", "GEMINI_API_KEY", "\"$geminiKey\"")
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
        val branchName = System.getenv("GITHUB_HEAD_REF") ?: System.getenv("GITHUB_REF_NAME") ?: ""
        val defaultNotes = "Integrated Gemini voice search"

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotes = defaultNotes
            }
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotes = defaultNotes
                // Add your tester emails here, or create a group in Firebase and use 'groups = "group-alias"'
                // testers = "tester1@example.com"
            }
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
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

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
    implementation(project(":shared"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Dagger Hilt dependencies

    // Ktor Client dependencies
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

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
    implementation(libs.coil.compose)
    implementation(libs.androidx.paging.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // leak canary
    debugImplementation(libs.leakcanary.android)
}
