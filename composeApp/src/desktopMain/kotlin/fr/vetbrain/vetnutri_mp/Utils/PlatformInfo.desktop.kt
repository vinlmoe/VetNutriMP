package fr.vetbrain.vetnutri_mp.Utils

actual val isIosPlatform: Boolean = false
actual val isAndroidPlatform: Boolean = false
actual val isWindowsPlatform: Boolean =
    System.getProperty("os.name", "").startsWith("Windows", ignoreCase = true)
