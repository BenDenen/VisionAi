val modulesList = mutableListOf<String>()
rootDir.walk().forEach {
    if(it.isFile){
        val regex = """(.+)/(.+)\.(.+)""".toRegex()
        val matchResult = regex.matchEntire(it.absolutePath)
        if(matchResult != null) {
            val (directory, fileName, extension) = matchResult.destructured
            if(fileName == "module" && extension == "properties") {
                val fis = java.io.FileInputStream(it.absolutePath)
                val prop = java.util.Properties()
                prop.load(fis)
                val moduleName = prop.getProperty("moduleName")
                modulesList.add(moduleName)
                println("Module registered: $moduleName")
            }
        }
    }
}

include(
    *modulesList.toTypedArray()
)

rootProject.buildFileName = "build.gradle.kts"
