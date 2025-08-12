package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectDropdownField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

/**
 * Écran d'analyse graphique des aliments. Permet de rechercher un aliment et d'afficher ses détails
 * de manière structurée.
 */
@Composable
fun AnalyseGraphiqueAlimentsView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
    val isLoadingFoodsState: Boolean by viewModel.isLoadingFoods.collectAsState()
    val availableFoods: List<AlimentEv> by viewModel.availableFoods.collectAsState()
    var searchQuery: String by remember { mutableStateOf(viewModel.alimentSearchQuery) }
    var selectedFoodType: FoodKind? by remember { mutableStateOf<FoodKind?>(null) }
    var selectedFoodGroup: GroupAlim? by remember { mutableStateOf<GroupAlim?>(null) }
    var selectedEspece: Espece? by remember { mutableStateOf<Espece?>(null) }
    var selectedIndications: Set<AlimIndic> by remember {
        mutableStateOf<Set<AlimIndic>>(emptySet())
    }
    var alimentSelectionne: AlimentEv? by remember { mutableStateOf(null) }
    var alimentChargeComplet: AlimentEv? by remember { mutableStateOf(null) }
    var selectedRightFoods: List<AlimentEv> by remember { mutableStateOf(emptyList()) }
    var selectedRightFood: AlimentEv? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }

    val alimentsFiltres: List<AlimentEv> =
            remember(
                    availableFoods,
                    searchQuery,
                    selectedFoodType,
                    selectedFoodGroup,
                    selectedEspece,
                    selectedIndications
            ) {
                availableFoods.filter { aliment: AlimentEv ->
                    val matchesSearch: Boolean =
                            if (searchQuery.isEmpty()) true
                            else {
                                (aliment.nom?.contains(searchQuery, ignoreCase = true) == true) ||
                                        (aliment.brand?.contains(searchQuery, ignoreCase = true) ==
                                                true) ||
                                        (aliment.ingredients?.contains(
                                                searchQuery,
                                                ignoreCase = true
                                        ) == true)
                            }

                    val matchesType: Boolean =
                            when (val sel: FoodKind? = selectedFoodType) {
                                null -> true
                                FoodKind.ALL -> true
                                else -> aliment.typeAliment == sel
                            }

                    val matchesGroup: Boolean =
                            when (val sel: GroupAlim? = selectedFoodGroup) {
                                null -> true
                                GroupAlim.ALL -> true
                                else -> aliment.group == sel
                            }

                    val matchesEspece: Boolean =
                            when (val sel: Espece? = selectedEspece) {
                                null -> true
                                Espece.CH -> true
                                else -> {
                                    val foodSpecies: List<Espece> = aliment.getEspecesList()
                                    foodSpecies.isEmpty() ||
                                            foodSpecies.contains(Espece.CH) ||
                                            foodSpecies.contains(sel)
                                }
                            }

                    val matchesIndications: Boolean =
                            if (selectedIndications.isEmpty() ||
                                            selectedIndications.contains(AlimIndic.ALL)
                            )
                                    true
                            else
                                    selectedIndications.any { indication: AlimIndic ->
                                        aliment.indicat.contains(indication)
                                    }

                    matchesSearch &&
                            matchesType &&
                            matchesGroup &&
                            matchesEspece &&
                            matchesIndications
                }
            }
    Column(modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium)) {
        Text(text = "Analyse graphique des aliments", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        // Panneau de filtres (réutilisation des mêmes éléments que AddAlimentView)
        Card(modifier = Modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
            Column(
                    modifier = Modifier.padding(AppSizes.paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    // Champ de recherche simple
                    OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { nv: String ->
                                searchQuery = nv
                                viewModel.setAlimentSearchQuery(nv)
                            },
                            modifier = Modifier.weight(1f),
                            label = { Text("Rechercher un aliment") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Rechercher"
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Effacer"
                                        )
                                    }
                                }
                            }
                    )
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    DropdownField(
                            label = "Type",
                            selectedValue = selectedFoodType,
                            options = FoodKind.entries,
                            onValueChange = { value: FoodKind? -> selectedFoodType = value },
                            valueToString = { it.translateEnum() },
                            modifier = Modifier.weight(1f),
                            height = 40.dp,
                            fontSize = 12.sp,
                            labelFontSize = 10.sp,
                            borderWidth = 0.5.dp
                    )

                    DropdownField(
                            label = "Groupe",
                            selectedValue = selectedFoodGroup,
                            options = GroupAlim.entries,
                            onValueChange = { value: GroupAlim? -> selectedFoodGroup = value },
                            valueToString = { it.translateEnum() },
                            modifier = Modifier.weight(1f),
                            height = 40.dp,
                            fontSize = 12.sp,
                            labelFontSize = 10.sp,
                            borderWidth = 0.5.dp
                    )
                }

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    DropdownField(
                            label = "Espèce",
                            selectedValue = selectedEspece,
                            options = Espece.entries,
                            onValueChange = { value: Espece? -> selectedEspece = value },
                            valueToString = { it.translateEnum() },
                            modifier = Modifier.weight(1f),
                            height = 40.dp,
                            fontSize = 12.sp,
                            labelFontSize = 10.sp,
                            borderWidth = 0.5.dp
                    )

                    MultiSelectDropdownField(
                            label = "Indications",
                            selectedValues = selectedIndications,
                            options = AlimIndic.entries,
                            onValuesChange = { values: Set<AlimIndic> ->
                                selectedIndications = values
                            },
                            valueToString = { it.translateEnum() },
                            modifier = Modifier.weight(1f),
                            height = 40.dp,
                            fontSize = 12.sp,
                            labelFontSize = 10.sp,
                            borderWidth = 0.5.dp
                    )
                }

                if (selectedFoodType != null ||
                                selectedFoodGroup != null ||
                                selectedEspece != null ||
                                selectedIndications.isNotEmpty()
                ) {
                    Text(
                            text = "Filtres actifs: ${alimentsFiltres.size} aliment(s) trouvé(s)",
                            style = MaterialTheme.typography.caption
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                    text = "Liste source: ${alimentsFiltres.size} aliment(s)",
                    style = MaterialTheme.typography.caption
            )
            Text(
                    text = "Liste cible: ${selectedRightFoods.size} aliment(s)",
                    style = MaterialTheme.typography.caption
            )
        }
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        var afficherVueAnalyse by remember { mutableStateOf(false) }
        if (afficherVueAnalyse) {
            AnalyseSelectionAlimentsView(
                    aliments = selectedRightFoods,
                    onClose = { afficherVueAnalyse = false }
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                    onClick = { afficherVueAnalyse = true },
                    enabled = selectedRightFoods.isNotEmpty()
            ) { Text("Analyser la sélection") }
        }
        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
        Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (isLoadingFoodsState) {
                    CircularProgressIndicator()
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(alimentsFiltres) { aliment: AlimentEv ->
                                AlimentListItem(
                                        aliment = aliment,
                                        isSelected = alimentSelectionne?.uuid == aliment.uuid,
                                        onClick = {
                                            alimentSelectionne = aliment
                                            scope.launch {
                                                val alimentComplet: AlimentEv? =
                                                        viewModel.getAlimentComplet(aliment.uuid)
                                                alimentChargeComplet = alimentComplet ?: aliment
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }
            // Colonne centrale avec actions de transfert entre listes
            Column(
                    modifier = Modifier.fillMaxHeight().padding(horizontal = AppSizes.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ajouter tous les aliments filtrés vers la liste de droite
                IconButton(
                        onClick = {
                            val union: MutableMap<String, AlimentEv> =
                                    selectedRightFoods.associateBy { it.uuid }.toMutableMap()
                            alimentsFiltres.forEach { a: AlimentEv ->
                                if (!union.containsKey(a.uuid)) union[a.uuid] = a
                            }
                            selectedRightFoods = union.values.toList()
                        }
                ) { Icon(imageVector = AppIcons.Export, contentDescription = "Ajouter tous →") }

                // Ajouter l'aliment sélectionné vers la liste de droite
                IconButton(
                        onClick = {
                            val a: AlimentEv = alimentSelectionne ?: return@IconButton
                            if (selectedRightFoods.none { it.uuid == a.uuid }) {
                                selectedRightFoods = selectedRightFoods + a
                            }
                        }
                ) { Icon(imageVector = AppIcons.Add, contentDescription = "Ajouter sélection →") }

                // Retirer l'aliment sélectionné de la liste de droite
                IconButton(
                        onClick = {
                            val a: AlimentEv = selectedRightFood ?: return@IconButton
                            selectedRightFoods = selectedRightFoods.filterNot { it.uuid == a.uuid }
                            if (selectedRightFood?.uuid == a.uuid) selectedRightFood = null
                        }
                ) {
                    Icon(imageVector = AppIcons.Delete, contentDescription = "Retirer sélection ←")
                }

                // Vider la liste de droite
                IconButton(
                        onClick = {
                            selectedRightFoods = emptyList()
                            selectedRightFood = null
                        }
                ) { Icon(imageVector = AppIcons.Close, contentDescription = "Retirer tous ←") }
            }

            // Liste de droite: aliments sélectionnés (target)
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (selectedRightFoods.isEmpty()) {
                    Text(text = "Aucun aliment sélectionné")
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(selectedRightFoods) { aliment: AlimentEv ->
                                AlimentListItem(
                                        aliment = aliment,
                                        isSelected = selectedRightFood?.uuid == aliment.uuid,
                                        onClick = { selectedRightFood = aliment }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
