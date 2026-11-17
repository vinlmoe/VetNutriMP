package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.NumberUtils
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlinx.coroutines.launch

/**
 * Vue détaillée pour l'analyse approfondie des aliments
 * Affiche toutes les informations nutritionnelles de manière structurée
 */
@Composable
fun AnalyseDetailleeAlimentsView(
        aliments: List<AlimentEv>,
        alimentsAnalyses: List<AlimentAnalyseData>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        viewModel: fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel?,
        useDryMatterPer100g: Boolean,
        alimentSelectionne: String?,
        onAlimentSelected: (String?) -> Unit,
        onUseDryMatterPer100gChange: ((Boolean) -> Unit)? = null,
        modifier: Modifier = Modifier
) {
    var alimentExpanded by remember { mutableStateOf<String?>(null) }
    var localUseDryMatterPer100g by remember { mutableStateOf(useDryMatterPer100g) }
    val coroutineScope = rememberCoroutineScope()
    
    // Synchroniser avec la valeur externe si elle change
    LaunchedEffect(useDryMatterPer100g) {
        localUseDryMatterPer100g = useDryMatterPer100g
    }
    
    Column(
            modifier = modifier
                    .fillMaxSize()
                    .padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
    ) {
        // En-tête
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = translate(LocalizationKeys.Chart.DETAILED_ANALYSIS_TITLE),
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                Text(
                        text = translate(LocalizationKeys.Chart.DETAILED_ANALYSIS_HINT),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }

            // Toggle pour /1000 kcal vs /100g MS
            if (onUseDryMatterPer100gChange != null) {
                Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = translate(LocalizationKeys.Chart.TOGGLE_PER_1000_KCAL),
                            style = MaterialTheme.typography.caption,
                            color = if (!localUseDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                    Switch(
                            checked = localUseDryMatterPer100g,
                            onCheckedChange = { newValue ->
                                localUseDryMatterPer100g = newValue
                                onUseDryMatterPer100gChange(newValue)
                            }
                    )
                    Text(
                            text = translate(LocalizationKeys.Chart.TOGGLE_PER_100G_DM),
                            style = MaterialTheme.typography.caption,
                            color = if (localUseDryMatterPer100g) VetNutriColors.Primary else MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

        // Liste des aliments avec détails
        if (alimentsAnalyses.isEmpty()) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = translate(LocalizationKeys.Chart.NO_FOOD_TO_ANALYZE),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    modifier = Modifier.fillMaxSize()
            ) {
                items(alimentsAnalyses) { data ->
                    AlimentDetailCard(
                            data = data,
                            isExpanded = alimentExpanded == data.aliment.uuid,
                            isSelected = alimentSelectionne == data.aliment.uuid,
                            useDryMatterPer100g = localUseDryMatterPer100g,
                            referenceEv = referenceEv,
                            equationRepository = equationRepository,
                            onExpandedChange = { uuid ->
                                alimentExpanded = if (alimentExpanded == uuid) null else uuid
                            },
                            onSelected = onAlimentSelected
                    )
                }
            }
        }
    }
}

/**
 * Carte détaillée pour un aliment avec toutes ses informations nutritionnelles
 */
@Composable
private fun AlimentDetailCard(
        data: AlimentAnalyseData,
        isExpanded: Boolean,
        isSelected: Boolean,
        useDryMatterPer100g: Boolean,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        onExpandedChange: (String) -> Unit,
        onSelected: (String?) -> Unit,
        modifier: Modifier = Modifier
) {
    Card(
            modifier = modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(data.aliment.uuid) },
            elevation = if (isSelected) AppSizes.elevationLarge else AppSizes.elevationMedium,
            backgroundColor = if (isSelected) Color(0xFF9C27B0).copy(alpha = 0.1f) else MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSizes.paddingMedium)
        ) {
            // En-tête de la carte
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = "${data.numero}. ${data.aliment.nom ?: translate(LocalizationKeys.NewReference.COEF_UNNAMED)}",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                    )
                    if (data.aliment.brand != null) {
                        Text(
                                text = translate(LocalizationKeys.Chart.BRAND_PREFIX, data.aliment.brand!!),
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (data.aliment.gamme != null) {
                        Text(
                                text = translate(LocalizationKeys.Chart.GAMME_PREFIX, data.aliment.gamme!!),
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Bouton d'expansion
                IconButton(onClick = { onExpandedChange(data.aliment.uuid) }) {
                    Icon(
                            imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (isExpanded) translate(LocalizationKeys.General.REDUCE) else translate(LocalizationKeys.Chart.EXPAND_TOOLTIP)
                    )
                }
            }

            // Informations principales (toujours visibles)
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = translate(LocalizationKeys.Chart.ENERGY_DENSITY_LABEL),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = GraphFormattingUtils.formatEnergyDensity(data.densiteEnergetique),
                            style = MaterialTheme.typography.body2,
                            color = VetNutriColors.Primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = translate(LocalizationKeys.Chart.PROTEIN_ENERGY_PERCENT),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = GraphFormattingUtils.formatPercentage(data.pourcentageProteines),
                            style = MaterialTheme.typography.body2
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                            text = translate(LocalizationKeys.Chart.FAT_ENERGY_PERCENT),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = GraphFormattingUtils.formatPercentage(data.pourcentageLipides),
                            style = MaterialTheme.typography.body2
                    )
                }
            }
            
            // Détails complets (affichés si expandé)
            if (isExpanded) {
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                Divider()
                Spacer(modifier = Modifier.height(AppSizes.paddingMedium))
                
                // Toutes les informations nutritionnelles détaillées
                AlimentNutrientsDetails(
                        aliment = data.aliment,
                        data = data,
                        useDryMatterPer100g = useDryMatterPer100g,
                        referenceEv = referenceEv,
                        equationRepository = equationRepository
                )
            }
        }
    }
}

