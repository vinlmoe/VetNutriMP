package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.View.components.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectDropdownField
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Localization.translateEnum

/**
 * Vue de sélection d'aliments avec deux listes (gauche et droite) et boutons de sélection.
 * Permet de sélectionner des aliments pour l'analyse graphique.
 */
@Composable
fun AnalyseSelectionAlimentsView(
    aliments: List<AlimentEv>,
    onClose: () -> Unit,
    onAlimentSelected: ((AlimentEv) -> Unit)? = null,
    onAnalyseGraphique: ((List<AlimentEv>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // État pour les aliments sélectionnés
    var alimentsSelectionnes by remember { mutableStateOf(listOf<AlimentEv>()) }
    
    // État pour les filtres de recherche
    var filters by remember { mutableStateOf(FoodSearchFilters()) }
    
    // État pour le texte de recherche
    var searchText by remember { mutableStateOf("") }
    
    // Filtrer les aliments disponibles (exclure ceux déjà sélectionnés)
    val alimentsDisponibles = aliments.filter { aliment ->
        !alimentsSelectionnes.any { it.uuid == aliment.uuid }
    }
    
    // Filtrer par tous les critères
    val alimentsFiltres = alimentsDisponibles.filter { aliment ->
        // Filtre par recherche textuelle
        val matchesSearch =
            if (searchText.isEmpty()) true
            else {
                (aliment.nom?.contains(searchText, ignoreCase = true) == true) ||
                (aliment.brand?.contains(searchText, ignoreCase = true) == true) ||
                (aliment.gamme?.contains(searchText, ignoreCase = true) == true) ||
                (aliment.ingredients?.contains(searchText, ignoreCase = true) == true)
            }

        // Filtre par type d'aliment
        val matchesType =
            when (val sel = filters.selectedFoodType) {
                null -> true
                FoodKind.ALL -> true
                else -> aliment.typeAliment == sel
            }

        // Filtre par groupe d'aliment
        val matchesGroup =
            when (val sel = filters.selectedFoodGroup) {
                null -> true
                GroupAlim.ALL -> true
                else -> aliment.group == sel
            }

        // Filtre par espèce
        val matchesEspece =
            when (val sel = filters.selectedEspece) {
                null -> true
                Espece.CH -> true
                else -> {
                    val foodSpecies = aliment.getEspecesList()
                    foodSpecies.isEmpty() ||
                    foodSpecies.contains(Espece.CH) ||
                    foodSpecies.contains(sel)
                }
            }

        // Filtre par indications
        val matchesIndications =
            if (filters.selectedIndications.isEmpty() ||
                filters.selectedIndications.contains(AlimIndic.ALL)
            ) true
            else filters.selectedIndications.any { indication ->
                aliment.indicat.contains(indication)
            }

        matchesSearch && matchesType && matchesGroup && matchesEspece && matchesIndications
    }

    Card(
        modifier = modifier.fillMaxWidth(), 
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // En-tête avec titre et bouton de fermeture
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sélection d'aliments pour l'analyse",
                        style = MaterialTheme.typography.h6
                    )
                    Text(
                        text = "Sélectionnez les aliments à analyser graphiquement",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                    )
                }
                IconButton(onClick = onClose) { 
                    Icon(Icons.Default.Close, contentDescription = "Fermer") 
                }
            }

            // Section des filtres de recherche
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall
            ) {
                Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(
                        text = "Filtres de recherche",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Barre de recherche textuelle avec design respecté
                    BasicAppTextField(
                        value = searchText,
                        onValueChange = { newValue -> searchText = newValue },
                        placeholder = "Rechercher par nom, marque, gamme ou ingrédients...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = if (searchText.isNotEmpty()) Icons.Default.Clear else null,
                        onTrailingIconClick = { searchText = "" },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )
                    
                    // Filtres par combobox en grille 2x2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        // Type d'aliment
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Type",
                                selectedValue = filters.selectedFoodType,
                                options = FoodKind.entries,
                                onValueChange = { filters = filters.copy(selectedFoodType = it) },
                                valueToString = { it.translateEnum() },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                            )
                        }

                        // Groupe d'aliment
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Groupe",
                                selectedValue = filters.selectedFoodGroup,
                                options = GroupAlim.entries,
                                onValueChange = { filters = filters.copy(selectedFoodGroup = it) },
                                valueToString = { it.translateEnum() },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        // Espèce
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                label = "Espèce",
                                selectedValue = filters.selectedEspece,
                                options = Espece.entries,
                                onValueChange = { filters = filters.copy(selectedEspece = it) },
                                valueToString = { it.translateEnum() },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                            )
                        }

                        // Indications (multi-sélection)
                        Box(modifier = Modifier.weight(1f)) {
                            MultiSelectDropdownField(
                                label = "Indications",
                                selectedValues = filters.selectedIndications,
                                options = AlimIndic.entries,
                                onValuesChange = { filters = filters.copy(selectedIndications = it) },
                                valueToString = { it.translateEnum() },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                            )
                        }
                    }
                }
            }

            // Contenu principal avec les deux listes
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
            ) {
                // Liste de gauche : Aliments disponibles
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = AppSizes.elevationSmall
                ) {
                    Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium)
                    ) {
                        Text(
                            text = "Aliments disponibles (${alimentsFiltres.size})",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(alimentsFiltres) { aliment ->
                                AlimentItem(
                                    aliment = aliment,
                                    onAdd = {
                                        alimentsSelectionnes = alimentsSelectionnes + aliment
                                    },
                                    showAddButton = true
                                )
                            }
                        }
                    }
                }

                // Boutons de sélection
                Column(
                    modifier = Modifier.padding(vertical = AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            // Ajouter tous les aliments filtrés
                            alimentsSelectionnes = alimentsSelectionnes + alimentsFiltres
                        },
                        enabled = alimentsFiltres.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tout ajouter")
                    }
                    
                    Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                    
                    Button(
                        onClick = {
                            // Retirer tous les aliments sélectionnés
                            alimentsSelectionnes = emptyList()
                        },
                        enabled = alimentsSelectionnes.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tout retirer")
                    }
                }

                // Liste de droite : Aliments sélectionnés
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = AppSizes.elevationSmall
                ) {
                    Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium)
                    ) {
                        Text(
                            text = "Aliments sélectionnés (${alimentsSelectionnes.size})",
                            style = MaterialTheme.typography.subtitle1,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(alimentsSelectionnes) { aliment ->
                                AlimentItem(
                                    aliment = aliment,
                                    onRemove = {
                                        alimentsSelectionnes = alimentsSelectionnes.filter { it.uuid != aliment.uuid }
                                    },
                                    showRemoveButton = true
                                )
                            }
                        }
                    }
                }
            }

            // Bouton d'analyse graphique
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        // Lancer l'analyse graphique avec tous les aliments sélectionnés
                        onAnalyseGraphique?.invoke(alimentsSelectionnes)
                    },
                    enabled = alimentsSelectionnes.isNotEmpty()
                ) {
                    Text("Voir l'analyse graphique")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}

/**
 * Composant pour afficher un aliment avec bouton d'ajout ou de suppression
 */
@Composable
private fun AlimentItem(
    aliment: AlimentEv,
    onAdd: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    showAddButton: Boolean = false,
    showRemoveButton: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = AppSizes.elevationSmall
    ) {
        Row(
            modifier = Modifier.padding(AppSizes.paddingSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = aliment.nom ?: "Sans nom",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${aliment.brand ?: "Sans marque"} - ${aliment.gamme ?: "Sans gamme"}",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            
            if (showAddButton && onAdd != null) {
                IconButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            }
            
            if (showRemoveButton && onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Remove, contentDescription = "Retirer")
                }
            }
        }
    }
}

