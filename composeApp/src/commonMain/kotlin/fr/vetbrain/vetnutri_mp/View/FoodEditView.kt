package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectionCard
import fr.vetbrain.vetnutri_mp.Components.NutrientSection
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.DataBMapping
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodEditView(
        viewModel: FoodEditViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToSettings: () -> Unit,
        onFoodSaved: (AlimentEv) -> Unit = {},
        modifier: Modifier = Modifier
) {
        val aliment = viewModel.alimentState.collectAsState().value
        val scope = rememberCoroutineScope()
        val scrollState = rememberScrollState()

        // État pour les messages d'erreur et de succès
        var showErrorMessage by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf("") }
        var showSuccessMessage by remember { mutableStateOf(false) }

        // États pour le formulaire
        val nomState = remember { mutableStateOf("") }
        val brandState = remember { mutableStateOf("") }
        val gammeState = remember { mutableStateOf("") }
        val ingredientsState = remember { mutableStateOf("") }
        val priceState = remember { mutableStateOf("") }
        val categPriceState = remember { mutableStateOf("") }
        val quantIntState = remember { mutableStateOf("") }
        val contState = remember { mutableStateOf("") }
        val consistentState = remember { mutableStateOf(false) }
        val deprecatedState = remember { mutableStateOf(false) }
        val dataBState = remember { mutableStateOf("") }

        val selectedFoodType = remember { mutableStateOf<FoodKind?>(null) }
        val selectedFoodGroup = remember { mutableStateOf<GroupAlim?>(null) }
        val selectedEspecesState = remember { mutableStateOf(mutableListOf<Espece>()) }
        val selectedIndications = remember {
                mutableStateOf<MutableList<AlimIndic>>(mutableListOf())
        }

        val allNutrients = viewModel.getAllNutrients()
        val nutrientValues = remember { mutableStateMapOf<Nutrient, String>() }
        val nutrientErrors = remember { mutableStateMapOf<Nutrient, Boolean>() }
        val customNutrientNameState = remember { mutableStateOf("") }
        val customNutrientUnitState = remember { mutableStateOf("g") }
        val customNutrientSelectedLabelState = remember { mutableStateOf("") }
        val customNutrientErrorState = remember { mutableStateOf<String?>(null) }

        val selectedBiblioRefs by viewModel.selectedBiblioRefs.collectAsState()
        val availableBiblioRefs by viewModel.availableBiblioRefs.collectAsState()

        // État pour les onglets
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabTitles = listOf(
                translate(LocalizationKeys.FoodEdit.TAB_GENERAL),
                translate(LocalizationKeys.FoodEdit.TAB_NUTRITION),
                "Bibliographie"
        )

        // Fonction de validation des nutriments
        fun validateNutrients(): Boolean {
                var isValid = true
                nutrientErrors.clear()

                nutrientValues.forEach { (nutrient, value) ->
                        if (value.isNotBlank()) {
                                val doubleValue = value.replace(",", ".").toDoubleOrNull()
                                if (doubleValue == null || doubleValue < 0) {
                                        nutrientErrors[nutrient] = true
                                        isValid = false
                                } else {
                                        nutrientErrors[nutrient] = false
                                }
                        }
                }

                return isValid
        }

        // Mettre à jour les états locaux lorsque l'aliment change
        LaunchedEffect(aliment) {
                nomState.value = aliment.nom ?: ""
                brandState.value = aliment.brand ?: ""
                gammeState.value = aliment.gamme ?: ""
                ingredientsState.value = aliment.ingredients ?: ""
                priceState.value = aliment.price?.toString() ?: ""
                categPriceState.value = aliment.categPrice ?: ""
                quantIntState.value = aliment.quantInt?.toString() ?: ""
                contState.value = aliment.cont?.toString() ?: ""
                consistentState.value = aliment.consistent
                deprecatedState.value = aliment.deprecated
                dataBState.value = aliment.dataB ?: ""
                selectedFoodType.value = aliment.typeAliment
                selectedFoodGroup.value = aliment.group

                // Nouvelle approche pour initialiser les espèces
                val matchedEspeces = mutableListOf<Espece>()

                // Pour chaque espèce dans l'aliment
                aliment.especes.forEach { especeStr ->
                        // Essayer de trouver l'espèce correspondante de plusieurs façons
                        val espece = Espece.getFromString(especeStr)
                        if (espece != null) {
                                matchedEspeces.add(espece)
                        } else {}
                }

                selectedEspecesState.value = matchedEspeces
                selectedIndications.value = aliment.indicat.toMutableList()

                // Mettre à jour les valeurs des nutriments à partir des valeurs réellement
                // stockées sur l'aliment pour éviter toute perte silencieuse au rechargement.
                nutrientValues.clear()
                nutrientErrors.clear()
                aliment.valMap.forEach { (nutrient, quantity) ->
                        nutrientValues[nutrient] = quantity.value.toString()
                }
        }

        Scaffold(
                topBar = {
                        TopBar(
                                title =
                                        if (aliment.uuid.isBlank()) translate(LocalizationKeys.FoodEdit.TITLE_ADD)
                                        else translate(LocalizationKeys.FoodEdit.TITLE_EDIT),
                                onBackClick = onNavigateBack,
                                onSettingsClick = onNavigateToSettings
                        )
                },
                snackbarHost = {
                        SnackbarHost(
                                hostState =
                                        remember { SnackbarHostState() }.apply {
                                                if (showErrorMessage) {
                                                        scope.launch {
                                                                showSnackbar(
                                                                         message = errorMessage,
                                                                         actionLabel = translate(LocalizationKeys.General.OK),
                                                                         duration =
                                                                                 SnackbarDuration
                                                                                         .Short
                                                                )
                                                                showErrorMessage = false
                                                        }
                                                }
                                                if (showSuccessMessage) {
                                                        scope.launch {
                                                                showSnackbar(
                                                                         message =
                                                                                 translate(LocalizationKeys.FoodEdit.SUCCESS_SAVE),
                                                                         actionLabel = translate(LocalizationKeys.General.OK),
                                                                         duration =
                                                                                 SnackbarDuration
                                                                                         .Short
                                                                )
                                                                showSuccessMessage = false
                                                        }
                                                }
                                        }
                        )
                }
        ) { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                        // Bouton d'enregistrement
                        Button(
                                onClick = {
                                        scope.launch {
                                                // Validation des champs obligatoires
                                                 if (nomState.value.isBlank()) {
                                                         errorMessage =
                                                                 translate(LocalizationKeys.FoodEdit.ERROR_NAME_REQUIRED)
                                                         showErrorMessage = true
                                                         return@launch
                                                 }

                                                 if (selectedEspecesState.value.isEmpty()) {
                                                         errorMessage =
                                                                 translate(LocalizationKeys.FoodEdit.ERROR_SPECIES_REQUIRED)
                                                         showErrorMessage = true
                                                         return@launch
                                                 }

                                                // Validation des nutriments
                                                 if (!validateNutrients()) {
                                                         errorMessage =
                                                                 translate(LocalizationKeys.FoodEdit.ERROR_NUTRIENTS_INVALID)
                                                         showErrorMessage = true
                                                        selectedTabIndex =
                                                                1 // Basculer sur l'onglet des
                                                        // nutriments
                                                        return@launch
                                                }

                                                // Conversion et sauvegarde
                                                try {
                                                        // Créer une map de valeurs nutritionnelles
                                                        // en traitant correctement les valeurs
                                                        // vides
                                                        val processedNutrientValues =
                                                                mutableMapOf<
                                                                        Nutrient,
                                                                        fr.vetbrain.vetnutri_mp.Data.NutrientQuantity>()

                                                        // Log pour débugger
                                                        println(
                                                                "[FoodSaveDebug][UI] Save clicked uuid=${aliment.uuid} " +
                                                                        "rawInputs=${nutrientValues.size} " +
                                                                        "sample=${nutrientValues.entries.take(8).joinToString { "${it.key.label}=${it.value}" }}"
                                                        )

                                                        // Traiter les valeurs présentes dans l'UI
                                                        nutrientValues.forEach { (nutrient, valueStr) ->

                                                                // Appliquer la protection de
                                                                // l'aminogramme :
                                                                // ne pas sauvegarder les acides
                                                                // aminés pour VF24
                                                                val isProtectedAA =
                                                                        nutrient is AAEnum &&
                                                                                dataBState.value ==
                                                                                        "VF24"

                                                                if (!isProtectedAA) {
                                                                        // Si la valeur n'est pas
                                                                        // vide et
                                                                        // est valide, l'ajouter à
                                                                        // la map
                                                                        if (valueStr.isNotBlank()) {
                                                                                val value =
                                                                                        valueStr.replace(
                                                                                                        ",",
                                                                                                        "."
                                                                                                )
                                                                                                .toDoubleOrNull()
                                                                                                ?: 0.0
                                                                                if (value > 0.0) {
                                                                                        processedNutrientValues[
                                                                                                nutrient] =
                                                                                                fr.vetbrain
                                                                                                        .vetnutri_mp
                                                                                                        .Data
                                                                                                        .NutrientQuantity(
                                                                                                                value,
                                                                                                                nutrient.label
                                                                                                        )
                                                                                }
                                                                        }
                                                                        // Si vide ou valeur ≤ 0,
                                                                        // ne pas ajouter pour
                                                                        // supprimer la valeur.
                                                                }
                                                        }

                                                        // Log pour débugger
                                                        println(
                                                                "[FoodSaveDebug][UI] Processed map uuid=${aliment.uuid} " +
                                                                        "processedCount=${processedNutrientValues.size} " +
                                                                        "sample=${processedNutrientValues.entries.take(8).joinToString { "${it.key.label}=${it.value.value}" }}"
                                                        )

                                                        val updatedAliment =
                                                                aliment.copy(
                                                                        nom = nomState.value,
                                                                        brand =
                                                                                brandState.value
                                                                                        .takeIf {
                                                                                                it.isNotBlank()
                                                                                        },
                                                                        gamme =
                                                                                gammeState.value
                                                                                        .takeIf {
                                                                                                it.isNotBlank()
                                                                                        },
                                                                        ingredients =
                                                                                ingredientsState
                                                                                        .value
                                                                                        .takeIf {
                                                                                                it.isNotBlank()
                                                                                        },
                                                                        price =
                                                                                priceState
                                                                                        .value
                                                                                        .replace(
                                                                                                ",",
                                                                                                "."
                                                                                        )
                                                                                        .toDoubleOrNull(),
                                                                        categPrice =
                                                                                categPriceState
                                                                                        .value
                                                                                        .takeIf {
                                                                                                it.isNotBlank()
                                                                                        },
                                                                        quantInt =
                                                                                quantIntState
                                                                                        .value
                                                                                        .replace(
                                                                                                ",",
                                                                                                "."
                                                                                        )
                                                                                        .toDoubleOrNull(),
                                                                        cont =
                                                                                ContEnum.getByName(
                                                                                        contState
                                                                                                .value
                                                                                ),
                                                                        consistent =
                                                                                consistentState
                                                                                        .value,
                                                                        deprecated =
                                                                                deprecatedState
                                                                                        .value,
                                                                        dataB =
                                                                                dataBState.value
                                                                                        .takeIf {
                                                                                                it.isNotBlank()
                                                                                        },
                                                                        typeAliment =
                                                                                selectedFoodType
                                                                                        .value,
                                                                        group =
                                                                                selectedFoodGroup
                                                                                        .value,
                                                                        especes =
                                                                                selectedEspecesState
                                                                                        .value
                                                                                        .map {
                                                                                                it.id
                                                                                        }
                                                                                        .toMutableList(),
                                                                        indicat =
                                                                                selectedIndications
                                                                                        .value,
                                                                        valMap =
                                                                                processedNutrientValues
                                                                                        .toMutableMap(),
                                                                        // Préserver la référence à
                                                                        // la ration pour éviter les
                                                                        // problèmes de clé
                                                                        // étrangère
                                                                        rationUUID =
                                                                                aliment.rationUUID
                                                                )
                                                         try {
                                                                println(
                                                                        "[FoodSaveDebug][UI] Calling saveAliment uuid=${updatedAliment.uuid} " +
                                                                                "valMapCount=${updatedAliment.valMap.size}"
                                                                )
                                                                viewModel.saveAliment(
                                                                        updatedAliment
                                                                )
                                                                showSuccessMessage = true
                                                                onFoodSaved(updatedAliment)
                                                                onNavigateBack()
                                                         } catch (e: Exception) {
                                                                 e.printStackTrace()
                                                                 errorMessage =
                                                                         translate(LocalizationKeys.FoodEdit.ERROR_SAVE_FAILED, e.message ?: "")
                                                                 showErrorMessage = true
                                                         }
                                                 } catch (e: Exception) {
                                                         errorMessage =
                                                                 translate(LocalizationKeys.FoodEdit.ERROR_SAVE_FAILED, e.message ?: "")
                                                         showErrorMessage = true
                                                 }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                                 Icon(Icons.Default.Check, contentDescription = null)
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text(translate(LocalizationKeys.General.SAVE))
                        }

                        // Onglets
                        TabRow(
                                selectedTabIndex = selectedTabIndex,
                                backgroundColor = MaterialTheme.colors.surface,
                                contentColor = VetNutriColors.Primary
                        ) {
                                tabTitles.forEachIndexed { index, title ->
                                        Tab(
                                                selected = selectedTabIndex == index,
                                                onClick = { selectedTabIndex = index },
                                                text = { Text(title) }
                                        )
                                }
                        }

                        // Contenu de l'onglet
                        Box(
                                modifier =
                                        Modifier.weight(1f)
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp)
                        ) {
                                when (selectedTabIndex) {
                                        0 ->
                                                GeneralInfoTab(
                                                        nomState = nomState,
                                                        brandState = brandState,
                                                        gammeState = gammeState,
                                                        ingredientsState = ingredientsState,
                                                        priceState = priceState,
                                                        categPriceState = categPriceState,
                                                        quantIntState = quantIntState,
                                                        contState = contState,
                                                        consistentState = consistentState,
                                                        deprecatedState = deprecatedState,
                                                        dataBState = dataBState,
                                                        selectedFoodType = selectedFoodType,
                                                        selectedFoodGroup = selectedFoodGroup,
                                                        selectedEspecesState = selectedEspecesState,
                                                        selectedIndications = selectedIndications,
                                                        onSelectEspece = {
                                                                selectedEspecesState.value =
                                                                        it.toMutableList()
                                                        },
                                                        onSelectIndication = {
                                                                selectedIndications.value =
                                                                        it.toMutableList()
                                                        }
                                                )
                                        1 ->
                                                NutritionInfoTab(
                                                        allNutrients = allNutrients,
                                                        nutrientValues = nutrientValues,
                                                        nutrientErrors = nutrientErrors,
                                                        customNutrientNameState = customNutrientNameState,
                                                        customNutrientUnitState = customNutrientUnitState,
                                                        customNutrientSelectedLabelState = customNutrientSelectedLabelState,
                                                        customNutrientErrorState = customNutrientErrorState,
                                                        onAddCustomNutrient = { name, unit ->
                                                                val nutrient =
                                                                        viewModel.addOrGetCustomNutrient(name, unit)
                                                                if (nutrient != null && nutrient !in nutrientValues) {
                                                                        nutrientValues[nutrient] = ""
                                                                        customNutrientErrorState.value = null
                                                                } else if (nutrient == null) {
                                                                        customNutrientErrorState.value =
                                                                                "Nom déjà utilisé par un nutriment existant."
                                                                }
                                                        },
                                                        onUpdateCustomNutrient = { original, newName, newUnit ->
                                                                val updated =
                                                                        viewModel.updateCustomNutrient(
                                                                                original,
                                                                                newName,
                                                                                newUnit
                                                                        )
                                                                if (updated != null) {
                                                                        val previousKey =
                                                                                nutrientValues.keys.firstOrNull {
                                                                                        it.label == original.label
                                                                                }
                                                                        val previousValue =
                                                                                previousKey?.let {
                                                                                        nutrientValues.remove(it)
                                                                                }
                                                                        if (previousValue != null) {
                                                                                nutrientValues[updated] =
                                                                                        previousValue
                                                                        }
                                                                        customNutrientSelectedLabelState.value =
                                                                                updated.nameToString()
                                                                        customNutrientErrorState.value = null
                                                                } else {
                                                                        customNutrientErrorState.value =
                                                                                "Modification impossible (doublon ou unité invalide)."
                                                                }
                                                        },
                                                        onDeleteCustomNutrient = { nutrient ->
                                                                viewModel.deleteCustomNutrient(nutrient)
                                                                nutrientValues.remove(nutrient)
                                                                customNutrientSelectedLabelState.value = ""
                                                                customNutrientNameState.value = ""
                                                                customNutrientUnitState.value = "g"
                                                                customNutrientErrorState.value = null
                                                        }
                                                )
                                        2 ->
                                                BiblioRefTab(
                                                        selectedRefs = selectedBiblioRefs,
                                                        availableRefs = availableBiblioRefs,
                                                        onAdd = { viewModel.addBiblioRef(it) },
                                                        onRemove = { viewModel.removeBiblioRef(it) }
                                                )
                                }
                        }
                }
        }
}

