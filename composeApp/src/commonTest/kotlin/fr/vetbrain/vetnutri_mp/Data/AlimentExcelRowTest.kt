package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlimentExcelRowTest {

    // ── encodeEnergieParEspece ──────────────────────────────────────────────

    @Test
    fun encodeEnergieParEspece_empty_returnsEmptyString() {
        assertEquals("", AlimentExcelRow.encodeEnergieParEspece(emptyMap()))
    }

    @Test
    fun encodeEnergieParEspece_singleEntry_encodesWithoutSeparator() {
        assertEquals("CHIEN:340.0", AlimentExcelRow.encodeEnergieParEspece(mapOf("CHIEN" to 340.0)))
    }

    @Test
    fun encodeEnergieParEspece_multipleEntries_joinsWithSemicolon() {
        val encoded = AlimentExcelRow.encodeEnergieParEspece(
            linkedMapOf("CHIEN" to 340.0, "CHAT" to 330.0)
        )
        assertEquals("CHIEN:340.0;CHAT:330.0", encoded)
    }

    // ── decodeEnergieParEspece ───────────────────────────────────────────────

    @Test
    fun decodeEnergieParEspece_null_returnsEmptyMap() {
        assertEquals(emptyMap(), AlimentExcelRow.decodeEnergieParEspece(null))
    }

    @Test
    fun decodeEnergieParEspece_blank_returnsEmptyMap() {
        assertEquals(emptyMap(), AlimentExcelRow.decodeEnergieParEspece("   "))
    }

    @Test
    fun decodeEnergieParEspece_singleEntry_decodesCorrectly() {
        assertEquals(mapOf("CHIEN" to 340.0), AlimentExcelRow.decodeEnergieParEspece("CHIEN:340"))
    }

    @Test
    fun decodeEnergieParEspece_multipleEntries_decodesAll() {
        val decoded = AlimentExcelRow.decodeEnergieParEspece("CHIEN:340;CHAT:330")
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), decoded)
    }

    @Test
    fun decodeEnergieParEspece_toleratesWhitespaceAroundEntries() {
        val decoded = AlimentExcelRow.decodeEnergieParEspece(" CHIEN : 340 ; CHAT : 330 ")
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), decoded)
    }

    @Test
    fun decodeEnergieParEspece_toleratesCommaDecimalSeparator() {
        val decoded = AlimentExcelRow.decodeEnergieParEspece("CHIEN:340,5")
        assertEquals(mapOf("CHIEN" to 340.5), decoded)
    }

    @Test
    fun decodeEnergieParEspece_ignoresMalformedEntry() {
        // "CHAT" n'a pas de valeur -> entrée ignorée, le reste est conservé
        val decoded = AlimentExcelRow.decodeEnergieParEspece("CHIEN:340;CHAT")
        assertEquals(mapOf("CHIEN" to 340.0), decoded)
    }

    @Test
    fun decodeEnergieParEspece_ignoresNonNumericValue() {
        val decoded = AlimentExcelRow.decodeEnergieParEspece("CHIEN:abc;CHAT:330")
        assertEquals(mapOf("CHAT" to 330.0), decoded)
    }

    // ── round-trip encode/decode ─────────────────────────────────────────────

    @Test
    fun encodeThenDecode_roundTripsToSameMap() {
        val original = mapOf("CHIEN" to 340.0, "CHAT" to 330.0, "FURET" to 300.0)
        val decoded = AlimentExcelRow.decodeEnergieParEspece(AlimentExcelRow.encodeEnergieParEspece(original))
        assertEquals(original, decoded)
    }

    // ── fromAlimentEv / toAlimentEv ──────────────────────────────────────────

    @Test
    fun fromAlimentEv_copiesEnergieParEspece() {
        val aliment = AlimentEv(nom = "Test").apply {
            energieParEspece = mapOf("CHIEN" to 340.0, "CHAT" to 330.0)
        }
        val row = AlimentExcelRow.fromAlimentEv(aliment)
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), row.energieParEspece)
    }

    @Test
    fun toAlimentEv_restoresEnergieParEspece() {
        val row = AlimentExcelRow(
            nom = "Test",
            energieParEspece = mapOf("CHIEN" to 340.0, "CHAT" to 330.0)
        )
        val aliment = AlimentExcelRow.toAlimentEv(row)
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), aliment.energieParEspece)
    }

    @Test
    fun fromAlimentEv_thenToAlimentEv_roundTripsEnergieParEspece() {
        val original = AlimentEv(nom = "Croquettes").apply {
            energieParEspece = mapOf("CHIEN" to 340.0, "CHAT" to 330.0)
        }
        val restored = AlimentExcelRow.toAlimentEv(AlimentExcelRow.fromAlimentEv(original))
        assertEquals(original.energieParEspece, restored.energieParEspece)
    }

    @Test
    fun fromAlimentEv_noEnergieParEspece_producesEmptyMap() {
        val aliment = AlimentEv(nom = "Sans energie par espece")
        val row = AlimentExcelRow.fromAlimentEv(aliment)
        assertTrue(row.energieParEspece.isEmpty())
    }
}
