package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizationErrorTest {
    private lateinit var mockResourceReader: ResourceReader

    @BeforeTest
    fun setup() {
        mockResourceReader = ErrorResourceReader()
        LocalizationManager.setResourceReader(mockResourceReader)
    }

    @Test
    fun `test invalid json handling`() {
        LocalizationManager.initialize()
        // Devrait retourner la clé elle-même en cas d'erreur
        assertEquals("test_key", LocalizationManager.translate("test_key"))
    }

    @Test
    fun `test missing resource file handling`() {
        LocalizationManager.initialize()
        // Devrait retourner la clé elle-même si le fichier de ressources est manquant
        assertEquals("another_key", LocalizationManager.translate("another_key"))
    }

    @Test
    fun `test empty json handling`() {
        (mockResourceReader as ErrorResourceReader).setEmptyJson()
        LocalizationManager.initialize()
        // Devrait retourner la clé elle-même si le JSON est vide
        assertEquals("some_key", LocalizationManager.translate("some_key"))
    }

    private class ErrorResourceReader : ResourceReader() {
        private var shouldReturnEmptyJson = false

        fun setEmptyJson() {
            shouldReturnEmptyJson = true
        }

        override fun readResource(name: String): String {
            if (shouldReturnEmptyJson) {
                return "{}"
            }
            throw Exception("Erreur simulée pour les tests")
        }
    }
}
