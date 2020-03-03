import java.io.File
import java.nio.file.Paths

object ModulesConfigurationManager {

    private val configList: MutableList<VisionModuleConfiguration> = mutableListOf()

    init {
        File(Paths.get("").toAbsolutePath().toString()).walk().forEach {
            if (it.isFile) {
                val regex = """(.+)/(.+)\.(.+)""".toRegex()
                val matchResult = regex.matchEntire(it.absolutePath)

                if (matchResult != null) {
                    val (_, fileName, extension) = matchResult.destructured
                    if (fileName == "module" && extension == "properties") {
                        val fis = java.io.FileInputStream(it.absolutePath)
                        val prop = java.util.Properties()
                        prop.load(fis)
                        val projectName = prop.getProperty("projectName")
                        val moduleName = prop.getProperty("moduleName")
                        val versionCode = prop.getProperty("versionCode")
                        val versionName = prop.getProperty("versionName")
                        configList.add(
                            VisionModuleConfiguration(projectName, moduleName, versionCode.toInt(), versionName)
                        )
                        println("Module Instance created: $moduleName")
                    }
                }
            }
        }
    }

    data class VisionModuleConfiguration(
        val projectName: String,
        val moduleName: String,
        val versionCode: Int,
        val versionName: String
    )

    fun getConfigByProjectName(projectName: String): VisionModuleConfiguration =
        configList.first { it.projectName == projectName }
}