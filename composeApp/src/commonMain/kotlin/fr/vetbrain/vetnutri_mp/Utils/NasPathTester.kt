package fr.vetbrain.vetnutri_mp.Utils

sealed class NasTestResult {
    object Ok : NasTestResult()
    data class VersionTooHigh(val fileVersion: Int) : NasTestResult()
    object Inaccessible : NasTestResult()
    object EmptyPath : NasTestResult()
}

expect fun testNasDbPath(path: String): NasTestResult
