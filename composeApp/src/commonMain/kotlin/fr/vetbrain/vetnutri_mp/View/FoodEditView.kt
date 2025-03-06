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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
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

        val nomState = remember { mutableStateOf("") }
        val brandState = remember { mutableStateOf("") }
        val gammeState = remember { mutableStateOf("") }
        val ingredientsState = remember { mutableStateOf("") }
        val priceState = remember { mutableStateOf("") }
        val categPriceState = remember { mutableStateOf("") }
        val quantIntState = remember { mutableStateOf("") }
        val contState = remember { mutableStateOf("") }
        val consistentState = remember { mutableStateOf(false) }

        val selectedFoodType = remember { mutableStateOf<FoodKind?>(null) }
        val selectedFoodGroup = remember { mutableStateOf<GroupAlim?>(null) }
        val selectedEspecesState = remember {
                mutableStateOf(
                        Espece.entries
                                .filter { espece -> aliment.especes.any { id -> id == espece.id } }
                                .toMutableList()
                )
        }
        val selectedIndications = remember {
                mutableStateOf<MutableList<AlimIndic>>(mutableListOf())
        }

        val allNutrients = viewModel.getAllNutrients()
        val nutrientValues = remember { mutableStateMapOf<Nutrient, String>() }

        // Mettre à jour les états locaux lorsque l'aliment change
        LaunchedEffect(aliment) {
                println("DEBUG FoodEditView: Mise à jour des champs avec aliment: ${aliment.nom}")
                nomState.value = aliment.nom ?: ""
                brandState.value = aliment.brand ?: ""
                gammeState.value = aliment.gamme ?: ""
                ingredientsState.value = aliment.ingredients ?: ""
                priceState.value = aliment.price?.toString() ?: ""
                categPriceState.value = aliment.categPrice ?: ""
                quantIntState.value = aliment.quantInt?.toString() ?: ""
                contState.value = aliment.cont?.toString() ?: ""
                consistentState.value = aliment.consistent
                selectedFoodType.value = aliment.typeAliment
                selectedFoodGroup.value = aliment.group
                selectedEspecesState.value =
                        Espece.entries
                                .filter { espece -> aliment.especes.any { id -> id == espece.id } }
                                .toMutableList()
                selectedIndications.value = aliment.indicat.toMutableList()

                // Mettre à jour les valeurs des nutriments
                nutrientValues.clear()
                aliment.valMap.forEach { (nutrient, value) ->
                        nutrientValues[nutrient] = value.toString()
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
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.padding(paddingValues)
                                        .fillMaxSize()
                                        .padding(16.dp)
                                        .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        // Bouton d'enregistrement
                        Button(
                                onClick = {
                                        scope.launch {
                                                val updatedAliment =
                                                        aliment.copy(
                                                                nom =
                                                                        nomState.value.takeIf {
                                                                                it.isNotBlank()
                                                                        },
                                                                brand =
                                                                        brandState.value.takeIf {
                                                                                it.isNotBlank()
                                                                        },
                                                                gamme =
                                                                        gammeState.value.takeIf {
                                                                                it.isNotBlank()
                                                                        },
                                                                ingredients =
                                                                        ingredientsState.value
                                                                                .takeIf {
                                                                                        it.isNotBlank()
                                                                                },
                                                                price =
                                                                        priceState.value
                                                                                .toDoubleOrNull(),
                                                                categPrice =
                                                                        categPriceState.value
                                                                                .takeIf {
                                                                                        it.isNotBlank()
                                                                                },
                                                                quantInt =
                                                                        quantIntState.value
                                                                                .toFloatOrNull(),
                                                                cont =
                                                                        fr.vetbrain.vetnutri_mp
                                                                                .Enumer.ContEnum
                                                                                .getByName(
                                                                                        contState
                                                                                                .value
                                                                                ),
                                                                consistent = consistentState.value,
                                                                typeAliment =
                                                                        selectedFoodType.value,
                                                                group = selectedFoodGroup.value,
                                                                especes =
                                                                        selectedEspecesState
                                                                                .value
                                                                                .map { it.id }
                                                                                .toMutableList(),
                                                                indicat = selectedIndications.value,
                                                                valMap =
                                                                        nutrientValues
                                                                                .mapValues {
                                                                                        it.value
                                                                                                .toFloatOrNull()
                                                                                                ?: 0f
                                                                                }
                                                                                .filterValues {
                                                                                        it > 0f
                                                                                }
                                                        )
                                                viewModel.saveAliment(updatedAliment)
                                                onNavigateBack()
                                        }
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Enregistrer")
                        }

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
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
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
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
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
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
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
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )

                                                OutlinedTextField(
                                                        value = categPriceState.value,
                                                        onValueChange = {
                                                                categPriceState.value = it
                                                        },
                                                        label = { Text("Catégorie de prix") },
                                                        modifier = Modifier.weight(1f),
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )
                                        }

                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                OutlinedTextField(
                                                        value = quantIntState.value,
                                                        onValueChange = {
                                                                quantIntState.value = it
                                                        },
                                                        label = { Text("Quantité") },
                                                        modifier = Modifier.weight(1f),
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                focusedBorderColor =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                unfocusedBorderColor =
                                                                                        Color.Gray
                                                                        )
                                                )

                                                ContDropdown(
                                                        selectedCont =
                                                                ContEnum.getByName(contState.value),
                                                        onContSelected = {
                                                                contState.value = it.name
                                                        },
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
                                                        onCheckedChange = {
                                                                consistentState.value = it
                                                        },
                                                        colors =
                                                                CheckboxDefaults.colors(
                                                                        checkedColor =
                                                                                VetNutriColors
                                                                                        .Primary
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
                                                onFoodTypeSelected = {
                                                        selectedFoodType.value = it
                                                },
                                                availableFoodTypes = FoodKind.valuesExcept(),
                                                modifier = Modifier.fillMaxWidth()
                                        )

                                        // Dropdown pour le groupe d'aliment
                                        FoodGroupDropdown(
                                                selectedFoodGroup = selectedFoodGroup.value,
                                                onFoodGroupSelected = {
                                                        selectedFoodGroup.value = it
                                                },
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
                                        Text(
                                                text = "Ingrédients",
                                                style = MaterialTheme.typography.h6
                                        )

                                        OutlinedTextField(
                                                value = ingredientsState.value,
                                                onValueChange = { ingredientsState.value = it },
                                                label = { Text("Liste des ingrédients") },
                                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                                colors =
                                                        TextFieldDefaults.outlinedTextFieldColors(
                                                                focusedBorderColor =
                                                                        VetNutriColors.Primary,
                                                                unfocusedBorderColor = Color.Gray
                                                        )
                                        )
                                }
                        }

                        // Section Espèces
                        EspeceMultiSelectionCard(
                                title = "Espèces compatibles",
                                availableItems = Espece.entries,
                                selectedItems = selectedEspecesState.value,
                                onSelectionChange = {
                                        selectedEspecesState.value = it.toMutableList()
                                }
                        )

                        // Section Indications
                        IndicMultiSelectionCard(
                                title = "Indications",
                                availableItems = AlimIndic.valuesExcept(),
                                selectedItems = selectedIndications.value,
                                onSelectionChange = {
                                        selectedIndications.value = it.toMutableList()
                                }
                        )

                        // Section Valeurs nutritionnelles
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                                Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                        Text(
                                                text = "Valeurs nutritionnelles",
                                                style = MaterialTheme.typography.h6
                                        )

                                        allNutrients.forEach { nutrient ->
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Text(
                                                                text = "${nutrient}",
                                                                modifier = Modifier.weight(1f)
                                                        )

                                                        OutlinedTextField(
                                                                value =
                                                                        nutrientValues.getOrDefault(
                                                                                nutrient,
                                                                                ""
                                                                        ),
                                                                onValueChange = {
                                                                        nutrientValues[nutrient] =
                                                                                it
                                                                },
                                                                modifier = Modifier.width(120.dp),
                                                                colors =
                                                                        TextFieldDefaults
                                                                                .outlinedTextFieldColors(
                                                                                        focusedBorderColor =
                                                                                                VetNutriColors
                                                                                                        .Primary,
                                                                                        unfocusedBorderColor =
                                                                                                Color.Gray
                                                                                ),
                                                                trailingIcon = {
                                                                        Text(
                                                                                text =
                                                                                        nutrient.unite,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .caption
                                                                        )
                                                                }
                                                        )
                                                }
                                        }
                                }
                        }
                }
        }
}

