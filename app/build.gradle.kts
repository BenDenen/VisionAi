import dependencies.Dependencies
import dependencies.Dependencies.KOTLIN_STD
import dependencies.Dependencies.MATERIAL
import dependencies.Dependencies.X_APP_COMPACT
import dependencies.Dependencies.X_CONSTRAINT_LAYOUT
import dependencies.Dependencies.X_COORDINATOR_LAYOUT
import dependencies.Dependencies.X_LIFECYCLE_VIEWMODEL
import dependencies.Dependencies.X_RECYCLER_VIEW
import dependencies.TestDependencies.JUNIT4
import dependencies.TestDependencies.TEST_RUNNER
import dependencies.implementationFor
import dependencies.testImplementationFor

plugins {
    id("com.android.application")
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

    buildFeatures {
        // Enables Jetpack Compose for this module
        compose = true
    }

    compileOptions {
        setSourceCompatibility(JavaVersion.VERSION_1_8)
        setTargetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    aaptOptions {
        noCompress("tflite")
    }
    composeOptions{
        kotlinCompilerExtensionVersion = Dependencies.Versions.COMPOSE_VERSION
    }
}

dependencies {
    implementationFor(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))),
        KOTLIN_STD,
        *Dependencies.getKoinGroup(),
        X_APP_COMPACT,
        X_LIFECYCLE_VIEWMODEL,
        *Dependencies.getKtxGroup(),
        X_RECYCLER_VIEW,
        X_CONSTRAINT_LAYOUT,
        X_COORDINATOR_LAYOUT,
        MATERIAL,
        *Dependencies.getComposeGroup(),
        project(":visionai-core"),
        project(":tflite:styletransfer"),
        project(":tflite:bodysegmentation")
    )

    testImplementationFor(
        JUNIT4,
        TEST_RUNNER
    )
}
