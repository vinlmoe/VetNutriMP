package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatistiquesBoiteTest {

    @Test
    fun quartiles_commeR() {
        // R : quantile(c(1,2,3,4,5,6,7,8)) -> 2.75 4.5 6.25
        val s = StatistiquesBoite.de(listOf(8.0, 1.0, 3.0, 2.0, 5.0, 4.0, 7.0, 6.0))!!
        assertEquals(8, s.n)
        assertEquals(2.75, s.q1, 1e-9)
        assertEquals(4.5, s.mediane, 1e-9)
        assertEquals(6.25, s.q3, 1e-9)
        assertEquals(1.0, s.moustacheBasse)
        assertEquals(8.0, s.moustacheHaute)
        assertEquals(emptyList(), s.isolees)
        // R : mean(1:8) = 4.5 ; sd(1:8) = 2.44949
        assertEquals(4.5, s.moyenne, 1e-9)
        assertEquals(2.449490, s.ecartType, 1e-6)
    }

    @Test
    fun valeurIsoleeHorsMoustache() {
        // q1 = 2,25, q3 = 4,75, écart 2,5 : au-delà de 8,5, la valeur est isolée
        val s = StatistiquesBoite.de(listOf(1.0, 2.0, 3.0, 4.0, 5.0, 30.0, Double.NaN))!!
        assertEquals(6, s.n)
        assertEquals(5.0, s.moustacheHaute)
        assertEquals(listOf(30.0), s.isolees)
        assertEquals(30.0, s.maximum)
    }

    @Test
    fun uneSeuleValeur_ouAucune() {
        val s = StatistiquesBoite.de(listOf(12.0))!!
        assertEquals(12.0, s.q1)
        assertEquals(12.0, s.q3)
        assertEquals(0.0, s.ecartType)
        assertNull(StatistiquesBoite.de(listOf(Double.NaN)))
    }
}
