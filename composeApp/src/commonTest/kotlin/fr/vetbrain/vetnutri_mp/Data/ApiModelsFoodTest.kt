package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApiModelsFoodTest {

    // ── AlimentEv.toApi() : biblioRefIds ─────────────────────────────────────

    @Test
    fun toApi_noBiblioRefs_producesEmptyBiblioRefIds() {
        val aliment = AlimentEv(nom = "Sans biblio")
        val api = aliment.toApi()
        assertTrue(api.biblioRefIds.isEmpty())
    }

    @Test
    fun toApi_withBiblioRefs_reflectsTheirUuids() {
        val refs = listOf(
            BiblioRef(uuid = "b1", firstAuthor = "Dupont", year = 2020, completeRef = "Ref 1"),
            BiblioRef(uuid = "b2", firstAuthor = "Martin", year = 2021, completeRef = "Ref 2")
        )
        val aliment = AlimentEv(nom = "Test", biblioRefs = refs)
        val api = aliment.toApi()
        assertEquals(listOf("b1", "b2"), api.biblioRefIds)
    }

    // ── FoodApi.toDomain() : placeholders uuid-only ──────────────────────────

    @Test
    fun toDomain_noBiblioRefIds_producesEmptyBiblioRefs() {
        val api = FoodApi(uuid = "food-1", name = "Test", group = null, kind = null)
        val aliment = api.toDomain()
        assertTrue(aliment.biblioRefs.isEmpty())
    }

    @Test
    fun toDomain_withBiblioRefIds_producesUuidOnlyPlaceholders() {
        val api = FoodApi(
            uuid = "food-1",
            name = "Test",
            group = null,
            kind = null,
            biblioRefIds = listOf("b1", "b2")
        )
        val aliment = api.toDomain()
        assertEquals(listOf("b1", "b2"), aliment.biblioRefs.map { it.uuid })
        // Les placeholders ne portent que l'uuid : consistent=0 (constructeur secondaire de BiblioRef)
        assertTrue(aliment.biblioRefs.all { it.consistent == 0 })
    }

    // ── Round-trip toApi() -> toDomain() (uuids uniquement, résolution complète
    // des BiblioRef se fait via ExportImportRepository.biblioCache) ───────────

    @Test
    fun toApi_thenToDomain_preservesBiblioRefUuids() {
        val refs = listOf(
            BiblioRef(uuid = "b1", firstAuthor = "Dupont", year = 2020, completeRef = "Ref 1")
        )
        val original = AlimentEv(nom = "Test", biblioRefs = refs)
        val restored = original.toApi().toDomain()
        assertEquals(original.biblioRefs.map { it.uuid }, restored.biblioRefs.map { it.uuid })
    }
}
