package fr.vetbrain.vetnutri_mp.Localization

expect open class ResourceReader() {
    open fun readResource(name: String): String
}
