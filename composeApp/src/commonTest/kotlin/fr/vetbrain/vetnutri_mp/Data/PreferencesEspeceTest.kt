package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PreferencesEspeceTest {

    // ── getEspeceEnum ─────────────────────────────────────────────────────────

    @Test
    fun getEspeceEnum_validName_returnsEnum() {
        assertEquals(Espece.CHIEN, PreferencesEspece(espece = Espece.CHIEN.name).getEspeceEnum())
    }

    @Test
    fun getEspeceEnum_chatName_returnsChat() {
        assertEquals(Espece.CHAT, PreferencesEspece(espece = Espece.CHAT.name).getEspeceEnum())
    }

    @Test
    fun getEspeceEnum_unknownName_defaultsToChien() {
        assertEquals(Espece.CHIEN, PreferencesEspece(espece = "UNKNOWN_XYZ").getEspeceEnum())
    }

    @Test
    fun getEspeceEnum_allEspeces_roundTrip() {
        Espece.values().forEach { espece ->
            val result = PreferencesEspece(espece = espece.name).getEspeceEnum()
            assertEquals(espece, result, "Round-trip failed for $espece")
        }
    }

    // ── getTotalSelectedNutrients ─────────────────────────────────────────────

    @Test
    fun getTotalSelectedNutrients_emptyMap_returnsZero() {
        assertEquals(0, PreferencesEspece(nutrimentsSelectionnes = emptyMap()).getTotalSelectedNutrients())
    }

    @Test
    fun getTotalSelectedNutrients_sumsAllCategories() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(
                "BASE" to listOf(1, 2, 3),
                "MACRO" to listOf(10, 11)
            )
        )
        assertEquals(5, prefs.getTotalSelectedNutrients())
    }

    // ── getSelectedNutrientsCount ─────────────────────────────────────────────

    @Test
    fun getSelectedNutrientsCount_existingCategory_returnsCount() {
        val prefs = PreferencesEspece(nutrimentsSelectionnes = mapOf("BASE" to listOf(1, 2, 3)))
        assertEquals(3, prefs.getSelectedNutrientsCount("BASE"))
    }

    @Test
    fun getSelectedNutrientsCount_missingCategory_returnsZero() {
        assertEquals(0, PreferencesEspece(nutrimentsSelectionnes = emptyMap()).getSelectedNutrientsCount("BASE"))
    }

    // ── isNutrientSelected ────────────────────────────────────────────────────

    @Test
    fun isNutrientSelected_presentNutrient_returnsTrue() {
        val prefs = PreferencesEspece(nutrimentsSelectionnes = mapOf("BASE" to listOf(1, 2)))
        assertTrue(prefs.isNutrientSelected("BASE", 1))
    }

    @Test
    fun isNutrientSelected_absentNutrient_returnsFalse() {
        val prefs = PreferencesEspece(nutrimentsSelectionnes = mapOf("BASE" to listOf(1, 2)))
        assertFalse(prefs.isNutrientSelected("BASE", 99))
    }

    @Test
    fun isNutrientSelected_missingCategory_returnsFalse() {
        assertFalse(PreferencesEspece(nutrimentsSelectionnes = emptyMap()).isNutrientSelected("BASE", 1))
    }

    // ── addNutrient ───────────────────────────────────────────────────────────

    @Test
    fun addNutrient_newCoef_isPresent() {
        val updated = PreferencesEspece(nutrimentsSelectionnes = emptyMap())
            .addNutrient(MainNutrientEnum.BASE, 1)
        assertTrue(updated.isNutrientSelected(MainNutrientEnum.BASE.name, 1))
    }

    @Test
    fun addNutrient_doesNotMutateOriginal() {
        val original = PreferencesEspece(nutrimentsSelectionnes = emptyMap())
        original.addNutrient(MainNutrientEnum.BASE, 1)
        assertFalse(original.isNutrientSelected(MainNutrientEnum.BASE.name, 1))
    }

    @Test
    fun addNutrient_duplicateCoef_notAddedTwice() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1))
        )
        val updated = prefs.addNutrient(MainNutrientEnum.BASE, 1)
        assertEquals(1, updated.getSelectedNutrientsCount(MainNutrientEnum.BASE.name))
    }

    @Test
    fun addNutrient_multipleDistinct_allPresent() {
        val updated = PreferencesEspece(nutrimentsSelectionnes = emptyMap())
            .addNutrient(MainNutrientEnum.BASE, 1)
            .addNutrient(MainNutrientEnum.BASE, 2)
            .addNutrient(MainNutrientEnum.BASE, 3)
        assertEquals(3, updated.getSelectedNutrientsCount(MainNutrientEnum.BASE.name))
    }

    // ── removeNutrient ────────────────────────────────────────────────────────

    @Test
    fun removeNutrient_existingCoef_isRemoved() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1, 2))
        )
        val updated = prefs.removeNutrient(MainNutrientEnum.BASE, 1)
        assertFalse(updated.isNutrientSelected(MainNutrientEnum.BASE.name, 1))
    }

    @Test
    fun removeNutrient_keepsOtherCoefs() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1, 2))
        )
        val updated = prefs.removeNutrient(MainNutrientEnum.BASE, 1)
        assertTrue(updated.isNutrientSelected(MainNutrientEnum.BASE.name, 2))
    }

    @Test
    fun removeNutrient_doesNotMutateOriginal() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1))
        )
        prefs.removeNutrient(MainNutrientEnum.BASE, 1)
        assertTrue(prefs.isNutrientSelected(MainNutrientEnum.BASE.name, 1))
    }

    @Test
    fun removeNutrient_missingCategory_returnsUnchanged() {
        val prefs = PreferencesEspece(nutrimentsSelectionnes = emptyMap())
        assertEquals(prefs, prefs.removeNutrient(MainNutrientEnum.BASE, 1))
    }

    // ── updateNutrientsForCategory ────────────────────────────────────────────

    @Test
    fun updateNutrientsForCategory_replacesEntireList() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1, 2, 3))
        )
        val updated = prefs.updateNutrientsForCategory(MainNutrientEnum.BASE, listOf(10, 20))
        assertEquals(listOf(10, 20), updated.nutrimentsSelectionnes[MainNutrientEnum.BASE.name])
    }

    @Test
    fun updateNutrientsForCategory_emptyList_clearsCategory() {
        val prefs = PreferencesEspece(
            nutrimentsSelectionnes = mapOf(MainNutrientEnum.BASE.name to listOf(1, 2))
        )
        val updated = prefs.updateNutrientsForCategory(MainNutrientEnum.BASE, emptyList())
        assertEquals(0, updated.getSelectedNutrientsCount(MainNutrientEnum.BASE.name))
    }

    // ── equationsComplementaires ──────────────────────────────────────────────

    @Test
    fun setEquationComplementaire_storesMapping() {
        val updated = PreferencesEspece().setEquationComplementaire("PROTEINE", "eq-uuid-1")
        assertEquals("eq-uuid-1", updated.getEquationComplementaire("PROTEINE"))
    }

    @Test
    fun setEquationComplementaire_doesNotMutateOriginal() {
        val prefs = PreferencesEspece()
        prefs.setEquationComplementaire("PROTEINE", "eq-uuid-1")
        assertNull(prefs.getEquationComplementaire("PROTEINE"))
    }

    @Test
    fun hasEquationComplementaire_afterSet_returnsTrue() {
        val prefs = PreferencesEspece().setEquationComplementaire("PROTEINE", "eq-uuid-1")
        assertTrue(prefs.hasEquationComplementaire("PROTEINE"))
    }

    @Test
    fun hasEquationComplementaire_notSet_returnsFalse() {
        assertFalse(PreferencesEspece().hasEquationComplementaire("PROTEINE"))
    }

    @Test
    fun removeEquationComplementaire_removesIt() {
        val prefs = PreferencesEspece()
            .setEquationComplementaire("PROTEINE", "eq-uuid-1")
            .removeEquationComplementaire("PROTEINE")
        assertFalse(prefs.hasEquationComplementaire("PROTEINE"))
    }

    @Test
    fun removeEquationComplementaire_keepsOthers() {
        val prefs = PreferencesEspece()
            .setEquationComplementaire("PROTEINE", "eq-1")
            .setEquationComplementaire("LIPIDE", "eq-2")
            .removeEquationComplementaire("PROTEINE")
        assertTrue(prefs.hasEquationComplementaire("LIPIDE"))
    }

    @Test
    fun isEquationSelected_knownUuid_returnsTrue() {
        val prefs = PreferencesEspece().setEquationComplementaire("PROTEINE", "eq-uuid-1")
        assertTrue(prefs.isEquationSelected("eq-uuid-1"))
    }

    @Test
    fun isEquationSelected_unknownUuid_returnsFalse() {
        assertFalse(PreferencesEspece().isEquationSelected("unknown-uuid"))
    }

    @Test
    fun addEquation_isInSelectedUuids() {
        val prefs = PreferencesEspece().addEquation("eq-uuid-1")
        assertTrue(prefs.getSelectedEquationUuids().contains("eq-uuid-1"))
    }

    @Test
    fun removeEquation_removesFromSelectedUuids() {
        val prefs = PreferencesEspece().addEquation("eq-uuid-1").removeEquation("eq-uuid-1")
        assertFalse(prefs.getSelectedEquationUuids().contains("eq-uuid-1"))
    }

    @Test
    fun addEquation_idempotent_noDuplicate() {
        val prefs = PreferencesEspece().addEquation("eq-uuid-1").addEquation("eq-uuid-1")
        assertEquals(1, prefs.getSelectedEquationUuids().size)
    }
}

