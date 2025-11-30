package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeEditViewModel

/**
 * Vue pour ajouter un aliment à une recette
 */
@Composable
fun RecipeAddAlimentView(
    recipeEditViewModel: RecipeEditViewModel,
    onNavigateBack: () -> Unit,
    onAddAliment: (AlimentEv, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // États pour les filtres de recherche
    var searchFilters by remember { 
        mutableStateOf(FoodSearchFilters()) 
    }
    
    // État pour l'aliment sélectionné et la quantité
    var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
    var quantite by remember { mutableStateOf("100") }
    var quantiteError by remember { mutableStateOf(false) }
    
    // Charger les aliments
    val allFoods = remember {
        mutableStateListOf<AlimentEv>()
    }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
                val foods = recipeEditViewModel.foodRepository.getAllFoods()
                kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.Main) {
                    allFoods.clear()
                    allFoods.addAll(foods)
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un aliment à la recette") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                backgroundColor = VetNutriColors.Primary,
                contentColor = VetNutriColors.OnPrimary
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedFood != null && !quantiteError && quantite.isNotEmpty()) {
                        selectedFood?.let { aliment ->
                            try {
                                val quantiteValue = quantite.toDouble()
                                if (quantiteValue > 0) {
                                    onAddAliment(aliment, quantiteValue)
                                }
                            } catch (e: NumberFormatException) {
                                // Ignore
                            }
                        }
                    }
                },
                backgroundColor = if (selectedFood != null && !quantiteError && quantite.isNotEmpty()) {
                    VetNutriColors.Primary
                } else {
                    VetNutriColors.Primary.copy(alpha = 0.5f)
                },
                contentColor = VetNutriColors.OnPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter l'aliment sélectionné",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) {
        // Contenu principal - layout à deux colonnes
        Row(
            modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // Colonne gauche - Recherche d'aliments (60% de l'espace)
            Column(
                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                // Utilisation du composant FoodSearchComponent
                FoodSearchComponent(
                    foods = allFoods,
                    filters = searchFilters,
                    onFiltersChange = { searchFilters = it },
                    config = FoodSearchConfig(
                        showFilters = true,
                        showSearchBar = true,
                        showResultsCount = true,
                        layout = FoodSearchLayout.VERTICAL,
                        onFoodSelected = { aliment -> selectedFood = aliment },
                        availableActions = emptyList(),
                        isLoading = isLoading,
                        selectedFood = selectedFood
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Colonne droite - Détails de l'aliment sélectionné (40% de l'espace)
            Card(
                modifier = Modifier.weight(0.4f).fillMaxHeight(),
                elevation = AppSizes.elevationSmall
            ) {
                if (selectedFood == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sélectionnez un aliment\npour voir ses détails",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AlimentDetailsPanel(
                            aliment = selectedFood!!,
                            quantite = quantite,
                            onQuantiteChange = { newQuantite ->
                                quantite = newQuantite
                                quantiteError = try {
                                    newQuantite.toDouble() <= 0
                                } catch (e: NumberFormatException) {
                                    true
                                }
                            },
                            quantiteError = quantiteError
                        )
                    }
                }
            }
        }
    }
}


/** Panneau de détails de l'aliment sélectionné */
@Composable
private fun AlimentDetailsPanel(
    aliment: AlimentEv,
    quantite: String,
    onQuantiteChange: (String) -> Unit,
    quantiteError: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .padding(AppSizes.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
        state = rememberLazyListState()
    ) {
        item {
            Text(
                text = "Détails de l'aliment",
                style = MaterialTheme.typography.h6,
                color = VetNutriColors.Primary
            )
        }
        
        item { Divider() }
        
        // Informations générales
        item {
            Text(
                text = aliment.nom ?: "Sans nom",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (!aliment.brand.isNullOrEmpty()) {
            item { DetailRow("Marque", aliment.brand!!) }
        }
        
        if (!aliment.gamme.isNullOrEmpty()) {
            item { DetailRow("Gamme", aliment.gamme!!) }
        }
        
        aliment.typeAliment?.let { type ->
            item { DetailRow("Type", type.translateEnum()) }
        }
        
        aliment.group?.let { group ->
            item { DetailRow("Groupe", group.translateEnum()) }
        }
        
        // Espèces ciblées
        val species = aliment.getEspecesList()
            .filter { it != Espece.CH }
            .map { it.translateEnum() }
        if (species.isNotEmpty()) {
            item { DetailRow("Espèces", species.joinToString(", ")) }
        }
        
        // Indications
        val indications = aliment.getIndications()
            .filter { it != AlimIndic.ALL && it != AlimIndic.AUTRE }
            .map { it.translateEnum() }
        if (indications.isNotEmpty()) {
            item { DetailRow("Indications", indications.joinToString(", ")) }
        }
        
        if (!aliment.ingredients.isNullOrEmpty()) {
            item { DetailRow("Ingrédients", aliment.ingredients!!) }
        }
        
        item { Divider() }
        
        // Section quantité
        item {
            Text(
                text = "Quantité à ajouter",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            BasicAppTextField(
                value = quantite,
                onValueChange = onQuantiteChange,
                placeholder = "Quantité (g)",
                modifier = Modifier.fillMaxWidth(),
                isError = quantiteError,
                errorMessage = if (quantiteError) "Veuillez entrer une quantité valide > 0" else null
            )
        }
        
        // Informations nutritionnelles principales
        if (aliment.valMap.isNotEmpty()) {
            item { Divider() }
            
            item {
                Text(
                    text = "Composition nutritionnelle (pour 100g)",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
            }
            
            val nutrientsToShow = listOf(
                "PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "HUMIDITE"
            )
            
            nutrientsToShow.forEach { nutrientLabel ->
                val nutrient = aliment.valMap.keys.find { it.label == nutrientLabel }
                if (nutrient != null) {
                    val value = aliment.valMap[nutrient]
                    if (value != null) {
                        item {
                            DetailRow(
                                nutrient.label,
                                "${TextUtils.formatDecimal(value.value.toDouble(), 1)} ${value.unit ?: ""}"
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Composant pour afficher une ligne de détail */
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}
