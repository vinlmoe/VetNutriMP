package fr.vetbrain.vetnutri_mp.View.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectDropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.DataB
import fr.vetbrain.vetnutri_mp.Utils.DataBMapping
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.View.components.NutrientPieChart

/** Filtre par nutriment avec opérateur et valeur */
data class NutrientFilter(
        val nutrient: Nutrient? = null,
        val operator: NutrientOperator = NutrientOperator.GREATER_OR_EQUAL,
        val value: Double? = null
)

/** Opérateur pour les filtres de nutriments */
enum class NutrientOperator(val displayName: String) {
        GREATER_OR_EQUAL("≥"),
        LESS_OR_EQUAL("≤")
}

/** Critère de tri */
enum class SortCriteria(val displayName: String) {
        NAME("Nom"),
        PROTEIN("Protéines"),
        FAT("Lipides"),
        CARBOHYDRATE("Glucides"),
        FIBER("Fibres"),
        ENERGY("Énergie"),
        CALCIUM("Calcium"),
        PHOSPHORUS("Phosphore")
}

/** Ordre de tri */
enum class SortOrder(val displayName: String) {
        ASCENDING("Croissant"),
        DESCENDING("Décroissant")
}

/** État partagé pour les filtres de recherche d'aliments */
data class FoodSearchFilters(
        val searchQuery: String = "",
        val selectedFoodType: FoodKind? = null,
        val selectedFoodGroup: GroupAlim? = null,
        val selectedEspece: Espece? = null,
        val selectedIndications: Set<AlimIndic> = emptySet(),
        val dataB: String? = null,
        val aminoOnly: Boolean = false,
        val nutrientFilters: List<NutrientFilter> = emptyList(),
        val sortCriteria: SortCriteria? = null,
        val sortOrder: SortOrder = SortOrder.ASCENDING
)

/** Configuration du composant de recherche d'aliments */
data class FoodSearchConfig(
        val showFilters: Boolean = true,
        val showSearchBar: Boolean = true,
        val showResultsCount: Boolean = true,
        val layout: FoodSearchLayout = FoodSearchLayout.VERTICAL,
        val maxHeight: Int? = null,
        val onFoodSelected: ((AlimentEv) -> Unit)? = null,
        val onFoodAction: ((AlimentEv, String) -> Unit)? = null,
        val availableActions: List<String> = emptyList(),
        val isLoading: Boolean = false,
        val selectedFood: AlimentEv? = null,
        val referenceEv: ReferenceEv? = null,
        val equationRepository: EquationRepository? = null,
        val onLoadNutrients: (suspend (List<String>, List<Nutrient>) -> Map<String, Map<Nutrient, Double>>)? = null
)

/** Types de layout disponibles */
enum class FoodSearchLayout {
        VERTICAL, // Layout vertical simple (comme FoodListView)
        HORIZONTAL, // Layout à deux colonnes (comme AddAlimentView)
        COMPACT // Layout compact pour les petits espaces
}

/**
 * Composant principal de recherche d'aliments partagé
 *
 * @param foods Liste des aliments disponibles
 * @param filters Filtres de recherche actuels
 * @param onFiltersChange Callback appelé quand les filtres changent
 * @param config Configuration du composant
 * @param modifier Modificateur optionnel
 */
