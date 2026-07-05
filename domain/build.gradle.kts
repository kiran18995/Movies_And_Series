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

        // mockk is JVM-only — keep it out of commonTest to avoid
        // "Could Not Resolve" errors on iosArm64/iosX64/iosSimulatorArm64
        jvmTest.dependencies {
            implementation(libs.junit)
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