@Composable
private fun BiblioRefTab(
        selectedRefs: List<BiblioRef>,
        availableRefs: List<BiblioRef>,
        onAdd: (BiblioRef) -> Unit,
        onRemove: (BiblioRef) -> Unit
) {
        var showPicker by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                Button(
                        onClick = { showPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary)
                ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = VetNutriColors.OnPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Ajouter une référence", color = VetNutriColors.OnPrimary)
                }

                Spacer(Modifier.height(8.dp))

                if (selectedRefs.isEmpty()) {
                        Text(
                                "Aucune référence bibliographique associée",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 16.dp)
                        )
                } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(selectedRefs, key = { it.uuid }) { ref ->
                                        Card(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                elevation = 2.dp
                                        ) {
                                                Row(
                                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                Text(
                                                                        "${ref.firstAuthor} (${ref.year})",
                                                                        style = MaterialTheme.typography.subtitle2
                                                                )
                                                                if (ref.completeRef.isNotBlank()) {
                                                                        Text(
                                                                                ref.completeRef,
                                                                                style = MaterialTheme.typography.caption,
                                                                                color = Color.Gray,
                                                                                maxLines = 2
                                                                        )
                                                                }
                                                        }
                                                        IconButton(onClick = { onRemove(ref) }) {
                                                                Icon(
                                                                        Icons.Default.Close,
                                                                        contentDescription = "Supprimer",
                                                                        tint = MaterialTheme.colors.error
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }

        if (showPicker) {
                val unselected = availableRefs.filter { avail ->
                        selectedRefs.none { it.uuid == avail.uuid }
                }
                val filtered = if (searchQuery.isBlank()) unselected else unselected.filter { ref ->
                        ref.firstAuthor.contains(searchQuery, ignoreCase = true) ||
                                ref.completeRef.contains(searchQuery, ignoreCase = true) ||
                                ref.year.toString().contains(searchQuery)
                }
                AlertDialog(
                        onDismissRequest = { showPicker = false; searchQuery = "" },
                        title = { Text("Choisir une référence") },
                        text = {
                                Column {
                                        OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                placeholder = { Text("Rechercher…") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        if (filtered.isEmpty()) {
                                                Text("Aucune référence disponible", color = Color.Gray)
                                        } else {
                                                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                                                        items(filtered, key = { it.uuid }) { ref ->
                                                                TextButton(
                                                                        onClick = {
                                                                                onAdd(ref)
                                                                                showPicker = false
                                                                                searchQuery = ""
                                                                        },
                                                                        modifier = Modifier.fillMaxWidth()
                                                                ) {
                                                                        Column(modifier = Modifier.fillMaxWidth()) {
                                                                                Text(
                                                                                        "${ref.firstAuthor} (${ref.year})",
                                                                                        style = MaterialTheme.typography.subtitle2
                                                                                )
                                                                                if (ref.completeRef.isNotBlank()) {
                                                                                        Text(
                                                                                                ref.completeRef,
                                                                                                style = MaterialTheme.typography.caption,
                                                                                                color = Color.Gray,
                                                                                                maxLines = 1
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                                Divider()
                                                        }
                                                }
                                        }
                                }
                        },
                        confirmButton = {},
                        dismissButton = {
                                TextButton(onClick = { showPicker = false; searchQuery = "" }) {
                                        Text("Fermer")
                                }
                        }
                )
        }
}

@Composable
private fun GeneralInfoTab(
        nomState: MutableState<String>,
        brandState: MutableState<String>,
        gammeState: MutableState<String>,
        ingredientsState: MutableState<String>,
        priceState: MutableState<String>,
        categPriceState: MutableState<String>,
        quantIntState: MutableState<String>,
        contState: MutableState<String>,
        consistentState: MutableState<Boolean>,
        deprecatedState: MutableState<Boolean>,
        dataBState: MutableState<String>,
        selectedFoodType: MutableState<FoodKind?>,
        selectedFoodGroup: MutableState<GroupAlim?>,
        selectedEspecesState: MutableState<MutableList<Espece>>,
        selectedIndications: MutableState<MutableList<AlimIndic>>,
        onSelectEspece: (List<Espece>) -> Unit,
        onSelectIndication: (List<AlimIndic>) -> Unit
) {
        val scrollState = rememberScrollState()

        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Section Informations générales
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.FoodEdit.TAB_GENERAL),
                                        style = MaterialTheme.typography.h6
                                )

                                OutlinedTextField(
                                        value = nomState.value,
                                        onValueChange = { nomState.value = it },
                                        label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_NAME)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )

                                OutlinedTextField(
                                        value = brandState.value,
                                        onValueChange = { brandState.value = it },
                                        label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_BRAND)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )

                                OutlinedTextField(
                                        value = gammeState.value,
                                        onValueChange = { gammeState.value = it },
                                        label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_GAMME)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )

                                DropdownField(
                                        label = translate(LocalizationKeys.FoodEdit.FIELD_DATABASE),
                                        selectedValue = dataBState.value.ifBlank { "" },
                                        options =
                                                buildList {
                                                        add(
                                                                ""
                                                        ) // Valeur vide pour "Sélectionner..."
                                                        addAll(
                                                                DataBMapping.getAllMappings()
                                                                        .keys
                                                                        .sorted()
                                                        )
                                                        // Ajouter la valeur actuelle si elle n'est
                                                        // pas dans la liste
                                                        if (dataBState.value.isNotBlank() &&
                                                                        !DataBMapping.hasMapping(
                                                                                dataBState.value
                                                                        )
                                                        ) {
                                                                add(dataBState.value)
                                                        }
                                                },
                                        onValueChange = { dataBState.value = it },
                                        valueToString = {
                                                if (it.isBlank()) translate(LocalizationKeys.General.SELECT_PLACEHOLDER)
                                                else DataBMapping.getDisplayName(it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        height = 40.dp,
                                        fontSize = 14.sp,
                                        labelFontSize = 12.sp,
                                        borderWidth = 1.dp
                                )

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        OutlinedTextField(
                                                value = priceState.value,
                                                onValueChange = { priceState.value = it },
                                                label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_PRICE)) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
                                        )

                                        OutlinedTextField(
                                                value = categPriceState.value,
                                                onValueChange = { categPriceState.value = it },
                                                label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_PRICE_CATEGORY)) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
                                        )
                                }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        OutlinedTextField(
                                                value = quantIntState.value,
                                                onValueChange = { quantIntState.value = it },
                                                label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_QUANTITY)) },
                                                modifier = Modifier.weight(1f),
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
                                        )

                                        ContDropdown(
                                                selectedCont = ContEnum.getByName(contState.value),
                                                onContSelected = { contState.value = it.name },
                                                availableConts = ContEnum.entries,
                                                modifier = Modifier.weight(1f)
                                        )
                                }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Checkbox(
                                                checked = consistentState.value,
                                                onCheckedChange = { consistentState.value = it },
                                                colors =
                                                        CheckboxDefaults.colors(
                                                                checkedColor =
                                                                        VetNutriColors.Primary
                                                        )
                                        )

                                        Text(translate(LocalizationKeys.FoodEdit.FIELD_CONSISTENT))
                                }

                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Checkbox(
                                                checked = deprecatedState.value,
                                                onCheckedChange = {
                                                        deprecatedState.value = it
                                                },
                                                colors =
                                                        CheckboxDefaults.colors(
                                                                checkedColor =
                                                                        VetNutriColors.Primary
                                                        )
                                        )

                                        Text(translate(LocalizationKeys.FoodEdit.FIELD_DEPRECATED))
                                }
                        }
                }

                // Section Type et classification
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text(
                                        text = translate(LocalizationKeys.FoodEdit.FIELD_TYPE_CLASS),
                                        style = MaterialTheme.typography.h6
                                )

                                // Dropdown pour le type d'aliment
                                FoodTypeDropdown(
                                        selectedFoodType = selectedFoodType.value,
                                        onFoodTypeSelected = { selectedFoodType.value = it },
                                        availableFoodTypes = FoodKind.valuesExcept(),
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Dropdown pour le groupe d'aliment
                                FoodGroupDropdown(
                                        selectedFoodGroup = selectedFoodGroup.value,
                                        onFoodGroupSelected = { selectedFoodGroup.value = it },
                                        availableFoodGroups = GroupAlim.valuesExcept(),
                                        modifier = Modifier.fillMaxWidth()
                                )
                        }
                }

                // Section Ingrédients
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text(text = translate(LocalizationKeys.FoodEdit.FIELD_INGREDIENTS), style = MaterialTheme.typography.h6)

                                OutlinedTextField(
                                        value = ingredientsState.value,
                                        onValueChange = { ingredientsState.value = it },
                                        label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_INGREDIENTS_LIST)) },
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )
                        }
                }

                // Section Espèces - Utilise le composant réutilisable MultiSelectionCard
                MultiSelectionCard(
                        titre = translate(LocalizationKeys.FoodEdit.FIELD_COMPATIBLE_SPECIES),
                        elementsDisponibles = Espece.entries,
                        elementsSelectionnes = selectedEspecesState.value,
                        onSelectionChange = onSelectEspece,
                        getLabel = { it.translateEnum() },
                        getIdentifiant = { it.name },
                        couleurArrierePlan = VetNutriColors.Secondary
                )

                // Section Indications - Utilise le composant réutilisable MultiSelectionCard
                MultiSelectionCard(
                        titre = translate(LocalizationKeys.FoodEdit.FIELD_INDICATIONS),
                        elementsDisponibles = AlimIndic.valuesExcept(),
                        elementsSelectionnes = selectedIndications.value,
                        onSelectionChange = onSelectIndication,
                        getLabel = { it.translateEnum() },
                        getIdentifiant = { it.name },
                        couleurArrierePlan = VetNutriColors.Primary
                )

                Spacer(modifier = Modifier.height(8.dp))
        }
}

