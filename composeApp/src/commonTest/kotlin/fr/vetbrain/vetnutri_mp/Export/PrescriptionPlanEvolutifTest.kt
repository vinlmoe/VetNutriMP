package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Evolutive
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Ordonnance : une ration sélectionnée qui porte un plan évolutif est exportée en tableau
 * ingrédients × étapes (elle-même au poids réel + ses étapes), sans interpolation.
 */
class PrescriptionPlanEvolutifTest {

    private val croquettes = AlimentEv(uuid = "croq", nom = "Croquettes chiot")

    private fun ration(uuid: String, nom: String, quantite: Double, parent: String? = null, poids: Double? = null) =
            Ration(
                    uuid = uuid,
                    idConsult = "c",
                    name = nom,
                    etapeEvolutive = parent != null,
                    poids = poids,
                    refRationParente = parent,
                    alimentMutableList =
                            mutableListOf(
                                    AlimentRation(uuid = "a-$uuid", refAlimUnif = "croq", quantite = quantite, aliment = croquettes)
                            )
            )

    private val parent = ration("p", "RationCroissance", 120.0)
    private val e12 = ration("e12", "EtapeDouze", 250.0, parent = "p", poids = 12.0)
    private val e3 = ration("e3", "EtapeTrois", 80.0, parent = "p", poids = 3.0)
    private val standard = ration("std", "RationStandard", 60.0)
    private val consultation =
            ConsultationEv(uuid = "c", weight = 5.0, rations = mutableListOf(parent, e12, e3, standard))

    private suspend fun html(selection: List<Ration>) =
            HtmlDocumentBuilder.buildHtml(
                    DocumentType.PRESCRIPTION,
                    ExportData(animal = null, ration = null, reference = null, rations = selection, consultation = consultation)
            )

    @Test
    fun rationAvecPlan_exporteeEnTableau_colonnesTrieesParPoids() = runTest {
        val html = html(listOf(parent, e12, e3, standard))

        assertEquals(1, Regex(Regex.escape(translate(Evolutive.PRESCRIPTION_TITLE))).findAll(html).count())
        val enteteParent = translate(Evolutive.STEP_LABEL_REAL, "5.0 kg")
        assertTrue(html.contains(enteteParent))
        assertTrue(html.indexOf("3.0 kg") < html.indexOf(enteteParent))
        assertTrue(html.indexOf(enteteParent) < html.indexOf("12.0 kg"))
        assertTrue(html.contains("80.0 g") && html.contains("120.0 g") && html.contains("250.0 g"))
        // Les étapes ne sont pas répétées en blocs de ration ; la ration sans plan reste un bloc
        assertFalse(html.contains("EtapeDouze"))
        assertFalse(html.contains("EtapeTrois"))
        assertTrue(html.contains("RationStandard"))
    }

    @Test
    fun rationAvecPlanNonSelectionnee_pasDeTableau() = runTest {
        val html = html(listOf(standard, e12))
        assertFalse(html.contains(translate(Evolutive.PRESCRIPTION_TITLE)))
        assertFalse(html.contains("EtapeDouze"))
        assertTrue(html.contains("RationStandard"))
    }
}
