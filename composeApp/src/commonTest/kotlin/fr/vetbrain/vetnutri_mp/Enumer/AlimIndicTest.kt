package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals

class AlimIndicTest {

    @Test
    fun `test byCoef - conversion par identifiant numérique`() {
        // Test avec un coefficient existant
        val indic1 = AlimIndic.byCoef(5)
        assertEquals(AlimIndic.OBES, indic1)

        // Test avec un coefficient inexistant
        val indic2 = AlimIndic.byCoef(999999)
        assertEquals(
                AlimIndic.PHYS,
                indic2,
                "La méthode doit retourner PHYS pour un coefficient inexistant"
        )
    }

    @Test
    fun `test byName - conversion par nom`() {
        // Test avec un nom exact
        val indic1 = AlimIndic.byName("Obésité")
        assertEquals(AlimIndic.OBES, indic1)

        // Test avec un nom en minuscules
        val indic2 = AlimIndic.byName("obésité")
        assertEquals(AlimIndic.OBES, indic2, "La méthode doit être insensible à la casse")

        // Test avec un nom inexistant
        val indic3 = AlimIndic.byName("Indication Inexistante")
        assertEquals(
                AlimIndic.AUTRE,
                indic3,
                "La méthode doit retourner AUTRE pour un nom inexistant"
        )
    }

    @Test
    fun `test getFromString - conversion avec nettoyage`() {
        // Test avec un nom d'énumération
        val indic1 = AlimIndic.getFromString("OBES")
        assertEquals(AlimIndic.OBES, indic1)

        // Test avec un nom avec crochets et guillemets
        val indic2 = AlimIndic.getFromString("[\"URO\"]")
        assertEquals(AlimIndic.URO, indic2, "La méthode doit nettoyer les crochets et guillemets")

        // Test avec minuscules
        val indic3 = AlimIndic.getFromString("obes")
        assertEquals(AlimIndic.OBES, indic3, "La méthode doit être insensible à la casse")

        // Test avec espaces
        val indic4 = AlimIndic.getFromString(" OBES ")
        assertEquals(AlimIndic.OBES, indic4, "La méthode doit nettoyer les espaces")

        // Test avec label
        val indic5 = AlimIndic.getFromString("Obésité")
        assertEquals(AlimIndic.OBES, indic5, "La méthode doit reconnaître les labels")

        // Test avec ID numérique
        val indic6 = AlimIndic.getFromString("5")
        assertEquals(
                AlimIndic.OBES,
                indic6,
                "La méthode doit reconnaître les identifiants numériques"
        )

        // Test avec valeur inexistante
        val indic7 = AlimIndic.getFromString("INEXISTANT")
        assertEquals(
                AlimIndic.AUTRE,
                indic7,
                "La méthode doit retourner AUTRE pour les valeurs non reconnues"
        )
    }

    @Test
    fun `test nettoyer et convertir chaîne d'indication`() {
        // Cette fonction teste le nettoyage et la conversion qui est implémentée dans Mappers.kt
        // Elle est ici pour documenter le comportement attendu

        // Cas 1: Nom d'énumération standard
        val result1 = nettoyerEtConvertirIndication("OBES")
        assertEquals(AlimIndic.OBES, result1)

        // Cas 2: Nom avec crochets et guillemets
        val result2 = nettoyerEtConvertirIndication("[\"URO\"]")
        assertEquals(AlimIndic.URO, result2)

        // Cas 3: Nom en minuscules
        val result3 = nettoyerEtConvertirIndication("obes")
        assertEquals(AlimIndic.OBES, result3)

        // Cas 4: Nom avec espaces
        val result4 = nettoyerEtConvertirIndication(" OBES ")
        assertEquals(AlimIndic.OBES, result4)

        // Cas 5: Label au lieu du nom
        val result5 = nettoyerEtConvertirIndication("Obésité")
        assertEquals(AlimIndic.OBES, result5)

        // Cas 6: Valeur numérique (coefficient)
        val result6 = nettoyerEtConvertirIndication("5")
        assertEquals(AlimIndic.OBES, result6)

        // Cas 7: Valeur inexistante
        val result7 = nettoyerEtConvertirIndication("INDICATION_INEXISTANTE")
        assertEquals(AlimIndic.AUTRE, result7)
    }

    // Fonction utilitaire qui simule le nettoyage et la conversion utilisés dans Mappers.kt
    private fun nettoyerEtConvertirIndication(indicStr: String): AlimIndic {
        // Utiliser directement getFromString qui gère désormais le nettoyage
        return AlimIndic.getFromString(indicStr)
    }
}
