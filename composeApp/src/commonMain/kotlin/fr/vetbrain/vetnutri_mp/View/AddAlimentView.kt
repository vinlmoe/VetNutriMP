package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.MultiSelectDropdownField
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
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

        // État pour l'aliment sélectionné et la quantité
        var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
        var quantite by remember { mutableStateOf("100") }
        var quantiteError by remember { mutableStateOf(false) }

        // Charger les aliments au premier affichage
        LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }

        // Observer la liste des aliments depuis le ViewModel
        val availableFoods by viewModel.availableFoods.collectAsState()
        val isLoadingFoods by viewModel.isLoadingFoods.collectAsState()

        // Configuration pour FoodSearchComponent
        val searchConfig = remember {
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
                                // Utilisation du composant partagé FoodSearchComponent
                                FoodSearchComponent(
                                        foods = availableFoods,
                                        filters = filters,
                                        onFiltersChange = { filters = it },
                                        config = searchConfig,
                                        modifier = Modifier.fillMaxSize()
                                )
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
                                                                        newQuantite.toDouble() <= 0
                                                                } catch (e: NumberFormatException) {
                                                                        true
                                                                }
                                                },
                                                quantiteError = quantiteError
                                        )
                                }
                        }
                }

                // Bouton d'ajout flottant en bas à droite
                Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                ) {
                        if (selectedFood != null && !quantiteError && quantite.isNotEmpty()) {
                                FloatingActionButton(
                                        onClick = {
                                                selectedFood?.let { aliment ->
                                                        try {
                                                                val quantiteValue = quantite.toDouble()
                                                                if (quantiteValue > 0) {
                                                                        onAddAliment(aliment, quantiteValue)
                                                                }
                                                        } catch (e: NumberFormatException) {
                                                                // Ignore
                                                        }
                                                }
                                        },
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary,
                                        modifier = Modifier.padding(AppSizes.paddingLarge)
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

/** Composant pour afficher un aliment dans la liste (rendu public pour réutilisation) */
@Composable
fun AlimentListItem(aliment: AlimentEv, isSelected: Boolean, onClick: () -> Unit) {
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

                        // Afficher quelques infos clés (type et groupe) en ignorant les valeurs ALL
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
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
                                                        it != AlimIndic.ALL && it != AlimIndic.AUTRE
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

                aliment.typeAliment?.let { type ->
                        val text: String = type.translateEnum()
                        DetailRow("Type", text)
                }

                aliment.group?.let { group ->
                        val text: String = group.translateEnum()
                        DetailRow("Groupe", text)
                }

                run {
                        val species: List<String> =
                                aliment.getEspecesList().filter { it != Espece.CH }.map {
                                        it.translateEnum()
                                }
                        if (species.isNotEmpty()) {
                                DetailRow("Espèces", species.joinToString(", "))
                        }
                }

                run {
                        val indications: List<String> =
                                aliment.getIndications()
                                        .filter { it != AlimIndic.ALL && it != AlimIndic.AUTRE }
                                        .map { it.translateEnum() }
                        if (indications.isNotEmpty()) {
                                DetailRow("Indications", indications.joinToString(", "))
                        }
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

                BasicAppTextField(
                        value = quantite,
                        onValueChange = onQuantiteChange,
                        placeholder = "Quantité (g)",
                        modifier = Modifier.fillMaxWidth(),
                        isError = quantiteError,
                        errorMessage =
                                if (quantiteError) "Veuillez entrer une quantité valide > 0"
                                else null
                )

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
                                                        "${TextUtils.formatDecimal(value.value.toDouble(), 1)} ${value.unit ?: ""}"
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