@Composable
fun FoodSearchComponent(
        foods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        config: FoodSearchConfig = FoodSearchConfig(),
        modifier: Modifier = Modifier
) {
        // État pour stocker les nutriments chargés depuis la base de données
        var loadedNutrients by remember { 
                mutableStateOf<Map<String, Map<Nutrient, Double>>>(emptyMap()) 
        }
        
        // Charger les nutriments nécessaires depuis la base de données si on a des filtres par nutriments ou un tri par nutriment
        LaunchedEffect(filters.nutrientFilters, filters.sortCriteria, foods.map { it.uuid }) {
                val nutrientsForFilters = filters.nutrientFilters.mapNotNull { it.nutrient }
                val nutrientForSort = when (filters.sortCriteria) {
                        SortCriteria.PROTEIN -> listOf(NutrientMain.PROTEINE)
                        SortCriteria.FAT -> listOf(NutrientMain.LIPIDE)
                        SortCriteria.CARBOHYDRATE -> listOf(NutrientMain.GLUCIDE)
                        SortCriteria.FIBER -> listOf(NutrientMain.FIBRE)
                        SortCriteria.ENERGY -> listOf(NutrientMain.ENERGIE)
                        SortCriteria.CALCIUM -> listOf(NutrientMacro.CAL)
                        SortCriteria.PHOSPHORUS -> listOf(NutrientMacro.PHOS)
                        else -> emptyList()
                }
                val requiredNutrients = (nutrientsForFilters + nutrientForSort).distinct()
                
                if (requiredNutrients.isNotEmpty() && config.onLoadNutrients != null) {
                        val foodUuids = foods.map { it.uuid }
                        val nutrientsMap = config.onLoadNutrients!!(foodUuids, requiredNutrients)
                        loadedNutrients = nutrientsMap
                } else {
                        loadedNutrients = emptyMap()
                }
        }
        
        // Créer une version enrichie des aliments avec les nutriments chargés
        val enrichedFoods = remember(foods, loadedNutrients) {
                foods.map { aliment ->
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

        // Filtrer les aliments selon les critères
        val filteredFoods =
                remember(
                        enrichedFoods,
                        filters.searchQuery,
                        filters.selectedFoodType,
                        filters.selectedFoodGroup,
                        filters.selectedEspece,
                        filters.selectedIndications,
                        filters.dataB,
                        filters.nutrientFilters,
                        filters.sortCriteria,
                        filters.sortOrder,
                        config.referenceEv
                ) {
                        var result =
                                enrichedFoods.filter { aliment ->
                                        // Filtre par recherche textuelle avec recherche multi-mots
                                        // (AND)
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
                                                                // Chaque mot doit être trouvé dans
                                                                // au moins un des
                                                                // champs
                                                                searchWords.all { word ->
                                                                        aliment.nom
                                                                                ?.lowercase()
                                                                                ?.contains(word) ==
                                                                                true ||
                                                                                aliment.brand
                                                                                        ?.lowercase()
                                                                                        ?.contains(
                                                                                                word
                                                                                        ) == true ||
                                                                                aliment.gamme
                                                                                        ?.lowercase()
                                                                                        ?.contains(
                                                                                                word
                                                                                        ) == true ||
                                                                                aliment.ingredients
                                                                                        ?.lowercase()
                                                                                        ?.contains(
                                                                                                word
                                                                                        ) == true
                                                                }
                                                        }
                                                }

                                        // Filtre par type d'aliment (ALL = pas de filtre)
                                        val matchesType =
                                                when (val sel = filters.selectedFoodType) {
                                                        null -> true
                                                        FoodKind.ALL ->
                                                                true // "ALL" = pas de filtre, tous
                                                        // les types
                                                        // acceptés
                                                        else -> aliment.typeAliment == sel
                                                }

                                        // Filtre par groupe d'aliment (ALL = pas de filtre)
                                        val matchesGroup =
                                                when (val sel = filters.selectedFoodGroup) {
                                                        null -> true
                                                        GroupAlim.ALL ->
                                                                true // "ALL" = pas de filtre, tous
                                                        // les groupes
                                                        // acceptés
                                                        else -> aliment.group == sel
                                                }

                                        // Filtre par espèce (CH = "ALL" = pas de filtre)
                                        val matchesEspece =
                                                when (val sel = filters.selectedEspece) {
                                                        null -> true
                                                        Espece.CH ->
                                                                true // "CH" (avec label "ALL") =
                                                        // pas de filtre,
                                                        // toutes les espèces acceptées
                                                        else -> {
                                                                val foodSpecies =
                                                                        aliment.getEspecesList()
                                                                foodSpecies.isEmpty() ||
                                                                        foodSpecies.contains(
                                                                                Espece.CH
                                                                        ) || // Aliment compatible
                                                                        // avec toutes les
                                                                        // espèces
                                                                        foodSpecies.contains(sel)
                                                        }
                                                }

                                        // Filtre par indications (ALL = pas de filtre)
                                        val matchesIndications =
                                                if (filters.selectedIndications.isEmpty() ||
                                                                filters.selectedIndications
                                                                        .contains(
                                                                                AlimIndic.ALL
                                                                        ) // "ALL" = pas de filtre,
                                                // toutes les
                                                // indications acceptées
                                                ) true
                                                else
                                                        filters.selectedIndications.any { indication
                                                                ->
                                                                aliment.indicat.contains(indication)
                                                        }

                                        // Filtre par base de données (null/"" = pas de filtre)
                                        val matchesDataB =
                                                when (val dataBFilter = filters.dataB) {
                                                        null -> true // null = pas de filtre
                                                        "" -> true // chaîne vide = pas de filtre
                                                        else -> {
                                                                aliment.dataB?.trim() ==
                                                                        dataBFilter.trim() // Comparaison exacte
                                                        }
                                                }

                                        // Filtre par nutriments
                                        val matchesNutrients =
                                                if (filters.nutrientFilters.isEmpty()) true
                                                else {
                                                        filters.nutrientFilters.all { nutrientFilter ->
                                                                if (nutrientFilter.nutrient == null || nutrientFilter.value == null) true
                                                                else {
                                                                        val nutrientValue = aliment.getNutrient(nutrientFilter.nutrient, config.referenceEv)
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

                                        matchesSearch &&
                                                matchesType &&
                                                matchesGroup &&
                                                matchesEspece &&
                                                matchesIndications &&
                                                matchesDataB &&
                                                matchesNutrients
                                }

                        // Trier les résultats
                        result = if (filters.sortCriteria != null) {
                                val sortedList = when (filters.sortCriteria) {
                                        SortCriteria.NAME -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.nom ?: "" }
                                                } else {
                                                        result.sortedByDescending { it.nom ?: "" }
                                                }
                                        }
                                        SortCriteria.PROTEIN -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMain.PROTEINE, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMain.PROTEINE, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.FAT -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMain.LIPIDE, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMain.LIPIDE, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.CARBOHYDRATE -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMain.GLUCIDE, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMain.GLUCIDE, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.FIBER -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMain.FIBRE, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMain.FIBRE, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.ENERGY -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMain.ENERGIE, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMain.ENERGIE, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.CALCIUM -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMacro.CAL, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMacro.CAL, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                        SortCriteria.PHOSPHORUS -> {
                                                if (filters.sortOrder == SortOrder.ASCENDING) {
                                                        result.sortedBy { it.getNutrient(NutrientMacro.PHOS, config.referenceEv) ?: 0.0 }
                                                } else {
                                                        result.sortedByDescending { it.getNutrient(NutrientMacro.PHOS, config.referenceEv) ?: 0.0 }
                                                }
                                        }
                                }
                                sortedList
                        } else {
                                result
                        }

                        result
                }

        when (config.layout) {
                FoodSearchLayout.VERTICAL ->
                        VerticalLayout(
                                allFoods = foods,
                                filteredFoods = filteredFoods,
                                filters = filters,
                                onFiltersChange = onFiltersChange,
                                config = config,
                                modifier = modifier
                        )
                FoodSearchLayout.HORIZONTAL ->
                        HorizontalLayout(
                                allFoods = foods,
                                filteredFoods = filteredFoods,
                                filters = filters,
                                onFiltersChange = onFiltersChange,
                                config = config,
                                modifier = modifier
                        )
                FoodSearchLayout.COMPACT ->
                        CompactLayout(
                                allFoods = foods,
                                filteredFoods = filteredFoods,
                                filters = filters,
                                onFiltersChange = onFiltersChange,
                                config = config,
                                modifier = modifier
                        )
        }
}


