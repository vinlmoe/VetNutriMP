package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

@OptIn(ExperimentalForeignApi::class)
actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        val bundle = NSBundle.mainBundle
        val path =
                bundle.pathForResource(name.removeSuffix(".json"), "json")
                        ?: throw IllegalStateException("Resource $name not found")
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
                ?: throw IllegalStateException("Failed to read resource $name")
    }
}