@Composable
fun EspeceMultiSelectionCard(
        title: String,
        availableItems: List<Espece>,
        selectedItems: MutableList<Espece>,
        onSelectionChange: (List<Espece>) -> Unit,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Text(text = title, style = MaterialTheme.typography.h6)

                        OutlinedButton(
                                onClick = { showDialog = true },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                if (selectedItems.isEmpty()) "Sélectionner..."
                                                else
                                                        "${selectedItems.size} élément(s) sélectionné(s)"
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                        }

                        if (selectedItems.isNotEmpty()) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        selectedItems.take(5).forEach { espece ->
                                                Surface(
                                                        color =
                                                                VetNutriColors.Secondary.copy(
                                                                        alpha = 0.2f
                                                                ),
                                                        shape = MaterialTheme.shapes.small,
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 8.dp,
                                                                                vertical = 4.dp
                                                                        ),
                                                                horizontalAlignment =
                                                                        Alignment.CenterHorizontally
                                                        ) {
                                                                Text(text = espece.label)
                                                                Text(
                                                                        text = "(${espece.name})",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                        if (selectedItems.size > 5) {
                                                Text("+ ${selectedItems.size - 5} autres")
                                        }
                                }
                        }
                }
        }

        if (showDialog) {
                AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(title) },
                        text = {
                                Box(modifier = Modifier.height(300.dp)) { // Hauteur fixe
                                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                                                items(availableItems, key = { it.name }) { espece ->
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        vertical =
                                                                                                4.dp
                                                                                ),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Checkbox(
                                                                        checked =
                                                                                selectedItems
                                                                                        .contains(
                                                                                                espece
                                                                                        ),
                                                                        onCheckedChange = { checked
                                                                                ->
                                                                                val newList =
                                                                                        selectedItems
                                                                                                .toMutableList()
                                                                                if (checked) {
                                                                                        if (!newList.contains(
                                                                                                        espece
                                                                                                )
                                                                                        ) {
                                                                                                newList.add(
                                                                                                        espece
                                                                                                )
                                                                                        }
                                                                                } else {
                                                                                        newList.remove(
                                                                                                espece
                                                                                        )
                                                                                }
                                                                                onSelectionChange(
                                                                                        newList
                                                                                )
                                                                        }
                                                                )
                                                                Column(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        start = 8.dp
                                                                                )
                                                                ) {
                                                                        Text(text = espece.label)
                                                                        Text(
                                                                                text =
                                                                                        "(${espece.name})",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .caption,
                                                                                color = Color.Gray
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        },
                        confirmButton = {
                                Button(onClick = { showDialog = false }) { Text("Fermer") }
                        }
                )
        }
}

