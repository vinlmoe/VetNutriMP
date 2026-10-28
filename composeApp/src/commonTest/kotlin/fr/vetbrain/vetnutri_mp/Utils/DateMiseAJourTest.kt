package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class DateMiseAJourTest {

    @Test
    fun dateIso_estLue() {
        assertEquals(Instant.parse("2026-10-27T00:00:00Z"), parseDateMiseAJourOrNull("2026-10-27"))
    }

    @Test
    fun instantIso_estLu() {
        assertEquals(Instant.parse("2026-10-27T10:15:00Z"), parseDateMiseAJourOrNull("2026-10-27T10:15:00Z"))
    }

    @Test
    fun dateIsoSuivieDUneHeure_estLueAuJour() {
        assertEquals(Instant.parse("2026-10-27T00:00:00Z"), parseDateMiseAJourOrNull("2026-10-27 10:15"))
    }

    @Test
    fun dateFrancaise_estLueJourPuisMois() {
        // 02/01/2025 = 2 janvier (format des données initiales La Normandise)
        assertEquals(Instant.parse("2025-01-02T00:00:00Z"), parseDateMiseAJourOrNull("02/01/2025"))
        assertEquals(Instant.parse("2026-08-23T00:00:00Z"), parseDateMiseAJourOrNull(" 23/08/2026 "))
    }

    @Test
    fun dateFrancaiseEtIso_sontComparables() {
        val francaise = parseDateMiseAJourOrNull("31/12/2025")!!
        val iso = parseDateMiseAJourOrNull("2026-01-01")!!
        assertTrue(iso > francaise)
    }

    @Test
    fun dateVideOuIllisible_donneNull() {
        assertNull(parseDateMiseAJourOrNull(null))
        assertNull(parseDateMiseAJourOrNull(""))
        assertNull(parseDateMiseAJourOrNull("   "))
        assertNull(parseDateMiseAJourOrNull("hier"))
        assertNull(parseDateMiseAJourOrNull("31/02/2026"))
    }
}
