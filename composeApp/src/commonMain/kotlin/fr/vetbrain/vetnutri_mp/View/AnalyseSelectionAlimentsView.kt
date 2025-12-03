package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Data.FoodSearchFilters
import fr.vetbrain.vetnutri_mp.Data.NutrientOperator
import fr.vetbrain.vetnutri_mp.Data.SortCriteria
import fr.vetbrain.vetnutri_mp.Data.SortOrder
import fr.vetbrain.vetnutri_mp.View.Components.AdvancedSortDialog
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectDropdownField
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Utils.DataB

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
    alimentsInitialementSelectionnes: List<AlimentEv> = emptyList(),
    onSelectionChanged: ((List<AlimentEv>) -> Unit)? = null, // ✨ Callback pour synchroniser les changements
    onLoadNutrients: (suspend (List<String>, List<fr.vetbrain.vetnutri_mp.Enumer.Nutrient>) -> Map<String, Map<fr.vetbrain.vetnutri_mp.Enumer.Nutrient, Double>>)? = null,
    modifier: Modifier = Modifier
) {
    // État pour les aliments sélectionnés (synchronisé avec le ViewModel)
    var alimentsSelectionnes by remember { 
        mutableStateOf(alimentsInitialementSelectionnes.toList()) 
    }
    
    // Synchroniser les changements avec le ViewModel
    LaunchedEffect(alimentsSelectionnes) {
        onSelectionChanged?.invoke(alimentsSelectionnes)
    }
    
    // Synchroniser avec les changements externes (redimensionnement)
    LaunchedEffect(alimentsInitialementSelectionnes) {
        alimentsSelectionnes = alimentsInitialementSelectionnes.toList()
    }
    
    // État pour les filtres de recherche (utilisant FoodSearchFilters comme AddAlimentView)
    var filters by remember { mutableStateOf(FoodSearchFilters()) }
    
    // État pour stocker les nutriments chargés depuis la base de données
    var loadedNutrients by remember { 
        mutableStateOf<Map<String, Map<fr.vetbrain.vetnutri_mp.Enumer.Nutrient, Double>>>(emptyMap()) 
    }
    
    // Charger les nutriments nécessaires depuis la base de données si on a des filtres par nutriments ou un tri par nutriment
    LaunchedEffect(filters.nutrientFilters, filters.sortCriteria, aliments.map { it.uuid }) {
        val nutrientsForFilters = filters.nutrientFilters.mapNotNull { it.nutrient }
        val nutrientForSort = when (filters.sortCriteria) {
            SortCriteria.PROTEIN -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE)
            SortCriteria.FAT -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE)
            SortCriteria.CARBOHYDRATE -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.GLUCIDE)
            SortCriteria.FIBER -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.FIBRE)
            SortCriteria.ENERGY -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE)
            SortCriteria.CALCIUM -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL)
            SortCriteria.PHOSPHORUS -> listOf(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS)
            else -> emptyList()
        }
        val requiredNutrients = (nutrientsForFilters + nutrientForSort).distinct()
        
        if (requiredNutrients.isNotEmpty() && onLoadNutrients != null) {
            val foodUuids = aliments.map { it.uuid }
            val nutrientsMap = onLoadNutrients!!(foodUuids, requiredNutrients)
            loadedNutrients = nutrientsMap
        } else {
            loadedNutrients = emptyMap()
        }
    }
    
    // Créer une version enrichie des aliments avec les nutriments chargés
    val enrichedAliments = remember(aliments, loadedNutrients) {
        aliments.map { aliment ->
            val nutrients = loadedNutrients[aliment.uuid]
            if (nutrients != null && nutrients.isNotEmpty()) {
                // Créer une copie de l'aliment avec les nutriments chargés
                val enrichedValMap = aliment.valMap.toMutableMap()
                nutrients.forEach { (nutrient, value) ->
                    enrichedValMap[nutrient] = fr.vetbrain.vetnutri_mp.Data.NutrientQuantity(
                        value, 
                        nutrient.label
                    )
                }
                aliment.copy(valMap = enrichedValMap)
            } else {
                aliment
            }
        }
    }
    
    // Filtrer les aliments disponibles (exclure ceux déjà sélectionnés)
    val alimentsDisponibles = enrichedAliments.filter { aliment ->
        !alimentsSelectionnes.any { it.uuid == aliment.uuid }
    }
    
    // Filtrer en utilisant la même logique que FoodSearchComponent (comme AddAlimentView)
    val alimentsFiltres = remember(alimentsDisponibles, filters) {
        var result = alimentsDisponibles.filter { aliment ->
            // Filtre par recherche textuelle avec recherche multi-mots (AND)
            val matchesSearch =
                if (filters.searchQuery.isEmpty()) true
                else {
                    val searchWords =
                        filters.searchQuery
                            .trim()
                            .split("\\s+".toRegex())
                            .filter { it.isNotEmpty() }
                            .map { it.lowercase() }

                    if (searchWords.isEmpty()) true
                    else {
                        searchWords.all { word ->
                            aliment.nom?.lowercase()?.contains(word) == true ||
                            aliment.brand?.lowercase()?.contains(word) == true ||
                            aliment.gamme?.lowercase()?.contains(word) == true ||
                            aliment.ingredients?.lowercase()?.contains(word) == true
                        }
                    }
                }

            val matchesType =
                when (val sel = filters.selectedFoodType) {
                    null -> true
                    FoodKind.ALL -> true
                    else -> aliment.typeAliment == sel
                }

            val matchesGroup =
                when (val sel = filters.selectedFoodGroup) {
                    null -> true
                    GroupAlim.ALL -> true
                    else -> aliment.group == sel
                }

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

            val matchesIndications =
                if (filters.selectedIndications.isEmpty() ||
                    filters.selectedIndications.contains(AlimIndic.ALL)
                ) true
                else filters.selectedIndications.any { indication ->
                    aliment.indicat.contains(indication)
                }

            val matchesDataB =
                when (val dataBFilter = filters.dataB) {
                    null -> true
                    "" -> true
                    else -> aliment.dataB?.trim() == dataBFilter.trim()
                }

            // Filtre par nutriments
            val matchesNutrients =
                if (filters.nutrientFilters.isEmpty()) true
                else {
                    filters.nutrientFilters.all { nutrientFilter ->
                        if (nutrientFilter.nutrient == null || nutrientFilter.value == null) true
                        else {
                            val nutrientValue = aliment.getNutrient(nutrientFilter.nutrient, null)
                            // Si le nutriment n'est pas disponible, on accepte l'aliment
                            // (car c'est peut-être une version légère)
                            if (nutrientValue == null) true
                            else {
                                when (nutrientFilter.operator) {
                                    NutrientOperator.GREATER_OR_EQUAL -> nutrientValue >= nutrientFilter.value!!
                                    NutrientOperator.LESS_OR_EQUAL -> nutrientValue <= nutrientFilter.value!!
                                }
                            }
                        }
                    }
                }

            matchesSearch && matchesType && matchesGroup && matchesEspece && matchesIndications && matchesDataB && matchesNutrients
        }
        
        // Trier les résultats
        result = if (filters.sortCriteria != null) {
            val sortCriteria = filters.sortCriteria!!
            val sortedList = when (sortCriteria) {
                SortCriteria.NAME -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.nom ?: "" }
                    } else {
                        result.sortedByDescending { it.nom ?: "" }
                    }
                }
                SortCriteria.PROTEIN -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.PROTEINE, null) ?: 0.0 }
                    }
                }
                SortCriteria.FAT -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.LIPIDE, null) ?: 0.0 }
                    }
                }
                SortCriteria.CARBOHYDRATE -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.GLUCIDE, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.GLUCIDE, null) ?: 0.0 }
                    }
                }
                SortCriteria.FIBER -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.FIBRE, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.FIBRE, null) ?: 0.0 }
                    }
                }
                SortCriteria.ENERGY -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMain.ENERGIE, null) ?: 0.0 }
                    }
                }
                SortCriteria.CALCIUM -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.CAL, null) ?: 0.0 }
                    }
                }
                SortCriteria.PHOSPHORUS -> {
                    if (filters.sortOrder == SortOrder.ASCENDING) {
                        result.sortedBy { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS, null) ?: 0.0 }
                    } else {
                        result.sortedByDescending { it.getNutrient(fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro.PHOS, null) ?: 0.0 }
                    }
                }
            }
            sortedList
        } else {
            result
        }
        
        result
    }

    Card(
        modifier = modifier.fillMaxWidth(), 
        elevation = AppSizes.elevationSmall
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {

            // Section des filtres de recherche - Utilisation des mêmes composants que FoodSearchComponent
            // mais sans afficher les résultats (car on utilise nos propres listes gauche/droite)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = AppSizes.elevationSmall
            ) {
                Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    var showAdvancedSortDialog by remember { mutableStateOf(false) }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtres de recherche",
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold
                    )
                        
                        OutlinedButton(
                            onClick = { showAdvancedSortDialog = true },
                            border = ButtonDefaults.outlinedBorder,
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) MaterialTheme.colors.primary.copy(alpha = 0.08f) else MaterialTheme.colors.surface,
                                contentColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tri avancé", style = MaterialTheme.typography.caption)
                        }
                    }
                    
                    if (showAdvancedSortDialog) {
                        AdvancedSortDialog(
                            filters = filters,
                            onFiltersChange = { filters = it },
                            onDismiss = { showAdvancedSortDialog = false }
                        )
                    }
                    
                    // Barre de recherche (même style que FoodSearchComponent)
                    BasicAppTextField(
                        value = filters.searchQuery,
                        onValueChange = { filters = filters.copy(searchQuery = it) },
                        placeholder = "Nom, marque, ingrédients...",
                        leadingIcon = Icons.Default.Search,
                        trailingIcon = if (filters.searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                        onTrailingIconClick = { filters = filters.copy(searchQuery = "") },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    )
                    
                    // Filtres en grille (même structure que FoodSearchComponent)
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
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        // Base de données
                        Box(modifier = Modifier.weight(1f)) {
                            val dataBOptions = remember(aliments) {
                                listOf("") + aliments
                                    .mapNotNull { it.dataB?.trim() }
                                    .filter { it.isNotEmpty() }
                                    .distinct()
                                    .sorted()
                            }
                            val selectedDataB = filters.dataB ?: ""

                            DropdownField(
                                label = "Base",
                                selectedValue = selectedDataB,
                                options = dataBOptions,
                                onValueChange = {
                                    val newDataB = if (it.isEmpty()) null else it
                                    filters = filters.copy(dataB = newDataB)
                                },
                                valueToString = {
                                    if (it.isEmpty()) "Toutes"
                                    else {
                                        val dataBEnum = DataB.fromCode(it)
                                        dataBEnum?.displayName ?: it
                                    }
                                },
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

            // Contenu principal responsive avec détection de largeur
            BoxWithConstraints(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                val isCompact = maxWidth < 800.dp
                
                if (isCompact) {
                    // ✨ Mode COMPACT : Organisation verticale optimisée
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        // Section 1: Aliments disponibles (compacte)
                        AlimentsDisponiblesCompactSection(
                            alimentsFiltres = alimentsFiltres,
                            alimentsSelectionnes = alimentsSelectionnes,
                            onAlimentAdd = { aliment ->
                                alimentsSelectionnes = alimentsSelectionnes + aliment
                            }
                        )
                        
                        // Section 2: Boutons d'action (horizontaux)
                        ActionsRapidesSection(
                            alimentsFiltres = alimentsFiltres,
                            alimentsSelectionnes = alimentsSelectionnes,
                            onToutAjouter = {
                                alimentsSelectionnes = alimentsSelectionnes + alimentsFiltres
                            },
                            onToutRetirer = {
                                alimentsSelectionnes = emptyList()
                            }
                        )
                        
                        // Section 3: Aliments sélectionnés (compacte)
                        AlimentsSelectionnesCompactSection(
                            alimentsSelectionnes = alimentsSelectionnes,
                            onAlimentRemove = { aliment ->
                                alimentsSelectionnes = alimentsSelectionnes.filter { it.uuid != aliment.uuid }
                            }
                        )
                    }
                } else {
                    // 🖥️ Mode LARGE : Organisation horizontale conservée
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                // Liste de gauche : Aliments disponibles
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = AppSizes.elevationSmall
                ) {
                    Column(
                        modifier = Modifier.padding(AppSizes.paddingSmall)
                    ) {
                        Text(
                            text = "Aliments disponibles (${alimentsFiltres.size})",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                        Text("Tout ajouter", style = MaterialTheme.typography.caption)
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
                        Text("Tout retirer", style = MaterialTheme.typography.caption)
                    }
                }

                // Liste de droite : Aliments sélectionnés
                Card(
                    modifier = Modifier.weight(1f),
                    elevation = AppSizes.elevationSmall
                ) {
                    Column(
                        modifier = Modifier.padding(AppSizes.paddingSmall)
                    ) {
                        Text(
                            text = "Aliments sélectionnés (${alimentsSelectionnes.size})",
                            style = MaterialTheme.typography.subtitle2,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                    } // Fin Row mode large
                } // Fin else
            } // Fin BoxWithConstraints

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
                    Icon(Icons.AutoMirrored.Default.ArrowForward, contentDescription = null)
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
            modifier = Modifier.padding(AppSizes.paddingSmall / 2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                    Text(
                    text = aliment.nom ?: "Sans nom",
                        style = MaterialTheme.typography.body2,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${aliment.brand ?: "Sans marque"} - ${aliment.gamme ?: "Sans gamme"}",
                    style = MaterialTheme.typography.caption.copy(fontSize = 11.sp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (showAddButton && onAdd != null) {
                IconButtonWithTooltip(
                    onClick = onAdd,
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    tooltip = "Ajouter"
                )
            }
            
            if (showRemoveButton && onRemove != null) {
                IconButtonWithTooltip(
                    onClick = onRemove,
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Retirer",
                    tooltip = "Retirer"
                )
            }
        }
    }
}

/**
 * Section compacte pour les aliments disponibles
 */
@Composable
private fun AlimentsDisponiblesCompactSection(
    alimentsFiltres: List<AlimentEv>,
    alimentsSelectionnes: List<AlimentEv>,
    onAlimentAdd: (AlimentEv) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingSmall)
        ) {
            Text(
                text = "Aliments disponibles (${alimentsFiltres.size})",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // Liste avec hauteur limitée pour mode compact
            LazyColumn(
                modifier = Modifier.height(180.dp), // Hauteur fixe compact
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                items(alimentsFiltres) { aliment ->
                    AlimentItem(
                        aliment = aliment,
                        onAdd = { onAlimentAdd(aliment) },
                        showAddButton = true
                    )
                }
            }
        }
    }
}

/**
 * Section compacte pour les actions rapides
 */
@Composable
private fun ActionsRapidesSection(
    alimentsFiltres: List<AlimentEv>,
    alimentsSelectionnes: List<AlimentEv>,
    onToutAjouter: () -> Unit,
    onToutRetirer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSizes.paddingSmall),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToutAjouter,
                enabled = alimentsFiltres.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tout ajouter", style = MaterialTheme.typography.caption)
            }
            
            Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
            
            Button(
                onClick = onToutRetirer,
                enabled = alimentsSelectionnes.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Remove, 
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tout retirer", style = MaterialTheme.typography.caption)
            }
        }
    }
}

/**
 * Section compacte pour les aliments sélectionnés
 */
@Composable
private fun AlimentsSelectionnesCompactSection(
    alimentsSelectionnes: List<AlimentEv>,
    onAlimentRemove: (AlimentEv) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(AppSizes.paddingSmall)
        ) {
            Text(
                text = "Aliments sélectionnés (${alimentsSelectionnes.size})",
                style = MaterialTheme.typography.subtitle2,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (alimentsSelectionnes.isEmpty()) {
                Text(
                    text = "Aucun aliment sélectionné",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(AppSizes.paddingSmall)
                )
            } else {
                // Liste avec hauteur limitée pour mode compact
                LazyColumn(
                    modifier = Modifier.height(120.dp), // Hauteur fixe plus petite
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(alimentsSelectionnes) { aliment ->
                        AlimentItem(
                            aliment = aliment,
                            onRemove = { onAlimentRemove(aliment) },
                            showRemoveButton = true
                        )
                    }
                }
            }
        }
    }
}

