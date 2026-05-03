package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.BuildConfig

actual object AppSecrets {
    actual val jsonbinCreateKey: String? = BuildConfig.JSONBIN_CREATE_KEY.ifBlank { null }
    actual val jsonbinReadKey: String?   = BuildConfig.JSONBIN_READ_KEY.ifBlank { null }
}