// ── PreferencesApplication ────────────────────────────────────────────────────

class PreferencesApplicationTest {

    @Test
    fun getPreferencesEspece_existingEntry_returnsIt() {
        val prefs = PreferencesEspece(espece = Espece.CHIEN.name)
        val app = PreferencesApplication(
            preferencesParEspece = mapOf(Espece.CHIEN.name to prefs)
        )
        assertEquals(prefs, app.getPreferencesEspece(Espece.CHIEN))
    }

    @Test
    fun getPreferencesEspece_missingEntry_returnsDefault() {
        val app = PreferencesApplication(preferencesParEspece = emptyMap())
        val result = app.getPreferencesEspece(Espece.CHIEN)
        assertEquals(Espece.CHIEN, result.getEspeceEnum())
    }

    @Test
    fun updatePreferencesEspece_storesUpdated() {
        val app = PreferencesApplication()
        val prefs = PreferencesEspece(espece = Espece.CHAT.name)
        val updated = app.updatePreferencesEspece(prefs)
        assertEquals(prefs, updated.getPreferencesEspece(Espece.CHAT))
    }

    @Test
    fun updatePreferencesEspece_doesNotMutateOriginal() {
        val app = PreferencesApplication(preferencesParEspece = emptyMap())
        val prefs = PreferencesEspece(espece = Espece.CHAT.name)
        app.updatePreferencesEspece(prefs)
        assertTrue(app.preferencesParEspece.isEmpty())
    }
}