/**
 * Affiche tous les détails nutritionnels d'un aliment
 */
@Composable
private fun AlimentNutrientsDetails(
        aliment: AlimentEv,
        data: AlimentAnalyseData,
        useDryMatterPer100g: Boolean,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?
) {
    val coroutineScope = rememberCoroutineScope()
    var nutrientsData by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var customNutrientsData by remember { mutableStateOf<List<Triple<String, Double, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(aliment, useDryMatterPer100g) {
        isLoading = true
        val composition = compositionDetaillee(
                aliment, data.densiteEnergetique, useDryMatterPer100g, referenceEv, equationRepository
        )
        nutrientsData = composition.nutriments
        customNutrientsData = composition.personnalises
        isLoading = false
    }
    
    if (isLoading) {
        CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = VetNutriColors.Primary
        )
    } else {
        Column(
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Text(
                    text = translate(
                            LocalizationKeys.Chart.NUTRITIONAL_COMPOSITION_PREFIX,
                            if (useDryMatterPer100g) translate(LocalizationKeys.Chart.TOGGLE_PER_100G_DM) else translate(LocalizationKeys.Chart.TOGGLE_PER_1000_KCAL)
                    ),
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )

            // Nutriments principaux
            Text(
                    text = translate(LocalizationKeys.FoodEdit.SECTION_MACRONUTRIENTS),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )

            nutrientsData.filter { it.key in listOf("Protéines", "Lipides", "Glucides", "Cellulose brute", "Cendres") }
                    .forEach { (nom, valeur) ->
                        NutrientRow(nom = translate(nutrientLabelKey(nom)), valeur = valeur, unit = "g")
                    }

            if (!useDryMatterPer100g && nutrientsData["Humidité"] != null) {
                NutrientRow(nom = translate(LocalizationKeys.Chart.MOISTURE), valeur = nutrientsData["Humidité"]!!, unit = "g")
            }

            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

            // Minéraux
            Text(
                    text = translate(LocalizationKeys.FoodEdit.SECTION_MINERALS),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )

            nutrientsData.filter { it.key in listOf("Calcium", "Phosphore", "Magnésium", "Sodium", "Potassium") }
                    .forEach { (nom, valeur) ->
                        NutrientRow(nom = translate(nutrientLabelKey(nom)), valeur = valeur, unit = "g")
                    }

            // Nutriments personnalisés
            if (customNutrientsData.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                Text(
                    text = translate(LocalizationKeys.Chart.PERSONALIZED_NUTRIENTS),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
                )
                customNutrientsData.forEach { (nom, valeur, unit) ->
                    NutrientRow(nom = nom, valeur = valeur, unit = unit)
                }
            }

            // Ratios importants
            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
            Text(
                    text = translate(LocalizationKeys.Chart.RATIOS_SECTION),
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )

            val calcium = nutrientsData["Calcium"] ?: 0.0
            val phosphore = nutrientsData["Phosphore"] ?: 0.0
            val proteine = nutrientsData["Protéines"] ?: 0.0

            if (phosphore > 0) {
                NutrientRow(nom = translate(LocalizationKeys.Chart.RATIO_CA_P), valeur = calcium / phosphore, unit = "")
            }
            if (phosphore > 0) {
                NutrientRow(nom = translate(LocalizationKeys.Chart.RATIO_PROTEIN_PHOSPHORUS), valeur = proteine / phosphore, unit = "")
            }
        }
    }
}

