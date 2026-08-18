package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class NutrientDisplayCalculationsTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.001) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    private fun alimentAvecProteine(proteine: Double = 20.0) =
        AlimentEv().also { it.setNutrient(NutrientMain.PROTEINE, proteine) }

    // ── calculerContributionIngredient : seule ReferenceEv pilote les équations ─

    @Test
    fun calculerContributionIngredient_sansReferenceEv_aucuneEquationComplementaire_estZero() = runTest {
        // Sans ReferenceEv, aucune équation complémentaire n'est appliquée : un nutriment sans
        // valeur directe en table reste à 0, même si un repository d'équations est fourni.
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-mg", equationScript = "PROTEINE*0.1"))

        val contribution = calculerContributionIngredient(
                alimentRation, NutrientMacro.MG, reference = null, equationRepository = equationRepository
        )
        assertNear(0.0, contribution)
    }

    @Test
    fun calculerContributionIngredient_avecReferenceEv_utiliseEquationComplementaire() = runTest {
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-mg",
                equationScript = "PROTEINE*0.1",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMacro.MG,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationsNut = mutableListOf(equation)

        val contribution = calculerContributionIngredient(
                alimentRation,
                NutrientMacro.MG,
                reference = reference,
                equationRepository = equationRepository
        )
        // valeurPour100g = PROTEINE(20) * 0.1 = 2.0 ; contribution = 2.0 * 200 / 100 = 4.0
        assertNear(4.0, contribution)
    }

    @Test
    fun getNutrientWithComplementary_valeurExpliciteZero_neLancePasEquation() = runTest {
        val aliment = AlimentEv().also { it.setNutrient(NutrientMain.ENA, 0.0) }
        val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0)
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-ena",
                equationScript = "100-PROTEINE-HUMIDITE-CELLULOSE-CENDRE-LIPIDE",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMain.ENA,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN).also {
            it.equationsNut = mutableListOf(equation)
        }

        val ena = alimentRation.getNutrientWithComplementary(
                NutrientMain.ENA,
                equationRepository,
                reference
        )

        assertEquals(0.0, ena)
    }

    @Test
    fun getNutrientWithComplementary_enaAbsente_utiliseEquationComplementaire() = runTest {
        val aliment = AlimentEv().also {
            it.setNutrient(NutrientMain.PROTEINE, 20.0)
            it.setNutrient(NutrientMain.HUMIDITE, 10.0)
            it.setNutrient(NutrientMain.CELLULOSE, 5.0)
            it.setNutrient(NutrientMain.CENDRE, 5.0)
            it.setNutrient(NutrientMain.LIPIDE, 10.0)
        }
        val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0)
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-ena-absente",
                equationScript = "100-PROTEINE-HUMIDITE-CELLULOSE-CENDRE-LIPIDE",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMain.ENA,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN).also {
            it.equationsNut = mutableListOf(equation)
        }

        val ena = alimentRation.getNutrientWithComplementary(
                NutrientMain.ENA,
                equationRepository,
                reference
        )

        assertEquals(50.0, ena)
    }

    @Test
    fun calculerContributionsIngredients_sommeEgaleApportTotal() = runTest {
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-mg",
                equationScript = "PROTEINE*0.1",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMacro.MG,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationsNut = mutableListOf(equation)

        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(aliment = alimentAvecProteine(20.0), quantite = 200.0, weight = 1.0),
                AlimentRation(aliment = alimentAvecProteine(30.0), quantite = 100.0, weight = 1.0)
        ))

        val apportTotal = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                equationRepository = equationRepository,
                referenceEv = reference
        )[NutrientMacro.MG.label]?.valeur ?: -1.0

        val sommeContributions = calculerContributionsIngredients(
                ration, NutrientMacro.MG, reference = reference, equationRepository = equationRepository
        ).sumOf { it.contribution }

        assertTrue(apportTotal > 0.0, "L'apport total devrait être non nul")
        assertNear(apportTotal, sommeContributions)
    }

    // ── estNutrimentAnalysisRatio : voir aussi RationNutrientAnalyzerTest ─────

    @Test
    fun calculerContributionsIngredients_vraiRatio_returnsEmptyList() = runTest {
        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(aliment = alimentAvecProteine(), quantite = 100.0, weight = 1.0)
        ))
        val contributions = calculerContributionsIngredients(
                ration,
                fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.PCa,
                reference = null,
                equationRepository = null
        )
        assertTrue(contributions.isEmpty())
    }

    // ── calculerConformite / calculerBulletGraphData : seuils g/1000kcal vs BE réel ─
    //
    // Cas rapporté : chien 28kg, BEE 1497 kcal/j, BE réel 1078 kcal/j (K2=0.90 x K3=0.80).
    // Seuil protéines OPTIMIN = 65 g/1000kcal, apport réel = 88g/j.
    // Le seuil absolu doit se calculer sur le BEE (97.305g), jamais sur le BE réel (70.07g) :
    // sinon un apport pourtant insuffisant (88g < 97.305g) paraît conforme (88g > 70.07g).

    private fun referenceProteineOptimin(seuilPerKcal: Double = 65.0) =
        ReferenceEv(espece = Espece.CHIEN).also {
            it.definirNutriment(seuilPerKcal, NutrientMain.PROTEINE, Reflevel.OPTIMIN, UnitReqEnum.PERKCAL, BiblioRef())
        }

    private fun apportProteine(grammes: Double) = ValeurNutritionnelle(
        nutriment = NutrientMain.PROTEINE,
        unite = NutrientMain.PROTEINE.ue,
        valeur = grammes,
        description = "",
        complete = true
    )

    @Test
    fun calculerConformite_seuilPerKcal_utiliseLeBeeStandardPasLeBeReel() {
        val resultat = calculerConformite(
            valeurNutritionnelle = apportProteine(88.0),
            referenceUtilisee = referenceProteineOptimin(),
            besoinEnergetiqueEntretien = 1497.0, // BEE brut
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueCible = 1078.0 // BE réel après K2 x K3 — ne doit pas servir ici
        )

        // Seuil correct = 65 * 1497 / 1000 = 97.305g > 88g apporté -> carence détectée.
        // Avec l'ancien bug (division par le BE réel), le seuil tombait à 70.07g < 88g
        // et aucune carence n'était détectée.
        assertNotNull(resultat, "Une carence doit être détectée : 88g < seuil BEE (97.305g)")
        assertEquals(ConformiteStatus.CARENCE, resultat!!.status)
        assertTrue(!resultat.isCritical, "OPTIMIN n'est pas un niveau critique (une seule flèche)")
    }

    @Test
    fun calculerConformite_seuilPerKcal_estIndependantDuBesoinEnergetiqueCible() {
        val base = calculerConformite(
            valeurNutritionnelle = apportProteine(88.0),
            referenceUtilisee = referenceProteineOptimin(),
            besoinEnergetiqueEntretien = 1497.0,
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueCible = 1078.0
        )
        val avecAutreCible = calculerConformite(
            valeurNutritionnelle = apportProteine(88.0),
            referenceUtilisee = referenceProteineOptimin(),
            besoinEnergetiqueEntretien = 1497.0,
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueCible = 5000.0 // valeur absurde, sans effet sur un nutriment non-énergie
        )

        assertEquals(base?.status, avecAutreCible?.status)
        assertEquals(ConformiteStatus.CARENCE, avecAutreCible?.status)
    }

    @Test
    fun calculerConformite_seuilPerKcal_conformeQuandApportDepasseLeSeuilBee() {
        // Même seuil, mais apport suffisant pour couvrir le besoin calculé sur le BEE (97.305g).
        val resultat = calculerConformite(
            valeurNutritionnelle = apportProteine(100.0),
            referenceUtilisee = referenceProteineOptimin(),
            besoinEnergetiqueEntretien = 1497.0,
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueCible = 1078.0
        )
        assertNull(resultat)
    }

    @Test
    fun calculerConformite_ligneEnergie_bandeParDefaut_utiliseLeBeReelPasLeBee() {
        // Pas de référence ENERGIE explicite -> bande synthétique ±10% autour du besoin cible.
        val reference = ReferenceEv(espece = Espece.CHIEN)
        val apportEnergie = ValeurNutritionnelle(
            nutriment = NutrientMain.ENERGIE,
            unite = NutrientMain.ENERGIE.ue,
            valeur = 1000.0,
            description = "",
            complete = true
        )

        val resultat = calculerConformite(
            valeurNutritionnelle = apportEnergie,
            referenceUtilisee = reference,
            besoinEnergetiqueEntretien = 1497.0, // BEE brut : ignoré pour la ligne ENERGIE
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueCible = 1078.0 // BE réel : bande = [970.2, 1185.8]
        )

        // 1000 kcal est dans la bande du BE réel [970.2, 1185.8] -> conforme.
        // Avec le BEE (bande [1347.3, 1646.7]), 1000 kcal serait faussement en carence.
        assertNull(resultat)
    }

    @Test
    fun calculerBulletGraphData_seuilPerKcal_convertiAvecLeBeeMemeSiCibleDiffere() {
        val apport = apportProteine(88.0)

        val data = calculerBulletGraphData(
            valeurNutritionnelle = apport,
            reference = referenceProteineOptimin(),
            typeExpressionBesoin = TypeExpressionBesoin.PAR_KG,
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueEntretien = 1497.0,
            besoinEnergetiqueCible = 1078.0
        )

        assertNotNull(data)
        // Seuil OPTIMIN converti en g/kg : (65 * 1497 / 1000) / 28 ≈ 3.4752
        assertNear(3.4752, data!!.optiminRef!!, tol = 0.001)
        // Apport en g/kg : 88 / 28 ≈ 3.1429, sous le seuil -> confirme la carence ci-dessus.
        assertNear(3.1429, data.apport, tol = 0.001)
    }

    @Test
    fun calculerBulletGraphData_ligneEnergie_apportEtBandeUtilisentLeBeReel() {
        val reference = ReferenceEv(espece = Espece.CHIEN)
        val apport = ValeurNutritionnelle(
            nutriment = NutrientMain.ENERGIE,
            unite = NutrientMain.ENERGIE.ue,
            valeur = 1000.0,
            description = "",
            complete = true
        )

        val data = calculerBulletGraphData(
            valeurNutritionnelle = apport,
            reference = reference,
            typeExpressionBesoin = TypeExpressionBesoin.PAR_KCAL,
            poidsAnimal = 28.0,
            poidsMetabolique = null,
            besoinEnergetiqueEntretien = 1497.0,
            besoinEnergetiqueCible = 1078.0
        )

        assertNotNull(data)
        assertNear(90.0, data!!.minRef!!)
        assertNear(110.0, data.maxRef!!)
        // apport = 1000 / 1078 * 100 ≈ 92.76% -> dans la bande [90, 110] du BE réel.
        // Avec le BEE (1000/1497*100 ≈ 66.8%), l'apport paraîtrait faussement en carence.
        assertNear((1000.0 / 1078.0) * 100.0, data.apport)
        assertTrue(data.apport in data.minRef!!..data.maxRef!!)
    }
}
