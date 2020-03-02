import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

object RepositoryHandlerHelper {

    fun applyDefault(handler: RepositoryHandler) {
        handler.google()
        handler.jcenter()
        handler.mavenCentral()
        handler.maven(url = "https://jitpack.io")
    }
}