/** Composition d'un aliment affichée par l'analyse détaillée (libellés internes en français). */
internal data class CompositionDetaillee(
        val nutriments: Map<String, Double>,
        /** Nutriments personnalisés : nom, valeur, unité. */
        val personnalises: List<Triple<String, Double, String>>
)

/** Nutriments principaux puis minéraux, dans l'ordre de l'analyse détaillée. */
internal val NUTRIMENTS_DETAILLES =
        listOf("Protéines", "Lipides", "Glucides", "Cellulose brute", "Cendres", "Humidité",
               "Calcium", "Phosphore", "Magnésium", "Sodium", "Potassium")

/**
 * Composition d'un aliment pour 1000 kcal (ou 100 g de MS) : nutriments principaux, minéraux et
 * nutriments personnalisés. L'humidité reste en g/100 g (0 en matière sèche).
 */
internal suspend fun compositionDetaillee(
        aliment: AlimentEv,
        densiteEnergetique: Double,
        useDryMatterPer100g: Boolean,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?
): CompositionDetaillee {
        val alimentRation = AlimentRation(
                aliment = aliment,
                quantite = 100.0,
                weight = 1.0
        )
        
        val nutrients = mutableMapOf<String, Double>()
        
        // Nutriments principaux
        val humidite = alimentRation.getNutrientWithComplementary(
                NutrientMain.HUMIDITE, equationRepository, referenceEv
        ) ?: 0.0
        val proteine = alimentRation.getNutrientWithComplementary(
                NutrientMain.PROTEINE, equationRepository, referenceEv
        ) ?: 0.0
        val lipide = alimentRation.getNutrientWithComplementary(
                NutrientMain.LIPIDE, equationRepository, referenceEv
        ) ?: 0.0
        val glucide = alimentRation.getNutrientWithComplementary(
                NutrientMain.GLUCIDE, equationRepository, referenceEv
        ) ?: 0.0
        val cellulose = alimentRation.getNutrientWithComplementary(
                NutrientMain.CELLULOSE, equationRepository, referenceEv
        ) ?: 0.0
        val cendre = alimentRation.getNutrientWithComplementary(
                NutrientMain.CENDRE, equationRepository, referenceEv
        ) ?: 0.0
        
        // Minéraux
        val calcium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.CAL, equationRepository, referenceEv
        ) ?: 0.0
        val phosphore = alimentRation.getNutrientWithComplementary(
                NutrientMacro.PHOS, equationRepository, referenceEv
        ) ?: 0.0
        val magnesium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.MG, equationRepository, referenceEv
        ) ?: 0.0
        val sodium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.NA, equationRepository, referenceEv
        ) ?: 0.0
        val potassium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.K, equationRepository, referenceEv
        ) ?: 0.0
        
        // Conversion selon le mode
        val matiereSeche = 100.0 - humidite
        
        fun convertir(valeur: Double): Double {
            return if (useDryMatterPer100g) {
                if (matiereSeche > 0) (valeur * 100.0) / matiereSeche else valeur
            } else {
                if (densiteEnergetique > 0) (valeur * 1000.0) / densiteEnergetique else 0.0
            }
        }
        
        nutrients["Humidité"] = if (useDryMatterPer100g) 0.0 else humidite
        nutrients["Protéines"] = convertir(proteine)
        nutrients["Lipides"] = convertir(lipide)
        nutrients["Glucides"] = convertir(glucide)
        nutrients["Cellulose brute"] = convertir(cellulose)
        nutrients["Cendres"] = convertir(cendre)
        nutrients["Calcium"] = convertir(calcium)
        nutrients["Phosphore"] = convertir(phosphore)
        nutrients["Magnésium"] = convertir(magnesium)
        nutrients["Sodium"] = convertir(sodium)
        nutrients["Potassium"] = convertir(potassium)
        

        // Nutriments personnalisés : valeurs brutes depuis valMap avec même conversion
        val personnalises = aliment.valMap.entries
            .filter { (nutrient, _) -> nutrient is CustomNutrient }
            .map { (nutrient, nq) ->
                Triple(
                    (nutrient as CustomNutrient).nameToString(),
                    convertir(nq.value),
                    nutrient.unite
                )
            }
            .sortedBy { it.first }

    return CompositionDetaillee(nutrients, personnalises)
}

/**
 * Tableau exporté de l'analyse détaillée : une ligne par mesure (densité, part d'énergie,
 * nutriments, rapports), une colonne par aliment repéré par son numéro ; « - » sans valeur.
 */