/** Layout vertical simple (comme FoodListView) */
@Composable
private fun VerticalLayout(
        allFoods: List<AlimentEv>,
        filteredFoods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        config: FoodSearchConfig,
        modifier: Modifier
) {
        Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
                // Barre de recherche
                if (config.showSearchBar) {
                        SearchBar(
                                searchQuery = filters.searchQuery,
                                onSearchQueryChange = {
                                        onFiltersChange(filters.copy(searchQuery = it))
                                },
                                modifier = Modifier.fillMaxWidth()
                        )
                }

                // Filtres
                if (config.showFilters) {
                        FiltersSection(
                                foods = allFoods,
                                filters = filters,
                                onFiltersChange = onFiltersChange,
                                modifier = Modifier.fillMaxWidth()
                        )
                }

                // Compteur de résultats
                if (config.showResultsCount) {
                        ResultsCount(
                                totalCount = allFoods.size,
                                filteredCount = filteredFoods.size,
                                modifier = Modifier.fillMaxWidth()
                        )
                }

                // Liste des résultats
                FoodSearchResults(
                        foods = filteredFoods,
                        config = config,
                        modifier = Modifier.fillMaxWidth()
                )
        }
}


/** Layout horizontal à deux colonnes (comme AddAlimentView) */
@Composable
private fun HorizontalLayout(
        allFoods: List<AlimentEv>,
        filteredFoods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        config: FoodSearchConfig,
        modifier: Modifier
) {
        Row(
                modifier = modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                // Colonne gauche - Filtres et liste (60% de l'espace)
                Column(
                        modifier = Modifier.weight(0.6f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // Section des filtres
                        if (config.showFilters) {
                                FiltersCard(
                                        foods = allFoods,
                                        filters = filters,
                                        onFiltersChange = onFiltersChange,
                                        modifier = Modifier.fillMaxWidth()
                                )
                        }

                        // Liste des aliments
                        FoodSearchResults(
                                foods = filteredFoods,
                                config = config,
                                modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                }

                // Colonne droite - Détails ou espace réservé (40% de l'espace)
                if (config.onFoodSelected != null) {
                        FoodDetailsPanel(
                                foods = filteredFoods,
                                config = config,
                                modifier = Modifier.weight(0.4f).fillMaxHeight()
                        )
                }
        }
}


/** Layout compact pour les petits espaces */
@Composable
private fun CompactLayout(
        allFoods: List<AlimentEv>,
        filteredFoods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        config: FoodSearchConfig,
        modifier: Modifier
) {
        Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
                // Barre de recherche et filtres compacts
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        if (config.showSearchBar) {
                                SearchBar(
                                        searchQuery = filters.searchQuery,
                                        onSearchQueryChange = {
                                                onFiltersChange(filters.copy(searchQuery = it))
                                        },
                                        modifier = Modifier.weight(1f)
                                )
                        }

                        if (config.showFilters) {
                                CompactFilters(
                                        foods = allFoods,
                                        filters = filters,
                                        onFiltersChange = onFiltersChange,
                                        modifier = Modifier.weight(1f)
                                )
                        }
                }

                // Liste des résultats
                FoodSearchResults(
                        foods = filteredFoods,
                        config = config,
                        modifier = Modifier.fillMaxWidth()
                )
        }
}


