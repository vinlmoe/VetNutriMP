package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * formatAlimentDisplayName utilisait une regex `[^\p{L}\p{N}]+` qui provoque un
 * PatternSyntaxException ("No such character class") sur le moteur regex de
 * Kotlin/Native (iOS), absent sur la JVM (Android/Desktop). Ces tests couvrent
 * des noms d'aliments réels (accents, apostrophes, tirets, ponctuation) pour
 * garantir un comportement identique sur toutes les cibles.
 */
class FormatAlimentDisplayNameTest {

    @Test
    fun nullAliment_returnsIngredientFallback() {
        assertEquals("Ingredient", formatAlimentDisplayName(null))
    }

    @Test
    fun accentsApostrophesAndHyphens_areKeptAsIs() {
        val aliment = AlimentEv(
            brand = "Royal Canin",
            gamme = "Veterinary Diet",
            nom = "Pâtée à l'agneau - saveur bœuf"
        )
        assertEquals(
            "Royal Canin, Veterinary Diet, Pâtée à l'agneau - saveur bœuf",
            formatAlimentDisplayName(aliment)
        )
    }

    @Test
    fun parenthesesAndPunctuation_areKeptAsIs() {
        val aliment = AlimentEv(
            brand = null,
            gamme = null,
            nom = "Croquettes (chat stérilisé) - 3kg"
        )
        assertEquals("Croquettes (chat stérilisé) - 3kg", formatAlimentDisplayName(aliment))
    }

    @Test
    fun quotedAndBlankFields_areClearedOrTrimmed() {
        val aliment = AlimentEv(
            brand = "  \"Hill's\"  ",
            gamme = "   ",
            nom = "'Science Plan'"
        )
        assertEquals("Hill's, Science Plan", formatAlimentDisplayName(aliment))
    }

    @Test
    fun placeholderValues_areTreatedAsAbsent() {
        val aliment = AlimentEv(
            brand = "null",
            gamme = "N/A",
            nom = "Purina Pro Plan"
        )
        assertEquals("Purina Pro Plan", formatAlimentDisplayName(aliment))
    }

    @Test
    fun allPlaceholderOrBlankFields_fallsBackToIngredient() {
        val aliment = AlimentEv(brand = "none", gamme = "na", nom = "   ")
        assertEquals("Ingredient", formatAlimentDisplayName(aliment))
    }
}
