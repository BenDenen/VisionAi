import dependencies.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
}

android {
    val config = ModulesConfigurationManager.getConfigByProjectName(project.name,project.rootDir)

    compileSdkVersion(AndroidConfig.COMPILE_SDK_VERSION)
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
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
            abiFilters.add("arm64-v8a")
        }
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path("CMakeLists.txt")
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
