import dependencies.apiFor
import dependencies.implementationFor
import dependencies.Dependencies
import dependencies.TestDependencies
import dependencies.testApiFor

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
        consumerProguardFiles(
            file("proguard-rules.pro")
        )

        externalNativeBuild {
            cmake {
                arguments.add("-DANDROID_STL=c++_static")
                cppFlags.add("-std=c++17 -fsigned-char -frtti")
            }
        }

        ndk {
            abiFilters("arm64-v8a")
        }
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            setPath("CMakeLists.txt")
        }
    }

}

dependencies {
    implementationFor(
        fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))),
        Dependencies.KOTLIN_STD
    )

    apiFor(
        *Dependencies.getCoroutinesGroup()
    )
    testApiFor(
        TestDependencies.JUNIT4
    )
}
