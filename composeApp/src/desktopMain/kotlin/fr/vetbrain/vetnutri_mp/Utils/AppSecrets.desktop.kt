package fr.vetbrain.vetnutri_mp.Utils

actual object AppSecrets {
    actual val jsonbinCreateKey: String? = System.getProperty("jsonbin.create.key")?.ifBlank { null }
    actual val jsonbinReadKey: String?   = System.getProperty("jsonbin.read.key")?.ifBlank { null }
}
