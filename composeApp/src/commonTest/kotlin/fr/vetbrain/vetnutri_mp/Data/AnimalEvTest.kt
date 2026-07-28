package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class AnimalEvTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 1.0) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    // ── getSex / setSex ───────────────────────────────────────────────────────

    @Test
    fun getSex_defaultSexId_returnsMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, AnimalEv().getSex())
    }

    @Test
    fun setSex_thenGetSex_roundTrips() {
        val animal = AnimalEv()
        animal.setSex(Sex.FEMELLE_STERILISEE)
        assertEquals(Sex.FEMELLE_STERILISEE, animal.getSex())
    }

    @Test
    fun getSex_unknownId_defaultsToMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, AnimalEv(sexId = 999).getSex())
    }

    @Test
    fun setSex_allValues_roundTrip() {
        Sex.values().forEach { sex ->
            val animal = AnimalEv()
            animal.setSex(sex)
            assertEquals(sex, animal.getSex(), "Round-trip failed for $sex")
        }
    }

    // ── getEspece / setEspece ─────────────────────────────────────────────────

    @Test
    fun getEspece_defaultSpecieId_returnsChien() {
        assertEquals(Espece.CHIEN, AnimalEv().getEspece())
    }

    @Test
    fun setEspece_thenGetEspece_roundTrips() {
        val animal = AnimalEv()
        animal.setEspece(Espece.CHAT)
        assertEquals(Espece.CHAT, animal.getEspece())
    }

    @Test
    fun getEspece_byLabel_resolves() {
        assertEquals(Espece.LAPIN, AnimalEv(specieId = Espece.LAPIN.label).getEspece())
    }

    @Test
    fun getEspece_byEnumName_resolves() {
        assertEquals(Espece.CHAT, AnimalEv(specieId = "CHAT").getEspece())
    }

    @Test
    fun getEspece_byNumericId_resolves() {
        assertEquals(Espece.CHEVAL, AnimalEv(specieId = Espece.CHEVAL.id).getEspece())
    }

    @Test
    fun getEspece_unknownSpecieId_defaultsToChien() {
        assertEquals(Espece.CHIEN, AnimalEv(specieId = "UNKNOWN_ANIMAL_XYZ").getEspece())
    }

    @Test
    fun setEspece_allValues_roundTrip() {
        Espece.values().forEach { espece ->
            val animal = AnimalEv()
            animal.setEspece(espece)
            assertEquals(espece, animal.getEspece(), "Round-trip failed for $espece")
        }
    }

    // ── getBEE ────────────────────────────────────────────────────────────────

    @Test
    fun getBEE_noConsultations_returnsNull() {
        assertNull(AnimalEv().getBEE())
    }

    @Test
    fun getBEE_consultationWithNullWeight_returnsNull() {
        val animal = AnimalEv(
            consultations = mutableListOf(ConsultationEv(weight = null))
        )
        assertNull(animal.getBEE())
    }

    @Test
    fun getBEE_consultationWithWeight_returnsExpectedBEE() {
        val animal = AnimalEv(
            consultations = mutableListOf(ConsultationEv(weight = 10.0))
        )
        val bee = animal.getBEE()
        assertNotNull(bee)
        // BEE = 130 * 10^0.75 ≈ 730.9
        assertNear(730.9, bee!!)
    }

    @Test
    fun getBEE_usesLastConsultation() {
        val animal = AnimalEv(
            consultations = mutableListOf(
                ConsultationEv(weight = 5.0),
                ConsultationEv(weight = 20.0)
            )
        )
        val bee = animal.getBEE()
        assertNotNull(bee)
        // BEE = 130 * 20^0.75 ≈ 1229.4
        assertNear(1229.4, bee!!, tol = 2.0)
    }

    // ── createTestAnimal ──────────────────────────────────────────────────────

    @Test
    fun createTestAnimal_hasExpectedName() {
        assertEquals("Rex", AnimalEv.createTestAnimal().nom)
    }

    @Test
    fun createTestAnimal_isNotDead() {
        assertFalse(AnimalEv.createTestAnimal().dead)
    }

    @Test
    fun createTestAnimal_isChien() {
        assertEquals(Espece.CHIEN, AnimalEv.createTestAnimal().getEspece())
    }

    @Test
    fun createTestAnimal_isMaleEntier() {
        assertEquals(Sex.MALE_ENTIER, AnimalEv.createTestAnimal().getSex())
    }

    @Test
    fun createTestAnimal_hasOwner() {
        assertTrue(AnimalEv.createTestAnimal().ownerName.isNotBlank())
    }
}
