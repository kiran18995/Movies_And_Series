plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.androidx.paging.common)
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.room.common)
            api(libs.kotlinx.serialization.json)
        }
        
        commonTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
