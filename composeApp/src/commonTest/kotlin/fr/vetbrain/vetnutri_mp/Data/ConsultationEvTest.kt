package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class ConsultationEvTest {

    // ── effectiveWeight ───────────────────────────────────────────────────────

    @Test
    fun effectiveWeight_noWeightOrIdeal_returnsNull() {
        assertNull(ConsultationEv(weight = null, idealWeight = null).effectiveWeight)
    }

    @Test
    fun effectiveWeight_weightOnlyNoIdeal_returnsWeight() {
        assertEquals(12.5, ConsultationEv(weight = 12.5, idealWeight = null).effectiveWeight)
    }

    @Test
    fun effectiveWeight_idealWeightPresent_returnsIdealOverWeight() {
        assertEquals(12.0, ConsultationEv(weight = 15.0, idealWeight = 12.0).effectiveWeight)
    }

    @Test
    fun effectiveWeight_sameResultOnRepeatCall() {
        val c = ConsultationEv(weight = 10.0, idealWeight = null)
        assertEquals(c.effectiveWeight, c.effectiveWeight)
    }

    @Test
    fun effectiveWeight_updatesAfterWeightChange() {
        val c = ConsultationEv(weight = 10.0, idealWeight = null)
        assertEquals(10.0, c.effectiveWeight)
        c.weight = 20.0
        assertEquals(20.0, c.effectiveWeight)
    }

    @Test
    fun effectiveWeight_updatesAfterIdealWeightSet() {
        val c = ConsultationEv(weight = 10.0, idealWeight = null)
        assertEquals(10.0, c.effectiveWeight)
        c.idealWeight = 8.0
        assertEquals(8.0, c.effectiveWeight)
    }

    // ── ajouterReferenceMaladie / contient / supprimer ────────────────────────

    @Test
    fun ajouterReferenceMaladie_addsReference() {
        val c = ConsultationEv()
        c.ajouterReferenceMaladie("ref-1")
        assertTrue(c.contientReferenceMaladie("ref-1"))
    }

    @Test
    fun ajouterReferenceMaladie_noDuplicates() {
        val c = ConsultationEv()
        c.ajouterReferenceMaladie("ref-1")
        c.ajouterReferenceMaladie("ref-1")
        assertEquals(1, c.referencesMaladies.size)
    }

    @Test
    fun ajouterReferenceMaladie_multipleDistinct_allAdded() {
        val c = ConsultationEv()
        c.ajouterReferenceMaladie("ref-1")
        c.ajouterReferenceMaladie("ref-2")
        assertEquals(2, c.referencesMaladies.size)
    }

    @Test
    fun supprimerReferenceMaladie_removesIt() {
        val c = ConsultationEv()
        c.ajouterReferenceMaladie("ref-1")
        c.supprimerReferenceMaladie("ref-1")
        assertFalse(c.contientReferenceMaladie("ref-1"))
    }

    @Test
    fun supprimerReferenceMaladie_unknownId_doesNotThrow() {
        val c = ConsultationEv()
        c.supprimerReferenceMaladie("no-such-ref")
        assertTrue(c.referencesMaladies.isEmpty())
    }

    @Test
    fun supprimerReferenceMaladie_keepsOtherReferences() {
        val c = ConsultationEv()
        c.ajouterReferenceMaladie("ref-1")
        c.ajouterReferenceMaladie("ref-2")
        c.supprimerReferenceMaladie("ref-1")
        assertTrue(c.contientReferenceMaladie("ref-2"))
    }

    // ── obtenirToutesReferences ───────────────────────────────────────────────

    @Test
    fun obtenirToutesReferences_noReferences_returnsEmpty() {
        assertTrue(ConsultationEv(referenceGeneraleId = null).obtenirToutesReferences().isEmpty())
    }

    @Test
    fun obtenirToutesReferences_generalOnly_containsIt() {
        val c = ConsultationEv(referenceGeneraleId = "gen-1")
        assertEquals(listOf("gen-1"), c.obtenirToutesReferences())
    }

    @Test
    fun obtenirToutesReferences_generalAndDiseases_allIncluded() {
        val c = ConsultationEv(referenceGeneraleId = "gen-1")
        c.ajouterReferenceMaladie("dis-1")
        c.ajouterReferenceMaladie("dis-2")
        val refs = c.obtenirToutesReferences()
        assertEquals(3, refs.size)
        assertTrue(refs.contains("gen-1"))
        assertTrue(refs.contains("dis-1"))
        assertTrue(refs.contains("dis-2"))
    }

    @Test
    fun obtenirToutesReferences_diseasesOnly_noGeneral_returnsOnlyDiseases() {
        val c = ConsultationEv(referenceGeneraleId = null)
        c.ajouterReferenceMaladie("dis-1")
        assertEquals(listOf("dis-1"), c.obtenirToutesReferences())
    }

    // ── ajouterMotCle / contient / supprimer ──────────────────────────────────

    @Test
    fun ajouterMotCle_addsKeyword() {
        val c = ConsultationEv()
        c.ajouterMotCle("kw-1")
        assertTrue(c.contientMotCle("kw-1"))
    }

    @Test
    fun ajouterMotCle_noDuplicates() {
        val c = ConsultationEv()
        c.ajouterMotCle("kw-1")
        c.ajouterMotCle("kw-1")
        assertEquals(1, c.keywordIds.size)
    }

    @Test
    fun supprimerMotCle_removesIt() {
        val c = ConsultationEv()
        c.ajouterMotCle("kw-1")
        c.supprimerMotCle("kw-1")
        assertFalse(c.contientMotCle("kw-1"))
    }

    @Test
    fun supprimerMotCle_keepsOtherKeywords() {
        val c = ConsultationEv()
        c.ajouterMotCle("kw-1")
        c.ajouterMotCle("kw-2")
        c.supprimerMotCle("kw-1")
        assertTrue(c.contientMotCle("kw-2"))
    }

    @Test
    fun contientMotCle_unknownKeyword_returnsFalse() {
        assertFalse(ConsultationEv().contientMotCle("unknown"))
    }
}
