package fr.vetbrain.vetnutri_mp.View

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.RationPossibilityAreas
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraphScope

internal val AlimentAnalyseData.graphLabel: String
    get() = if (isRationSum) "Σ" else if (rationActual != null) "R$numero" else numero.toString()

internal val AlimentAnalyseData.rationColor: Color?
    get() = rationActual?.let { if (it) Color(0xFFFF9800) else VetNutriColors.Primary }

/** A transient per-100g composition, used only for plotting, never added to the food catalogue. */
internal suspend fun rationFoodGraphData(
    ration: Ration,
    number: Int,
    isSum: Boolean,
    reference: ReferenceEv?,
    repository: EquationRepository?,
    dryMatter: Boolean
): AlimentAnalyseData? {
    val ingredients = ration.alimentMutableList.filter { it.aliment != null && it.quantite.isFinite() && it.quantite > 0 }
    val mass = ingredients.sumOf { it.quantite }
    if (mass <= 0 || !mass.isFinite()) return null
    val nutrients: List<Nutrient> = NutrientMain.entries + NutrientMacro.entries +
        NutrientMin.entries + NutrientVitam.entries + NutrientLipid.entries + AAEnum.entries + NutrientOther.entries
    val food = AlimentEv(uuid = "graph-ration-" + ration.uuid, nom = ration.name)
    for (nutrient in nutrients) {
        var amount = 0.0
        for (ingredient in ingredients) {
            amount += (ingredient.getNutrientWithComplementary(nutrient, repository, reference) ?: 0.0) * ingredient.quantite / mass
        }
        food.setNutrient(nutrient, amount)
    }
    var energy = 0.0
    for (ingredient in ingredients) energy += ingredient.getEnergie(referenceEv = reference, equationRepository = repository)
    val energyPer100g = energy * 100.0 / mass
    if (!energyPer100g.isFinite() || energyPer100g <= 0) return null
    food.setNutrient(NutrientMain.ENERGIE, energyPer100g)
    val dm = 100.0 - (food.getNutrient(NutrientMain.HUMIDITE) ?: 0.0)
    if (dryMatter && dm <= 0) return null
    fun value(nutrient: Nutrient) = food.getNutrient(nutrient) ?: 0.0
    fun normalized(nutrient: Nutrient) = if (dryMatter) value(nutrient) * 100.0 / dm else value(nutrient) * 1000.0 / energyPer100g
    return AlimentAnalyseData(
        aliment = food, numero = number,
        densiteEnergetique = if (dryMatter) energyPer100g * 100.0 / dm else energyPer100g,
        pourcentageProteines = value(NutrientMain.PROTEINE) * 3.5 / energyPer100g * 100.0,
        pourcentageLipides = value(NutrientMain.LIPIDE) * 8.5 / energyPer100g * 100.0,
        phosphorePer1000Kcal = normalized(NutrientMacro.PHOS),
        proteinePer1000Kcal = normalized(NutrientMain.PROTEINE),
        calciumPer1000Kcal = normalized(NutrientMacro.CAL),
        energiePer1000Kcal = normalized(NutrientMain.ENERGIE),
        lipidePer1000Kcal = normalized(NutrientMain.LIPIDE),
        glucidePer1000Kcal = normalized(NutrientMain.GLUCIDE),
        rationActual = ration.actual, isRationSum = isSum,
        energyAsFed = energyPer100g
    )
}

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
internal fun XYGraphScope<Float, Float>.FoodGraphRationAreas(
    data: List<AlimentAnalyseData>,
    points: List<Point<Float, Float>>
) {
    val indices = data.indices.filter { data[it].rationActual != null }
    RationPossibilityAreas(
        indices.map { points[it] },
        indices.map { "selected-consultation" to (data[it].rationActual == true) },
        indices.map { data[it].isRationSum }
    )
}
