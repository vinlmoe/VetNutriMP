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
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

@Composable
fun SpeciesPreferencesView(
        species: Espece,
        preferencesRepository: PreferencesRepository,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository,
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
                                Text("preferences.title".translate() + "...")
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
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1),
                                        elevation = 2.dp
                                ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        text =
                                                                "${"preferences.speciesTitle".translate()} ${species.translateEnum()}",
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
                                                                                                                                        Exception) {} finally {
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
                                                                                Modifier.weight(1.0)
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
                                                                                                .translateEnum(),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color = Color.Gray
                                                                        )
                                                                        Text(
                                                                                text =
                                                                                        "Unité: ${type.unitReqEnum.translateEnum()}",
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
                                                        } catch (e: Exception) {} finally {
                                                                isSaving = false
                                                        }
                                                }
                                        }
                                )
                        }

                        item {
                                // Section Équations pour nutriments complémentaires
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
                                                                        "Équations complémentaires",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                                text =
                                                                        "Équations de nutriments complémentaires",
                                                                style = MaterialTheme.typography.h6,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                        text =
                                                                "Sélectionnez les équations pour calculer les nutriments complémentaires pour cette espèce",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))

                                                ComplementaryNutrientEquationsSection(
                                                        species = species,
                                                        currentPreferences = currentPreferences,
                                                        equationRepository = equationRepository,
                                                        isSaving = isSaving,
                                                        onEquationSelectionChanged = { updatedPrefs
                                                                ->
                                                                scope.launch {
                                                                        try {
                                                                                isSaving = true
                                                                                println(
                                                                                        "🔍 PERSISTANCE: Début sauvegarde - isSaving = true"
                                                                                )

                                                                                val speciesPrefsToSave =
                                                                                        updatedPrefs
                                                                                                .getPreferencesEspece(
                                                                                                        species
                                                                                                )
                                                                                println(
                                                                                        "🔍 PERSISTANCE: Préférences à sauvegarder pour $species: ${speciesPrefsToSave.equationsComplementaires}"
                                                                                )

                                                                                preferencesRepository
                                                                                        .savePreferences(
                                                                                                updatedPrefs
                                                                                        )
                                                                                println(
                                                                                        "🔍 PERSISTANCE: Sauvegarde repository terminée"
                                                                                )

                                                                                // Recharger les
                                                                                // préférences
                                                                                // depuis le
                                                                                // repository pour
                                                                                // s'assurer de la
                                                                                // cohérence
                                                                                preferencesRepository
                                                                                        .loadPreferences()
                                                                                val reloadedPrefs =
                                                                                        preferencesRepository
                                                                                                .preferences

                                                                                currentPreferences =
                                                                                        reloadedPrefs
                                                                                println(
                                                                                        "🔍 PERSISTANCE: currentPreferences rechargées depuis le repository"
                                                                                )

                                                                                // Vérifier que les
                                                                                // préférences sont
                                                                                // bien mises à jour
                                                                                val verificationPrefs =
                                                                                        currentPreferences
                                                                                                ?.getPreferencesEspece(
                                                                                                        species
                                                                                                )
                                                                                println(
                                                                                        "🔍 PERSISTANCE: Vérification après sauvegarde: ${verificationPrefs?.equationsComplementaires}"
                                                                                )
                                                                        } catch (e: Exception) {
                                                                                println(
                                                                                        "❌ PERSISTANCE: Erreur lors de la sauvegarde: ${e.message}"
                                                                                )
                                                                                e.printStackTrace()
                                                                        } finally {
                                                                                isSaving = false
                                                                                println(
                                                                                        "🔍 PERSISTANCE: Fin sauvegarde - isSaving = false"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                )
                                        }
                                }
                        }

                        item {
                                // Informations sur la persistance
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1),
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

/** Section pour la gestion des équations complémentaires spécifiques à une espèce */
@Composable
private fun ComplementaryNutrientEquationsSection(
        species: fr.vetbrain.vetnutri_mp.Enumer.Espece,
        currentPreferences: fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?,
        equationRepository: fr.vetbrain.vetnutri_mp.Repository.EquationRepository,
        isSaving: Boolean,
        onEquationSelectionChanged: (fr.vetbrain.vetnutri_mp.Data.PreferencesApplication) -> Unit
) {
        var allEquations by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Data.Equation>>(emptyList())
        }

        // Charger seulement les équations de nutriments complémentaires pour cette espèce ou
        // globales (CH)
        LaunchedEffect(Unit) {
                val allEquationsFromRepo = equationRepository.getAllEquations()
                println("🔍 PERSISTANCE: ${allEquationsFromRepo.size} équations totales chargées")

                allEquations =
                        allEquationsFromRepo.filter { equation ->
                                val isComplementary =
                                        equation.kind ==
                                                fr.vetbrain.vetnutri_mp.Enumer.EquationKind
                                                        .COMPLEMENTARY_NUTRIENT
                                val isCorrectSpecies =
                                        equation.specie == species ||
                                                equation.specie ==
                                                        fr.vetbrain.vetnutri_mp.Enumer.Espece.CH

                                isComplementary && isCorrectSpecies
                        }

                println(
                        "🔍 PERSISTANCE: ${allEquations.size} équations COMPLEMENTARY_NUTRIENT trouvées pour $species"
                )
                allEquations.forEach { eq ->
                        println("  - ${eq.name} (${eq.uuid}) - nutrient: ${eq.nutrient?.label}")
                }

                // Vérifier les préférences actuelles
                currentPreferences?.getPreferencesEspece(species)?.let { prefs ->
                        val selectedUuids = prefs.getSelectedEquationUuids()
                        println(
                                "🔍 PERSISTANCE: ${selectedUuids.size} équations sélectionnées dans les préférences: $selectedUuids"
                        )
                        val map = prefs.equationsComplementaires
                        println("🔍 PERSISTANCE: Map complete: $map")
                }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
                // Liste de toutes les équations avec checkboxes
                allEquations.forEach { equation ->
                        val speciesPrefs = currentPreferences?.getPreferencesEspece(species)
                        val isSelected = speciesPrefs?.isEquationSelected(equation.uuid) == true

                        println(
                                "🔍 PERSISTANCE: Affichage équation ${equation.name} (${equation.uuid}) - isSelected: $isSelected"
                        )

                        Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                                println(
                                                        "EQDBG PREF species=${species.name} equation=${equation.uuid} (${equation.name}) checked=$checked"
                                                )

                                                currentPreferences?.let { prefs ->
                                                        println(
                                                                "🔍 PERSISTANCE: Préférences actuelles trouvées"
                                                        )
                                                        val currentSpeciesPrefs =
                                                                prefs.getPreferencesEspece(species)
                                                        println(
                                                                "🔍 PERSISTANCE: Préférences espèce $species avant modification: ${currentSpeciesPrefs.equationsComplementaires}"
                                                        )

                                                        val updatedSpeciesPrefs =
                                                                if (checked) {
                                                                        println(
                                                                                "EQDBG PREF add uuid=${equation.uuid} for species=${species.name}"
                                                                        )
                                                                        currentSpeciesPrefs
                                                                                .addEquation(
                                                                                        equation.uuid
                                                                                )
                                                                } else {
                                                                        println(
                                                                                "EQDBG PREF remove uuid=${equation.uuid} for species=${species.name}"
                                                                        )
                                                                        currentSpeciesPrefs
                                                                                .removeEquation(
                                                                                        equation.uuid
                                                                                )
                                                                }

                                                        println(
                                                                "🔍 PERSISTANCE: Préférences espèce après modification: ${updatedSpeciesPrefs.equationsComplementaires}"
                                                        )

                                                        val updatedPrefs =
                                                                prefs.updatePreferencesEspece(
                                                                        updatedSpeciesPrefs
                                                                )

                                                        println(
                                                                "EQDBG PREF call onEquationSelectionChanged for species=${species.name}"
                                                        )
                                                        onEquationSelectionChanged(updatedPrefs)
                                                }
                                        },
                                        enabled = !isSaving
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(modifier = Modifier.weight(1.0)) {
                                        Text(
                                                text = equation.name,
                                                style = MaterialTheme.typography.body1,
                                                fontWeight =
                                                        if (isSelected) FontWeight.Bold
                                                        else FontWeight.Normal,
                                                color =
                                                        if (isSelected) VetNutriColors.Primary
                                                        else Color.Black
                                        )

                                        // Afficher le nutriment concerné entre parenthèses
                                        val nutrientName = getNutrientNameForEquation(equation)
                                        if (nutrientName != null) {
                                                Text(
                                                        text = "($nutrientName)",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }
                }
        }
}

/** Retourne le nom du nutriment concerné par une équation */
private fun getNutrientNameForEquation(equation: fr.vetbrain.vetnutri_mp.Data.Equation): String? {
        return equation.nutrient?.label
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
                                Column(modifier = Modifier.weight(1.0)) {
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
                                                Column(modifier = Modifier.weight(1.0)) {
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
