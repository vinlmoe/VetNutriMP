package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class FormatDateTest {

    @Test
    fun dateAfficheeEnJourMoisAnnee() {
        assertEquals("05/03/2026", LocalDate(2026, 3, 5).enTexte())
    }

    @Test
    fun dateSaisieEnFrancaisOuIso() {
        val attendu = LocalDate(2026, 10, 25)
        assertEquals(attendu, lireDate("25/10/2026"))
        assertEquals(attendu, lireDate(" 25-10-2026 "))
        assertEquals(attendu, lireDate("25.10.2026"))
        assertEquals(attendu, lireDate("2026-10-25"))
        assertEquals(LocalDate(2026, 3, 5), lireDate("5/3/2026"))
    }

    @Test
    fun dateInvalideDonneNull() {
        assertNull(lireDate(""))
        assertNull(lireDate("31/02/2026"))
        assertNull(lireDate("2026/10"))
        assertNull(lireDate("abc"))
    }

    @Test
    fun allerRetourTexteDate() {
        val d = LocalDate(2019, 4, 12)
        assertEquals(d, lireDate(d.enTexte()))
    }
}
