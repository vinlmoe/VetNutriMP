package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals

class RationDisplayOrderTest {
    @Test
    fun sumsStayFirstThenCurrentAndProposedAreAlphabeticalIgnoringCaseAndAccents() {
        val rations = listOf(
            Ration(uuid = "p", name = "Abricot", actual = false),
            Ration(uuid = "z", name = "zèbre", actual = true),
            Ration(uuid = "e", name = "Équilibre", actual = true),
            Ration(uuid = RationAggregator.UUID_GROUPE_PROPOSEES + "-consultation", actual = false),
            Ration(uuid = "a", name = " alpha ", actual = true),
            Ration(uuid = RationAggregator.UUID_GROUPE_ACTUELLES, actual = true)
        )
        assertEquals(
            listOf(RationAggregator.UUID_GROUPE_ACTUELLES,
                RationAggregator.UUID_GROUPE_PROPOSEES + "-consultation", "a", "e", "z", "p"),
            rations.sortedForDisplay().map { it.uuid }
        )
        assertEquals("p", rations.first().uuid)
    }
}