internal suspend fun tableauAnalyseDetaillee(
        alimentsAnalyses: List<AlimentAnalyseData>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        useDryMatterPer100g: Boolean,
        nombre: (Double) -> String
): fr.vetbrain.vetnutri_mp.Export.TableauExporte? {
    if (alimentsAnalyses.isEmpty()) return null
    val equations = equationRepository?.let { fr.vetbrain.vetnutri_mp.Repository.EquationsMemorisees(it) }
    val compositions = fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.Default.let { dispatcher ->
        kotlinx.coroutines.withContext(dispatcher) {
            alimentsAnalyses.map {
                compositionDetaillee(it.aliment, it.densiteEnergetique, useDryMatterPer100g, referenceEv, equations)
            }
        }
    }
    val base = translate(if (useDryMatterPer100g) LocalizationKeys.Chart.TOGGLE_PER_100G_DM else LocalizationKeys.Chart.TOGGLE_PER_1000_KCAL)
    fun cellule(v: Double?) = if (v == null || !v.isFinite() || v <= 0) "-" else nombre(v)
    val lignes = mutableListOf<List<String>>()
    lignes += listOf(translate(LocalizationKeys.Chart.ENERGY_DENSITY_LABEL)) + alimentsAnalyses.map { cellule(it.densiteEnergetique) }
    lignes += listOf(translate(LocalizationKeys.Chart.PROTEIN_ENERGY_PERCENT)) + alimentsAnalyses.map { cellule(it.pourcentageProteines) }
    lignes += listOf(translate(LocalizationKeys.Chart.FAT_ENERGY_PERCENT)) + alimentsAnalyses.map { cellule(it.pourcentageLipides) }
    NUTRIMENTS_DETAILLES.forEach { nom ->
        if (nom == "Humidité" && useDryMatterPer100g) return@forEach
        val unite = if (nom == "Humidité") "g/100 g" else "g$base"
        lignes += listOf("${translate(nutrientLabelKey(nom))} ($unite)") + compositions.map { cellule(it.nutriments[nom]) }
    }
    compositions.flatMap { c -> c.personnalises.map { it.first to it.third } }.distinct().sortedBy { it.first }
            .forEach { (nom, unite) ->
                lignes += listOf("$nom ($unite$base)") +
                        compositions.map { c -> cellule(c.personnalises.firstOrNull { it.first == nom }?.second) }
            }
    lignes += listOf(translate(LocalizationKeys.Chart.RATIO_CA_P)) + compositions.map { c ->
        val p = c.nutriments["Phosphore"] ?: 0.0
        cellule(if (p > 0) (c.nutriments["Calcium"] ?: 0.0) / p else null)
    }
    lignes += listOf(translate(LocalizationKeys.Chart.RATIO_PROTEIN_PHOSPHORUS)) + compositions.map { c ->
        val p = c.nutriments["Phosphore"] ?: 0.0
        cellule(if (p > 0) (c.nutriments["Protéines"] ?: 0.0) / p else null)
    }
    return fr.vetbrain.vetnutri_mp.Export.TableauExporte(
            titre = translate(LocalizationKeys.Chart.DETAILED_ANALYSIS_TITLE),
            note = translate("graphAliments.export.detailNote", base),
            entetes = listOf(translate("graphAliments.export.measure")) + alimentsAnalyses.map { it.graphLabel },
            lignes = lignes
    )
}

/**
 * Associe le libellé interne (français) d'un nutriment à sa clé de localisation.
 */
private fun nutrientLabelKey(nom: String): String = when (nom) {
    "Humidité" -> LocalizationKeys.Chart.MOISTURE
    "Protéines" -> LocalizationKeys.Chart.PROTEIN
    "Lipides" -> LocalizationKeys.Chart.FAT
    "Glucides" -> LocalizationKeys.Chart.CARBOHYDRATES
    "Cellulose brute" -> LocalizationKeys.Chart.CRUDE_FIBER
    "Cendres" -> LocalizationKeys.Chart.ASH
    "Calcium" -> LocalizationKeys.Minerals.CALCIUM
    "Phosphore" -> LocalizationKeys.Minerals.PHOSPHORUS
    "Magnésium" -> LocalizationKeys.Minerals.MAGNESIUM
    "Sodium" -> LocalizationKeys.Minerals.SODIUM
    "Potassium" -> LocalizationKeys.Minerals.POTASSIUM
    else -> nom
}

/**
 * Ligne d'affichage d'un nutriment
 */
@Composable
private fun NutrientRow(
        nom: String,
        valeur: Double,
        unit: String,
        modifier: Modifier = Modifier
) {
    Row(
            modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
                text = nom,
                style = MaterialTheme.typography.body2
        )
        Text(
                text = if (unit.isEmpty()) {
                    NumberUtils.format(valeur, 2)
                } else {
                    "${NumberUtils.format(valeur, 2)} $unit"
                },
                style = MaterialTheme.typography.body2,
                fontWeight = FontWeight.Medium,
                color = VetNutriColors.Primary
        )
    }
}
