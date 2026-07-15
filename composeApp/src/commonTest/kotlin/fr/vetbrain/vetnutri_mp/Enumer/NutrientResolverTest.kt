package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NutrientResolverTest {

    // ── normalizeLabel ────────────────────────────────────────────────────────

    @Test
    fun normalizeLabel_proteinAlias_protein_normalizes() {
        assertEquals("PROTEINE", NutrientResolver.normalizeLabel("PROTEIN"))
    }

    @Test
    fun normalizeLabel_proteinAlias_cp_normalizes() {
        assertEquals("PROTEINE", NutrientResolver.normalizeLabel("CP"))
    }

    @Test
    fun normalizeLabel_proteinAlias_mat_normalizes() {
        assertEquals("PROTEINE", NutrientResolver.normalizeLabel("MAT"))
    }

    @Test
    fun normalizeLabel_fatAlias_fat_normalizes() {
        assertEquals("LIPIDE", NutrientResolver.normalizeLabel("FAT"))
    }

    @Test
    fun normalizeLabel_fatAlias_ee_normalizes() {
        assertEquals("LIPIDE", NutrientResolver.normalizeLabel("EE"))
    }

    @Test
    fun normalizeLabel_calciumAlias_ca_normalizes() {
        assertEquals("CAL", NutrientResolver.normalizeLabel("CA"))
    }

    @Test
    fun normalizeLabel_calciumAlias_calcium_normalizes() {
        assertEquals("CAL", NutrientResolver.normalizeLabel("CALCIUM"))
    }

    @Test
    fun normalizeLabel_omega3_dash_normalizes() {
        assertEquals("O3", NutrientResolver.normalizeLabel("OMEGA-3"))
    }

    @Test
    fun normalizeLabel_omega3_underscore_normalizes() {
        assertEquals("O3", NutrientResolver.normalizeLabel("OMEGA_3"))
    }

    @Test
    fun normalizeLabel_omega6_aliases_normalize() {
        assertEquals("O6", NutrientResolver.normalizeLabel("OMEGA-6"))
        assertEquals("O6", NutrientResolver.normalizeLabel("N6"))
    }

    @Test
    fun normalizeLabel_vitaminA_aliases_normalize() {
        assertEquals("VITA", NutrientResolver.normalizeLabel("VITAMIN_A"))
        assertEquals("VITA", NutrientResolver.normalizeLabel("RETINOL"))
        assertEquals("VITA", NutrientResolver.normalizeLabel("VIT_A"))
    }

    @Test
    fun normalizeLabel_vitaminD_aliases_normalize() {
        assertEquals("VITD", NutrientResolver.normalizeLabel("VITAMIN_D"))
        assertEquals("VITD", NutrientResolver.normalizeLabel("CHOLECALCIFEROL"))
    }

    @Test
    fun normalizeLabel_phosphorus_aliases_normalize() {
        assertEquals("PHOS", NutrientResolver.normalizeLabel("PHOSPHORUS"))
        assertEquals("PHOS", NutrientResolver.normalizeLabel("P"))
    }

    @Test
    fun normalizeLabel_zinc_aliases_normalize() {
        assertEquals("ZN", NutrientResolver.normalizeLabel("ZINC"))
        assertEquals("ZN", NutrientResolver.normalizeLabel("ZN"))
    }

    @Test
    fun normalizeLabel_drymatter_aliases_normalize() {
        assertEquals("DM", NutrientResolver.normalizeLabel("MS"))
        assertEquals("DM", NutrientResolver.normalizeLabel("DRY_MATTER"))
    }

    @Test
    fun normalizeLabel_stripsQuotesAndBrackets() {
        assertEquals("PROTEINE", NutrientResolver.normalizeLabel("[\"PROTEIN\"]"))
    }

    @Test
    fun normalizeLabel_trimsWhitespace() {
        assertEquals("PROTEINE", NutrientResolver.normalizeLabel("  PROTEIN  "))
    }

    // ── AllNutrientResolver ───────────────────────────────────────────────────

    @Test
    fun allNutrientResolver_exactLabel_returnsCorrectEnum() {
        assertEquals(NutrientMain.PROTEINE, NutrientResolver.AllNutrientResolver("PROTEINE"))
    }

    @Test
    fun allNutrientResolver_proteinAlias_resolvesToProteine() {
        assertEquals(NutrientMain.PROTEINE, NutrientResolver.AllNutrientResolver("PROTEIN"))
    }

    @Test
    fun allNutrientResolver_calciumAlias_resolvesToCAL() {
        assertEquals(NutrientMacro.CAL, NutrientResolver.AllNutrientResolver("CALCIUM"))
    }

    @Test
    fun allNutrientResolver_lipideLabel_resolvesToLipide() {
        assertEquals(NutrientMain.LIPIDE, NutrientResolver.AllNutrientResolver("LIPIDE"))
    }

    @Test
    fun allNutrientResolver_fatAlias_resolvesToLipide() {
        assertEquals(NutrientMain.LIPIDE, NutrientResolver.AllNutrientResolver("FAT"))
    }

    @Test
    fun allNutrientResolver_humiditeLabel_resolvesToHumidite() {
        assertEquals(NutrientMain.HUMIDITE, NutrientResolver.AllNutrientResolver("HUMIDITE"))
    }

    @Test
    fun allNutrientResolver_waterAlias_resolvesToHumidite() {
        assertEquals(NutrientMain.HUMIDITE, NutrientResolver.AllNutrientResolver("WATER"))
    }

    // ── resolveStoredLabel ────────────────────────────────────────────────────

    @Test
    fun resolveStoredLabel_exactKnownLabel_returnsNutrient() {
        assertEquals(NutrientMain.PROTEINE, NutrientResolver.resolveStoredLabel("PROTEINE"))
    }

    @Test
    fun resolveStoredLabel_calcLabel_returnsCal() {
        assertEquals(NutrientMacro.CAL, NutrientResolver.resolveStoredLabel("CAL"))
    }

    @Test
    fun resolveStoredLabel_stripsQuotesAndBrackets() {
        val result = NutrientResolver.resolveStoredLabel("[\"PROTEINE\"]")
        assertNotNull(result)
        assertEquals(NutrientMain.PROTEINE, result)
    }

    // ── Non-régression : un nutriment personnalisé ne doit jamais masquer un
    // label standard (ex: un utilisateur créant un nutriment "CAP" ne doit pas
    // faire disparaître NutrientAnalysis.PCa des références qui l'utilisent) ──

    @Test
    fun resolveStoredLabel_customNutrientNamedLikeBuiltinRatio_stillResolvesToBuiltin() {
        CustomNutrientRegistry.registerFromRaw("CAP", "g")
        try {
            assertEquals(NutrientAnalysis.PCa, NutrientResolver.resolveStoredLabel("CAP"))
        } finally {
            CustomNutrientRegistry.removeByLabel("CAP")
        }
    }

    @Test
    fun allNutrientResolver_customNutrientNamedLikeBuiltinRatio_stillResolvesToBuiltin() {
        CustomNutrientRegistry.registerFromRaw("CAP", "g")
        try {
            assertEquals(NutrientAnalysis.PCa, NutrientResolver.AllNutrientResolver("CAP"))
        } finally {
            CustomNutrientRegistry.removeByLabel("CAP")
        }
    }

    // ── getAllNutrientLabels ───────────────────────────────────────────────────

    @Test
    fun getAllNutrientLabels_isNotEmpty() {
        assertTrue(NutrientResolver.getAllNutrientLabels().isNotEmpty())
    }

    @Test
    fun getAllNutrientLabels_containsProteine() {
        assertTrue(NutrientResolver.getAllNutrientLabels().contains(NutrientMain.PROTEINE.label))
    }

    @Test
    fun getAllNutrientLabels_containsLipide() {
        assertTrue(NutrientResolver.getAllNutrientLabels().contains(NutrientMain.LIPIDE.label))
    }

    // ── isNutrientLabel ───────────────────────────────────────────────────────

    @Test
    fun isNutrientLabel_knownLabel_returnsTrue() {
        assertTrue(NutrientResolver.isNutrientLabel(NutrientMain.LIPIDE.label))
    }

    @Test
    fun isNutrientLabel_unknownLabel_returnsFalse() {
        assertFalse(NutrientResolver.isNutrientLabel("NOT_A_NUTRIENT_XXXYYY"))
    }

    // ── isSystemVariableLabel ─────────────────────────────────────────────────

    @Test
    fun isSystemVariableLabel_bodyWeight_returnsTrue() {
        assertTrue(NutrientResolver.isSystemVariableLabel("bodyWeight"))
    }

    @Test
    fun isSystemVariableLabel_metabolicWeight_returnsTrue() {
        assertTrue(NutrientResolver.isSystemVariableLabel("metabolicWeight"))
    }

    @Test
    fun isSystemVariableLabel_unknownLabel_returnsFalse() {
        assertFalse(NutrientResolver.isSystemVariableLabel("NOT_A_VARIABLE_XYZ"))
    }

    // ── getDefaultTestValue ───────────────────────────────────────────────────

    @Test
    fun getDefaultTestValue_bodyWeight_returns25() {
        assertEquals(25.0, NutrientResolver.getDefaultTestValue("bodyWeight"))
    }

    @Test
    fun getDefaultTestValue_unknownVariable_returnsOne() {
        assertEquals(1.0, NutrientResolver.getDefaultTestValue("UNKNOWN_VARIABLE"))
    }
}
