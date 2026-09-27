package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.TypeConsultation
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/** Ordonnance d'une consultation évolutive : tableau ingrédients × étapes, sans interpolation. */
class PrescriptionPlanEvolutifTest {

    private val croquettes = AlimentEv(uuid = "croq", nom = "Croquettes chiot")

    private fun etape(uuid: String, nom: String, poids: Double?, quantite: Double) =
            Ration(
                    uuid = uuid,
                    idConsult = "c",
                    name = nom,
                    etapeEvolutive = true,
                    poids = poids,
                    alimentMutableList =
                            mutableListOf(
                                    AlimentRation(
                                            uuid = "a-$uuid",
                                            refAlimUnif = "croq",
                                            quantite = quantite,
                                            aliment = croquettes
                                    )
                            )
            )

    @Test
    fun prescription_rendersPlanTable_andSkipsIndividualSteps() = runTest {
        val e12 = etape("e12", "EtapeDouze", 12.0, 250.0)
        val reel = etape("reel", "EtapeReelle", null, 120.0)
        val standard = Ration(uuid = "std", idConsult = "c", name = "RationStandard")
        val consultation =
                ConsultationEv(
                        uuid = "c",
                        weight = 5.0,
                        typeConsultation = TypeConsultation.EVOLUTIVE,
                        rations = mutableListOf(e12, reel, standard)
                )

        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(
                                animal = null,
                                ration = null,
                                reference = null,
                                rations = consultation.rations,
                                consultation = consultation
                        )
                )

        assertTrue(html.contains("Plan de rationnement"))
        // Colonnes triées par poids croissant, poids réel signalé
        assertTrue(html.indexOf("5.0 kg (actuel)") < html.indexOf("12.0 kg"))
        assertTrue(html.contains("Croquettes chiot"))
        assertTrue(html.contains("120.0 g") && html.contains("250.0 g"))
        // Les étapes ne sont pas répétées en blocs de ration ; les autres rations restent
        assertFalse(html.contains("EtapeDouze"))
        assertFalse(html.contains("EtapeReelle"))
        assertTrue(html.contains("RationStandard"))
    }

    @Test
    fun prescription_standardConsultation_hasNoPlanTable() = runTest {
        val ration = Ration(uuid = "r", idConsult = "c", name = "RationStandard")
        val consultation = ConsultationEv(uuid = "c", weight = 5.0, rations = mutableListOf(ration))

        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(
                                animal = null,
                                ration = null,
                                reference = null,
                                rations = consultation.rations,
                                consultation = consultation
                        )
                )

        assertFalse(html.contains("Plan de rationnement"))
        assertTrue(html.contains("RationStandard"))
    }
}
