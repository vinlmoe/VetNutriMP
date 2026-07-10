package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AlimentExcelServiceTest {

    private val service = AlimentExcelService()

    private fun alimentAvecEnergieParEspece(
        nom: String = "Croquettes Test",
        energieParEspece: Map<String, Double> = mapOf("CHIEN" to 340.0, "CHAT" to 330.0)
    ): AlimentEv = AlimentEv(nom = nom).apply {
        this.energieParEspece = energieParEspece
    }

    // ── Export ───────────────────────────────────────────────────────────────

    @Test
    fun exportToCsv_containsEnergieParEspeceHeader() {
        val csv = service.exportToCsv(listOf(alimentAvecEnergieParEspece()))
        val header = csv.lineSequence().first()
        assertTrue(header.contains("Énergie par Espèce"))
    }

    @Test
    fun exportToCsv_encodesEnergieParEspeceInExpectedFormat() {
        val csv = service.exportToCsv(listOf(alimentAvecEnergieParEspece()))
        // La cellule contient un ";" donc elle doit être entourée de guillemets par l'échappement CSV
        assertTrue(csv.contains("\"CHIEN:340.0;CHAT:330.0\""))
    }

    @Test
    fun exportToCsv_noEnergieParEspece_leavesCellEmpty() {
        val aliment = alimentAvecEnergieParEspece(energieParEspece = emptyMap())
        val csv = service.exportToCsv(listOf(aliment))
        val dataLine = csv.lineSequence().drop(1).first()
        // Aucune séquence "ESPECE:valeur" ne doit apparaître
        assertTrue(!dataLine.contains(":340") && !dataLine.contains(":330"))
    }

    // ── Round-trip export -> import ────────────────────────────────────────

    @Test
    fun exportThenImport_roundTripsEnergieParEspece() {
        val original = alimentAvecEnergieParEspece()
        val csv = service.exportToCsv(listOf(original))

        val result = service.importFromCsv(csv)

        assertEquals(emptyList(), result.errors)
        assertEquals(1, result.aliments.size)
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), result.aliments.first().energieParEspece)
    }

    @Test
    fun exportThenImport_multipleAliments_eachKeepsOwnEnergieParEspece() {
        val chien = alimentAvecEnergieParEspece("Aliment Chien", mapOf("CHIEN" to 340.0))
        val chat = alimentAvecEnergieParEspece("Aliment Chat", mapOf("CHAT" to 330.0))
        val csv = service.exportToCsv(listOf(chien, chat))

        val result = service.importFromCsv(csv)

        assertEquals(2, result.aliments.size)
        val parNom = result.aliments.associateBy { it.nom }
        assertEquals(mapOf("CHIEN" to 340.0), parNom["Aliment Chien"]?.energieParEspece)
        assertEquals(mapOf("CHAT" to 330.0), parNom["Aliment Chat"]?.energieParEspece)
    }

    @Test
    fun exportThenImport_noEnergieParEspece_roundTripsToEmptyMap() {
        val original = alimentAvecEnergieParEspece(energieParEspece = emptyMap())
        val csv = service.exportToCsv(listOf(original))

        val result = service.importFromCsv(csv)

        assertTrue(result.aliments.first().energieParEspece.isEmpty())
    }

    // ── Import direct d'un CSV écrit à la main ──────────────────────────────

    @Test
    fun importFromCsv_parsesHandWrittenEnergieParEspeceColumn() {
        val headers = "UUID;Nom;Marque;Gamme;Ingrédients;Groupe Alimentaire;Type Aliment;Conditionnement;" +
                "Prix;Catégorie Prix;Quantité Interne;Consistant;Obsolète;Date dernière mise à jour;" +
                "Données Base;Espèces;Énergie par Espèce;Indications;UUID Ration"
        val line = "test-uuid;Aliment Manuel;;;;;;;;;;false;false;2024-01-01;;;\"CHIEN:340;CHAT:330\";;"
        val csv = "$headers\n$line"

        val result = service.importFromCsv(csv)

        assertEquals(emptyList(), result.errors)
        assertEquals(1, result.aliments.size)
        assertEquals(mapOf("CHIEN" to 340.0, "CHAT" to 330.0), result.aliments.first().energieParEspece)
    }

    @Test
    fun generateExampleCsv_includesEnergieParEspeceExample() {
        val csv = AlimentExcelService.generateExampleCsv()
        assertTrue(csv.contains("Énergie par Espèce"))
        assertTrue(csv.contains("CHIEN:340.0;CHAT:330.0"))
    }

    // ── Sauts de ligne dans les champs texte ────────────────────────────────

    @Test
    fun exportToCsv_ingredientsWithEmbeddedNewline_isQuoted() {
        val aliment = AlimentEv(nom = "Test", ingredients = "Poulet\nRiz\nMaïs")
        val csv = service.exportToCsv(listOf(aliment))
        assertTrue(csv.contains("\"Poulet\nRiz\nMaïs\""))
    }

    @Test
    fun exportThenImport_ingredientsWithEmbeddedNewline_roundTrips() {
        val original = AlimentEv(nom = "Test", ingredients = "Poulet\nRiz\nMaïs")
        val csv = service.exportToCsv(listOf(original))

        val result = service.importFromCsv(csv)

        assertEquals(emptyList(), result.errors)
        assertEquals(1, result.aliments.size)
        assertEquals("Poulet\nRiz\nMaïs", result.aliments.first().ingredients)
    }

    @Test
    fun exportThenImport_ingredientsWithWindowsLineEnding_roundTrips() {
        val original = AlimentEv(nom = "Test", ingredients = "Poulet\r\nRiz\r\nMaïs")
        val csv = service.exportToCsv(listOf(original))

        val result = service.importFromCsv(csv)

        assertEquals(1, result.aliments.size)
        assertEquals("Poulet\r\nRiz\r\nMaïs", result.aliments.first().ingredients)
    }

    @Test
    fun exportThenImport_ingredientsWithLoneCarriageReturn_roundTrips() {
        // Retour chariot isolé (sans saut de ligne) : ancien format Mac / saisie non normalisée.
        // Doit être protégé par des guillemets à l'export, sinon il casserait le découpage des
        // lignes CSV à la réimportation.
        val original = AlimentEv(nom = "Test", ingredients = "Poulet\rRiz\rMaïs")
        val csv = service.exportToCsv(listOf(original))

        assertTrue(csv.contains("\"Poulet\rRiz\rMaïs\""))

        val result = service.importFromCsv(csv)

        assertEquals(emptyList(), result.errors)
        assertEquals(1, result.aliments.size)
        assertEquals("Poulet\rRiz\rMaïs", result.aliments.first().ingredients)
    }

    @Test
    fun exportThenImport_multilineFieldDoesNotCorruptOtherAliments() {
        val avecSautDeLigne = AlimentEv(nom = "Aliment Multiligne", ingredients = "Poulet\nRiz")
        val suivant = AlimentEv(nom = "Aliment Suivant", ingredients = "Simple")
        val csv = service.exportToCsv(listOf(avecSautDeLigne, suivant))

        val result = service.importFromCsv(csv)

        assertEquals(emptyList(), result.errors)
        assertEquals(2, result.aliments.size)
        val parNom = result.aliments.associateBy { it.nom }
        assertEquals("Poulet\nRiz", parNom["Aliment Multiligne"]?.ingredients)
        assertEquals("Simple", parNom["Aliment Suivant"]?.ingredients)
    }
}
