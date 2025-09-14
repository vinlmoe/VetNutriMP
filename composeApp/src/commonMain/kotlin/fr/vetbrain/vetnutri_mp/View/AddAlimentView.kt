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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchComponent
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchConfig
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchLayout

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
        modifier: Modifier = Modifier
) {
        // États pour les filtres - maintenant gérés par FoodSearchComponent
        var filters by remember { mutableStateOf(FoodSearchFilters()) }

        // État pour l'aliment sélectionné
        var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
        
        // État pour le message de confirmation
        var showConfirmation by remember { mutableStateOf(false) }
        
        // Compteur d'aliments ajoutés
        var alimentsAdded by remember { mutableStateOf(0) }

        // Charger les aliments au premier affichage
        LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }
        
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

        // Configuration pour FoodSearchComponent
        val searchConfig = remember(isLoadingFoods, selectedFood) {
                FoodSearchConfig(
                        layout = FoodSearchLayout.HORIZONTAL,
                        showFilters = true,
                        showSearchBar = true,
                        showResultsCount = true,
                        onFoodSelected = { aliment -> selectedFood = aliment },
                        isLoading = isLoadingFoods,
                        selectedFood = selectedFood
                )
                }

        Column(modifier = modifier.fillMaxSize()) {
                // Barre de navigation avec signature correcte et taille réduite
                TopBar(
                        title = "Ajouter aliment - ${ration.name}",
                        onBackClick = onNavigateBack,
                        onSettingsClick = { /* Pas de settings pour cette vue */}
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
                                // Utilisation du composant partagé FoodSearchComponent
                                FoodSearchComponent(
                                    foods = availableFoods,
                                    filters = filters,
                                    onFiltersChange = {
                                        println("DEBUG AddAlimentView - onFiltersChange appelé avec dataB: ${it.dataB}")
                                        println("DEBUG AddAlimentView - Ancien filters.dataB: ${filters.dataB}")
                                        filters = it
                                        println("DEBUG AddAlimentView - Nouveau filters.dataB: ${filters.dataB}")
                                    },
                                    config = searchConfig,
                                    modifier = Modifier.fillMaxSize()
                                )
                                }

                                                        // Colonne droite - Détails de l'aliment sélectionné (40% de l'espace)
                        // FoodSearchComponent gère déjà l'affichage des détails via FoodDetailsPanel
                        }

                        if (selectedFood != null) {
                                FloatingActionButton(
                                        onClick = {
                                                selectedFood?.let { aliment ->
                                                        // Ajouter l'aliment à la ration avec une quantité par défaut de 100g
                                                        onAddAliment(aliment, 100.0)
                                                        // Réinitialiser la sélection pour permettre d'ajouter d'autres aliments
                                                        selectedFood = null
                                                        // Incrémenter le compteur d'aliments ajoutés
                                                        alimentsAdded++
                                                        // Afficher le message de confirmation
                                                        showConfirmation = true
                                                }
                                        },
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary,
                                        modifier = Modifier.align(Alignment.BottomEnd).padding(AppSizes.paddingLarge)
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Ajouter l'aliment",
                                                modifier = Modifier.size(24.dp)
                                        )
                                }
                        }
                }
        }
}

// Le composant AlimentListItem est maintenant fourni par FoodSearchComponent

// Le panneau de détails est maintenant géré par FoodSearchComponent

// Le composant DetailRow n'est plus utilisé
