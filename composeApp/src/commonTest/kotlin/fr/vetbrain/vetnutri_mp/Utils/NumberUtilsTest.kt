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
}
