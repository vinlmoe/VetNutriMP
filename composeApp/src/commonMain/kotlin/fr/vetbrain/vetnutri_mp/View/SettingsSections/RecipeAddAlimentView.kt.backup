package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlinx.coroutines.delay

/** Vue pour ajouter un aliment à une recette */
@Composable
fun RecipeAddAlimentView(
        foodRepository: FoodRepository,
        onNavigateBack: () -> Unit,
        onAddAliment: (AlimentEv, Double) -> Unit,
        modifier: Modifier = Modifier
) {
    // États pour les filtres avec debouncing
    var searchQuery by remember { mutableStateOf("") }
    var debouncedSearchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<FoodKind?>(null) }
    var selectedGroup by remember { mutableStateOf<GroupAlim?>(null) }
    var selectedEspece by remember { mutableStateOf<Espece?>(null) }
    var selectedIndic by remember { mutableStateOf<AlimIndic?>(null) }

    // État pour l'aliment sélectionné et la quantité
    var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
    var quantite by remember { mutableStateOf("100") }
    var quantiteError by remember { mutableStateOf(false) }

    // Debouncing de la recherche (300ms)
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedSearchQuery = searchQuery
    }

    // Charger les aliments
    val allFoods = remember { mutableStateListOf<AlimentEv>() }

    LaunchedEffect(Unit) {
        try {
            println("🔍 RecipeAddAlimentView: Début du chargement des aliments...")
            kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
                val foods = foodRepository.getAllFoods()
                println("🔍 RecipeAddAlimentView: ${foods.size} aliments récupérés du repository")
                kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.Main) {
                    allFoods.clear()
                    allFoods.addAll(foods)
                    println(
                            "🔍 RecipeAddAlimentView: ${allFoods.size} aliments ajoutés à la liste locale"
                    )
                }
            }
        } catch (e: Exception) {
            println("❌ RecipeAddAlimentView: Erreur lors du chargement: ${e.message}")
            e.printStackTrace()
        }
    }

    // Filtrer les aliments selon les critères avec debouncing
    val filteredFoods by derivedStateOf {
        val filtered =
                allFoods.filter { aliment ->
                    val matchesSearch =
                            debouncedSearchQuery.isEmpty() ||
                                    aliment.nom?.contains(
                                            debouncedSearchQuery,
                                            ignoreCase = true
                                    ) == true ||
                                    aliment.brand?.contains(
                                            debouncedSearchQuery,
                                            ignoreCase = true
                                    ) == true

                    val matchesType = selectedType == null || aliment.typeAliment == selectedType
                    val matchesGroup = selectedGroup == null || aliment.group == selectedGroup
                    val matchesEspece =
                            selectedEspece == null ||
                                    aliment.getEspecesList().contains(selectedEspece)
                    val matchesIndic =
                            selectedIndic == null ||
                                    aliment.getIndications().contains(selectedIndic)

                    matchesSearch && matchesType && matchesGroup && matchesEspece && matchesIndic
                }
        println(
                "🔍 RecipeAddAlimentView: Filtrage - ${allFoods.size} aliments totaux, ${filtered.size} après filtrage"
        )
        filtered
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Ajouter un aliment à la recette") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                            }
                        },
                        backgroundColor = VetNutriColors.Primary,
                        contentColor = VetNutriColors.OnPrimary
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = {
                            if (selectedFood != null && !quantiteError && quantite.isNotEmpty()) {
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
                            }
                        },
                        backgroundColor =
                                if (selectedFood != null && !quantiteError && quantite.isNotEmpty()
                                ) {
                                    VetNutriColors.Primary
                                } else {
                                    VetNutriColors.Primary.copy(alpha = 0.5f)
                                },
                        contentColor = VetNutriColors.OnPrimary
                ) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter l'aliment sélectionné",
                            modifier = Modifier.size(24.dp)
                    )
                }
            }
    ) {
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
                // Barre de recherche
                BasicAppTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "Rechercher un aliment...",
                        modifier = Modifier.fillMaxWidth()
                )

                // Filtres simplifiés avec icônes
                Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    // Légende des filtres
                    Text(
                            text = "Filtres :",
                            style = MaterialTheme.typography.caption,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        // Type d'aliment - Icône de type
                        Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Filtrer par type d'aliment",
                                tint =
                                        if (selectedType != null) VetNutriColors.Primary
                                        else Color.Gray,
                                modifier =
                                        Modifier.size(AppSizes.iconSizeSmall).clickable {
                                            // TODO: Implémenter la sélection de type
                                            // Pour l'instant, on peut ajouter un menu déroulant ou
                                            // un dialogue
                                        }
                        )

                        // Groupe d'aliment - Icône de groupe
                        Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Filtrer par groupe d'aliment",
                                tint =
                                        if (selectedGroup != null) VetNutriColors.Primary
                                        else Color.Gray,
                                modifier =
                                        Modifier.size(AppSizes.iconSizeSmall).clickable {
                                            // TODO: Implémenter la sélection de groupe
                                            // Pour l'instant, on peut ajouter un menu déroulant ou
                                            // un dialogue
                                        }
                        )

                        // Espèce - Icône d'animal
                        Icon(
                                imageVector = Icons.Default.Pets,
                                contentDescription = "Filtrer par espèce",
                                tint =
                                        if (selectedEspece != null) VetNutriColors.Primary
                                        else Color.Gray,
                                modifier =
                                        Modifier.size(AppSizes.iconSizeSmall).clickable {
                                            // TODO: Implémenter la sélection d'espèce
                                            // Pour l'instant, on peut ajouter un menu déroulant ou
                                            // un dialogue
                                        }
                        )

                        // Indication - Icône d'information
                        Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Filtrer par indication",
                                tint =
                                        if (selectedIndic != null) VetNutriColors.Primary
                                        else Color.Gray,
                                modifier =
                                        Modifier.size(AppSizes.iconSizeSmall).clickable {
                                            // TODO: Implémenter la sélection d'indication
                                            // Pour l'instant, on peut ajouter un menu déroulant ou
                                            // un dialogue
                                        }
                        )

                        // Réinitialiser les filtres - Icône de réinitialisation
                        Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Réinitialiser tous les filtres",
                                tint =
                                        if (selectedType != null ||
                                                        selectedGroup != null ||
                                                        selectedEspece != null ||
                                                        selectedIndic != null
                                        )
                                                VetNutriColors.Secondary
                                        else Color.Gray,
                                modifier =
                                        Modifier.size(AppSizes.iconSizeSmall).clickable {
                                            selectedType = null
                                            selectedGroup = null
                                            selectedEspece = null
                                            selectedIndic = null
                                        }
                        )
                    }

                    // Affichage des filtres actifs
                    if (selectedType != null ||
                                    selectedGroup != null ||
                                    selectedEspece != null ||
                                    selectedIndic != null
                    ) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                        ) {
                            Text(
                                    text = "Filtres actifs :",
                                    style = MaterialTheme.typography.caption,
                                    color = Color.Gray
                            )

                            selectedType?.let { type ->
                                Text(
                                        text = "Type: ${type.translateEnum()}",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                            }

                            selectedGroup?.let { group ->
                                Text(
                                        text = "Groupe: ${group.translateEnum()}",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                            }

                            selectedEspece?.let { espece ->
                                Text(
                                        text = "Espèce: ${espece.translateEnum()}",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                            }

                            selectedIndic?.let { indic ->
                                Text(
                                        text = "Indication: ${indic.translateEnum()}",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary
                                )
                            }
                        }
                    }
                }

                // Liste des aliments
                if (allFoods.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            state = rememberLazyListState(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        if (filteredFoods.isEmpty()) {
                            item {
                                Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                            text =
                                                    "Aucun aliment trouvé\nEssayez de modifier vos filtres",
                                            style = MaterialTheme.typography.body1,
                                            color =
                                                    MaterialTheme.colors.onSurface.copy(
                                                            alpha = 0.6f
                                                    ),
                                            textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(
                                    items = filteredFoods,
                                    key = { it.uuid } // Optimisation avec des clés uniques
                            ) { aliment ->
                                AlimentListItem(
                                        aliment = aliment,
                                        isSelected = selectedFood?.uuid == aliment.uuid,
                                        onClick = { selectedFood = aliment }
                                )
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                                text = "Sélectionnez un aliment\npour voir ses détails",
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
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
            // Nom de l'aliment
            Text(
                    text = aliment.nom ?: "Sans nom",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
            )

            // Marque et gamme sur la même ligne
            if (!aliment.brand.isNullOrEmpty() || !aliment.gamme.isNullOrEmpty()) {
                Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        modifier = Modifier.fillMaxWidth()
                ) {
                    aliment.brand?.let {
                        Text(
                                text = it,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f)
                        )
                    }
                    aliment.gamme?.let {
                        Text(
                                text = it,
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Type et groupe sur la même ligne
            Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    modifier = Modifier.fillMaxWidth()
            ) {
                aliment.typeAliment?.takeIf { it != FoodKind.ALL }?.let { type ->
                    Text(
                            text = type.translateEnum(),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary,
                            modifier = Modifier.weight(1f)
                    )
                }
                aliment.group?.takeIf { it != GroupAlim.ALL }?.let { group ->
                    Text(
                            text = group.translateEnum(),
                            style = MaterialTheme.typography.caption,
                            color = VetNutriColors.Primary,
                            modifier = Modifier.weight(1f)
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
    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
            state = rememberLazyListState()
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

        aliment.typeAliment?.let { type -> item { DetailRow("Type", type.translateEnum()) } }

        aliment.group?.let { group -> item { DetailRow("Groupe", group.translateEnum()) } }

        // Espèces ciblées
        val species = aliment.getEspecesList().filter { it != Espece.CH }.map { it.translateEnum() }
        if (species.isNotEmpty()) {
            item { DetailRow("Espèces", species.joinToString(", ")) }
        }

        // Indications
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

        // Section quantité
        item {
            Text(
                    text = "Quantité à ajouter",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
            )
        }

        item {
            BasicAppTextField(
                    value = quantite,
                    onValueChange = onQuantiteChange,
                    placeholder = "Quantité (g)",
                    modifier = Modifier.fillMaxWidth(),
                    isError = quantiteError,
                    errorMessage =
                            if (quantiteError) "Veuillez entrer une quantité valide > 0" else null
            )
        }

        // Informations nutritionnelles principales
        if (aliment.valMap.isNotEmpty()) {
            item { Divider() }

            item {
                Text(
                        text = "Composition nutritionnelle (pour 100g)",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                )
            }

            val nutrientsToShow =
                    listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "HUMIDITE")

            nutrientsToShow.forEach { nutrientLabel ->
                val nutrient = aliment.valMap.keys.find { it.label == nutrientLabel }
                if (nutrient != null) {
                    val value = aliment.valMap[nutrient]
                    if (value != null) {
                        item {
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
