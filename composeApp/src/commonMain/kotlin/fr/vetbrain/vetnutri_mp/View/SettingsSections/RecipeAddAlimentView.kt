package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/**
 * Vue pour ajouter un aliment à une recette
 */
@Composable
fun RecipeAddAlimentView(
    foodRepository: FoodRepository,
    onNavigateBack: () -> Unit,
    onAddAliment: (AlimentEv, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // États pour les filtres
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<FoodKind?>(null) }
    var selectedGroup by remember { mutableStateOf<GroupAlim?>(null) }
    var selectedEspece by remember { mutableStateOf<Espece?>(null) }
    var selectedIndic by remember { mutableStateOf<AlimIndic?>(null) }
    
    // État pour l'aliment sélectionné et la quantité
    var selectedFood by remember { mutableStateOf<AlimentEv?>(null) }
    var quantite by remember { mutableStateOf("100") }
    var quantiteError by remember { mutableStateOf(false) }
    
    // Charger les aliments
    val allFoods = remember {
        mutableStateListOf<AlimentEv>()
    }
    
    LaunchedEffect(Unit) {
        try {
            println("🔍 RecipeAddAlimentView: Chargement des aliments...")
            kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
                val foods = foodRepository.getAllFoods()
                println("🔍 RecipeAddAlimentView: ${foods.size} aliments chargés")
                kotlinx.coroutines.withContext(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.Main) {
                    allFoods.clear()
                    allFoods.addAll(foods)
                    println("🔍 RecipeAddAlimentView: ${allFoods.size} aliments dans la liste")
                }
            }
        } catch (e: Exception) {
            println("❌ RecipeAddAlimentView: Erreur lors du chargement: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Filtrer les aliments selon les critères
    val filteredFoods = remember(allFoods, searchQuery, selectedType, selectedGroup, selectedEspece, selectedIndic) {
        val filtered = allFoods.filter { aliment ->
            val matchesSearch = searchQuery.isEmpty() || 
                aliment.nom?.contains(searchQuery, ignoreCase = true) == true ||
                aliment.brand?.contains(searchQuery, ignoreCase = true) == true
            
            val matchesType = selectedType == null || aliment.typeAliment == selectedType
            val matchesGroup = selectedGroup == null || aliment.group == selectedGroup
            val matchesEspece = selectedEspece == null || aliment.getEspecesList().contains(selectedEspece)
            val matchesIndic = selectedIndic == null || aliment.getIndications().contains(selectedIndic)
            
            matchesSearch && matchesType && matchesGroup && matchesEspece && matchesIndic
        }
        println("🔍 RecipeAddAlimentView: ${filtered.size} aliments après filtrage (sur ${allFoods.size} total)")
        filtered
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Barre de navigation
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
                
                // Filtres (simplifiés pour l'instant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    // Type d'aliment
                    OutlinedTextField(
                        value = selectedType?.translateEnum() ?: "Tous types",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Type") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Groupe d'aliment
                    OutlinedTextField(
                        value = selectedGroup?.translateEnum() ?: "Tous groupes",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Groupe") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                // Liste des aliments
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFoods) { aliment ->
                        AlimentListItem(
                            aliment = aliment,
                            isSelected = selectedFood?.uuid == aliment.uuid,
                            onClick = { selectedFood = aliment }
                        )
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
                            text = "Sélectionnez un aliment\npour voir ses détails",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    AlimentDetailsPanel(
                        aliment = selectedFood!!,
                        quantite = quantite,
                        onQuantiteChange = { newQuantite ->
                            quantite = newQuantite
                            quantiteError = try {
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

/** Composant pour afficher un aliment dans la liste */
@Composable
private fun AlimentListItem(aliment: AlimentEv, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = if (isSelected) AppSizes.elevationMedium else AppSizes.elevationSmall,
        backgroundColor = if (isSelected) VetNutriColors.Primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface
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
                val typeText = aliment.typeAliment?.takeIf { it != FoodKind.ALL }?.translateEnum()
                val groupText = aliment.group?.takeIf { it != GroupAlim.ALL }?.translateEnum()
                
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
        modifier = Modifier.fillMaxSize()
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
            DetailRow("Type", type.translateEnum())
        }
        
        aliment.group?.let { group ->
            DetailRow("Groupe", group.translateEnum())
        }
        
        // Espèces ciblées
        val species = aliment.getEspecesList()
            .filter { it != Espece.CH }
            .map { it.translateEnum() }
        if (species.isNotEmpty()) {
            DetailRow("Espèces", species.joinToString(", "))
        }
        
        // Indications
        val indications = aliment.getIndications()
            .filter { it != AlimIndic.ALL && it != AlimIndic.AUTRE }
            .map { it.translateEnum() }
        if (indications.isNotEmpty()) {
            DetailRow("Indications", indications.joinToString(", "))
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
            errorMessage = if (quantiteError) "Veuillez entrer une quantité valide > 0" else null
        )
        
        // Informations nutritionnelles principales
        if (aliment.valMap.isNotEmpty()) {
            Divider()
            
            Text(
                text = "Composition nutritionnelle (pour 100g)",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold
            )
            
            val nutrientsToShow = listOf(
                "PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "HUMIDITE"
            )
            
            nutrientsToShow.forEach { nutrientLabel ->
                val nutrient = aliment.valMap.keys.find { it.label == nutrientLabel }
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
