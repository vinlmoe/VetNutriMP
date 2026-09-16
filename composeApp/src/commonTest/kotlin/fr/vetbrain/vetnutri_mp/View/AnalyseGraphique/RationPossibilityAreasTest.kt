package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RationPossibilityAreasTest {
    @Test
    fun envelopeHandlesInteriorDuplicateAndCollinearPoints() {
        val corners = listOf(0f to 0f, 4f to 0f, 4f to 4f, 0f to 4f)
        assertEquals(corners.toSet(), rationEnvelope(corners.reversed() + listOf(2f to 2f, 0f to 0f)).toSet())
        assertEquals(listOf(0f to 0f, 4f to 4f), rationEnvelope(listOf(2f to 2f, 4f to 4f, 0f to 0f)))
        assertEquals(emptyList(), rationEnvelope(listOf(Float.NaN to 0f)))
        assertEquals(listOf(1f to 1f), rationEnvelope(listOf(1f to 1f, 1f to 1f)))
    }

    @Test
    fun currentFilterUsesSelectedConsultationAndNeverFallsBackToHistory() {
        val historical = ConsultationEv(uuid = "old")
        val selected = ConsultationEv(uuid = "selected")
        assertEquals(listOf(selected), prepareGraphConsultations(listOf(historical), selected, true, false))
        assertTrue(prepareGraphConsultations(listOf(historical), null, true, false).isEmpty())
    }

    @Test
    fun sumsRespectCoefficientsStatusAndConsultationWithoutMutatingSources() {
        fun ration(id: String, actual: Boolean, coefficient: Double, quantity: Double) = Ration(
            uuid = id, actual = actual, coef = coefficient,
            alimentMutableList = mutableListOf(AlimentRation(uuidUnif = "food", quantite = quantity))
        )
        val consultation = ConsultationEv(uuid = "first", rations = mutableListOf(
            ration("a", true, 1.0, 100.0), ration("b", true, 3.0, 300.0),
            ration("c", false, 1.0, 50.0)
        ))
        val second = consultation.copy(uuid = "second")
        val result = prepareGraphConsultations(listOf(consultation, second), null, false, true)
        val sums = result.first().rations.take(2)
        assertEquals(250.0, sums.single { it.actual }.alimentMutableList.single().quantite)
        assertEquals(50.0, sums.single { !it.actual }.alimentMutableList.single().quantite)
        assertEquals(4, result.flatMap { it.rations.take(2) }.map { it.uuid }.distinct().size)
        assertEquals(3, consultation.rations.size)
    }
}
