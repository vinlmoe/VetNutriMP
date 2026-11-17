package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiblioRefTest {

    // ── isConsistent ───────────────────────────────────────────────────────────

    @Test
    fun isConsistent_consistentOne_returnsTrue() {
        assertTrue(BiblioRef(consistent = 1).isConsistent())
    }

    @Test
    fun isConsistent_consistentZero_returnsFalse() {
        assertFalse(BiblioRef(consistent = 0).isConsistent())
    }

    @Test
    fun isConsistent_consistentNegative_returnsFalse() {
        assertFalse(BiblioRef(consistent = -1).isConsistent())
    }

    @Test
    fun isConsistent_consistentGreaterThanOne_returnsTrue() {
        assertTrue(BiblioRef(consistent = 5).isConsistent())
    }

    // ── toString ───────────────────────────────────────────────────────────────

    @Test
    fun toString_withAuthorAndYear_returnsFormattedString() {
        val ref = BiblioRef(firstAuthor = "Martin", year = 2020)
        assertEquals("Martin, 2020", ref.toString())
    }

    @Test
    fun toString_defaultValues_usesDefaults() {
        val ref = BiblioRef(uuid = "fixed-uuid")
        assertEquals(", 1800", ref.toString())
    }

    // ── Constructeur secondaire ────────────────────────────────────────────────

    @Test
    fun secondaryConstructor_uuidOnly_setsConsistentToZero() {
        val ref = BiblioRef("my-uuid")
        assertEquals(0, ref.consistent)
    }

    @Test
    fun secondaryConstructor_uuidOnly_firstAuthorEmpty() {
        val ref = BiblioRef("my-uuid")
        assertEquals("", ref.firstAuthor)
    }

    @Test
    fun secondaryConstructor_uuidOnly_storedCorrectly() {
        val ref = BiblioRef("specific-uuid")
        assertEquals("specific-uuid", ref.uuid)
    }

    // ── Companion EMPTY ────────────────────────────────────────────────────────

    @Test
    fun EMPTY_isConsistent_returnsTrue() {
        assertTrue(BiblioRef.EMPTY.isConsistent())
    }

    @Test
    fun EMPTY_firstAuthor_isEmpty() {
        assertEquals("", BiblioRef.EMPTY.firstAuthor)
    }

    // ── Égalité data class ─────────────────────────────────────────────────────

    @Test
    fun equality_sameFields_areEqual() {
        val b1 = BiblioRef(uuid = "x", firstAuthor = "A", year = 2000, consistent = 1)
        val b2 = BiblioRef(uuid = "x", firstAuthor = "A", year = 2000, consistent = 1)
        assertEquals(b1, b2)
    }

    @Test
    fun equality_differentUuid_notEqual() {
        val b1 = BiblioRef(uuid = "x", firstAuthor = "A", year = 2000)
        val b2 = BiblioRef(uuid = "y", firstAuthor = "A", year = 2000)
        assertTrue(b1 != b2)
    }

    @Test
    fun copy_modifyYear_otherFieldsUnchanged() {
        val original = BiblioRef(uuid = "u", firstAuthor = "Dupont", year = 2010, consistent = 1)
        val modified = original.copy(year = 2023)
        assertEquals("Dupont", modified.firstAuthor)
        assertEquals(2023, modified.year)
        assertEquals(1, modified.consistent)
    }
}
