package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportWeightGridTest {
    @Test
    fun `uses one kilogram lines for normal and large ranges`() {
        assertEquals(ExportWeightGrid(step = 1f, labelEvery = 2), exportWeightGridFor(12f))
        assertEquals(ExportWeightGrid(step = 1f, labelEvery = 10), exportWeightGridFor(70f))
    }

    @Test
    fun `uses one hundred gram lines for ranges below ten kilograms`() {
        assertEquals(ExportWeightGrid(step = 0.1f, labelEvery = 5), exportWeightGridFor(3f))
        assertEquals(ExportWeightGrid(step = 0.1f, labelEvery = 5), exportWeightGridFor(9.9f))
    }

    @Test
    fun `clearly distinguishes kilograms from one hundred gram lines`() {
        val hundredGramGrid = ExportWeightGrid(step = 0.1f, labelEvery = 5)

        assertEquals(
            ExportWeightGridLineStyle(color = "#909090", strokeWidth = 1.1f),
            exportWeightGridLineStyle(5f, hundredGramGrid)
        )
        assertEquals(
            ExportWeightGridLineStyle(color = "#B8B8B8", strokeWidth = 0.7f),
            exportWeightGridLineStyle(6f, hundredGramGrid)
        )
        assertEquals(
            ExportWeightGridLineStyle(color = "#E8E8E8", strokeWidth = 0.2f),
            exportWeightGridLineStyle(5.1f, hundredGramGrid)
        )
    }
}
