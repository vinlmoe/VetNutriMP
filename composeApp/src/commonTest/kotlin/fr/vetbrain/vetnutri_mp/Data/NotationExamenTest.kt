package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotationExamenTest {

    private val bareme = ExamGradingRuleSet(
            examId = "E1", exerciseId = "X1",
            ingredientRule = IngredientRule(forbidden = listOf("chocolat"), points = 5),
            // Sans bornes : ne rapporte jamais de points, donc hors barème
            rationQuantityRule = MinMaxPointsRule(points = 5),
            energyDensityRule = MinMaxPointsRule(min = 1.0, max = 2.0, points = 4),
            nutrientRules = listOf(NutrientRule("PROTEINE", min = 20.0, points = 3), NutrientRule("", min = 1.0, points = 9)),
            adviceRule = AdviceRule(enabled = false, requiredWords = listOf("eau"), points = 3),
            customCriteria = listOf(FlexibleCriterionRule(id = "c1", label = "Poids", metric = FlexibleMetricKind.ANIMAL_WEIGHT_KG, min = 5.0, points = 1.5))
    )

    @Test
    fun pointsBareme_neCompteQueLesCriteresActifsEtConfigures() {
        // ingrédients 5 + densité 4 + protéines 3 + critère perso 1,5
        assertEquals(13.5, pointsBareme(bareme), 1e-9)
        assertEquals(4, criteresBareme(bareme).size)
    }

    @Test
    fun noteAutomatique_plafonneeOuProportionnelle() {
        // Barème de 13,5 points, note sur 20
        assertEquals(9.0, noteAutomatique(9.0, bareme), 1e-9)
        assertEquals(20.0, noteAutomatique(25.0, bareme), 1e-9)
        val proportionnel = bareme.copy(notationProportionnelle = true)
        assertEquals(13.33, noteAutomatique(9.0, proportionnel), 1e-9)
        assertEquals(20.0, noteAutomatique(13.5, proportionnel), 1e-9)
    }

    private fun note(etudiant: String, finale: Double, ingredientOk: Boolean, energyOk: Boolean) = ExamGrade(
            examId = "E1", exerciseId = "X1", studentId = etudiant, animalId = etudiant, animalName = etudiant,
            consultationId = null, autoScore = finale, manualScore = null, finalScore = finale,
            detail = ExamGradeDetail(ingredientOk = ingredientOk, energyOk = energyOk,
                    nutrientResults = listOf(NutrientResult("PROTEINE", 25.0, true, 3)),
                    customCriteriaResults = listOf(FlexibleCriterionResult("c1", "Poids", FlexibleMetricKind.ANIMAL_WEIGHT_KG, RationScope.ALL, ok = false)))
    )

    @Test
    fun statistiquesNotes_moyenneMedianeEtReussiteParCritere() {
        val stats = statistiquesNotes(
                listOf(note("a", 8.0, true, false), note("b", 12.0, true, true), note("c", 16.0, false, true), note("d", 20.0, true, true)),
                bareme
        )!!
        assertEquals(4, stats.copies)
        assertEquals(14.0, stats.moyenne, 1e-9)
        assertEquals(14.0, stats.mediane, 1e-9)
        assertEquals(3, stats.auDessusMoyenne)
        assertEquals(4, stats.histogramme.sumOf { it.copies })
        assertEquals(1, stats.histogramme.last().copies) // 20/20 dans la dernière classe
        val parType = stats.reussites.associate { it.critere.type to it.valides }
        assertEquals(3, parType[TypeCritereNotation.INGREDIENTS])
        assertEquals(3, parType[TypeCritereNotation.DENSITE])
        assertEquals(4, parType[TypeCritereNotation.NUTRIMENT])
        assertEquals(0, parType[TypeCritereNotation.PERSONNALISE])
        assertNull(statistiquesNotes(emptyList(), bareme))
    }

    @Test
    fun identifiantsCopies_uniquesMemeSansNumeroOuEnDoublon() {
        val ids = identifiantsCopies(listOf("a1" to "123", "a2" to "", "a3" to null, "a4" to "456", "a5" to "456"))
        assertEquals("123", ids["a1"])
        assertEquals("animal:a2", ids["a2"])
        assertEquals("animal:a3", ids["a3"])
        assertEquals("456 (a4)", ids["a4"])
        assertEquals("456 (a5)", ids["a5"])
        assertEquals(5, ids.values.toSet().size)
    }

    @Test
    fun examensDisponibles_compteLesAnimauxDistincts() {
        val examens = examensDisponibles(listOf(
                Triple("E1", "X1", "a1"), Triple("E1", "X1", "a1"), Triple("E1", "X1", "a2"),
                Triple("E1", "X2", "a3"), Triple(null, "X1", "a4"), Triple("E2", " ", "a5")))
        assertEquals(listOf(ExamenDisponible("E1", "X1", 2), ExamenDisponible("E1", "X2", 1)), examens)
    }

    @Test
    fun dossierNotation_allerRetourEtRefusDesAutresJson() {
        val dossier = DossierNotation(
                enregistreLeEpochMs = 42, examId = "E1", exerciseId = "X1",
                bareme = bareme.copy(notationProportionnelle = true),
                notes = listOf(note("a", 12.5, true, false).copy(manualScore = 14.0, finalScore = 14.0)),
                copiesJson = "{\"animals\":[]}"
        )
        val relu = decoderDossierNotation(encoderDossierNotation(dossier))
        assertEquals(dossier, relu)
        assertNull(decoderDossierNotation("{\"animals\":[]}"))
        assertNull(decoderDossierNotation("pas du json"))
        assertNull(decoderDossierNotation(encoderDossierNotation(dossier.copy(format = "autre"))))
    }
}
