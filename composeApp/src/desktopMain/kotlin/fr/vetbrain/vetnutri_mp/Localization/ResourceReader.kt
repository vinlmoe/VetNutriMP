package fr.vetbrain.vetnutri_mp.Localization

import kotlin.io.path.Path
import kotlin.io.path.readText

actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        val resourcePath = "src/commonMain/resources/$name"
        return try {
            Path(resourcePath).readText()
        } catch (e: Exception) {
            throw IllegalStateException("Failed to read resource $name: ${e.message}")
        }
    }
}
