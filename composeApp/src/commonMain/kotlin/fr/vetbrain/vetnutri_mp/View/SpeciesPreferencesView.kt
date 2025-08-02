package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

@Composable
fun SpeciesPreferencesView(
        species: Espece,
        preferencesRepository: PreferencesRepository,
        modifier: Modifier = Modifier
) {
        var currentPreferences by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }
        var isLoading by remember { mutableStateOf(true) }
        var isSaving by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                try {
                        preferencesRepository.loadPreferences()
                        currentPreferences = preferencesRepository.preferences
                } catch (e: Exception) {
                        currentPreferences = fr.vetbrain.vetnutri_mp.Data.PreferencesApplication()
                } finally {
                        isLoading = false
                }
        }

        if (isLoading) {
                Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = VetNutriColors.Primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chargement des préférences...")
                        }
                }
        } else {
                val speciesPrefs = currentPreferences?.getPreferencesEspece(species)
                val currentExpressionType =
                        speciesPrefs?.getTypeExpressionBesoinEnum() ?: TypeExpressionBesoin.DEFAULT

                LazyColumn(
                        modifier = modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                        item {
                                // En-tête avec informations sur l'espèce
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                        elevation = 2.dp
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        text =
                                                                "Configuration pour ${species.label}",
                                                        style = MaterialTheme.typography.h5,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text =
                                                                "Personnalisez l'expression des besoins et les nutriments à afficher pour cette espèce",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }

                        item {
                                // Section Expression des besoins
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = Color.White,
                                        elevation = 2.dp
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Default.Info,
                                                                contentDescription =
                                                                        "Expression des besoins",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = "Expression des besoins",
                                                                style = MaterialTheme.typography.h6,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                        text =
                                                                "Sélection actuelle: ${currentExpressionType.displayName}",
                                                        style = MaterialTheme.typography.subtitle1,
                                                        color = VetNutriColors.Primary,
                                                        fontWeight = FontWeight.Medium
                                                )

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Options de type d'expression
                                                TypeExpressionBesoin.values().forEach { type ->
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .selectable(
                                                                                        selected =
                                                                                                currentExpressionType ==
                                                                                                        type,
                                                                                        onClick = {
                                                                                                if (!isSaving &&
                                                                                                                currentExpressionType !=
                                                                                                                        type
                                                                                                ) {
                                                                                                        scope
                                                                                                                .launch {
                                                                                                                        try {
                                                                                                                                isSaving =
                                                                                                                                        true

                                                                                                                                currentPreferences
                                                                                                                                        ?.let {
                                                                                                                                                prefs
                                                                                                                                                ->
                                                                                                                                                val updatedSpeciesPrefs =
                                                                                                                                                        prefs.getPreferencesEspece(
                                                                                                                                                                        species
                                                                                                                                                                )
                                                                                                                                                                .copy(
                                                                                                                                                                        typeExpressionBesoinId =
                                                                                                                                                                                type.id
                                                                                                                                                                )
                                                                                                                                                val updatedPrefs =
                                                                                                                                                        prefs.updatePreferencesEspece(
                                                                                                                                                                updatedSpeciesPrefs
                                                                                                                                                        )

                                                                                                                                                preferencesRepository
                                                                                                                                                        .savePreferences(
                                                                                                                                                                updatedPrefs
                                                                                                                                                        )
                                                                                                                                                currentPreferences =
                                                                                                                                                        updatedPrefs

                                                                                                                                        }
                                                                                                                        } catch (
                                                                                                                                e:
                                                                                                                                        Exception) {
                                                                                                                        } finally {
                                                                                                                                isSaving =
                                                                                                                                        false
                                                                                                                        }
                                                                                                                }
                                                                                                }
                                                                                        }
                                                                                )
                                                                                .padding(
                                                                                        vertical =
                                                                                                8.dp
                                                                                ),
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                RadioButton(
                                                                        selected =
                                                                                currentExpressionType ==
                                                                                        type,
                                                                        onClick = null,
                                                                        enabled = !isSaving
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        12.dp
                                                                                )
                                                                )
                                                                Column(
                                                                        modifier =
                                                                                Modifier.weight(1f)
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        type.displayName,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body1,
                                                                                fontWeight =
                                                                                        if (currentExpressionType ==
                                                                                                        type
                                                                                        )
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                        else
                                                                                                FontWeight
                                                                                                        .Normal,
                                                                                color =
                                                                                        if (isSaving
                                                                                        )
                                                                                                Color.Gray
                                                                                        else
                                                                                                Color.Black
                                                                        )
                                                                        Text(
                                                                                text =
                                                                                        type.unitReqEnum
                                                                                                .label,
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray
                                                                        )
                                                                        Text(
                                                                                text =
                                                                                        "Unité: ${type.unitReqEnum.label}",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .caption,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                }
                                                                if (isSaving &&
                                                                                currentExpressionType ==
                                                                                        type
                                                                ) {
                                                                        CircularProgressIndicator(
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        ),
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                strokeWidth = 2.dp
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }

                        // Section Nutriments à afficher par catégorie
                        items(
                                fr.vetbrain.vetnutri_mp.Utils.NutrientUtils.getRelevantCategories()
                        ) { category ->
                                NutrientCategoryCard(
                                        category = category,
                                        currentPreferences = currentPreferences,
                                        species = species,
                                        isSaving = isSaving,
                                        onNutrientSelectionChanged = { updatedPrefs ->
                                                scope.launch {
                                                        try {
                                                                isSaving = true
                                                                preferencesRepository
                                                                        .savePreferences(
                                                                                updatedPrefs
                                                                        )
                                                                currentPreferences = updatedPrefs
                                                        } catch (e: Exception) {
                                                        } finally {
                                                                isSaving = false
                                                        }
                                                }
                                        }
                                )
                        }

                        item {
                                // Informations sur la persistance
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                                        elevation = 1.dp
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.CheckCircle,
                                                                contentDescription =
                                                                        "Persistance active",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text = "Sauvegarde automatique",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                fontWeight = FontWeight.Bold,
                                                                color = VetNutriColors.Primary
                                                        )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text =
                                                                "Toutes les modifications sont automatiquement sauvegardées et seront restaurées au prochain démarrage.",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }
                }
        }
}

