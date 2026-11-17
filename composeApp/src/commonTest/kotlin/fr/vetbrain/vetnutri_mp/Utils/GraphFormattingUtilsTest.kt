package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals

class GraphFormattingUtilsTest {
    @Test
    fun phosphorusTicksDoNotDisplayFloatRoundingNoise() {
        val range = 0.4f..1.3f
        val ticks = (8..11).map { it * 0.1f }
        assertEquals(
            listOf("0.8", "0.9", "1", "1.1"),
            ticks.map { GraphFormattingUtils.formatAxisTick(it, range) }
        )
        assertEquals("0.9", GraphFormattingUtils.formatAxisTick(0.9000004f, range))
    }

    @Test
    fun smallRangesKeepDistinctDecimalTicks() {
        val range = 0.001f..0.002f
        assertEquals("0.0012", GraphFormattingUtils.formatAxisTick(0.0012000001f, range))
        assertEquals("0.0013", GraphFormattingUtils.formatAxisTick(0.0013000001f, range))
    }

    @Test
    fun pannedAxesHandleNegativeValuesAndZero() {
        val range = -1f..1f
        assertEquals("-0.9", GraphFormattingUtils.formatAxisTick(-0.90000004f, range))
        assertEquals("0", GraphFormattingUtils.formatAxisTick(-0.00000004f, range))
        assertEquals("0", GraphFormattingUtils.formatAxisTick(0f, range))
    }

    @Test
    fun formatDecimal_partieDecimaleSansTroncatureBinaire() {
        // (6,8 − 6) × 100 = 79,999… en Double : le rapport O6/O3 d'une ration s'affichait « 6.79 »
        assertEquals("6.80", GraphFormattingUtils.formatDecimal(3.4 / 0.5, 2))
        assertEquals("1.38", GraphFormattingUtils.formatDecimal(1.1 / 0.8, 2))
        assertEquals("12.67", GraphFormattingUtils.formatDecimal(19.0 / 1.5, 2))
        assertEquals("2.3", GraphFormattingUtils.formatDecimal(2.3, 1))
        assertEquals("0.05", GraphFormattingUtils.formatDecimal(0.05, 2))
        assertEquals("-1.5", GraphFormattingUtils.formatDecimal(-1.5, 1))
        assertEquals("-0.5", GraphFormattingUtils.formatDecimal(-0.5, 1))
        assertEquals("0.0", GraphFormattingUtils.formatDecimal(-0.001, 1))
        assertEquals("1667", GraphFormattingUtils.formatDecimal(1666.5, 0))
    }
}
