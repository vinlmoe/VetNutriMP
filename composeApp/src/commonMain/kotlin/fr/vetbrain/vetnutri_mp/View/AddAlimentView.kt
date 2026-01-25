package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.Components.FoodSearchLayout
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel

/**
 * Vue complète pour ajouter un aliment à une ration
 *
 * @param viewModel ViewModel contenant les données
 * @param ration Ration à laquelle ajouter l'aliment
 * @param onNavigateBack Action pour revenir à la vue précédente
 * @param onAddAliment Action pour ajouter l'aliment (aliment, quantité)
 */
@Composable
fun AddAlimentView(
        viewModel: AnimalDetailViewModel,
        ration: Ration,
        onNavigateBack: () -> Unit,
        onAddAliment: (AlimentEv, Double) -> Unit,
        equationRepository: EquationRepository? = null,
        modifier: Modifier = Modifier
) {
        var showFoodEditView by remember { mutableStateOf(false) }
        var foodEditViewModel by remember { mutableStateOf<FoodEditViewModel?>(null) }

        // États pour les filtres - mémorisés tant que l'animal ne change pas
        val filtersFromViewModel by viewModel.addAlimentFilters.collectAsState()
        var filters by remember { mutableStateOf(filtersFromViewModel) }
        var aminoEligibility by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
        var isEvaluatingAmino by remember { mutableStateOf(false) }

        // État pour l'aliment sélectionné (version complète avec données nutritionnelles)
        val selectedFoodIdFromViewModel by viewModel.addAlimentSelectedFoodId.collectAsState()
        var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
        var isLoadingCompleteFood by remember { mutableStateOf(false) }
        
        // État pour le message de confirmation
        var showConfirmation by remember { mutableStateOf(false) }
        
        // Compteur d'aliments ajoutés
        var alimentsAdded by remember { mutableStateOf(0) }
        
        // CoroutineScope pour les opérations asynchrones
        val coroutineScope = rememberCoroutineScope()

        // Charger les aliments au premier affichage
        LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }

        LaunchedEffect(filtersFromViewModel) { filters = filtersFromViewModel }
        
        // Faire disparaître le message de confirmation après 3 secondes
        LaunchedEffect(showConfirmation) {
                if (showConfirmation) {
                        kotlinx.coroutines.delay(3000)
                        showConfirmation = false
                }
        }

        // Observer la liste des aliments depuis le ViewModel
        val availableFoods by viewModel.availableFoods.collectAsState()
        val isLoadingFoods by viewModel.isLoadingFoods.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()

        // Configuration pour FoodSearchComponent
        val searchConfig = remember(isLoadingFoods, selectedFood, isLoadingCompleteFood, referenceUtilisee, equationRepository) {
                FoodSearchConfig(
                        layout = FoodSearchLayout.HORIZONTAL,
                        showFilters = true,
                        showSearchBar = true,
                        showResultsCount = true,
                        onFoodSelected = { aliment ->
                            viewModel.setAddAlimentSelectedFoodId(aliment.uuid)
                            // Charger l'aliment complet avec ses données nutritionnelles
                            isLoadingCompleteFood = true
                            viewModel.loadCompleteFood(aliment.uuid) { alimentComplet ->
                                selectedFood = alimentComplet
                                isLoadingCompleteFood = false
                            }
                        },
                        isLoading = isLoadingFoods || isLoadingCompleteFood,
                        selectedFood = selectedFood,
                        referenceEv = referenceUtilisee,
                        equationRepository = equationRepository,
                        onLoadNutrients = { foodUuids, nutrients ->
                                viewModel.loadNutrientsForFoods(foodUuids, nutrients)
                        }
                )
                }

        // Pré-calcul asynchrone de l'éligibilité AA (nécessite aliment complet)
        LaunchedEffect(filters.aminoOnly, availableFoods) {
                if (!filters.aminoOnly) return@LaunchedEffect
                isEvaluatingAmino = true
                try {
                        val currentMap = aminoEligibility.toMutableMap()
                        for (aliment in availableFoods) {
                                if (currentMap.containsKey(aliment.uuid)) continue
                                try {
                                        val complet = viewModel.getAlimentComplet(aliment.uuid)
                                        val lys = complet?.getNutrient(AAEnum.LYSINE)
                                        val met = complet?.getNutrient(AAEnum.METHIONINE)
                                        currentMap[aliment.uuid] = (lys != null && lys > 0.0) && (met != null && met > 0.0)
                                } catch (e: Exception) {
                                        currentMap[aliment.uuid] = false
                                }
                        }
                        aminoEligibility = currentMap
                } finally {
                        isEvaluatingAmino = false
                }
        }

        // Appliquer le filtre "Acide Aminé" (Lysine & Méthionine requis)
        val displayedFoods = remember(availableFoods, filters.aminoOnly, aminoEligibility) {
                if (!filters.aminoOnly) availableFoods
                else availableFoods.filter { aliment -> aminoEligibility[aliment.uuid] == true }
        }

        LaunchedEffect(selectedFoodIdFromViewModel, availableFoods) {
                if (selectedFoodIdFromViewModel.isNullOrBlank()) {
                        selectedFood = null
                        return@LaunchedEffect
                }
                if (selectedFood?.uuid == selectedFoodIdFromViewModel) return@LaunchedEffect
                isLoadingCompleteFood = true
                viewModel.loadCompleteFood(selectedFoodIdFromViewModel!!) { alimentComplet ->
                        selectedFood = alimentComplet
                        isLoadingCompleteFood = false
                }
        }

        if (showFoodEditView && foodEditViewModel != null) {
                FoodEditView(
                        viewModel = foodEditViewModel!!,
                        onNavigateBack = {
                                showFoodEditView = false
                                foodEditViewModel = null
                        },
                        onNavigateToSettings = {},
                        onFoodSaved = { savedFood ->
                                viewModel.setAddAlimentSelectedFoodId(savedFood.uuid)
                        },
                        modifier = Modifier.fillMaxSize()
                )
                return
        }

        Column(modifier = modifier.fillMaxSize()) {
                // Barre de navigation avec signature correcte et taille réduite
                TopBar(
                        title = "Ajouter aliment - ${ration.name}",
                        onBackClick = onNavigateBack,
                        onSettingsClick = { /* Pas de settings pour cette vue */ },
                        actions = {
                                IconButton(
                                        onClick = {
                                                foodEditViewModel = FoodEditViewModel(
                                                        foodRepository = viewModel.foodRepository,
                                                        alimentUuid = null
                                                )
                                                showFoodEditView = true
                                        },
                                        modifier = Modifier.size(AppSizes.iconSizeLarge)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Créer un aliment",
                                                modifier = Modifier.size(AppSizes.iconSizeMedium)
                                        )
                                }
                        }
                )
                
                // Message de confirmation d'ajout d'aliment
                if (showConfirmation) {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                elevation = AppSizes.elevationSmall
                        ) {
                                Row(
                                        modifier = Modifier.padding(AppSizes.paddingMedium),
                                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Succès",
                                                tint = VetNutriColors.Primary,
                                                modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                                text = "Aliment ajouté à la ration avec succès !",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }
                        }
                }

                // Contenu principal et bouton d'action superposés
                Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Colonne gauche - Filtres et liste (60% de l'espace)
                                Column(
                                        modifier = Modifier.weight(0.6f).fillMaxHeight(),
                                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                // Composant principal de recherche (le bouton Ac. Aminé est dans FoodSearchComponent)
                                FoodSearchComponent(
                                    foods = displayedFoods,
                                    filters = filters,
                                    onFiltersChange = {
                                        filters = it
                                        viewModel.setAddAlimentFilters(it)
                                    },
                                    config = searchConfig,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (filters.aminoOnly && isEvaluatingAmino) {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                                }

                                                        // Colonne droite - Détails de l'aliment sélectionné (40% de l'espace)
                        // FoodSearchComponent gère déjà l'affichage des détails via FoodDetailsPanel
                        }

                        if (selectedFood != null) {
                                // Bouton + (ajouter à la ration existante)
                                FloatingActionButton(
                                        onClick = {
                                                selectedFood?.let { aliment ->
                                                        // Ajouter l'aliment à la ration avec une quantité par défaut de 100g
                                                        onAddAliment(aliment, 100.0)
                                                        // L'aliment reste sélectionné pour permettre d'ajouter plusieurs fois le même aliment
                                                        // selectedFood = null // Supprimé pour garder l'aliment sélectionné
                                                        // Incrémenter le compteur d'aliments ajoutés
                                                        alimentsAdded++
                                                        // Afficher le message de confirmation
                                                        showConfirmation = true
                                                }
                                        },
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(AppSizes.paddingLarge).offset(y = (-80).dp)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ajouter l'aliment",
                                                modifier = Modifier.size(24.dp)
                                        )
                                }
                                
                                // Bouton +R (créer nouvelle ration)
                                FloatingActionButton(
                                        onClick = {
                                                selectedFood?.let { aliment ->
                                                        // Utiliser la même logique que dans AnalyseGraphiqueAlimentsView.kt
                                                        coroutineScope.launch {
                                                                val alimentComplet = viewModel.getAlimentComplet(aliment.uuid)
                                                                if (alimentComplet != null) {
                                                                        // Créer une nouvelle ration avec le nom de la marque de l'aliment
                                                                        val nomRation = alimentComplet.brand ?: alimentComplet.nom ?: "Nouvelle ration"
                                                                        val nouvelleRation = Ration(
                                                                                name = nomRation,
                                                                                actual = false,
                                                                                alimentMutableList = mutableListOf()
                                                                        )
                                                                        
                                                                        // Ajouter l'aliment à la nouvelle ration
                                                                        val alimentRation = AlimentRation(
                                                                                aliment = alimentComplet,
                                                                                quantite = 100.0,
                                                                                proportion = 0.0, // Sera calculé par le ViewModel
                                                                                weight = 0.0, // Sera calculé par le ViewModel
                                                                                densiteEnergetique = 0.0 // Sera calculé par le ViewModel
                                                                        )
                                                                        nouvelleRation.alimentMutableList.add(alimentRation)
                                                                        
                                                                        // Ajouter la ration à la consultation et la sélectionner
                                                                        val selectedConsultation = viewModel.selectedConsultation.value
                                                                        if (selectedConsultation != null) {
                                                                                viewModel.addRationToConsultation(nouvelleRation)
                                                                                viewModel.updateConsultation(selectedConsultation)
                                                                                viewModel.selectRation(nouvelleRation)
                                                                        }
                                                                        
                                                                        // L'aliment reste sélectionné pour permettre d'ajouter plusieurs fois le même aliment
                                                                        // selectedFood = null // Supprimé pour garder l'aliment sélectionné
                                                                        // Incrémenter le compteur d'aliments ajoutés
                                                                        alimentsAdded++
                                                                        // Afficher le message de confirmation
                                                                        showConfirmation = true
                                                                }
                                                        }
                                                }
                                        },
                                        backgroundColor = VetNutriColors.Secondary,
                                        contentColor = Color.White,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(AppSizes.paddingLarge)
                                ) {
                                        Text(
                                                text = "+R",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold
                                        )
                                }
                        }
                }
        }
}

// Le composant AlimentListItem est maintenant fourni par FoodSearchComponent

// Le panneau de détails est maintenant géré par FoodSearchComponent

// Le composant DetailRow n'est plus utilisé
