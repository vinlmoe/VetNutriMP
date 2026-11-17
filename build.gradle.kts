plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false

}
val releaseVersionNameSuffix by extra("")

// Permet de placer tous les artefacts de build hors d'un dossier synchronisé
// (par exemple OneDrive) : -Pvetnutri.buildDir=C:\\VetNutriBuild
providers.gradleProperty("vetnutri.buildDir").orNull?.let { configuredPath ->
    val externalBuildRoot = file(configuredPath)
    layout.buildDirectory.set(externalBuildRoot.resolve("root"))
    subprojects {
        layout.buildDirectory.set(externalBuildRoot.resolve(name))
    }
}
