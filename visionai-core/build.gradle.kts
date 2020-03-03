import dependencies.Dependencies.KOTLIN_STD
import dependencies.Dependencies
import dependencies.Dependencies.X_APP_COMPACT
import dependencies.Dependencies.X_CORE_KTX
import dependencies.TestDependencies.JUNIT4
import dependencies.implementationFor
import dependencies.testImplementationFor

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    val config = ModulesConfigurationManager.getConfigByProjectName(project.name)

    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    defaultConfig {
        minSdkVersion(AndroidConfig.MIN_SDK_VERSION)
        targetSdkVersion(AndroidConfig.TARGET_SDK_VERSION)
        versionCode = config.versionCode
        versionName = config.versionName
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
        *Dependencies.getJcodecGroup(),
        *Dependencies.getCoroutinesGroup(),
        X_APP_COMPACT,
        X_CORE_KTX
    )
}
