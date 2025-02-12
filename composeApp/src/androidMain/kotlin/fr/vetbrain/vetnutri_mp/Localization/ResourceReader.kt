package fr.vetbrain.vetnutri_mp.Localization

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        return AndroidContext.appContext.assets.open(name).bufferedReader().use { it.readText() }
    }
}
