package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.SupplementalvariableP
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate

class GrowthCurvesDefaultTest {
    private fun courbeChien(sexe: Sex, poidsMax: Double?): String =
        courbesCroissanceChien[indexCourbeCroissanceParDefaut(courbesCroissanceChien, sexe, poidsMax)].description

    @Test
    fun `chien male de 32 kg recoit la courbe male 30-40 kg`() {
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_30_40, courbeChien(Sex.MALE_ENTIER, 32.0))
    }

    @Test
    fun `la classe de poids suit le plus grand poids connu`() {
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_FEMALE_LT_6_5, courbeChien(Sex.FEMELLE_ENTIERE, 4.0))
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_FEMALE_6_5_9, courbeChien(Sex.FEMELLE_STERILISEE, 6.5))
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_9_15, courbeChien(Sex.MALE_CASTRE, 12.0))
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_15_30, courbeChien(Sex.MALE_ENTIER, 29.9))
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_FEMALE_30_40, courbeChien(Sex.FEMELLE_ENTIERE, 55.0))
    }

    @Test
    fun `sans poids la plus petite classe du sexe`() {
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_LT_6_5, courbeChien(Sex.MALE_ENTIER, null))
    }

    @Test
    fun `chat selon le sexe`() {
        val femelle = indexCourbeCroissanceParDefaut(courbesCroissanceChat, Sex.FEMELLE_STERILISEE, 4.0)
        val male = indexCourbeCroissanceParDefaut(courbesCroissanceChat, Sex.MALE_ENTIER, 5.0)
        assertEquals(LocalizationKeys.Graph.CURVE_CAT_FEMALE, courbesCroissanceChat[femelle].description)
        assertEquals(LocalizationKeys.Graph.CURVE_CAT_MALE, courbesCroissanceChat[male].description)
    }

    private fun consultation(
        date: String,
        poidsAdulte: Double?,
        poids: Double? = null,
        nec: Int? = null,
        poidsIdeal: Double? = null
    ) =
        ConsultationEv(
            date = LocalDate.parse(date),
            weight = poids,
            BCS = nec,
            idealWeight = poidsIdeal,
            suppVarp = poidsAdulte?.let { mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, it)) }
                ?: mutableListOf()
        )

    @Test
    fun `chiot de 8 kg avec un poids adulte de 35 kg recoit la courbe 30-40 kg`() {
        val consultations = listOf(consultation("2026-05-01", 35.0))
        val poids = poidsReferenceCourbeCroissance(consultations, poidsDernierConnu = 8.0)
        assertEquals(35.0, poids)
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_30_40, courbeChien(Sex.MALE_ENTIER, poids))
    }

    @Test
    fun `le poids adulte de la consultation la plus recente l emporte`() {
        val consultations =
            listOf(consultation("2026-03-01", 20.0), consultation("2026-06-01", 25.0), consultation("2026-07-01", null))
        assertEquals(25.0, poidsReferenceCourbeCroissance(consultations, poidsDernierConnu = 12.0))
    }

    @Test
    fun `chien en surpoids classe d apres son poids ideal estime`() {
        // 32 kg, NEC 7/9 : poids idéal estimé 26,7 kg, donc classe [15-30] kg et non [30-40] kg
        val poids = poidsReferenceCourbeCroissance(listOf(consultation("2026-10-25", null, 32.0, 7)), 32.0)
        assertEquals(32.0 * 100 / 120, poids)
        assertEquals(LocalizationKeys.Graph.CURVE_DOG_MALE_15_30, courbeChien(Sex.MALE_ENTIER, poids))
    }

    @Test
    fun `le poids ideal saisi passe avant l estimation`() {
        val consultations = listOf(consultation("2026-10-25", null, 32.0, 7, poidsIdeal = 31.0))
        assertEquals(31.0, poidsReferenceCourbeCroissance(consultations, 32.0))
    }

    @Test
    fun `sans NEC le poids de la derniere consultation et non le maximum`() {
        val consultations = listOf(consultation("2025-01-10", null, 40.0), consultation("2026-10-25", null, 28.0))
        assertEquals(28.0, poidsReferenceCourbeCroissance(consultations, 28.0))
    }

    @Test
    fun `sans consultation pesee le dernier poids connu`() {
        assertEquals(12.0, poidsReferenceCourbeCroissance(listOf(consultation("2026-10-25", null)), 12.0))
        assertEquals(null, poidsReferenceCourbeCroissance(emptyList(), null))
    }
}
