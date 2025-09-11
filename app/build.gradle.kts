import com.android.build.api.dsl.Packaging
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dd3boh.outertune"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dd3boh.outertune"
        minSdk = 26
        targetSdk = 36
        versionCode = 30
        versionName = SimpleDateFormat("yyyyMMdd").format(Date())

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    flavorDimensions.add("distribution")

    productFlavors {
        create("universal") {
            dimension = "distribution"
            isDefault = true
        }

        create("arm64") {
            dimension = "distribution"
            ndk {
                abiFilters.add("arm64-v8a")
            }
        }

        create("arm32") {
            dimension = "distribution"
            ndk {
                abiFilters.add("armeabi-v7a")
            }
        }

        create("x86") {
            dimension = "distribution"
            ndk {
                abiFilters.addAll(listOf("x86", "x86_64"))
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-Xcontext-parameters",
                    "-Xannotation-default-target=param-property",
                    "-opt-in=kotlin.RequiresOptIn"
                )
            )
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    lint {
        lintConfig = file("lint.xml")
        abortOnError = false
        checkReleaseBuilds = false
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    fun Packaging.() {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Core Android & Kotlin
    implementation(libs.concurrent.futures)
    implementation(libs.coroutines.guava)
    implementation(libs.guava)
    implementation(libs.multidex)
    coreLibraryDesugaring(libs.desugaring)

    // Activity & Navigation
    implementation(libs.activity)
    implementation(libs.datastore)
    implementation(libs.hilt.navigation)
    implementation(libs.navigation)

    // Compose UI
    implementation(libs.adaptive)
    implementation(libs.compose.animation)
    implementation(libs.compose.foundation)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.reorderable)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.util)

    // ViewModel & Material
    implementation(libs.material3)
    implementation(libs.materialKolor)
    implementation(libs.palette)
    implementation(libs.viewmodel)
    implementation(libs.viewmodel.compose)

    // Image Loading & Effects
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.shimmer)

    // Media & Audio
    implementation(libs.media3)
    implementation(libs.media3.okhttp)
    implementation(libs.media3.session)
    implementation(libs.media3.workmanager)

    // Database
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // Dependency Injection
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    // Project Modules
    implementation(projects.innertube)
    implementation(projects.kizzy)
    implementation(projects.kugou)
    implementation(projects.lyricsProviders)

    // Networking
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.encoding)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.serialization.json)

    // Audio Metadata
    implementation(libs.taglib)

    // Logging
    implementation(libs.timber)

    // Debug Tools
    debugImplementation(libs.leakcanary)
}