@Composable
private fun NutrientCategoryCard(
        category: fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum,
        currentPreferences: fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?,
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        isSaving: Boolean,
        onNutrientSelectionChanged: (fr.vetbrain.vetnutri_mp.Data.PreferencesApplication) -> Unit
) {
        var expanded by remember { mutableStateOf(false) }
        val nutrients =
                fr.vetbrain.vetnutri_mp.Utils.NutrientUtils.getNutrientsForCategory(category)
        val speciesPrefs = currentPreferences?.getPreferencesEspece(species)

        Card(modifier = Modifier.fillMaxWidth(), backgroundColor = Color.White, elevation = 2.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                        // En-tête de la catégorie
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription =
                                                fr.vetbrain.vetnutri_mp.Utils.NutrientUtils
                                                        .getCategoryDisplayName(category),
                                        tint = VetNutriColors.Primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                text =
                                                        fr.vetbrain.vetnutri_mp.Utils.NutrientUtils
                                                                .getCategoryDisplayName(category),
                                                style = MaterialTheme.typography.h6,
                                                fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                                text =
                                                        fr.vetbrain.vetnutri_mp.Utils.NutrientUtils
                                                                .getCategoryDescription(category),
                                                style = MaterialTheme.typography.body2,
                                                color = Color.Gray
                                        )
                                }
                                IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                                imageVector =
                                                        if (expanded) Icons.Default.ExpandLess
                                                        else Icons.Default.ExpandMore,
                                                contentDescription =
                                                        if (expanded) "Réduire" else "Développer"
                                        )
                                }
                        }

                        // Contenu développable
                        if (expanded) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                // Affichage des nutriments
                                nutrients.forEach { nutrient ->
                                        val isSelected =
                                                speciesPrefs?.isNutrientSelected(
                                                        category.name,
                                                        nutrient.coef
                                                ) == true

                                        Row(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .clickable(enabled = !isSaving) {
                                                                        currentPreferences?.let {
                                                                                prefs ->
                                                                                val currentSpeciesPrefs =
                                                                                        prefs.getPreferencesEspece(
                                                                                                species
                                                                                        )
                                                                                val updatedSpeciesPrefs =
                                                                                        if (isSelected
                                                                                        ) {
                                                                                                currentSpeciesPrefs
                                                                                                        .removeNutrient(
                                                                                                                category,
                                                                                                                nutrient.coef
                                                                                                        )
                                                                                        } else {
                                                                                                currentSpeciesPrefs
                                                                                                        .addNutrient(
                                                                                                                category,
                                                                                                                nutrient.coef
                                                                                                        )
                                                                                        }
                                                                                val updatedPrefs =
                                                                                        prefs.updatePreferencesEspece(
                                                                                                updatedSpeciesPrefs
                                                                                        )
                                                                                onNutrientSelectionChanged(
                                                                                        updatedPrefs
                                                                                )
                                                                        }
                                                                }
                                                                .padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Checkbox(
                                                        checked = isSelected,
                                                        onCheckedChange =
                                                                null, // Géré par le clickable du
                                                        // Row
                                                        enabled = !isSaving
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                                text = nutrient.displayName,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                color =
                                                                        if (isSaving) Color.Gray
                                                                        else Color.Black
                                                        )
                                                        Text(
                                                                text = "Unité: ${nutrient.unite}",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }

                                // Résumé de sélection
                                val selectedCount =
                                        nutrients.count { nutrient ->
                                                speciesPrefs?.isNutrientSelected(
                                                        category.name,
                                                        nutrient.coef
                                                ) == true
                                        }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        text =
                                                "$selectedCount/${nutrients.size} nutriments sélectionnés",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Primary,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                }
        }
}
 