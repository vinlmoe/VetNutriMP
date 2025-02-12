package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.ResourceReader
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

class LocalizationTest {
    private lateinit var mockResourceReader: ResourceReader
    private val validJsonResponse =
            """
        {
            "translations": {
                "welcome": "Bienvenue",
                "app_name": "VetNutri MP",
                "loading": "Chargement...",
                "save": "Sauvegarder",
                "cancel": "Annuler",
                "delete": "Supprimer",
                "edit": "Éditer"
            }
        }
    """.trimIndent()

    @BeforeTest
    fun setup() {
        mockResourceReader = TestResourceReader(validJsonResponse)
        LocalizationManager.setResourceReader(mockResourceReader)
    }

    @Test
    fun `test initialization with default locale`() {
        LocalizationManager.initialize()
        assertEquals("Bienvenue", LocalizationManager.translate("welcome"))
    }

    @Test
    fun `test initialization with specific locale`() {
        LocalizationManager.initialize("en")
        // Devrait retomber sur le français car l'anglais n'existe pas
        assertEquals("Bienvenue", LocalizationManager.translate("welcome"))
    }

    @Test
    fun `test fallback for missing translations`() {
        LocalizationManager.initialize()
        val unknownKey = "unknown_key"
        assertEquals(unknownKey, LocalizationManager.translate(unknownKey))
    }

    @Test
    fun `test locale change`() {
        LocalizationManager.initialize()
        assertEquals("Bienvenue", LocalizationManager.translate("welcome"))

        // Change to non-existent locale should fallback to French
        LocalizationManager.setLocale("es")
        assertEquals("Bienvenue", LocalizationManager.translate("welcome"))
    }

    @Test
    fun `test all required keys are present`() {
        LocalizationManager.initialize()
        val requiredKeys =
                listOf(
                        LocalizationKeys.General.WELCOME,
                        LocalizationKeys.General.APP_NAME,
                        LocalizationKeys.General.LOADING,
                        LocalizationKeys.General.SAVE,
                        LocalizationKeys.General.CANCEL,
                        LocalizationKeys.General.DELETE,
                        LocalizationKeys.General.EDIT
                )

        requiredKeys.forEach { key ->
            assertNotNull(
                    LocalizationManager.translate(key),
                    "La clé '$key' devrait avoir une traduction"
            )
            assertNotEquals(
                    key,
                    LocalizationManager.translate(key),
                    "La clé '$key' ne devrait pas retourner la clé elle-même"
            )
        }
    }

    private class TestResourceReader(private val jsonResponse: String) : ResourceReader() {
        override fun readResource(name: String): String = jsonResponse
    }
}
