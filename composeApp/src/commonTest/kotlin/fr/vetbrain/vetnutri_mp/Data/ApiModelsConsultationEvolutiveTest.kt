package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.ProfilEvolutif
import fr.vetbrain.vetnutri_mp.Enumer.TypeConsultation
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

/**
 * Vérifie que les champs de la consultation évolutive (type, profil, étapes avec poids et
 * variables propres) survivent à l'aller-retour export/import JSON, et que les anciens exports
 * (sans ces champs) restent lisibles en consultation STANDARD.
 */
class ApiModelsConsultationEvolutiveTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun consultationEvolutive(): ConsultationEv =
            ConsultationEv(
                    uuid = "c1",
                    weight = 5.2,
                    typeConsultation = TypeConsultation.EVOLUTIVE,
                    profilEvolutif = ProfilEvolutif.CROISSANCE,
                    suppVarp = mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, 25.0)),
                    rations =
                            mutableListOf(
                                    Ration(uuid = "r-reel", idConsult = "c1", etapeEvolutive = true, refRationParente = "r-std"),
                                    Ration(
                                            uuid = "r-8",
                                            idConsult = "c1",
                                            etapeEvolutive = true,
                                            poids = 8.0,
                                            refRationParente = "r-std",
                                            suppVarp =
                                                    mutableListOf(
                                                            SupplementalvariableP(
                                                                    VariableKind.WeekGestation,
                                                                    6.0
                                                            )
                                                    )
                                    ),
                                    Ration(uuid = "r-std", idConsult = "c1")
                            )
            )

    @Test
    fun roundTrip_preservesTypeProfileAndSteps() {
        val restored = consultationEvolutive().toApi().toDomain()

        assertEquals(TypeConsultation.EVOLUTIVE, restored.typeConsultation)
        assertEquals(ProfilEvolutif.CROISSANCE, restored.profilEvolutif)
        assertTrue(restored.isEvolutive)
        assertEquals(listOf("r-reel", "r-8"), restored.etapesEvolutives.map { it.uuid })

        val reel = restored.rations.first { it.uuid == "r-reel" }
        assertNull(reel.poids)

        val etape8 = restored.rations.first { it.uuid == "r-8" }
        assertEquals(8.0, etape8.poids)
        assertEquals("r-std", etape8.refRationParente)
        assertEquals(
                listOf(SupplementalvariableP(VariableKind.WeekGestation, 6.0)),
                etape8.suppVarp
        )

        assertFalse(restored.rations.first { it.uuid == "r-std" }.etapeEvolutive)
    }

    @Test
    fun roundTrip_throughJsonString() {
        val encoded = json.encodeToString(ConsultationApi.serializer(), consultationEvolutive().toApi())
        val restored = json.decodeFromString(ConsultationApi.serializer(), encoded).toDomain()

        assertEquals(TypeConsultation.EVOLUTIVE, restored.typeConsultation)
        assertEquals(8.0, restored.rations.first { it.uuid == "r-8" }.poids)
    }

    @Test
    fun legacyJson_withoutNewFields_defaultsToStandard() {
        val legacy =
                """
                {"uuid":"old","rations":[{"uuid":"r","consultationId":"old","name":"Ration",
                "coef":1.0,"isCurrent":true,"number":1,"specie":null,"isRecipe":false,
                "description":"","items":[]}]}
                """.trimIndent()

        val restored = json.decodeFromString(ConsultationApi.serializer(), legacy).toDomain()

        assertEquals(TypeConsultation.STANDARD, restored.typeConsultation)
        assertNull(restored.profilEvolutif)
        assertFalse(restored.isEvolutive)
        val ration = restored.rations.single()
        assertFalse(ration.etapeEvolutive)
        assertNull(ration.refRationParente)
        assertNull(ration.poids)
        assertTrue(ration.suppVarp.isEmpty())
    }

    @Test
    fun unknownPersistedNames_fallBackSafely() {
        assertEquals(TypeConsultation.STANDARD, TypeConsultation.fromName("INCONNU"))
        assertEquals(TypeConsultation.STANDARD, TypeConsultation.fromName(null))
        assertNull(ProfilEvolutif.fromName("INCONNU"))
        assertEquals(ProfilEvolutif.GESTATION, ProfilEvolutif.fromName("GESTATION"))
    }
}
