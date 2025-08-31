package fr.vetbrain.vetnutri_mp

class WindowsPlatform : Platform {
    override val name: String = "Windows ${System.getProperty("os.version", "Unknown")}"
}

actual fun getPlatform(): Platform = WindowsPlatform()
