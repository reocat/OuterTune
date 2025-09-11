plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    testImplementation(libs.junit)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)

    implementation(libs.brotli)
}