@Composable
private fun NutritionInfoTab(
        allNutrients: List<Nutrient>,
        nutrientValues: SnapshotStateMap<Nutrient, String>,
        nutrientErrors: SnapshotStateMap<Nutrient, Boolean>,
        customNutrientNameState: MutableState<String>,
        customNutrientUnitState: MutableState<String>,
        customNutrientSelectedLabelState: MutableState<String>,
        customNutrientErrorState: MutableState<String?>,
        onAddCustomNutrient: (String, String) -> Unit,
        onUpdateCustomNutrient: (CustomNutrient, String, String) -> Unit,
        onDeleteCustomNutrient: (CustomNutrient) -> Unit
) {
        val scrollState = rememberScrollState()

        // Regrouper les nutriments par catégorie
        val mainNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.BASE }
        val lipidNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.LIPID }
        val macroNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.MACRO }
        val minNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.MIN }
        val vitamNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.VITAM }
        val otherNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.OTHER }
        val customNutrients = allNutrients.filter { it is CustomNutrient }.map { it as CustomNutrient }
        var customDropdownExpanded by remember { mutableStateOf(false) }
        var customUnitDropdownExpanded by remember { mutableStateOf(false) }
        val allowedCustomUnits =
                remember {
                        UnitEnum.entries.map { it.displayName }.filter { it.isNotBlank() }.distinct()
                }
        // Acides aminés
        val acidesAminesNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.AMA }

        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Section Principaux nutriments
                if (mainNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_MAIN_NUTRIENTS),
                                nutriments = mainNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE8F5E9)
                        )
                }

                // Section Lipides
                if (lipidNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_LIPIDS),
                                nutriments = lipidNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFFFF8E1)
                        )
                }

                // Section Macronutriments
                if (macroNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_MACRONUTRIENTS),
                                nutriments = macroNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE0F2F1)
                        )
                }

                // Section Minéraux
                if (minNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_MINERALS),
                                nutriments = minNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFF3E5F5)
                        )
                }

                // Section Vitamines
                if (vitamNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_VITAMINS),
                                nutriments = vitamNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFFFEBEE)
                        )
                }

                // Section Acides aminés
                if (acidesAminesNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_AMINO_ACIDS),
                                nutriments = acidesAminesNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE1F5FE) // Bleu clair
                        )
                }

                // Section Autres nutriments
                if (otherNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = translate(LocalizationKeys.FoodEdit.SECTION_OTHER_NUTRIENTS),
                                nutriments = otherNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFF1F8E9)
                        )
                }

                // Bloc d'ajout de nutriments personnalisés en fin d'onglet
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text("Nutriments personnalisés", style = MaterialTheme.typography.h6)
                                OutlinedTextField(
                                        value = customNutrientNameState.value,
                                        onValueChange = {
                                                customNutrientNameState.value = it
                                                customNutrientErrorState.value = null
                                        },
                                        label = { Text("Nom du nutriment") },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                        value = customNutrientUnitState.value,
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Unité") },
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = {
                                                                customUnitDropdownExpanded =
                                                                        !customUnitDropdownExpanded
                                                        }
                                                ) {
                                                        Icon(
                                                                Icons.Default.ArrowDropDown,
                                                                contentDescription = null
                                                        )
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                        expanded = customUnitDropdownExpanded,
                                        onDismissRequest = { customUnitDropdownExpanded = false }
                                ) {
                                        allowedCustomUnits.forEach { unit ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                customNutrientUnitState.value = unit
                                                                customUnitDropdownExpanded = false
                                                        }
                                                ) { Text(unit) }
                                        }
                                }
                                Button(
                                        onClick = {
                                                onAddCustomNutrient(
                                                        customNutrientNameState.value,
                                                        customNutrientUnitState.value
                                                )
                                                customNutrientSelectedLabelState.value =
                                                        customNutrientNameState.value.trim()
                                        },
                                        enabled = customNutrientNameState.value.isNotBlank()
                                ) { Text("Ajouter") }
                                customNutrientErrorState.value?.let { error ->
                                        Text(
                                                text = error,
                                                color = MaterialTheme.colors.error,
                                                style = MaterialTheme.typography.caption
                                        )
                                }
                                if (customNutrients.isNotEmpty()) {
                                        OutlinedTextField(
                                                value = customNutrientSelectedLabelState.value,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Sélectionner un nutriment existant") },
                                                trailingIcon = {
                                                        IconButton(
                                                                onClick = {
                                                                        customDropdownExpanded = !customDropdownExpanded
                                                                }
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.ArrowDropDown,
                                                                        contentDescription = null
                                                                )
                                                        }
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        DropdownMenu(
                                                expanded = customDropdownExpanded,
                                                onDismissRequest = { customDropdownExpanded = false }
                                        ) {
                                                customNutrients.forEach { nutrient ->
                                                        DropdownMenuItem(
                                                                onClick = {
                                                                        customNutrientSelectedLabelState.value =
                                                                                nutrient.nameToString()
                                                                        customNutrientNameState.value =
                                                                                nutrient.nameToString()
                                                                        customNutrientUnitState.value =
                                                                                nutrient.unite
                                                                        customDropdownExpanded = false
                                                                }
                                                        ) {
                                                                Text("${nutrient.nameToString()} (${nutrient.unite})")
                                                        }
                                                }
                                        }
                                        Button(
                                                onClick = {
                                                        val selected = customNutrients.firstOrNull {
                                                                it.nameToString() == customNutrientSelectedLabelState.value
                                                        }
                                                        if (selected != null && selected !in nutrientValues) {
                                                                nutrientValues[selected] = ""
                                                        }
                                                },
                                                enabled = customNutrientSelectedLabelState.value.isNotBlank()
                                        ) {
                                                Icon(Icons.Default.Add, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Utiliser ce nutriment")
                                        }
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                OutlinedButton(
                                                        onClick = {
                                                                val selected = customNutrients.firstOrNull {
                                                                        it.nameToString() ==
                                                                                customNutrientSelectedLabelState.value
                                                                }
                                                                if (selected != null) {
                                                                        onUpdateCustomNutrient(
                                                                                selected,
                                                                                customNutrientNameState.value,
                                                                                customNutrientUnitState.value
                                                                        )
                                                                }
                                                        },
                                                        enabled = customNutrientSelectedLabelState.value.isNotBlank()
                                                ) { Text("Modifier") }
                                                OutlinedButton(
                                                        onClick = {
                                                                val selected = customNutrients.firstOrNull {
                                                                        it.nameToString() ==
                                                                                customNutrientSelectedLabelState.value
                                                                }
                                                                if (selected != null) {
                                                                        onDeleteCustomNutrient(selected)
                                                                }
                                                        },
                                                        enabled = customNutrientSelectedLabelState.value.isNotBlank(),
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor =
                                                                                MaterialTheme.colors.error
                                                                )
                                                ) { Text("Supprimer") }
                                        }
                                }
                        }
                }

                Spacer(modifier = Modifier.height(8.dp))
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodTypeDropdown(
        selectedFoodType: FoodKind?,
        onFoodTypeSelected: (FoodKind) -> Unit,
        availableFoodTypes: List<FoodKind>,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
                OutlinedTextField(
                        value = selectedFoodType?.translateEnum() ?: translate(LocalizationKeys.FoodEdit.HINT_SELECT_TYPE),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                         tooltip = translate(LocalizationKeys.FoodEdit.HINT_SELECT_TYPE)
                                )
                        },
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor = Color.Gray
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableFoodTypes.forEach { foodType ->
                                DropdownMenuItem(
                                        onClick = {
                                                onFoodTypeSelected(foodType)
                                                expanded = false
                                        }
                                ) { Text(foodType.translateEnum()) }
                        }
                }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodGroupDropdown(
        selectedFoodGroup: GroupAlim?,
        onFoodGroupSelected: (GroupAlim) -> Unit,
        availableFoodGroups: List<GroupAlim>,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
                OutlinedTextField(
                        value = selectedFoodGroup?.translateEnum() ?: translate(LocalizationKeys.FoodEdit.HINT_SELECT_GROUP),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                         tooltip = translate(LocalizationKeys.FoodEdit.HINT_SELECT_GROUP)
                                )
                        },
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor = Color.Gray
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableFoodGroups.forEach { foodGroup ->
                                DropdownMenuItem(
                                         onClick = {
                                                onFoodGroupSelected(foodGroup)
                                                expanded = false
                                        }
                                ) { Text(foodGroup.translateEnum()) }
                        }
                }
        }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ContDropdown(
        selectedCont: ContEnum?,
        onContSelected: (ContEnum) -> Unit,
        availableConts: List<ContEnum>,
        modifier: Modifier = Modifier
) {
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = modifier) {
                OutlinedTextField(
                        value = selectedCont?.translateEnum() ?: translate(LocalizationKeys.FoodEdit.HINT_SELECT_CONT),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(translate(LocalizationKeys.FoodEdit.FIELD_CONT)) },
                        trailingIcon = {
                                IconButtonWithTooltip(
                                        onClick = { expanded = !expanded },
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                         tooltip = translate(LocalizationKeys.FoodEdit.HINT_SELECT_CONT)
                                )
                        },
                        colors =
                                TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = VetNutriColors.Primary,
                                        unfocusedBorderColor = Color.Gray
                                ),
                        modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            if (!isIosPlatform) {
                                expanded = false 
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableConts.forEach { cont ->
                                DropdownMenuItem(
                                         onClick = {
                                                onContSelected(cont)
                                                expanded = false
                                        }
                                ) { Text(cont.translateEnum()) }
                        }
                }
        }
}