@Composable
fun IndicMultiSelectionCard(
        title: String,
        availableItems: List<AlimIndic>,
        selectedItems: MutableList<AlimIndic>,
        onSelectionChange: (List<AlimIndic>) -> Unit,
        modifier: Modifier = Modifier
) {
        var showDialog by remember { mutableStateOf(false) }

        Card(modifier = modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Text(text = title, style = MaterialTheme.typography.h6)

                        OutlinedButton(
                                onClick = { showDialog = true },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                if (selectedItems.isEmpty()) "Sélectionner..."
                                                else
                                                        "${selectedItems.size} élément(s) sélectionné(s)"
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                        }

                        if (selectedItems.isNotEmpty()) {
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                        selectedItems.take(5).forEach { indication ->
                                                Surface(
                                                        color =
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.2f
                                                                ),
                                                        shape = MaterialTheme.shapes.small,
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                horizontal = 8.dp,
                                                                                vertical = 4.dp
                                                                        ),
                                                                horizontalAlignment =
                                                                        Alignment.CenterHorizontally
                                                        ) {
                                                                Badge(
                                                                        text = indication.label,
                                                                        subText = indication.name,
                                                                        id = indication.coef,
                                                                        backgroundColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                        }
                                                }
                                        }
                                        if (selectedItems.size > 5) {
                                                Text("+ ${selectedItems.size - 5} autres")
                                        }
                                }
                        }
                }
        }

        if (showDialog) {
                AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(title) },
                        text = {
                                LazyColumn(
                                        modifier = Modifier.heightIn(max = 300.dp).fillMaxHeight()
                                ) {
                                        items(availableItems, key = { it.name }) { indication ->
                                                Row(
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .padding(vertical = 4.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Checkbox(
                                                                checked =
                                                                        selectedItems.contains(
                                                                                indication
                                                                        ),
                                                                onCheckedChange = { checked ->
                                                                        val newList =
                                                                                selectedItems
                                                                                        .toMutableList()
                                                                        if (checked) {
                                                                                if (!newList.contains(
                                                                                                indication
                                                                                        )
                                                                                ) {
                                                                                        newList.add(
                                                                                                indication
                                                                                        )
                                                                                }
                                                                        } else {
                                                                                newList.remove(
                                                                                        indication
                                                                                )
                                                                        }
                                                                        onSelectionChange(newList)
                                                                }
                                                        )
                                                        Column(
                                                                modifier =
                                                                        Modifier.padding(
                                                                                start = 8.dp
                                                                        )
                                                        ) {
                                                                Text(text = indication.label)
                                                                Text(
                                                                        text =
                                                                                "(${indication.name})",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                }
                        },
                        confirmButton = {
                                Button(onClick = { showDialog = false }) { Text("Fermer") }
                        }
                )
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

@Composable
private fun <T> ScrollableSelectionList(
        items: List<T>,
        isSelected: (T) -> Boolean,
        onSelectionChange: (T, Boolean) -> Unit,
        itemContent: @Composable (T) -> Unit
) {
        Surface(color = MaterialTheme.colors.surface, modifier = Modifier.fillMaxWidth()) {
                Column(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .verticalScroll(rememberScrollState())
                ) {
                        items.forEach { item ->
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Checkbox(
                                                checked = isSelected(item),
                                                onCheckedChange = { checked ->
                                                        onSelectionChange(item, checked)
                                                }
                                        )
                                        itemContent(item)
                                }
                        }
                }
        }
}
