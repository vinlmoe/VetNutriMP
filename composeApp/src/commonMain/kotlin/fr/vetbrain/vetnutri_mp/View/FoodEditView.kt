package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectionCard
import fr.vetbrain.vetnutri_mp.Components.NutrientSection
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.DataBMapping
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FoodEditView(
        viewModel: FoodEditViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToSettings: () -> Unit,
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

        // État pour les onglets
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabTitles = listOf("Informations générales", "Composition nutritionnelle")

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

                // Mettre à jour les valeurs des nutriments en utilisant getNutrient()
                // pour appliquer la protection de l'aminogramme
                nutrientValues.clear()
                nutrientErrors.clear()
                allNutrients.forEach { nutrient ->
                        val nutrientValue = aliment.getNutrient(nutrient)
                        if (nutrientValue != null) {
                                nutrientValues[nutrient] = nutrientValue.toString()
                        }
                }
        }

        Scaffold(
                topBar = {
                        TopBar(
                                title =
                                        if (aliment.uuid.isBlank()) "Ajouter un aliment"
                                        else "Modifier l'aliment",
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
                                                                        actionLabel = "OK",
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
                                                                                "Aliment enregistré avec succès",
                                                                        actionLabel = "OK",
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
                                                                "Le nom de l'aliment est obligatoire"
                                                        showErrorMessage = true
                                                        return@launch
                                                }

                                                if (selectedEspecesState.value.isEmpty()) {
                                                        errorMessage =
                                                                "Sélectionnez au moins une espèce"
                                                        showErrorMessage = true
                                                        return@launch
                                                }

                                                // Validation des nutriments
                                                if (!validateNutrients()) {
                                                        errorMessage =
                                                                "Certaines valeurs nutritionnelles sont invalides"
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
                                                        nutrientValues.forEach { (nutrient, value)
                                                                ->
                                                        }

                                                        // Traiter chaque valeur nutritionnelle
                                                        allNutrients.forEach { nutrient ->
                                                                val valueStr =
                                                                        nutrientValues[nutrient]
                                                                                ?: ""

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
                                                                        // Si vide ou valeur ≤ 0, ne
                                                                        // pas
                                                                        // ajouter à la map pour que
                                                                        // le
                                                                        // nutriment soit supprimé
                                                                }
                                                        }

                                                        // Log pour débugger
                                                        processedNutrientValues.forEach {
                                                                (nutrient, quantity) ->
                                                        }

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
                                                                viewModel.saveAliment(
                                                                        updatedAliment
                                                                )
                                                                showSuccessMessage = true
                                                                onNavigateBack()
                                                        } catch (e: Exception) {
                                                                e.printStackTrace()
                                                                errorMessage =
                                                                        "Erreur lors de la sauvegarde: ${e.message}"
                                                                showErrorMessage = true
                                                        }
                                                } catch (e: Exception) {
                                                        errorMessage =
                                                                "Erreur lors de l'enregistrement: ${e.message}"
                                                        showErrorMessage = true
                                                }
                                        }
                                },
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                        ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enregistrer")
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
                                                        nutrientErrors = nutrientErrors
                                                )
                                }
                        }
                }
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
                                        text = "Informations générales",
                                        style = MaterialTheme.typography.h6
                                )

                                OutlinedTextField(
                                        value = nomState.value,
                                        onValueChange = { nomState.value = it },
                                        label = { Text("Nom de l'aliment") },
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
                                        label = { Text("Marque") },
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
                                        label = { Text("Gamme") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors =
                                                TextFieldDefaults.outlinedTextFieldColors(
                                                        focusedBorderColor = VetNutriColors.Primary,
                                                        unfocusedBorderColor = Color.Gray
                                                )
                                )

                                DropdownField(
                                        label = "Base de données",
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
                                                if (it.isBlank()) "Sélectionner..."
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
                                                label = { Text("Prix") },
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
                                                label = { Text("Catégorie de prix") },
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
                                                label = { Text("Quantité") },
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

                                        Text("Aliment consistant")
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
                                        text = "Type et classification",
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
                                Text(text = "Ingrédients", style = MaterialTheme.typography.h6)

                                OutlinedTextField(
                                        value = ingredientsState.value,
                                        onValueChange = { ingredientsState.value = it },
                                        label = { Text("Liste des ingrédients") },
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
                        titre = "Espèces compatibles",
                        elementsDisponibles = Espece.entries,
                        elementsSelectionnes = selectedEspecesState.value,
                        onSelectionChange = onSelectEspece,
                        getLabel = { it.label },
                        getIdentifiant = { it.name },
                        couleurArrierePlan = VetNutriColors.Secondary
                )

                // Section Indications - Utilise le composant réutilisable MultiSelectionCard
                MultiSelectionCard(
                        titre = "Indications",
                        elementsDisponibles = AlimIndic.valuesExcept(),
                        elementsSelectionnes = selectedIndications.value,
                        onSelectionChange = onSelectIndication,
                        getLabel = { it.label },
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
        nutrientErrors: SnapshotStateMap<Nutrient, Boolean>
) {
        val scrollState = rememberScrollState()

        // Regrouper les nutriments par catégorie
        val mainNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.BASE }
        val lipidNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.LIPID }
        val macroNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.MACRO }
        val minNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.MIN }
        val vitamNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.VITAM }
        val otherNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.OTHER }
        // Acides aminés
        val acidesAminesNutrients = allNutrients.filter { it.getMNE() == MainNutrientEnum.AMA }

        Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Utilisation des composants réutilisables pour chaque section de nutriments
                // Section Principaux nutriments
                if (mainNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Nutriments principaux",
                                nutriments = mainNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE8F5E9)
                        )
                }

                // Section Lipides
                if (lipidNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Lipides",
                                nutriments = lipidNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFFFF8E1)
                        )
                }

                // Section Macronutriments
                if (macroNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Macronutriments",
                                nutriments = macroNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE0F2F1)
                        )
                }

                // Section Minéraux
                if (minNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Minéraux",
                                nutriments = minNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFF3E5F5)
                        )
                }

                // Section Vitamines
                if (vitamNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Vitamines",
                                nutriments = vitamNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFFFEBEE)
                        )
                }

                // Section Acides aminés
                if (acidesAminesNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Acides aminés",
                                nutriments = acidesAminesNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFE1F5FE) // Bleu clair
                        )
                }

                // Section Autres nutriments
                if (otherNutrients.isNotEmpty()) {
                        NutrientSection(
                                titre = "Autres nutriments",
                                nutriments = otherNutrients,
                                valeursNutriments = nutrientValues,
                                erreursNutriments = nutrientErrors,
                                couleurArrierePlan = Color(0xFFF1F8E9)
                        )
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
                        value = selectedFoodType?.toString() ?: "Sélectionner un type d'aliment",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
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
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableFoodTypes.forEach { foodType ->
                                DropdownMenuItem(
                                        onClick = {
                                                onFoodTypeSelected(foodType)
                                                expanded = false
                                        }
                                ) { Text(foodType.toString()) }
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
                        value = selectedFoodGroup?.toString() ?: "Sélectionner un groupe d'aliment",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
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
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableFoodGroups.forEach { foodGroup ->
                                DropdownMenuItem(
                                        onClick = {
                                                onFoodGroupSelected(foodGroup)
                                                expanded = false
                                        }
                                ) { Text(foodGroup.toString()) }
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
                        value = selectedCont?.label ?: "Sélectionner un contenant",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Contenant") },
                        trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
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
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                        availableConts.forEach { cont ->
                                DropdownMenuItem(
                                        onClick = {
                                                onContSelected(cont)
                                                expanded = false
                                        }
                                ) { Text(cont.label) }
                        }
                }
        }
}
