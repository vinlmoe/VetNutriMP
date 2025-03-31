package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EspeceTest {

    @Test
    fun `test getFromString - différents formats d'entrée`() {
        // Test avec le nom de l'énumération
        val espece1 = Espece.getFromString("CHIEN")
        assertNotNull(espece1)
        assertEquals(Espece.CHIEN, espece1)

        // Test avec le label
        val espece2 = Espece.getFromString("DOG")
        assertNotNull(espece2)
        assertEquals(Espece.CHIEN, espece2)

        // Test avec l'ID sous forme de chaîne
        val espece3 = Espece.getFromString("0")
        assertNotNull(espece3)
        assertEquals(Espece.CHIEN, espece3)

        // Test avec des crochets et guillemets (nettoyage automatique)
        val espece4 = Espece.getFromString("[\"CHIEN\"]")
        assertNotNull(
                espece4,
                "La fonction getFromString doit gérer le nettoyage des crochets et guillemets"
        )
        assertEquals(Espece.CHIEN, espece4)

        // Test avec une chaîne en minuscules (insensible à la casse)
        val espece5 = Espece.getFromString("chien")
        assertNotNull(espece5, "La fonction getFromString doit être insensible à la casse")
        assertEquals(Espece.CHIEN, espece5)

        // Test avec une chaîne contenant des espaces
        val espece6 = Espece.getFromString(" CHIEN ")
        assertNotNull(espece6, "La fonction getFromString doit gérer le nettoyage des espaces")
        assertEquals(Espece.CHIEN, espece6)

        // Test avec une valeur inexistante
        val espece7 = Espece.getFromString("ESPECE_INEXISTANTE")
        assertNull(espece7)
    }

    @Test
    fun `test nettoyer et convertir chaîne d'espèce`() {
        // Cette fonction teste le nettoyage et la conversion qui est implémentée dans Mappers.kt
        // Elle est ici pour documenter le comportement attendu

        // Cas 1: Chaîne standard
        val result1 = nettoyerEtConvertirEspece("CHIEN")
        assertEquals("CHIEN", result1)

        // Cas 2: Chaîne avec crochets et guillemets
        val result2 = nettoyerEtConvertirEspece("[\"CHAT\"]")
        assertEquals("CHAT", result2)

        // Cas 3: Chaîne en minuscules
        val result3 = nettoyerEtConvertirEspece("chien")
        assertEquals("CHIEN", result3)

        // Cas 4: Chaîne avec espaces
        val result4 = nettoyerEtConvertirEspece(" CHIEN ")
        assertEquals("CHIEN", result4)

        // Cas 5: ID numérique
        val result5 = nettoyerEtConvertirEspece("0")
        assertEquals("CHIEN", result5)

        // Cas 6: Chaîne inexistante mais nettoyée
        val result6 = nettoyerEtConvertirEspece("[\"ESPECE_INEXISTANTE\"]")
        assertEquals("ESPECE_INEXISTANTE", result6)
    }

    // Fonction utilitaire qui simule le nettoyage et la conversion utilisés dans Mappers.kt
    private fun nettoyerEtConvertirEspece(especeStr: String): String {
        // Nettoyer la chaîne (supprimer les crochets et guillemets)
        val cleanedEspece = especeStr.replace("[", "").replace("]", "").replace("\"", "").trim()

        // Tenter de convertir vers une énumération Espece
        val espece = Espece.getFromString(cleanedEspece)

        // Utiliser le nom de l'énumération si trouvée, sinon garder la chaîne nettoyée
        return espece?.name ?: cleanedEspece
    }
}