/** Barre de recherche */
@Composable
private fun SearchBar(
        searchQuery: String,
        onSearchQueryChange: (String) -> Unit,
        modifier: Modifier = Modifier
) {
        BasicAppTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Nom, marque, ingrédients...",
                leadingIcon = Icons.Default.Search,
                trailingIcon = if (searchQuery.isNotEmpty()) Icons.Default.Clear else null,
                onTrailingIconClick = { onSearchQueryChange("") },
                modifier = modifier.height(40.dp)
        )
}

/** Section des filtres */
@Composable
private fun FiltersSection(
        foods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        modifier: Modifier = Modifier
) {
        Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
                // Première ligne : Type d'aliment et Indications
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
                                        onValueChange = {
                                                onFiltersChange(filters.copy(selectedFoodType = it))
                                        },
                                        valueToString = { it.translateEnum() },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 12.sp,
                                        labelFontSize = 10.sp,
                                        borderWidth = 0.5.dp
                                )
                        }

                        // Indications
                        Box(modifier = Modifier.weight(1f)) {
                                MultiSelectDropdownField(
                                        label = "Indications",
                                        selectedValues = filters.selectedIndications,
                                        options = AlimIndic.entries,
                                        onValuesChange = {
                                                onFiltersChange(
                                                        filters.copy(selectedIndications = it)
                                                )
                                        },
                                        valueToString = { it.translateEnum() },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 12.sp,
                                        labelFontSize = 10.sp,
                                        borderWidth = 0.5.dp
                                )
                        }
                }

                // Deuxième ligne : Espèce et Base de données
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
                                        onValueChange = {
                                                onFiltersChange(filters.copy(selectedEspece = it))
                                        },
                                        valueToString = { it.translateEnum() },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 12.sp,
                                        labelFontSize = 10.sp,
                                        borderWidth = 0.5.dp
                                )
                        }

                        // Base de données
                        Box(modifier = Modifier.weight(1f)) {
                                val dataBOptions =
                                        remember(foods) {
                                                val options =
                                                        listOf("") +
                                                                foods
                                                                        .mapNotNull {
                                                                                it.dataB?.trim()
                                                                        }
                                                                        .filter { it.isNotEmpty() }
                                                                        .distinct()
                                                                        .sorted()
                                                options
                                        }
                                val selectedDataB = filters.dataB ?: ""

                                DropdownField(
                                        label = "Base de données",
                                        selectedValue = selectedDataB,
                                        options = dataBOptions,
                                        onValueChange = {
                                                val newDataB = if (it.isEmpty()) null else it
                                                // Créer un nouvel objet complètement différent pour
                                                // forcer le re-rendu
                                val newFilters =
                                        FoodSearchFilters(
                                                searchQuery = filters.searchQuery,
                                                selectedFoodType =
                                                        filters.selectedFoodType,
                                                selectedFoodGroup =
                                                        filters.selectedFoodGroup,
                                                selectedEspece =
                                                        filters.selectedEspece,
                                                selectedIndications =
                                                        filters.selectedIndications,
                                                dataB = newDataB,
                                                aminoOnly = filters.aminoOnly
                                        )
                                                onFiltersChange(newFilters)
                                        },
                                        valueToString = {
                                                if (it.isEmpty()) "Toutes"
                                                else {
                                                        // Utilisation directe de l'enum
                                                        // DataB - plus propre et type-safe
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
                }
        }
}


/** Carte des filtres (pour le layout horizontal) */
@Composable
private fun FiltersCard(
        foods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        modifier: Modifier = Modifier
) {
        var showAdvancedSortDialog by remember { mutableStateOf(false) }
        Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
                Column(
                        modifier = Modifier.padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        Text(
                                text = "Filtres de recherche",
                                style = MaterialTheme.typography.subtitle2,
                                color = VetNutriColors.Primary
                        )

                        // Barre de recherche + raccourci Ac. Aminé avec disposition adaptative
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val isCompact = maxWidth < 600.dp
                                if (isCompact) {
                                        // Disposition verticale pour les petits écrans
                                        Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                SearchBar(
                                                        searchQuery = filters.searchQuery,
                                                        onSearchQueryChange = {
                                                                onFiltersChange(filters.copy(searchQuery = it))
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                                                ) {
                                                        OutlinedButton(
                                                                onClick = { onFiltersChange(filters.copy(aminoOnly = !filters.aminoOnly)) },
                                                                border = ButtonDefaults.outlinedBorder,
                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                        backgroundColor = if (filters.aminoOnly) VetNutriColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colors.surface,
                                                                        contentColor = if (filters.aminoOnly) VetNutriColors.Primary else MaterialTheme.colors.onSurface
                                                                ),
                                                                modifier = Modifier.weight(1f)
                                                        ) {
                                                                Text(text = "Ac. Aminé", style = MaterialTheme.typography.caption)
                                                        }
                                                        OutlinedButton(
                                                                onClick = { showAdvancedSortDialog = true },
                                                                border = ButtonDefaults.outlinedBorder,
                                                                colors = ButtonDefaults.outlinedButtonColors(
                                                                        backgroundColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) VetNutriColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colors.surface,
                                                                        contentColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) VetNutriColors.Primary else MaterialTheme.colors.onSurface
                                                                ),
                                                                modifier = Modifier.weight(1f)
                                                        ) {
                                                                Icon(Icons.AutoMirrored.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Tri avancé", style = MaterialTheme.typography.caption)
                                                        }
                                                }
                                        }
                                } else {
                                        // Disposition horizontale pour les grands écrans
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                SearchBar(
                                                        searchQuery = filters.searchQuery,
                                                        onSearchQueryChange = {
                                                                onFiltersChange(filters.copy(searchQuery = it))
                                                        },
                                                        modifier = Modifier.weight(1f)
                                                )
                                                OutlinedButton(
                                                        onClick = { onFiltersChange(filters.copy(aminoOnly = !filters.aminoOnly)) },
                                                        border = ButtonDefaults.outlinedBorder,
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                backgroundColor = if (filters.aminoOnly) VetNutriColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colors.surface,
                                                                contentColor = if (filters.aminoOnly) VetNutriColors.Primary else MaterialTheme.colors.onSurface
                                                        )
                                                ) {
                                                        Text(text = "Ac. Aminé", style = MaterialTheme.typography.caption)
                                                }
                                                OutlinedButton(
                                                        onClick = { showAdvancedSortDialog = true },
                                                        border = ButtonDefaults.outlinedBorder,
                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                backgroundColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) VetNutriColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colors.surface,
                                                                contentColor = if (filters.nutrientFilters.isNotEmpty() || filters.sortCriteria != null) VetNutriColors.Primary else MaterialTheme.colors.onSurface
                                                        )
                                                ) {
                                                        Icon(Icons.AutoMirrored.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Tri avancé", style = MaterialTheme.typography.caption)
                                                }
                                        }
                                }
                        }
                        if (showAdvancedSortDialog) {
                                AdvancedSortDialog(
                                        filters = filters,
                                        onFiltersChange = onFiltersChange,
                                        onDismiss = { showAdvancedSortDialog = false }
                                )
                        }

                        // Filtres
                        FiltersSection(
                                foods = foods,
                                filters = filters,
                                onFiltersChange = onFiltersChange,
                                modifier = Modifier.fillMaxWidth()
                        )

                        // Résumé des filtres actifs
                        if (filters.selectedFoodType != null ||
                                        filters.selectedFoodGroup != null ||
                                        filters.selectedEspece != null ||
                                        filters.selectedIndications.isNotEmpty() ||
                                        !filters.dataB.isNullOrEmpty()
                        ) {
                                Text(
                                        text = "Filtres actifs",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                        }
                }
        }
}


