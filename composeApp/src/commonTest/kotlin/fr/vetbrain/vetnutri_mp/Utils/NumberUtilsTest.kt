package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberUtilsTest {

    @Test
    fun format_zeroDigits_truncatesToInteger() {
        // Implementation uses toLong() which truncates (not rounds)
        assertEquals("42", NumberUtils.format(42.7, 0))
    }

    @Test
    fun format_twoDigits_returnsDecimalString() {
        assertEquals("3.14", NumberUtils.format(3.14, 2))
    }

    @Test
    fun format_wholeNumberWithDigits_omitsTrailingZeroDecimal() {
        // decimalPart == 0L → returns just the integer part
        assertEquals("5", NumberUtils.format(5.0, 2))
    }

    @Test
    fun format_negativeNumber_zeroDigits_works() {
        assertEquals("-3", NumberUtils.format(-3.0, 0))
    }

    @Test
    fun format_int_returnsString() {
        assertEquals("100", NumberUtils.format(100))
    }

    @Test
    fun format_zero_zeroDigits_returnsZero() {
        assertEquals("0", NumberUtils.format(0.0, 0))
    }

    @Test
    fun format_smallDecimal_oneDigit_works() {
        assertEquals("3.1", NumberUtils.format(3.1, 1))
    }

    @Test
    fun arrondirAuPas_pasDecimal_sansErreurBinaire() {
        // round(0.34 / 0.1) × 0.1 vaudrait 0.30000000000000004
        assertEquals(0.3, NumberUtils.arrondirAuPas(0.34, 0.1))
        assertEquals(0.9, NumberUtils.arrondirAuPas(0.92, 0.3))
        assertEquals(1.7, NumberUtils.arrondirAuPas(1.68, 0.1))
        assertEquals(12.5, NumberUtils.arrondirAuPas(12.6, 2.5))
        assertEquals(125.0, NumberUtils.arrondirAuPas(123.0, 5.0))
        assertEquals(0.0, NumberUtils.arrondirAuPas(0.04, 0.1))
    }

    @Test
    fun arrondirAuPasSuperieur_multipleSuperieurOuEgal() {
        assertEquals(0.3, NumberUtils.arrondirAuPasSuperieur(0.21, 0.1))
        // Déjà multiple (à l'erreur binaire près) : inchangé
        assertEquals(0.3, NumberUtils.arrondirAuPasSuperieur(0.1 * 3, 0.1))
        assertEquals(7.5, NumberUtils.arrondirAuPasSuperieur(6.0, 2.5))
        assertEquals(1000.0, NumberUtils.arrondirAuPasSuperieur(1000.0, 25.0))
    }
}
