package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiseEnPageGraphiquesTest {

    @Test
    fun plageBarres_partDeZero() {
        val plage = plageBarres(listOf(184.6f, 185.1f))
        assertEquals(0f, plage.start)
        assertTrue(plage.endInclusive > 185.1f && plage.endInclusive < 210f)
    }

    @Test
    fun plageBarres_sansValeurPositive() {
        assertEquals(0f..1f, plageBarres(emptyList()))
        assertEquals(0f..1f, plageBarres(listOf(0f, Float.NaN)))
    }

    @Test
    fun hauteurGraphique_suitLEcran() {
        // Ordinateur : 60 % de la hauteur visible
        assertEquals(540.dp, hauteurGraphiquePour(1100.dp, 900.dp))
        // Téléphone en portrait : pas plus haut que large
        assertEquals(384.dp, hauteurGraphiquePour(384.dp, 800.dp))
        // Bornes
        assertEquals(280.dp, hauteurGraphiquePour(300.dp, 300.dp))
        assertEquals(620.dp, hauteurGraphiquePour(2000.dp, 1600.dp))
    }
}
