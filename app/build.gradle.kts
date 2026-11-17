plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.buriktool"
    compileSdk = 34

    defaultConfig {
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions += "game"
    productFlavors {
        create("ffmax") {
            dimension = "game"
            applicationId = "com.example.buriktool.ffmax"
            buildConfigField("String", "GAME_PKG", "\"com.dts.freefiremax\"")
            buildConfigField("String", "GAME_NAME", "\"Free Fire MAX\"")
            resValue("string", "app_name", "Burik FF MAX")
        }
        create("ffth") {
            dimension = "game"
            applicationId = "com.example.buriktool.ffth"
            buildConfigField("String", "GAME_PKG", "\"com.dts.freefireth\"")
            buildConfigField("String", "GAME_NAME", "\"Free Fire\"")
            resValue("string", "app_name", "Burik FF")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
