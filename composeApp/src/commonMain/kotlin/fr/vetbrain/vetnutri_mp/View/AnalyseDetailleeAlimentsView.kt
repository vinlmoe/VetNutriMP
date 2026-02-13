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
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
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
                        text = "Analyse détaillée des aliments",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                Text(
                        text = "Cliquez sur un aliment pour voir ses détails nutritionnels complets",
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
                            text = "/1000 kcal",
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
                            text = "/100g MS",
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
                        text = "Aucun aliment à analyser",
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
                            preferencesEspece = preferencesEspece,
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
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?,
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
                            text = "${data.numero}. ${data.aliment.nom ?: "Sans nom"}",
                            style = MaterialTheme.typography.h6,
                            fontWeight = FontWeight.Bold,
                            color = VetNutriColors.Primary
                    )
                    if (data.aliment.brand != null) {
                        Text(
                                text = "Marque: ${data.aliment.brand}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    if (data.aliment.gamme != null) {
                        Text(
                                text = "Gamme: ${data.aliment.gamme}",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Bouton d'expansion
                IconButton(onClick = { onExpandedChange(data.aliment.uuid) }) {
                    Icon(
                            imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = if (isExpanded) "Réduire" else "Développer"
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
                            text = "Densité énergétique",
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
                            text = "Protéines (% énergie)",
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
                            text = "Lipides (% énergie)",
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
                        equationRepository = equationRepository,
                        preferencesEspece = preferencesEspece
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
        equationRepository: EquationRepository?,
        preferencesEspece: fr.vetbrain.vetnutri_mp.Data.PreferencesEspece?
) {
    val coroutineScope = rememberCoroutineScope()
    var nutrientsData by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(aliment, useDryMatterPer100g) {
        isLoading = true
        val alimentRation = AlimentRation(
                aliment = aliment,
                quantite = 100.0,
                weight = 1.0
        )
        
        val nutrients = mutableMapOf<String, Double>()
        
        // Nutriments principaux
        val humidite = alimentRation.getNutrientWithComplementary(
                NutrientMain.HUMIDITE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val proteine = alimentRation.getNutrientWithComplementary(
                NutrientMain.PROTEINE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val lipide = alimentRation.getNutrientWithComplementary(
                NutrientMain.LIPIDE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val glucide = alimentRation.getNutrientWithComplementary(
                NutrientMain.GLUCIDE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val cellulose = alimentRation.getNutrientWithComplementary(
                NutrientMain.CELLULOSE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val cendre = alimentRation.getNutrientWithComplementary(
                NutrientMain.CENDRE, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        
        // Minéraux
        val calcium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.CAL, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val phosphore = alimentRation.getNutrientWithComplementary(
                NutrientMacro.PHOS, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val magnesium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.MG, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val sodium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.NA, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        val potassium = alimentRation.getNutrientWithComplementary(
                NutrientMacro.K, preferencesEspece, equationRepository, referenceEv
        ) ?: 0.0
        
        // Conversion selon le mode
        val matiereSeche = 100.0 - humidite
        val densiteEnergetique = data.densiteEnergetique
        
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
        
        nutrientsData = nutrients
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
                    text = "Composition nutritionnelle (${if (useDryMatterPer100g) "/100g MS" else "/1000 kcal"})",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary
            )
            
            // Nutriments principaux
            Text(
                    text = "Macronutriments",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
            
            nutrientsData.filter { it.key in listOf("Protéines", "Lipides", "Glucides", "Cellulose brute", "Cendres") }
                    .forEach { (nom, valeur) ->
                        NutrientRow(nom = nom, valeur = valeur, unit = "g")
                    }
            
            if (!useDryMatterPer100g && nutrientsData["Humidité"] != null) {
                NutrientRow(nom = "Humidité", valeur = nutrientsData["Humidité"]!!, unit = "g")
            }
            
            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
            
            // Minéraux
            Text(
                    text = "Minéraux",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
            
            nutrientsData.filter { it.key in listOf("Calcium", "Phosphore", "Magnésium", "Sodium", "Potassium") }
                    .forEach { (nom, valeur) ->
                        NutrientRow(nom = nom, valeur = valeur, unit = "g")
                    }
            
            // Ratios importants
            Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
            Text(
                    text = "Ratios",
                    style = MaterialTheme.typography.caption,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.8f)
            )
            
            val calcium = nutrientsData["Calcium"] ?: 0.0
            val phosphore = nutrientsData["Phosphore"] ?: 0.0
            val proteine = nutrientsData["Protéines"] ?: 0.0
            
            if (phosphore > 0) {
                NutrientRow(nom = "Ratio Ca:P", valeur = calcium / phosphore, unit = "")
            }
            if (phosphore > 0) {
                NutrientRow(nom = "Ratio Protéines:Phosphore", valeur = proteine / phosphore, unit = "")
            }
        }
    }
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
