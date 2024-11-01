plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.dd3boh.ffMetadataEx"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        externalNativeBuild {
            cmake {
                arguments += listOf("-DCMAKE_SHARED_LINKER_FLAGS=-Wl,--build-id=none")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // proguard or whatever isn't set up
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/cpp/ffmpeg-android-maker/output/lib/")
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    ndkVersion = "27.0.11718014"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
}

dependencies {
    implementation(libs.timber)
}