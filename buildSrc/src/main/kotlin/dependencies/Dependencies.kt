package dependencies

import Plugins
import org.gradle.api.artifacts.dsl.DependencyHandler

private const val TYPE_IMPLEMENTATION = "implementation"
private const val TYPE_API = "api"

fun DependencyHandler.implementationFor(vararg dependencies: Any) {
    for (dependency in dependencies) {
        add(TYPE_IMPLEMENTATION, dependency)
    }
}

fun DependencyHandler.apiFor(vararg dependencies: Any) {
    for (dependency in dependencies) {
        add(TYPE_API, dependency)
    }
}

object Dependencies {

    // Kotlin STD
    const val KOTLIN_STD = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Plugins.Versions.KOTLIN}"

    // Koin
    const val KOIN_CORE = "org.koin:koin-android:${Versions.KOIN}"
    const val KOIN_X_SCOPE = "org.koin:koin-androidx-scope:${Versions.KOIN}"
    const val KOIN_X_VIEW_MODEL = "org.koin:koin-androidx-viewmodel:${Versions.KOIN}"

    fun getKoinGroup() = arrayOf(KOIN_CORE, KOIN_X_SCOPE, KOIN_X_VIEW_MODEL)

    // Android X
    const val X_APP_COMPACT = "androidx.appcompat:appcompat:${Versions.X_APP_COMPACT}"
    const val X_LIFECYCLE_VIEWMODEL = "androidx.lifecycle:lifecycle-viewmodel"

    const val X_CORE_KTX = "androidx.core:core-ktx:${Versions.X_KTX}"
    const val X_ACTIVITY_KTX = "androidx.activity:activity-ktx:${Versions.X_KTX}"
    const val X_LIFECYCLE_VIEWMODEL_KTX = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.X_LIFECYCLE_KTX}"
    const val X_LIFECYCLE_RUNTIME_KTX = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.X_LIFECYCLE_KTX}"

    fun getKtxGroup() = arrayOf(X_CORE_KTX, X_ACTIVITY_KTX, X_LIFECYCLE_VIEWMODEL_KTX, X_LIFECYCLE_RUNTIME_KTX)

    const val X_RECYCLER_VIEW = "androidx.recyclerview:recyclerview:${Versions.X_RECYCLER_VIEW}"
    const val X_CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.X_CONSTRAINT_LAYOUT}"
    const val X_COORDINATOR_LAYOUT = "androidx.coordinatorlayout:coordinatorlayout:${Versions.X_COORDINATOR_LAYOUT}"

    // Material
    const val MATERIAL = "com.google.android.material:material:${Versions.MATERIAL}"

    // JCodec
    const val JCODEC = "org.jcodec:jcodec:${Versions.JCODEC}"
    const val JCODEC_ANDROID = "org.jcodec:jcodec-android:${Versions.JCODEC}"

    fun getJcodecGroup() = arrayOf(JCODEC, JCODEC_ANDROID)

    //Coroutines
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.COROUTINES}"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.COROUTINES}"

    fun getCoroutinesGroup() = arrayOf(COROUTINES, COROUTINES_ANDROID)

    //Tensorflow Lite
    const val TENSORFLOW_LITE = "org.tensorflow:tensorflow-lite:${Versions.TENSORFLOW_LITE}"
    const val TENSORFLOW_LITE_GPU = "org.tensorflow:tensorflow-lite-gpu:${Versions.TENSORFLOW_LITE}"

    fun getTensorFlowLiteGroup() = arrayOf(TENSORFLOW_LITE, TENSORFLOW_LITE_GPU)

    // Compose
    const val COMPOSE_RUNTIME = "androidx.compose:compose-runtime:${Versions.COMPOSE_VERSION}"
    const val UI_FRAMEWORK = "androidx.ui:ui-framework:${Versions.COMPOSE_VERSION}"
    const val UI_FOUNDATION = "androidx.ui:ui-foundation:${Versions.COMPOSE_VERSION}"
    const val UI_TOOLING = "androidx.ui:ui-tooling:${Versions.COMPOSE_VERSION}"
    const val UI_LAYOUT = "androidx.ui:ui-layout:${Versions.COMPOSE_VERSION}"
    const val UI_MATERIAL = "androidx.ui:ui-material:${Versions.COMPOSE_VERSION}"

    fun getComposeGroup() = arrayOf(COMPOSE_RUNTIME,UI_FRAMEWORK, UI_FOUNDATION, UI_TOOLING, UI_LAYOUT, UI_MATERIAL)

    object Versions {
        const val KOIN = "2.0.1"

        const val X_APP_COMPACT = "1.1.0"
        const val X_KTX = "1.1.0"
        const val X_LIFECYCLE_KTX = "2.2.0-rc02"
        const val X_RECYCLER_VIEW = "1.1.0"
        const val X_CONSTRAINT_LAYOUT = "1.1.3"
        const val X_COORDINATOR_LAYOUT = "1.1.0"

        const val MATERIAL = "1.0.0"

        const val JCODEC = "0.2.3"

        const val COROUTINES = "1.3.1"

        const val TENSORFLOW_LITE = "1.14.0"

        const val COMPOSE_VERSION = "0.1.0-dev08"
    }
}

