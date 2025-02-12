package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalUuidApi::class, ExperimentalSerializationApi::class)
class BiblioRefTest {
    private lateinit var biblioRef: BiblioRef

    @BeforeTest
    fun setup() {
        biblioRef =
                BiblioRef(
                        firstAuthor = "Dupont",
                        year = "2024",
                        completeRef = "Dupont et al. (2024) Étude sur la nutrition animale",
                        comments = "Étude importante",
                        consistent = 1
                )
    }

    @Test
    fun `test biblio creation with all fields`() {
        assertEquals("Dupont", biblioRef.firstAuthor)
        assertEquals("2024", biblioRef.year)
        assertEquals("Dupont et al. (2024) Étude sur la nutrition animale", biblioRef.completeRef)
        assertEquals("Étude importante", biblioRef.comments)
        assertEquals(1, biblioRef.consistent)
    }

    @Test
    fun `test biblio creation with minimal fields`() {
        val minimalBiblio =
                BiblioRef(
                        firstAuthor = "Test",
                        year = null,
                        completeRef = null,
                        comments = null,
                        consistent = null
                )

        assertEquals("Test", minimalBiblio.firstAuthor)
        assertNull(minimalBiblio.year)
        assertNull(minimalBiblio.completeRef)
        assertNull(minimalBiblio.comments)
        assertNull(minimalBiblio.consistent)
    }

    @Test
    fun `test update biblio fields`() {
        biblioRef.firstAuthor = "Martin"
        biblioRef.year = "2023"
        biblioRef.completeRef = "Nouvelle référence"
        biblioRef.comments = "Nouveau commentaire"
        biblioRef.consistent = 0

        assertEquals("Martin", biblioRef.firstAuthor)
        assertEquals("2023", biblioRef.year)
        assertEquals("Nouvelle référence", biblioRef.completeRef)
        assertEquals("Nouveau commentaire", biblioRef.comments)
        assertEquals(0, biblioRef.consistent)
    }

    @Test
    fun `test uuid generation`() {
        val biblio1 = BiblioRef(
            firstAuthor = "Test",
            year = null,
            completeRef = null,
            comments = null,
            consistent = null
        )
        val biblio2 = BiblioRef(
            firstAuthor = "Test",
            year = null,
            completeRef = null,
            comments = null,
            consistent = null
        )

        assertNotNull(biblio1.uuid)
        assertNotNull(biblio2.uuid)
        assertNotEquals(biblio1.uuid, biblio2.uuid)
    }
}
