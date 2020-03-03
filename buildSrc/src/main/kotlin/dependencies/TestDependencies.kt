package dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler

private const val TYPE_TEST_IMPLEMENTATION = "testImplementation"
private const val TYPE_API = "testApi"

fun DependencyHandler.testImplementationFor(vararg dependencies: String) {
    for (dependency in dependencies) {
        add(TYPE_TEST_IMPLEMENTATION, dependency)
    }
}

fun DependencyHandler.testApiFor(vararg dependencies: Any) {
    for (dependency in dependencies) {
        add(TYPE_API, dependency)
    }
}

object TestDependencies {

    const val JUNIT4 = "junit:junit:${Versions.JUNIT4}"
    const val TEST_RUNNER = "androidx.test:runner:${Versions.TEST_RUNNER}"
    const val ESPRESSO = "androidx.test.espresso:espresso-core:${Versions.ESPRESSO}"

    object Versions {
        const val JUNIT4 = "4.12"
        const val TEST_RUNNER = "1.3.0-alpha02"
        const val ESPRESSO = "1.3.0-alpha02"
    }
}