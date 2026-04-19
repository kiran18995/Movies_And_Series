plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
    implementation(libs.androidx.paging.common.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.common)
    implementation(libs.gson)
    implementation("javax.inject:javax.inject:1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}
