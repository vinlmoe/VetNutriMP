package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextUtilsTest {

    // ── formatDecimal ──────────────────────────────────────────────────────────

    @Test
    fun formatDecimal_twoDecimals_roundsCorrectly() {
        assertEquals("3.14", TextUtils.formatDecimal(3.14159, 2))
    }

    @Test
    fun formatDecimal_zeroDecimals_roundsToInteger() {
        assertEquals("10", TextUtils.formatDecimal(10.0, 0))
    }

    @Test
    fun formatDecimal_zeroDecimals_roundsUp() {
        assertEquals("4", TextUtils.formatDecimal(3.6, 0))
    }

    @Test
    fun formatDecimal_negativeValue_includesSign() {
        assertEquals("-5.5", TextUtils.formatDecimal(-5.5, 1))
    }

    @Test
    fun formatDecimal_zero_withTwoDecimals_returnsZeroDotZeroZero() {
        assertEquals("0.00", TextUtils.formatDecimal(0.0, 2))
    }

    @Test
    fun formatDecimal_nan_returnsNaNString() {
        assertEquals("NaN", TextUtils.formatDecimal(Double.NaN, 2))
    }

    @Test
    fun formatDecimal_positiveInfinity_returnsInfString() {
        val result = TextUtils.formatDecimal(Double.POSITIVE_INFINITY, 2)
        assertTrue(result.contains("Infinity") || result.contains("∞"), "Expected infinity string but was $result")
    }

    @Test
    fun formatDecimal_defaultDecimals_usesTwoDecimals() {
        assertEquals("1.23", TextUtils.formatDecimal(1.234))
    }

    // ── toSuperscript ──────────────────────────────────────────────────────────

    @Test
    fun toSuperscript_digits_convertsAllDigits() {
        assertEquals("⁰¹²³⁴⁵⁶⁷⁸⁹", TextUtils.toSuperscript("0123456789"))
    }

    @Test
    fun toSuperscript_dotAndComma_convertToMiddleDot() {
        assertEquals("⁰·⁷⁵", TextUtils.toSuperscript("0.75"))
    }

    @Test
    fun toSuperscript_unknownChars_passedThrough() {
        assertEquals("abc", TextUtils.toSuperscript("abc"))
    }

    @Test
    fun toSuperscript_mixedString_convertsKnownOnly() {
        // '2' → '²', 'x' → 'x'
        assertEquals("²x", TextUtils.toSuperscript("2x"))
    }

    @Test
    fun toSuperscript_empty_returnsEmpty() {
        assertEquals("", TextUtils.toSuperscript(""))
    }

    // ── extrairePuissanceEquationBW ────────────────────────────────────────────

    @Test
    fun extrairePuissanceEquationBW_standardNotation_returns0_75() {
        assertEquals("0.75", TextUtils.extrairePuissanceEquationBW("BW ^ 0.75"))
    }

    @Test
    fun extrairePuissanceEquationBW_noSpaces_extractsCorrectly() {
        assertEquals("0.67", TextUtils.extrairePuissanceEquationBW("BW^0.67"))
    }

    @Test
    fun extrairePuissanceEquationBW_null_returnsDefault() {
        assertEquals("0.75", TextUtils.extrairePuissanceEquationBW(null))
    }

    @Test
    fun extrairePuissanceEquationBW_blank_returnsDefault() {
        assertEquals("0.75", TextUtils.extrairePuissanceEquationBW(""))
    }

    @Test
    fun extrairePuissanceEquationBW_caseInsensitive_extractsValue() {
        assertEquals("1", TextUtils.extrairePuissanceEquationBW("bw ^ 1"))
    }

    @Test
    fun extrairePuissanceEquationBW_noMatch_returnsDefault() {
        assertEquals("0.75", TextUtils.extrairePuissanceEquationBW("130 * BEE"))
    }

    // ── formatKgPuissance075 ───────────────────────────────────────────────────

    @Test
    fun formatKgPuissance075_value_containsKgAndSuperscript() {
        val result = TextUtils.formatKgPuissance075(5.62, 2)
        assertEquals("5.62 kg⁰·⁷⁵", result)
    }

    // ── formatKgAvecPuissanceDynamique ─────────────────────────────────────────

    @Test
    fun formatKgAvecPuissanceDynamique_customPower_usesExtractedPower() {
        val result = TextUtils.formatKgAvecPuissanceDynamique(5.62, "BW^0.67", 2)
        assertEquals("5.62 kg⁰·⁶⁷", result)
    }

    @Test
    fun formatKgAvecPuissanceDynamique_nullScript_usesDefault075() {
        val result = TextUtils.formatKgAvecPuissanceDynamique(5.62, null, 2)
        assertEquals("5.62 kg⁰·⁷⁵", result)
    }

    // ── formatAvecExposant ─────────────────────────────────────────────────────

    @Test
    fun formatAvecExposant_genericUnit_formatsCorrectly() {
        val result = TextUtils.formatAvecExposant(10.0, "m", "2", 0)
        assertEquals("10 m²", result)
    }
}
