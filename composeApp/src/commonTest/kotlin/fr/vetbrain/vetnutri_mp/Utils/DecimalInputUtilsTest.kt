package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DecimalInputUtilsTest {

    // ── normalizeDecimalInput ──────────────────────────────────────────────────

    @Test
    fun normalizeDecimalInput_empty_returnsEmpty() {
        assertEquals("", normalizeDecimalInput(""))
    }

    @Test
    fun normalizeDecimalInput_digitsOnly_unchanged() {
        assertEquals("123", normalizeDecimalInput("123"))
    }

    @Test
    fun normalizeDecimalInput_dotSeparator_convertedToComma() {
        assertEquals("1,5", normalizeDecimalInput("1.5"))
    }

    @Test
    fun normalizeDecimalInput_commaSeparator_keptAsComma() {
        assertEquals("1,5", normalizeDecimalInput("1,5"))
    }

    @Test
    fun normalizeDecimalInput_multipleDots_onlyFirstKept() {
        assertEquals("1,23", normalizeDecimalInput("1.2.3"))
    }

    @Test
    fun normalizeDecimalInput_lettersOnly_returnsEmpty() {
        assertEquals("", normalizeDecimalInput("abc"))
    }

    @Test
    fun normalizeDecimalInput_mixedLettersAndDigits_keepsDigitsOnly() {
        assertEquals("1234", normalizeDecimalInput("12abc34"))
    }

    @Test
    fun normalizeDecimalInput_leadingDot_prefixesZero() {
        assertEquals("0,5", normalizeDecimalInput(".5"))
    }

    @Test
    fun normalizeDecimalInput_leadingComma_prefixesZero() {
        assertEquals("0,5", normalizeDecimalInput(",5"))
    }

    @Test
    fun normalizeDecimalInput_secondSeparatorIgnored_digitsAfterKept() {
        // "1.2.3" → first dot accepted as separator → "1,2" then "3" appended → "1,23"
        assertEquals("1,23", normalizeDecimalInput("1.2.3"))
    }

    // ── parsePositiveDecimal ───────────────────────────────────────────────────

    @Test
    fun parsePositiveDecimal_dotNotation_returnsParsed() {
        assertEquals(1.5, parsePositiveDecimal("1.5"))
    }

    @Test
    fun parsePositiveDecimal_commaNotation_returnsParsed() {
        assertEquals(1.5, parsePositiveDecimal("1,5"))
    }

    @Test
    fun parsePositiveDecimal_zero_returnsNull() {
        assertNull(parsePositiveDecimal("0"))
    }

    @Test
    fun parsePositiveDecimal_zero_dotZero_returnsNull() {
        assertNull(parsePositiveDecimal("0.0"))
    }

    @Test
    fun parsePositiveDecimal_negative_returnsNull() {
        assertNull(parsePositiveDecimal("-1"))
    }

    @Test
    fun parsePositiveDecimal_letters_returnsNull() {
        assertNull(parsePositiveDecimal("abc"))
    }

    @Test
    fun parsePositiveDecimal_empty_returnsNull() {
        assertNull(parsePositiveDecimal(""))
    }

    @Test
    fun parsePositiveDecimal_largeValue_returnsParsed() {
        assertEquals(999.99, parsePositiveDecimal("999.99"))
    }

    @Test
    fun parsePositiveDecimal_verySmallPositive_returnsParsed() {
        val result = parsePositiveDecimal("0.001")
        kotlin.test.assertNotNull(result)
        kotlin.test.assertTrue(result!! > 0.0)
    }
}
