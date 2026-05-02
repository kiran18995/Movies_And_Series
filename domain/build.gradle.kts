plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    api(libs.androidx.paging.common.ktx)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.common)
    implementation(libs.gson)
    implementation("javax.inject:javax.inject:1")

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}
