package fr.vetbrain.vetnutri_mp.Utils

object AppSecrets {
    val jsonbinCreateKey: String? = JSONBIN_CREATE_KEY_VALUE.ifBlank { null }
    val jsonbinReadKey: String?   = JSONBIN_READ_KEY_VALUE.ifBlank { null }
    val supabaseUrl: String?      = SUPABASE_URL_VALUE.ifBlank { null }
    val supabaseAnonKey: String?  = SUPABASE_ANON_KEY_VALUE.ifBlank { null }
}