/** Filtres compacts (pour le layout compact) */
@Composable
private fun CompactFilters(
        foods: List<AlimentEv>,
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        modifier: Modifier = Modifier
) {
        // Version simplifiée des filtres pour les petits espaces
        Row(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
                // Type d'aliment
                DropdownField(
                        label = "Type",
                        selectedValue = filters.selectedFoodType,
                        options = FoodKind.entries,
                        onValueChange = { onFiltersChange(filters.copy(selectedFoodType = it)) },
                        valueToString = { it.translateEnum() },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        fontSize = 12.sp,
                        labelFontSize = 10.sp,
                        borderWidth = 0.5.dp
                )

                // Espèce
                DropdownField(
                        label = "Espèce",
                        selectedValue = filters.selectedEspece,
                        options = Espece.entries,
                        onValueChange = { onFiltersChange(filters.copy(selectedEspece = it)) },
                        valueToString = { it.translateEnum() },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        fontSize = 12.sp,
                        labelFontSize = 10.sp,
                        borderWidth = 0.5.dp
                )

                // Base de données
                val dataBOptions =
                        remember(foods) {
                                val options =
                                        listOf("") +
                                                foods
                                                        .mapNotNull { it.dataB?.trim() }
                                                        .filter { it.isNotEmpty() }
                                                        .distinct()
                                                        .sorted()
                                options
                        }
                val selectedDataB = filters.dataB ?: ""

                DropdownField(
                        label = "Base",
                        selectedValue = selectedDataB,
                        options = dataBOptions,
                        onValueChange = {
                                val newDataB = if (it.isEmpty()) null else it
                                // Créer un nouvel objet complètement différent pour forcer le
                                // re-rendu
                                val newFilters =
                                        FoodSearchFilters(
                                                searchQuery = filters.searchQuery,
                                                selectedFoodType = filters.selectedFoodType,
                                                selectedFoodGroup = filters.selectedFoodGroup,
                                                selectedEspece = filters.selectedEspece,
                                                selectedIndications = filters.selectedIndications,
                                                dataB = newDataB,
                                                aminoOnly = filters.aminoOnly
                                        )
                                onFiltersChange(newFilters)
                        },
                        valueToString = {
                                if (it.isEmpty()) "Toutes"
                                else {
                                        val displayName = DataBMapping.getDisplayName(it)
                                        // Fallback si DataBMapping ne fonctionne pas
                                        if (displayName == it) {
                                                when (it) {
                                                        "0" -> "CIQUAL"
                                                        "1" -> "FCEN"
                                                        "2" -> "PetFood Divers"
                                                        "4" -> "Générique"
                                                        "5" -> "Aliment Barf"
                                                        "VF24" -> "VetFood 2024"
                                                        else -> it
                                                }
                                        } else {
                                                displayName
                                        }
                                }
                        },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                        fontSize = 12.sp,
                        labelFontSize = 10.sp,
                        borderWidth = 0.5.dp
                )
        }
}


