package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

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
        onAddAliment: (AlimentEv, Float) -> Unit,
        modifier: Modifier = Modifier
) {
        // États pour les filtres
        var searchQuery by remember { mutableStateOf("") }
        var selectedFoodType by remember { mutableStateOf<FoodKind?>(null) }
        var selectedFoodGroup by remember { mutableStateOf<GroupAlim?>(null) }
        var selectedEspece by remember { mutableStateOf<Espece?>(null) }
        var selectedIndications by remember { mutableStateOf<Set<AlimIndic>>(emptySet()) }
        var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }

        // État pour l'aliment sélectionné et la quantité
        var quantite by remember { mutableStateOf("100") }
        var quantiteError by remember { mutableStateOf(false) }

        // États pour les dropdowns (plus nécessaires pour DropdownField)
        var showIndicationsDropdown by remember { mutableStateOf(false) }

        // Charger les aliments au premier affichage
        LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }

        // Observer la liste des aliments depuis le ViewModel
        val availableFoods by viewModel.availableFoods.collectAsState()
        val isLoadingFoods by viewModel.isLoadingFoods.collectAsState()

        // Filtrer les aliments selon les critères
        val filteredFoods =
                remember(
                        availableFoods,
                        searchQuery,
                        selectedFoodType,
                        selectedFoodGroup,
                        selectedEspece,
                        selectedIndications
                ) {
                        availableFoods.filter { aliment ->
                                // Filtre par recherche textuelle
                                val matchesSearch =
                                        if (searchQuery.isEmpty()) true
                                        else {
                                                aliment.nom?.contains(
                                                        searchQuery,
                                                        ignoreCase = true
                                                ) == true ||
                                                        aliment.brand?.contains(
                                                                searchQuery,
                                                                ignoreCase = true
                                                        ) == true ||
                                                        aliment.ingredients?.contains(
                                                                searchQuery,
                                                                ignoreCase = true
                                                        ) == true
                                        }

                                // Filtre par type d'aliment
                                val matchesType =
                                        selectedFoodType?.let { aliment.typeAliment == it } ?: true

                                // Filtre par groupe d'aliment
                                val matchesGroup =
                                        selectedFoodGroup?.let { aliment.group == it } ?: true

                                // Filtre par espèce
                                val matchesEspece =
                                        selectedEspece?.let { espece ->
                                                aliment.especes.any { it == espece.name }
                                        }
                                                ?: true

                                // Filtre par indications
                                val matchesIndications =
                                        if (selectedIndications.isEmpty()) true
                                        else {
                                                selectedIndications.any { indication ->
                                                        aliment.indicat.contains(indication)
                                                }
                                        }

                                matchesSearch &&
                                        matchesType &&
                                        matchesGroup &&
                                        matchesEspece &&
                                        matchesIndications
                        }
                }

        Column(modifier = modifier.fillMaxSize()) {
                // Barre de navigation avec signature correcte et taille réduite
                TopBar(
                        title = "Ajouter aliment - ${ration.name}",
                        onBackClick = onNavigateBack,
                        onSettingsClick = { /* Pas de settings pour cette vue */},
                        actions = {
                                // Bouton d'ajout (activé seulement si un aliment est sélectionné et
                                // quantité
                                // valide)
                                Button(
                                        onClick = {
                                                selectedFood?.let { aliment ->
                                                        try {
                                                                val quantiteValue =
                                                                        quantite.toFloat()
                                                                if (quantiteValue > 0) {
                                                                        onAddAliment(
                                                                                aliment,
                                                                                quantiteValue
                                                                        )
                                                                }
                                                        } catch (e: NumberFormatException) {
                                                                // Ignore
                                                        }
                                                }
                                        },
                                        enabled =
                                                selectedFood != null &&
                                                        !quantiteError &&
                                                        quantite.isNotEmpty(),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary
                                                )
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Ajouter",
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ajouter")
                                }
                        }
                )

                // Contenu principal - layout à deux colonnes
                Row(
                        modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Colonne gauche - Filtres et liste (60% de l'espace)
                        Column(
                                modifier = Modifier.weight(0.6f).fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                        ) {
                                // Section des filtres
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = AppSizes.elevationSmall
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                Text(
                                                        text = "Filtres de recherche",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        color = VetNutriColors.Primary
                                                )

                                                // Champ de recherche textuelle
                                                OutlinedTextField(
                                                        value = searchQuery,
                                                        onValueChange = { searchQuery = it },
                                                        label = {
                                                                Text("Rechercher", fontSize = 12.sp)
                                                        },
                                                        placeholder = {
                                                                Text(
                                                                        "Nom, marque, ingrédients...",
                                                                        fontSize = 12.sp
                                                                )
                                                        },
                                                        textStyle =
                                                                LocalTextStyle.current.copy(
                                                                        fontSize = 13.sp
                                                                ),
                                                        leadingIcon = {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Search,
                                                                        contentDescription =
                                                                                "Rechercher",
                                                                        modifier =
                                                                                Modifier.size(9.dp)
                                                                )
                                                        },
                                                        trailingIcon =
                                                                if (searchQuery.isNotEmpty()) {
                                                                        {
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                searchQuery =
                                                                                                        ""
                                                                                        }
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector =
                                                                                                        Icons.Default
                                                                                                                .Clear,
                                                                                                contentDescription =
                                                                                                        "Effacer",
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                18.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                                } else null,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                                        .height(70.dp),
                                                        singleLine = true,
                                                        colors =
                                                                TextFieldDefaults
                                                                        .outlinedTextFieldColors(
                                                                                textColor =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                        )
                                                )

                                                // Filtres par dropdowns en grille 2x2
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        // Type d'aliment
                                                        Box(modifier = Modifier.weight(1f)) {
                                                                DropdownField(
                                                                        label = "Type",
                                                                        selectedValue = selectedFoodType,
                                                                        options = FoodKind.entries,
                                                                        onValueChange = { selectedFoodType = it },
                                                                        valueToString = { it.label },
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
                                                                        selectedValue = selectedFoodGroup,
                                                                        options = GroupAlim.entries,
                                                                        onValueChange = { selectedFoodGroup = it },
                                                                        valueToString = { it.label },
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
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        // Espèce
                                                        Box(modifier = Modifier.weight(1f)) {
                                                                DropdownField(
                                                                        label = "Espèce",
                                                                        selectedValue = selectedEspece,
                                                                        options = Espece.entries,
                                                                        onValueChange = { selectedEspece = it },
                                                                        valueToString = { it.label },
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        height = 40.dp,
                                                                        fontSize = 12.sp,
                                                                        labelFontSize = 10.sp,
                                                                        borderWidth = 0.5.dp
                                                                )
                                                        }

                                                        // Indications (multi-sélection)
                                                        Box(modifier = Modifier.weight(1f)) {
                                                                OutlinedTextField(
                                                                        value =
                                                                                if (selectedIndications
                                                                                                .isEmpty()
                                                                                )
                                                                                        "Toutes indications"
                                                                                else
                                                                                        "${selectedIndications.size} sélectionnée(s)",
                                                                        onValueChange = {},
                                                                        label = {
                                                                                Text(
                                                                                        "Indications",
                                                                                        fontSize =
                                                                                                11.sp
                                                                                )
                                                                        },
                                                                        readOnly = true,
                                                                        trailingIcon = {
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                showIndicationsDropdown =
                                                                                                        true
                                                                                        }
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Default
                                                                                                        .ArrowDropDown,
                                                                                                "Dropdown",
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                16.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .height(
                                                                                                40.dp
                                                                                        ),
                                                                        colors =
                                                                                TextFieldDefaults
                                                                                        .outlinedTextFieldColors(
                                                                                                textColor =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .onSurface,
                                                                                                unfocusedBorderColor =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .onSurface
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.3f
                                                                                                                ),
                                                                                                focusedBorderColor =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .primary
                                                                                                                .copy(
                                                                                                                        alpha =
                                                                                                                                0.4f
                                                                                                                )
                                                                                        )
                                                                )
                                                                DropdownMenu(
                                                                        expanded =
                                                                                showIndicationsDropdown,
                                                                        onDismissRequest = {
                                                                                showIndicationsDropdown =
                                                                                        false
                                                                        }
                                                                ) {
                                                                        // Option "Toutes
                                                                        // indications"
                                                                        DropdownMenuItem(
                                                                                onClick = {
                                                                                        selectedIndications =
                                                                                                emptySet()
                                                                                        showIndicationsDropdown =
                                                                                                false
                                                                                }
                                                                        ) {
                                                                                Row(
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Icon(
                                                                                                if (selectedIndications
                                                                                                                .isEmpty()
                                                                                                )
                                                                                                        Icons.Default
                                                                                                                .Check
                                                                                                else
                                                                                                        Icons.Default
                                                                                                                .Clear,
                                                                                                contentDescription =
                                                                                                        null,
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                16.dp
                                                                                                        )
                                                                                        )
                                                                                        Spacer(
                                                                                                modifier =
                                                                                                        Modifier.width(
                                                                                                                8.dp
                                                                                                        )
                                                                                        )
                                                                                        Text(
                                                                                                "Toutes indications"
                                                                                        )
                                                                                }
                                                                        }

                                                                        // Options individuelles
                                                                        AlimIndic.entries.forEach {
                                                                                indication ->
                                                                                DropdownMenuItem(
                                                                                        onClick = {
                                                                                                selectedIndications =
                                                                                                        if (indication in
                                                                                                                        selectedIndications
                                                                                                        ) {
                                                                                                                selectedIndications -
                                                                                                                        indication
                                                                                                        } else {
                                                                                                                selectedIndications +
                                                                                                                        indication
                                                                                                        }
                                                                                        }
                                                                                ) {
                                                                                        Row(
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically
                                                                                        ) {
                                                                                                Icon(
                                                                                                        if (indication in
                                                                                                                        selectedIndications
                                                                                                        )
                                                                                                                Icons.Default
                                                                                                                        .Check
                                                                                                        else
                                                                                                                Icons.Default
                                                                                                                        .Clear,
                                                                                                        contentDescription =
                                                                                                                null,
                                                                                                        modifier =
                                                                                                                Modifier.size(
                                                                                                                        16.dp
                                                                                                                )
                                                                                                )
                                                                                                Spacer(
                                                                                                        modifier =
                                                                                                                Modifier.width(
                                                                                                                        8.dp
                                                                                                                )
                                                                                                )
                                                                                                Text(
                                                                                                        indication
                                                                                                                .label
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Résumé des filtres actifs
                                                if (selectedFoodType != null ||
                                                                selectedFoodGroup != null ||
                                                                selectedEspece != null ||
                                                                selectedIndications.isNotEmpty()
                                                ) {
                                                        Text(
                                                                text =
                                                                        "Filtres actifs: ${filteredFoods.size} aliment(s) trouvé(s)",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = VetNutriColors.Primary
                                                        )
                                                }
                                        }
                                }

                                // Liste des aliments
                                Card(
                                        modifier = Modifier.fillMaxWidth().weight(1f),
                                        elevation = AppSizes.elevationSmall
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .padding(AppSizes.paddingMedium)
                                        ) {
                                                Text(
                                                        text =
                                                                "Aliments disponibles (${filteredFoods.size})",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        color = VetNutriColors.Primary
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                if (isLoadingFoods) {
                                                        Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                        ) { CircularProgressIndicator() }
                                                } else if (filteredFoods.isEmpty()) {
                                                        Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                        ) {
                                                                Text(
                                                                        "Aucun aliment trouvé avec ces critères"
                                                                )
                                                        }
                                                } else {
                                                        LazyColumn(
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                items(filteredFoods) { aliment ->
                                                                        AlimentListItem(
                                                                                aliment = aliment,
                                                                                isSelected =
                                                                                        selectedFood
                                                                                                ?.uuid ==
                                                                                                aliment.uuid,
                                                                                onClick = {
                                                                                        selectedFood =
                                                                                                aliment
                                                                                }
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
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
                                                        text =
                                                                "Sélectionnez un aliment\npour voir ses détails",
                                                        style = MaterialTheme.typography.body1,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.6f
                                                                )
                                                )
                                        }
                                } else {
                                        AlimentDetailsPanel(
                                                aliment = selectedFood!!,
                                                quantite = quantite,
                                                onQuantiteChange = { newQuantite ->
                                                        quantite = newQuantite
                                                        quantiteError =
                                                                try {
                                                                        newQuantite.toFloat() <= 0
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

/** Composant pour afficher un aliment dans la liste */
@Composable
private fun AlimentListItem(aliment: AlimentEv, isSelected: Boolean, onClick: () -> Unit) {
        Card(
                modifier = Modifier.fillMaxWidth().clickable { onClick() },
                elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
                backgroundColor =
                        if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f)
                        else MaterialTheme.colors.surface
        ) {
                Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
                        Text(
                                text = aliment.nom ?: "Sans nom",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                        )

                        if (!aliment.brand.isNullOrEmpty()) {
                                Text(
                                        text = aliment.brand!!,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                        }

                        if (!aliment.gamme.isNullOrEmpty()) {
                                Text(
                                        text = "Gamme: ${aliment.gamme}",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                        }

                        // Afficher quelques infos clés
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                                aliment.typeAliment?.let { type ->
                                        Text(
                                                text = type.label,
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Primary
                                        )
                                }

                                aliment.group?.let { group ->
                                        Text(
                                                text = group.label,
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Primary
                                        )
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
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(AppSizes.paddingMedium)
                                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                Text(
                        text = "Détails de l'aliment",
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary
                )

                Divider()

                // Informations générales
                Text(
                        text = aliment.nom ?: "Sans nom",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Bold
                )

                if (!aliment.brand.isNullOrEmpty()) {
                        DetailRow("Marque", aliment.brand!!)
                }

                if (!aliment.gamme.isNullOrEmpty()) {
                        DetailRow("Gamme", aliment.gamme!!)
                }

                aliment.typeAliment?.let { type -> DetailRow("Type", type.label) }

                aliment.group?.let { group -> DetailRow("Groupe", group.label) }

                if (aliment.especes.isNotEmpty()) {
                        DetailRow("Espèces", aliment.especes.joinToString(", "))
                }

                if (aliment.indicat.isNotEmpty()) {
                        DetailRow("Indications", aliment.indicat.joinToString(", ") { it.label })
                }

                if (!aliment.ingredients.isNullOrEmpty()) {
                        DetailRow("Ingrédients", aliment.ingredients!!)
                }

                Divider()

                // Section quantité
                Text(
                        text = "Quantité à ajouter",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                        value = quantite,
                        onValueChange = onQuantiteChange,
                        label = { Text("Quantité (g)") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = quantiteError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                )

                if (quantiteError) {
                        Text(
                                text = "Veuillez entrer une quantité valide > 0",
                                color = Color.Red,
                                style = MaterialTheme.typography.caption
                        )
                }

                // Informations nutritionnelles principales (si disponibles)
                if (aliment.valMap.isNotEmpty()) {
                        Divider()

                        Text(
                                text = "Composition nutritionnelle (pour 100g)",
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                        )

                        // Afficher quelques nutriments clés
                        val nutrientsToShow =
                                listOf(
                                        "PROTEINE",
                                        "LIPIDE",
                                        "ENA",
                                        "CELLULOSE",
                                        "CENDRE",
                                        "HUMIDITE"
                                )
                        nutrientsToShow.forEach { nutrientLabel ->
                                val nutrient =
                                        aliment.valMap.keys.find { it.label == nutrientLabel }
                                if (nutrient != null) {
                                        val value = aliment.valMap[nutrient]
                                        if (value != null) {
                                                DetailRow(
                                                        nutrient.label,
                                                        "${String.format("%.1f", value.value)} ${value.unit ?: ""}"
                                                )
                                        }
                                }
                        }
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
