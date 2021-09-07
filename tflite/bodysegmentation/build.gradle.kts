import dependencies.Dependencies.KOTLIN_STD
import dependencies.implementationFor

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    val config = ModulesConfigurationManager.getConfigByProjectName(project.name,project.rootDir)

    compileSdk = AndroidConfig.COMPILE_SDK_VERSION
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementationFor(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))),
        KOTLIN_STD,
        project(":visionai-core"),
        project(":tflite:shared")
    )
}
