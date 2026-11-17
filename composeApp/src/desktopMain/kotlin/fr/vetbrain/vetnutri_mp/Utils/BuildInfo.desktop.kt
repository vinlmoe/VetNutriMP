package fr.vetbrain.vetnutri_mp.Utils

actual fun isDebugBuild(): Boolean =
    System.getProperty("vetnutri.debug")?.equals("true", ignoreCase = true) == true