/** Compteur de résultats */
@Composable
private fun ResultsCount(totalCount: Int, filteredCount: Int, modifier: Modifier = Modifier) {
        Text(
                text = "Aliments disponibles (${filteredCount})",
                style = MaterialTheme.typography.subtitle2,
                color = VetNutriColors.Primary,
                modifier = modifier
        )
}

/** Liste des résultats de recherche */
@Composable
private fun FoodSearchResults(
        foods: List<AlimentEv>,
        config: FoodSearchConfig,
        modifier: Modifier = Modifier
) {
        // État pour conserver la position de scroll
        val listState = rememberLazyListState()
        
        if (config.isLoading) {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                }
        } else if (foods.isEmpty()) {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun aliment trouvé avec ces critères")
                }
        } else {
                LazyColumn(
                        state = listState,
                        modifier = modifier,
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        items(foods) { aliment ->
                                FoodListItem(
                                        aliment = aliment,
                                        config = config,
                                        onClick = { config.onFoodSelected?.invoke(aliment) }
                                )
                        }
                }
        }
}


/** Élément de liste d'aliment */
@Composable
private fun FoodListItem(
        aliment: AlimentEv,
        config: FoodSearchConfig,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
) {
        val isSelected = config.selectedFood?.uuid == aliment.uuid

        Card(
                modifier = modifier.fillMaxWidth().clickable { onClick() },
                elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
                backgroundColor =
                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f)
                        else MaterialTheme.colors.surface
        ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                        // Actions disponibles (icônes à gauche)
                        if (config.availableActions.isNotEmpty() && config.onFoodAction != null) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        start = AppSizes.paddingSmall,
                                                        top = AppSizes.paddingSmall
                                                ),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingXSmall)
                                ) {
                                        config.availableActions.forEach { action ->
                                                val icon =
                                                        when (action) {
                                                                "Éditer" -> Icons.Default.Edit
                                                                "Supprimer" -> Icons.Default.Delete
                                                                "Analyser" ->
                                                                        Icons.Default.Analytics
                                                                "Comparer" -> Icons.Default.Compare
                                                                "Ajouter" -> Icons.Default.Add
                                                                else -> Icons.Default.MoreVert
                                                        }

                                                val iconColor =
                                                        when (action) {
                                                                "Éditer" -> VetNutriColors.Primary
                                                                "Supprimer" ->
                                                                        MaterialTheme.colors.error
                                                                "Analyser" ->
                                                                        VetNutriColors.Secondary
                                                                "Comparer" -> VetNutriColors.Primary
                                                                "Ajouter" -> VetNutriColors.Primary
                                                                else ->
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.6f
                                                                        )
                                                        }

                                                IconButton(
                                                        onClick = {
                                                                config.onFoodAction?.invoke(
                                                                        aliment,
                                                                        action
                                                                )
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                ) {
                                                        Icon(
                                                                imageVector = icon,
                                                                contentDescription = action,
                                                                modifier = Modifier.size(16.dp),
                                                                tint = iconColor
                                                        )
                                                }
                                        }
                                }
                        }

                        // Contenu principal de l'aliment
                        Column(modifier = Modifier.weight(1f).padding(AppSizes.paddingMedium)) {
                                Text(
                                        text = aliment.nom ?: "Sans nom",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight =
                                                if (isSelected) FontWeight.Bold
                                                else FontWeight.Normal
                                )

                                if (!aliment.brand.isNullOrEmpty()) {
                                        Text(
                                                text = aliment.brand!!,
                                                style = MaterialTheme.typography.body2,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.7f
                                                        )
                                        )
                                }

                                if (!aliment.gamme.isNullOrEmpty()) {
                                        Text(
                                                text = "Gamme: ${aliment.gamme}",
                                                style = MaterialTheme.typography.caption,
                                                color =
                                                        MaterialTheme.colors.onSurface.copy(
                                                                alpha = 0.6f
                                                        )
                                        )
                                }

                                // Afficher quelques infos clés (type et groupe) en ignorant les
                                // valeurs ALL
                                Row(
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        val typeText =
                                                aliment.typeAliment
                                                        ?.takeIf { it != FoodKind.ALL }
                                                        ?.translateEnum()
                                        val groupText =
                                                aliment.group
                                                        ?.takeIf { it != GroupAlim.ALL }
                                                        ?.translateEnum()
                                        typeText?.let {
                                                Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.caption,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                        groupText?.let {
                                                Text(
                                                        text = it,
                                                        style = MaterialTheme.typography.caption,
                                                        color = VetNutriColors.Primary
                                                )
                                        }
                                }

                                // Espèces ciblées (hors ALL)
                                run {
                                        val especeText =
                                                aliment.getEspecesList()
                                                        .filter { it != Espece.CH }
                                                        .map { it.translateEnum() }
                                                        .take(3)
                                                        .joinToString(", ")
                                        if (especeText.isNotEmpty()) {
                                                Text(
                                                        text = especeText,
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }

                                // Indications principales (hors ALL/AUTRE)
                                run {
                                        val indicText =
                                                aliment.getIndications()
                                                        .filter {
                                                                it != AlimIndic.ALL &&
                                                                        it != AlimIndic.AUTRE
                                                        }
                                                        .map { it.translateEnum() }
                                                        .take(3)
                                                        .joinToString(", ")
                                        if (indicText.isNotEmpty()) {
                                                Text(
                                                        text = indicText,
                                                        style = MaterialTheme.typography.caption,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}


/** Panneau de détails (pour le layout horizontal) */
@Composable
private fun FoodDetailsPanel(
        foods: List<AlimentEv>,
        config: FoodSearchConfig,
        modifier: Modifier = Modifier
) {
        Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
                if (config.selectedFood == null) {
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
                        AlimentDetailsContent(
                                aliment = config.selectedFood!!,
                                referenceEv = config.referenceEv,
                                equationRepository = config.equationRepository,
                                modifier = Modifier.fillMaxSize()
                        )
                }
        }
}


/** Contenu des détails de l'aliment */
@Composable
private fun AlimentDetailsContent(aliment: AlimentEv, referenceEv: ReferenceEv? = null, equationRepository: EquationRepository? = null, modifier: Modifier = Modifier) {
        LazyColumn(
                modifier = modifier.padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
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

                // Espèces ciblées (hors ALL)
                val species =
                        aliment.getEspecesList().filter { it != Espece.CH }.map {
                                it.translateEnum()
                        }
                if (species.isNotEmpty()) {
                        item { DetailRow("Espèces", species.joinToString(", ")) }
                }

                // Indications principales (hors ALL/AUTRE)
                val indications =
                        aliment.getIndications()
                                .filter { it != AlimIndic.ALL && it != AlimIndic.AUTRE }
                                .map { it.translateEnum() }
                if (indications.isNotEmpty()) {
                        item { DetailRow("Indications", indications.joinToString(", ")) }
                }

                if (!aliment.ingredients.isNullOrEmpty()) {
                        item { DetailRow("Ingrédients", aliment.ingredients!!) }
                }

                item { Divider() }

                // Informations nutritionnelles principales (si disponibles)
                if (aliment.valMap.isNotEmpty()) {
                        item {
                                Text(
                                        text = "Composition nutritionnelle (pour 100g)",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Bold
                                )
                        }

                }

                // Graphique en secteurs des nutriments
                item {
                        NutrientPieChart(
                            aliment = aliment,
                            referenceEv = referenceEv,
                            equationRepository = equationRepository,
                            modifier = Modifier.fillMaxWidth()
                        )
                }
        }
}

/** Composant pour afficher une ligne de détail */
@Composable
private fun DetailRow(label: String, value: String) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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

/** Dialog de tri avancé */
@Composable
fun AdvancedSortDialog(
        filters: FoodSearchFilters,
        onFiltersChange: (FoodSearchFilters) -> Unit,
        onDismiss: () -> Unit,
        availableNutrients: List<Nutrient> = getAllAvailableNutrients()
) {
        var localNutrientFilters by remember(filters.nutrientFilters) { 
                mutableStateOf(filters.nutrientFilters.toMutableList()) 
        }
        var localSortCriteria by remember(filters.sortCriteria) { 
                mutableStateOf(filters.sortCriteria) 
        }
        var localSortOrder by remember(filters.sortOrder) { 
                mutableStateOf(filters.sortOrder) 
        }
        
        // Synchroniser avec les changements externes
        LaunchedEffect(filters.nutrientFilters, filters.sortCriteria, filters.sortOrder) {
                localNutrientFilters = filters.nutrientFilters.toMutableList()
                localSortCriteria = filters.sortCriteria
                localSortOrder = filters.sortOrder
        }
        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Tri et filtres avancés", fontWeight = FontWeight.Bold) },
                text = {
                        Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Section filtres par nutriments
                                Text(
                                        text = "Filtres par nutriments",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )
                                LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        itemsIndexed(
                                                items = localNutrientFilters,
                                                key = { index, _ -> index }
                                        ) { index, filter ->
                                                NutrientFilterRow(
                                                        filter = filter,
                                                        availableNutrients = availableNutrients,
                                                        onFilterChange = { newFilter ->
                                                                val updatedList = localNutrientFilters.toMutableList()
                                                                if (index >= 0 && index < updatedList.size) {
                                                                        updatedList[index] = newFilter
                                                                        localNutrientFilters = updatedList
                                                                }
                                                        },
                                                        onRemove = {
                                                                val updatedList = localNutrientFilters.toMutableList()
                                                                if (index >= 0 && index < updatedList.size) {
                                                                        updatedList.removeAt(index)
                                                                        localNutrientFilters = updatedList
                                                                }
                                                        }
                                                )
                                        }
                                        item {
                                                Button(
                                                        onClick = {
                                                                localNutrientFilters = (localNutrientFilters + NutrientFilter()).toMutableList()
                                                        },
                                                        modifier = Modifier.fillMaxWidth()
                                                ) {
                                                        Icon(Icons.Default.Add, contentDescription = null)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Ajouter un filtre")
                                                }
                                        }
                                }
                                Divider()
                                // Section tri
                                Text(
                                        text = "Tri",
                                        style = MaterialTheme.typography.subtitle2,
                                        fontWeight = FontWeight.Bold
                                )
                                DropdownField(
                                        label = "Critère de tri",
                                        selectedValue = localSortCriteria,
                                        options = listOf(null) + SortCriteria.entries,
                                        onValueChange = { localSortCriteria = it },
                                        valueToString = { it?.displayName ?: "Aucun" },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 12.sp,
                                        labelFontSize = 10.sp,
                                        borderWidth = 0.5.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                DropdownField(
                                        label = "Ordre",
                                        selectedValue = localSortOrder,
                                        options = SortOrder.entries,
                                        onValueChange = { localSortOrder = it },
                                        valueToString = { it.displayName },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 12.sp,
                                        labelFontSize = 10.sp,
                                        borderWidth = 0.5.dp
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        onFiltersChange(
                                                filters.copy(
                                                        nutrientFilters = localNutrientFilters,
                                                        sortCriteria = localSortCriteria,
                                                        sortOrder = localSortOrder
                                                )
                                        )
                                        onDismiss()
                                }
                        ) {
                                Text("Appliquer")
                        }
                },
                dismissButton = {
                        TextButton(onClick = onDismiss) {
                                Text("Annuler")
                        }
                }
        )
}

/**
 * Obtient le nom traduit d'un nutriment selon son type en utilisant les traductions JSON
 */
private fun obtenirNomTraduitNutriment(nutriment: Nutrient?): String {
        if (nutriment == null) return "Sélectionner"
        return when (nutriment) {
                is NutrientLipid -> nutriment.translateEnum()
                is NutrientMacro -> nutriment.translateEnum()
                is NutrientMain -> nutriment.translateEnum()
                is NutrientMin -> nutriment.translateEnum()
                is NutrientOther -> nutriment.translateEnum()
                is NutrientVitam -> nutriment.translateEnum()
                is AAEnum -> nutriment.translateEnum()
                is NutrientAnalysis -> nutriment.translateEnum()
                else -> nutriment.label // Fallback sur le label original si le type n'est pas reconnu
        }
}

/** Ligne de filtre par nutriment */
@Composable
private fun NutrientFilterRow(
        filter: NutrientFilter,
        availableNutrients: List<Nutrient>,
        onFilterChange: (NutrientFilter) -> Unit,
        onRemove: () -> Unit
) {
        var valueText by remember { 
                mutableStateOf(filter.value?.toString() ?: "") 
        }
        
        // Synchroniser avec les changements externes du filtre
        LaunchedEffect(filter.value) {
                valueText = filter.value?.toString() ?: ""
        }
        
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
        ) {
                // Sélection du nutriment
                Box(modifier = Modifier.weight(2f)) {
                        val nutrientOptions = listOf(null) + availableNutrients
                        DropdownField(
                                label = "Nutriment",
                                selectedValue = filter.nutrient,
                                options = nutrientOptions,
                                onValueChange = { 
                                        onFilterChange(filter.copy(nutrient = it)) 
                                },
                                valueToString = { obtenirNomTraduitNutriment(it) },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                        )
                }
                // Opérateur
                Box(modifier = Modifier.weight(1f)) {
                        DropdownField(
                                label = "Opérateur",
                                selectedValue = filter.operator,
                                options = NutrientOperator.entries,
                                onValueChange = { 
                                        onFilterChange(filter.copy(operator = it)) 
                                },
                                valueToString = { it.displayName },
                                modifier = Modifier.fillMaxWidth(),
                                height = 40.dp,
                                fontSize = 12.sp,
                                labelFontSize = 10.sp,
                                borderWidth = 0.5.dp
                        )
                }
                // Valeur
                Box(modifier = Modifier.weight(1f)) {
                        BasicAppTextField(
                                value = valueText,
                                onValueChange = { newText ->
                                        valueText = newText
                                        val doubleValue = newText.toDoubleOrNull()
                                        onFilterChange(filter.copy(value = doubleValue))
                                },
                                placeholder = "Valeur",
                                modifier = Modifier.fillMaxWidth().height(40.dp)
                        )
                }
                // Bouton supprimer
                IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                }
        }
}

/** Obtient tous les nutriments disponibles */
private fun getAllAvailableNutrients(): List<Nutrient> {
        val allNutrients = mutableListOf<Nutrient>()
        allNutrients.addAll(NutrientMain.entries)
        allNutrients.addAll(NutrientMacro.entries)
        allNutrients.addAll(NutrientMin.entries)
        allNutrients.addAll(NutrientVitam.entries)
        allNutrients.addAll(NutrientLipid.entries)
        return allNutrients
}


