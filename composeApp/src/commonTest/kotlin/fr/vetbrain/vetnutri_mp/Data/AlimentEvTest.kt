package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class AlimentEvTest {

    // ── setNutrient / getNutrient / hasNutrient ───────────────────────────────

    @Test
    fun getNutrient_absent_returnsNull() {
        assertNull(AlimentEv().getNutrient(NutrientMain.PROTEINE))
    }

    @Test
    fun setNutrient_thenGetNutrient_returnsValue() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.PROTEINE, 25.0)
        assertEquals(25.0, aliment.getNutrient(NutrientMain.PROTEINE))
    }

    @Test
    fun setNutrient_negativeValue_getNutrientReturnsZero() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.PROTEINE, -5.0)
        assertEquals(0.0, aliment.getNutrient(NutrientMain.PROTEINE))
    }

    @Test
    fun setNutrient_zeroValue_getNutrientReturnsZero() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.LIPIDE, 0.0)
        assertEquals(0.0, aliment.getNutrient(NutrientMain.LIPIDE))
    }

    @Test
    fun setNutrient_overwritesPreviousValue() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.PROTEINE, 20.0)
        aliment.setNutrient(NutrientMain.PROTEINE, 30.0)
        assertEquals(30.0, aliment.getNutrient(NutrientMain.PROTEINE))
    }

    @Test
    fun setNutrient_multipleNutrients_eachReturnsCorrectValue() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.PROTEINE, 25.0)
        aliment.setNutrient(NutrientMain.LIPIDE, 12.0)
        aliment.setNutrient(NutrientMacro.CAL, 1.5)
        assertEquals(25.0, aliment.getNutrient(NutrientMain.PROTEINE))
        assertEquals(12.0, aliment.getNutrient(NutrientMain.LIPIDE))
        assertEquals(1.5, aliment.getNutrient(NutrientMacro.CAL))
    }

    // ── hasNutrient ───────────────────────────────────────────────────────────

    @Test
    fun hasNutrient_notSet_returnsFalse() {
        assertFalse(AlimentEv().hasNutrient(NutrientMain.PROTEINE))
    }

    @Test
    fun hasNutrient_afterSet_returnsTrue() {
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMain.PROTEINE, 20.0)
        assertTrue(aliment.hasNutrient(NutrientMain.PROTEINE))
    }

    // ── AAEnum protection VF24 ────────────────────────────────────────────────

    @Test
    fun getNutrient_aminoAcidWithVF24_returnsNull() {
        val aliment = AlimentEv(dataB = "VF24")
        aliment.setNutrient(AAEnum.ARGININE, 1.0)
        assertNull(aliment.getNutrient(AAEnum.ARGININE))
    }

    @Test
    fun getNutrient_aminoAcidWithOtherDb_returnsValue() {
        val aliment = AlimentEv(dataB = "OTHER_DB")
        aliment.setNutrient(AAEnum.ARGININE, 1.5)
        assertEquals(1.5, aliment.getNutrient(AAEnum.ARGININE))
    }

    @Test
    fun getNutrient_aminoAcidNoDb_returnsValue() {
        val aliment = AlimentEv(dataB = null)
        aliment.setNutrient(AAEnum.ARGININE, 2.0)
        assertEquals(2.0, aliment.getNutrient(AAEnum.ARGININE))
    }

    // ── getEspecesList / isForEspece ──────────────────────────────────────────

    @Test
    fun getEspecesList_emptyEspeces_returnsEmpty() {
        assertTrue(AlimentEv(especes = mutableListOf()).getEspecesList().isEmpty())
    }

    @Test
    fun getEspecesList_validLabels_resolvesEspeces() {
        val aliment = AlimentEv(especes = mutableListOf(Espece.CHIEN.label, Espece.CHAT.label))
        val list = aliment.getEspecesList()
        assertEquals(2, list.size)
        assertTrue(list.contains(Espece.CHIEN))
        assertTrue(list.contains(Espece.CHAT))
    }

    @Test
    fun getEspecesList_unknownLabel_isSkipped() {
        val aliment = AlimentEv(especes = mutableListOf("UNKNOWN_SPECIES"))
        assertTrue(aliment.getEspecesList().isEmpty())
    }

    @Test
    fun isForEspece_specieInList_returnsTrue() {
        val aliment = AlimentEv(especes = mutableListOf(Espece.CHIEN.label))
        assertTrue(aliment.isForEspece(Espece.CHIEN))
    }

    @Test
    fun isForEspece_specieNotInList_returnsFalse() {
        val aliment = AlimentEv(especes = mutableListOf(Espece.CHIEN.label))
        assertFalse(aliment.isForEspece(Espece.CHAT))
    }

    @Test
    fun isForEspece_emptyList_returnsFalse() {
        assertFalse(AlimentEv(especes = mutableListOf()).isForEspece(Espece.CHIEN))
    }

    // ── getIndications / hasIndication ────────────────────────────────────────

    @Test
    fun getIndications_emptyList_returnsEmpty() {
        assertTrue(AlimentEv().getIndications().isEmpty())
    }

    @Test
    fun getIndications_returnsAllStored() {
        val aliment = AlimentEv(indicat = mutableListOf(AlimIndic.OBES, AlimIndic.DIAB))
        val indics = aliment.getIndications()
        assertEquals(2, indics.size)
        assertTrue(indics.contains(AlimIndic.OBES))
        assertTrue(indics.contains(AlimIndic.DIAB))
    }

    @Test
    fun hasIndication_presentIndication_returnsTrue() {
        val aliment = AlimentEv(indicat = mutableListOf(AlimIndic.MRC))
        assertTrue(aliment.hasIndication(AlimIndic.MRC))
    }

    @Test
    fun hasIndication_absentIndication_returnsFalse() {
        assertFalse(AlimentEv().hasIndication(AlimIndic.OBES))
    }
}
