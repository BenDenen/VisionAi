import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope

object Plugins {

    object Versions {
        const val GRADLE_ANDROID = "7.1.0-alpha10"
        const val KOTLIN = "1.5.21"
    }
}


object ClassPathPluginsHelper {

    const val GRADLE_CLASS_PATH = "com.android.tools.build:gradle:${Plugins.Versions.GRADLE_ANDROID}"
    const val KOTLIN_CLASS_PATH = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Plugins.Versions.KOTLIN}"

    fun applyDefault(dependencies: DependencyHandlerScope) {
        dependencies {
            add(ScriptHandler.CLASSPATH_CONFIGURATION, GRADLE_CLASS_PATH)
            add(ScriptHandler.CLASSPATH_CONFIGURATION, KOTLIN_CLASS_PATH)
        }
    }
}