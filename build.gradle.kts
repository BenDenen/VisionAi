buildscript {
    RepositoryHandlerHelper.applyDefault(repositories)
    ClassPathPluginsHelper.applyDefault(dependencies)
}

allprojects {
    RepositoryHandlerHelper.applyDefault(repositories)
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}
