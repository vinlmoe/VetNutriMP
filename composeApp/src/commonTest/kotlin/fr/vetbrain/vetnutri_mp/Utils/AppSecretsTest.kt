package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppSecretsTest {

    @Test
    fun jsonbinCreateKey_isConfigured() {
        val key = AppSecrets.jsonbinCreateKey
        assertNotNull(key, "JSONBIN_CREATE_KEY n'est pas configurée. " +
            "Ajoutez 'jsonbin.create.key=...' dans local.properties " +
            "ou définissez la variable d'environnement JSONBIN_CREATE_KEY.")
        assertTrue(key.isNotBlank(), "JSONBIN_CREATE_KEY est vide.")
    }

    @Test
    fun jsonbinReadKey_isConfigured() {
        val key = AppSecrets.jsonbinReadKey
        assertNotNull(key, "JSONBIN_READ_KEY n'est pas configurée. " +
            "Ajoutez 'jsonbin.read.key=...' dans local.properties " +
            "ou définissez la variable d'environnement JSONBIN_READ_KEY.")
        assertTrue(key.isNotBlank(), "JSONBIN_READ_KEY est vide.")
    }
}
