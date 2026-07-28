package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LegacyV2MappingsTest {
    @Test
    fun nutrientTableLabels_preserveLegacyEnumOrdering() {
        assertEquals("HUMIDITE", LegacyV2Mappings.nutrientTableLabels.getValue("VALUEBASE")[0])
        assertEquals("PROTEINE", LegacyV2Mappings.nutrientTableLabels.getValue("VALUEBASE")[1])
        assertEquals("CAL", LegacyV2Mappings.nutrientTableLabels.getValue("VALUEMACRO")[0])
        assertEquals("O3", LegacyV2Mappings.nutrientTableLabels.getValue("VALUELIPID")[18])
    }

    @Test
    fun speciesMappings_convertLegacyIdsAndLabels() {
        assertEquals("DOG", LegacyV2Mappings.speciesLabels[0])
        assertEquals("CHAT", LegacyV2Mappings.speciesEnumNames[1])
        assertNull(LegacyV2Mappings.speciesEnumNames[2])
        assertEquals("CHIEN", LegacyV2Mappings.speciesEnumName("dog"))
        assertEquals("CHAT", LegacyV2Mappings.speciesEnumName(" CHAT "))
        assertNull(LegacyV2Mappings.speciesEnumName("ALL"))
        assertNull(LegacyV2Mappings.speciesEnumName("unknown"))
    }

    @Test
    fun stageAndEquationMappings_coverEveryKnownV2Ordinal() {
        assertEquals(
            listOf("ADULTE", "CROISSANCE", "LACTATION", "GESTATION", "HOSPIT"),
            (0..4).map { LegacyV2Mappings.stageLabels.getValue(it) }
        )
        assertEquals(7, LegacyV2Mappings.equationKindNames.size)
        assertEquals("COMPLEMENTARY_NUTRIENT", LegacyV2Mappings.equationKindNames[5])
    }

    @Test
    fun unitRequirementMappings_convertEveryV2Unit() {
        assertEquals(listOf(1, 0, 2, 6, 5), (0..4).map {
            LegacyV2Mappings.unitRequirementIds.getValue(it)
        })
    }

    @Test
    fun transpileScript_convertsMathFunctionsPowerAndLogicalOr() {
        val migrated = LegacyV2Mappings.transpileScript(
            "Math.pow(BW, 0.75) + Math.exp(K) + (A | B)"
        )

        assertEquals("pow(BW, 0.75) + exp(K) + (A + B)", migrated)
    }

    @Test
    fun transpileScript_convertsDoubleStarPower() {
        assertEquals("BW ^ 0.75", LegacyV2Mappings.transpileScript("BW ** 0.75"))
    }

    @Test
    fun transpileScript_foldsLegacyValueAssignments() {
        val migrated = LegacyV2Mappings.transpileScript(
            """
            value = 70 * BW;
            value = value + SUPP;
            """.trimIndent()
        )

        assertEquals("70 * BW+SUPP", migrated)
    }

    @Test
    fun transpileScript_foldsLegacyConditionalAssignments() {
        val migrated = LegacyV2Mappings.transpileScript(
            """
            value = BASE;
            if (BW > 10) {
            value = value + LARGE;
            } else {
            value = value + SMALL;
            value = value + EXTRA;
            }
            """.trimIndent()
        )

        assertEquals("BASE+if(BW > 10,LARGE,SMALL+EXTRA)", migrated)
    }

    @Test
    fun transpiledExpression_canBeEvaluatedByCurrentMathParser() {
        val migrated = LegacyV2Mappings.transpileScript("Math.pow(BW, 2) + 5")
        val result = ExpressionMathematique.evaluer(migrated, mapOf("BW" to 3.0))

        assertEquals(14.0, result)
    }

    @Test
    fun raceCodeMapper_handlesV2NamedAndZeroPaddedCodes() {
        assertEquals("Labrador", RaceCodeMapper.resolveRaceCode("DOG", "Labrador"))
        assertTrue(RaceCodeMapper.resolveRaceCode("CAT", "A01")?.isNotBlank() == true)
        assertNull(RaceCodeMapper.resolveRaceCode("RAT", "A1"))
    }
}
