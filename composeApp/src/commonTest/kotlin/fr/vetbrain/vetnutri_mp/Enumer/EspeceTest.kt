package fr.vetbrain.vetnutri_mp.Enumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EspeceTest {

    // ── getFromString ─────────────────────────────────────────────────────────

    @Test
    fun getFromString_blank_returnsNull() {
        assertNull(Espece.getFromString(""))
    }

    @Test
    fun getFromString_byLabel_resolves() {
        assertEquals(Espece.CHIEN, Espece.getFromString("DOG"))
    }

    @Test
    fun getFromString_byLabelLowercase_resolves() {
        assertEquals(Espece.CHAT, Espece.getFromString("cat"))
    }

    @Test
    fun getFromString_byEnumName_resolves() {
        assertEquals(Espece.LAPIN, Espece.getFromString("LAPIN"))
    }

    @Test
    fun getFromString_byNumericStringId_resolves() {
        assertEquals(Espece.CHIEN, Espece.getFromString("0"))
        assertEquals(Espece.CHAT, Espece.getFromString("1"))
    }

    @Test
    fun getFromString_unknownString_returnsNull() {
        assertNull(Espece.getFromString("UNKNOWN_ANIMAL_XYZ"))
    }

    @Test
    fun getFromString_stripsQuotesAndBrackets() {
        assertEquals(Espece.CHAT, Espece.getFromString("[\"CAT\"]"))
    }

    @Test
    fun getFromString_trimmedInput_resolves() {
        assertEquals(Espece.CHIEN, Espece.getFromString("  DOG  "))
    }

    // ── getStringFromInt ──────────────────────────────────────────────────────

    @Test
    fun getStringFromInt_chienCategory_returnsDog() {
        assertEquals(Espece.CHIEN.label, Espece.getStringFromInt(0))
    }

    @Test
    fun getStringFromInt_chatCategory_returnsCat() {
        assertEquals(Espece.CHAT.label, Espece.getStringFromInt(1))
    }

    @Test
    fun getStringFromInt_unknownCategory_returnsChienLabel() {
        assertEquals(Espece.CHIEN.label, Espece.getStringFromInt(999))
    }

    // ── getStringFromId ───────────────────────────────────────────────────────

    @Test
    fun getStringFromId_chienId_returnsLabel() {
        assertEquals(Espece.CHIEN.label, Espece.getStringFromId(Espece.CHIEN.id))
    }

    @Test
    fun getStringFromId_unknownId_returnsChienLabel() {
        assertEquals(Espece.CHIEN.label, Espece.getStringFromId("UNKNOWN"))
    }

    // ── getEnumFromInt ────────────────────────────────────────────────────────

    @Test
    fun getEnumFromInt_chienCategory_returnsChien() {
        assertEquals(Espece.CHIEN, Espece.getEnumFromInt(0))
    }

    @Test
    fun getEnumFromInt_chatCategory_returnsChat() {
        assertEquals(Espece.CHAT, Espece.getEnumFromInt(1))
    }

    @Test
    fun getEnumFromInt_unknownId_returnsChien() {
        assertEquals(Espece.CHIEN, Espece.getEnumFromInt(999))
    }

    // ── getEnumFromString ─────────────────────────────────────────────────────

    @Test
    fun getEnumFromString_knownLabel_resolves() {
        assertEquals(Espece.CHEVAL, Espece.getEnumFromString("CHEVAL"))
    }

    @Test
    fun getEnumFromString_unknownString_defaultsToChien() {
        assertEquals(Espece.CHIEN, Espece.getEnumFromString("UNKNOWN_XYZ"))
    }

    // ── valuesExcept() ────────────────────────────────────────────────────────

    @Test
    fun valuesExcept_excludesCH() {
        assertTrue(Espece.valuesExcept().none { it == Espece.CH })
    }

    @Test
    fun valuesExcept_containsChienAndChat() {
        val result = Espece.valuesExcept()
        assertTrue(result.contains(Espece.CHIEN))
        assertTrue(result.contains(Espece.CHAT))
    }

    @Test
    fun valuesExcept_hasExpectedSize() {
        // All entries minus CH
        assertEquals(Espece.entries.size - 1, Espece.valuesExcept().size)
    }

    // ── valuesExcept(vararg) ──────────────────────────────────────────────────

    @Test
    fun valuesExceptVararg_excludesSpecifiedValues() {
        val result = Espece.valuesExcept(Espece.CHIEN, Espece.CHAT)
        assertTrue(result.none { it == Espece.CHIEN })
        assertTrue(result.none { it == Espece.CHAT })
    }

    @Test
    fun valuesExceptVararg_keepsOthers() {
        val result = Espece.valuesExcept(Espece.CHIEN)
        assertTrue(result.contains(Espece.CHAT))
        assertTrue(result.contains(Espece.LAPIN))
    }

    // ── getByLabel ────────────────────────────────────────────────────────────

    @Test
    fun getByLabel_exactLabel_returnsEnum() {
        assertEquals(Espece.CHIEN, Espece.getByLabel("DOG"))
    }

    @Test
    fun getByLabel_unknownLabel_returnsNull() {
        assertNull(Espece.getByLabel("UNKNOWN"))
    }

    // ── getEnumFromStringId ───────────────────────────────────────────────────

    @Test
    fun getEnumFromStringId_exactLabel_returnsEnum() {
        assertEquals(Espece.CHAT, Espece.getEnumFromStringId(Espece.CHAT.label))
    }

    @Test
    fun getEnumFromStringId_unknownLabel_returnsNull() {
        assertNull(Espece.getEnumFromStringId("UNKNOWN"))
    }
}
