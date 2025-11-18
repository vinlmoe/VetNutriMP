package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.convertirPreferencesVersLabelsNutriments
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.EquationEvaluator
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.MultiNutrientAdjustmentView
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrientDetailDialog
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionAlimentsRation
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionBilanEnergetique
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionCoefficients
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionValeursMetaboliques
import fr.vetbrain.vetnutri_mp.View.components.RecipeDialog
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

// Suppression du calcul local de DE : on utilisera EquationEvaluator avec nutriments
// complémentaires

// Fonction locale InfoRow pour éviter les problèmes d'import
@Composable
private fun LocalInfoRow(label: String, value: String) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = AppSizes.paddingXSmall),
                horizontalArrangement = Arrangement.Start
        ) {
                Text(
                        text = "$label :",
                        style = MaterialTheme.typography.subtitle1,
                        modifier = Modifier.width(120.dp)
                )
                Text(text = value, style = MaterialTheme.typography.body1)
        }
}

/**
 * Vue pour afficher les rations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param showSnackbar Action à exécuter pour afficher un message snackbar
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun RationsView(
        viewModel: AnimalDetailViewModel,
        showSnackbar: (String) -> Unit,
        modifier: Modifier = Modifier,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val availableReferences by viewModel.availableReferences.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // Résolution centralisée des références maladies sélectionnées + logs
        val referencesMaladiesResolues =
                remember(selectedConsultation?.referencesMaladies, availableReferences) {
                        val ids = selectedConsultation?.referencesMaladies ?: emptyList()

                        val resolved =
                                ids.mapNotNull { id ->
                                        availableReferences.firstOrNull { it.uuid == id }
                                }
                        if (resolved.isEmpty()) {
                                if (ids.isNotEmpty()) {} else {}
                        } else {}
                        resolved
                }

        // Récupération des valeurs métaboliques calculées
        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()
        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
        // Le BE final devient la seule valeur de BE utilisée dans la vue
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()

        // L'énergie apportée sera calculée plus bas après chargement des préférences
        var energieApportee by remember { mutableStateOf(0.0) }

        // Calcul du K calculé (produit de tous les coefficients K + coefficient d'ajustement)
        val kCalcule =
                remember(selectedConsultation) {
                        selectedConsultation?.let { consultation ->
                                val k1 = consultation.k1Value ?: 1.0
                                val k2 = consultation.k2Value ?: 1.0
                                val k3 = consultation.k3Value ?: 1.0
                                val k4 = consultation.k4Value ?: 1.0
                                val k5 = consultation.k5Value ?: 1.0
                                val coeffAjustement = consultation.coefficientAjustement ?: 1.0
                                (k1 * k2 * k3 * k4 * k5) * coeffAjustement
                        }
                                ?: 1.0
                }

        // Système de préférences pour le filtrage des nutriments
        val preferencesStorage: PreferencesStorage = remember { createPreferencesStorage() }
        val preferencesRepository: PreferencesRepository = remember { PreferencesRepository(preferencesStorage) }
        var preferencesApplication by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null) }
        LaunchedEffect(Unit) {
                preferencesRepository.loadPreferences()
                preferencesApplication = preferencesRepository.preferences
        }

        // États pour énergie additionnelle (patho) et BE total (final)
        var energieAdditionnelle by remember { mutableStateOf(0.0) }
        var besoinEnergetiqueTotal by remember { mutableStateOf<Double?>(null) }

        // Calcul du BE après K et de l'énergie additionnelle, puis du BE total final
        val beApresK = remember(besoinEnergetiqueStandard, kCalcule) {
                besoinEnergetiqueStandard?.let { beeVal -> beeVal * kCalcule }
        }

        LaunchedEffect(
                selectedConsultation,
                referenceUtilisee,
                referencesMaladiesResolues,
                preferencesApplication,
                poidsMetabolique,
                besoinEnergetiqueStandard,
                selectedRation
        ) {
                val consultation = selectedConsultation
                val ration = selectedRation
                val prefsApp = preferencesApplication
                val mw = poidsMetabolique
                val bee = besoinEnergetiqueStandard
                val beK = beApresK
                val maladies = referencesMaladiesResolues
                if (consultation != null && ration != null && prefsApp != null && mw != null && bee != null && beK != null) {
                        try {
                        } catch (_: Throwable) {}
                        val prefsEspece = animal?.getEspece()?.let { prefsApp.getPreferencesEspece(it) }
                        val add = EquationEvaluator.calculerEnergieAdditionnelle(
                                referencesMaladies = maladies,
                                poidsCorps = consultation.effectiveWeight?.toDouble() ?: consultation.weight?.toDouble() ?: 0.0,
                                besoinEnergetiqueApresK = beK,
                                besoinEnergetiqueStandard = bee,
                                poidsMetabolique = mw,
                                variablesSupp = consultation.suppVarp,
                                ration = ration,
                                preferences = prefsEspece,
                                equationRepository = equationRepository
                        )
                        energieAdditionnelle = add
                        besoinEnergetiqueTotal = beK.let { it + add }
                        try {
                        } catch (_: Throwable) {}
                } else {
                        energieAdditionnelle = 0.0
                        besoinEnergetiqueTotal = beK
                }
        }

        // Calcul du pourcentage de couverture avec le BE total final
        val pourcentageCouverture =
                remember(energieApportee, besoinEnergetiqueTotal) {
                        besoinEnergetiqueTotal?.let { besoin ->
                                if (besoin > 0) (energieApportee / besoin) * 100.0 else 0.0
                        }
                                ?: 0.0
                }

        // Calcul du K Observé avec le besoin énergétique de référence
        val kObserve =
                remember(energieApportee, besoinEnergetiqueStandard) {
                        besoinEnergetiqueStandard?.let { besoin ->
                                if (besoin > 0) energieApportee / besoin else 0.0
                        }
                                ?: 0.0
                }

        // Calcul de l'énergie totale apportée par la ration sélectionnée (avec nutriments
        // complémentaires)
        val energieKey = listOf(selectedRation, referenceUtilisee, preferencesApplication, animal)
        LaunchedEffect(energieKey) {
                val rationActuelle = selectedRation
                val reference = referenceUtilisee
                val prefsApp = preferencesApplication
                val animalActuel = animal

                energieApportee =
                        if (rationActuelle != null &&
                                        reference != null &&
                                        prefsApp != null &&
                                        animalActuel != null
                        ) {
                                val prefsEspece =
                                        prefsApp.getPreferencesEspece(animalActuel.getEspece())
                                // Utiliser getDensiteEnergetiqueMoyenne() qui utilise les nouvelles
                                // fonctions d'énergie
                                // et multiplier par la quantité totale pour obtenir l'énergie
                                // totale
                                val densiteEnergetique =
                                        rationActuelle.getDensiteEnergetiqueMoyenne(
                                                referenceEv = reference,
                                                equationRepository = equationRepository
                                        )
                                densiteEnergetique * rationActuelle.getQuantiteTotale()
                        } else 0.0
        }

        // États pour les dialogues et navigation
        var showRationEditDialog by remember { mutableStateOf(false) }
        var showAddAlimentView by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }
        var rationForAddAliment by remember { mutableStateOf<Ration?>(null) }
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        // Scope pour les coroutines locales dans le composable
        val coroutineScope = rememberCoroutineScope()

        // États pour les dialogues de section agrandie
        var showMetabolicValuesDialog by remember { mutableStateOf(false) }
        var showCoefficientsDialog by remember { mutableStateOf(false) }

        // États pour le dialog détaillé de nutriment
        var showNutrimentDetailDialog by remember { mutableStateOf(false) }
        var selectedNutrimentData by remember {
                mutableStateOf<Triple<String, ValeurNutritionnelle, Ration>?>(null)
        }

        // État pour le dialog d'ajustement multi-nutriments
        var showMultiNutrientAdjustmentDialog by remember { mutableStateOf(false) }
        var showRecipeDialog by remember { mutableStateOf(false) }
        var showSaveRecipeDialog by remember { mutableStateOf(false) }
        var newRecipeName by remember { mutableStateOf("") }

        // État pour le dialog de confirmation de suppression de ration
        var showDeleteRationDialog by remember { mutableStateOf(false) }
        var rationToDelete by remember { mutableStateOf<Ration?>(null) }

        // Dialog de gestion des recettes
        if (showRecipeDialog) {
                RecipeDialog(
                        repository = recipeRepository,
                        foodRepository = viewModel.foodRepository,
                        onApply = { recette ->
                                viewModel.applyRecipeToRation(recette)
                                showRecipeDialog = false
                        },
                        onClose = { showRecipeDialog = false }
                )
        }

        if (showSaveRecipeDialog) {
                AlertDialog(
                        onDismissRequest = { showSaveRecipeDialog = false },
                        title = { Text("Créer une recette") },
                        text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                                value = newRecipeName,
                                                onValueChange = { newRecipeName = it },
                                                label = { Text("Nom de la recette") },
                                                modifier = Modifier.fillMaxWidth()
                                        )
                                        val especeLabel = selectedRation?.espece ?: ""
                                        if (especeLabel.isNotEmpty()) {
                                                Text(
                                                        "Espèce: $especeLabel",
                                                        style = MaterialTheme.typography.body2
                                                )
                                        }
                                }
                        },
                        confirmButton = {
                                Button(
                                        onClick = {
                                                val ration = selectedRation
                                                if (ration != null && newRecipeName.isNotBlank()) {
                                                        coroutineScope.launch {
                                                                try {
                                                                        val recette =
                                                                                recipeRepository
                                                                                        .createRecipe(
                                                                                                name =
                                                                                                        newRecipeName,
                                                                                                espece =
                                                                                                        ration.espece,
                                                                                                description =
                                                                                                        null
                                                                                        )
                                                                        recipeRepository
                                                                                .replaceAliments(
                                                                                        recette.uuid,
                                                                                        ration.alimentMutableList
                                                                                )
                                                                        showSnackbar(
                                                                                "Recette créée: $newRecipeName"
                                                                        )
                                                                } catch (err: Exception) {
                                                                        showSnackbar(
                                                                                "Erreur: ${err.message ?: "création recette"}"
                                                                        )
                                                                } finally {
                                                                        showSaveRecipeDialog = false
                                                                }
                                                        }
                                                }
                                        }
                                ) { Text("Créer") }
                        },
                        dismissButton = {
                                OutlinedButton(onClick = { showSaveRecipeDialog = false }) {
                                        Text("Annuler")
                                }
                        }
                )
        }

        // Afficher la vue d'ajout d'aliment si nécessaire
        if (showAddAlimentView && rationForAddAliment != null) {
                AddAlimentView(
                        viewModel = viewModel,
                        ration = rationForAddAliment!!,
                        onNavigateBack = {
                                showAddAlimentView = false
                                rationForAddAliment = null
                        },
                        equationRepository = equationRepository,
                        onAddAliment = { aliment, quantite ->
                                // Ajout asynchrone pour garantir la version complète de l'aliment
                                coroutineScope.launch {
                                        val alimentComplet =
                                                viewModel.getAlimentComplet(aliment.uuid)
                                        if (alimentComplet != null) {
                                                selectedConsultation?.let { consultation ->
                                                        // Créer un nouvel AlimentRation
                                                        val newAlimentRation =
                                                                AlimentRation(
                                                                        refAlimUnif =
                                                                                alimentComplet.uuid,
                                                                        quantite = quantite,
                                                                        aliment = alimentComplet,
                                                                        refRation =
                                                                                rationForAddAliment!!
                                                                                        .uuid
                                                                )

                                                        // Créer une copie de la liste des aliments
                                                        // de la ration
                                                        val updatedAliments =
                                                                rationForAddAliment!!
                                                                        .alimentMutableList
                                                                        .toMutableList()
                                                        updatedAliments.add(newAlimentRation)

                                                        // Créer une ration mise à jour
                                                        val updatedRation =
                                                                rationForAddAliment!!.copy(
                                                                        alimentMutableList =
                                                                                updatedAliments
                                                                )

                                                        // Mettre à jour la consultation avec la
                                                        // ration modifiée
                                                        val updatedRations =
                                                                consultation.rations.toMutableList()
                                                        val rationIndex =
                                                                updatedRations.indexOfFirst {
                                                                        it.uuid ==
                                                                                rationForAddAliment!!
                                                                                        .uuid
                                                                }
                                                        if (rationIndex >= 0) {
                                                                updatedRations[rationIndex] =
                                                                        updatedRation

                                                                val updatedConsultation =
                                                                        consultation.copy(
                                                                                rations =
                                                                                        updatedRations
                                                                        )

                                                                // Sauvegarder la consultation mise
                                                                // à jour
                                                                viewModel.updateConsultation(
                                                                        updatedConsultation
                                                                )

                                                                // Sélectionner la ration mise à
                                                                // jour
                                                                viewModel.selectRation(
                                                                        updatedRation
                                                                )

                                                                // Mettre à jour la référence locale
                                                                // pour les ajouts suivants
                                                                rationForAddAliment =
                                                                        updatedRation
                                                        }
                                                }

                                                showSnackbar(
                                                        "Aliment '${alimentComplet.nom}' ajouté à la ration (${quantite}g)"
                                                )
                                        } else {
                                                showSnackbar(
                                                        "Erreur : aliment non trouvé dans la base complète"
                                                )
                                        }
                                        // Ne pas fermer la vue pour permettre l'ajout multiple
                                        // d'aliments
                                }
                        },
                        modifier = modifier
                )
        } else {
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                        if (selectedConsultation == null) {
                                Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                ) { Text("Sélectionnez une consultation pour voir les rations") }
                        } else {
                                // En-tête avec nom de la consultation
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = AppSizes.elevationSmall
                                ) {
                                        Column(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .padding(AppSizes.paddingMedium),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingXSmall)
                                        ) {
                                                Text(
                                                        text =
                                                                "Consultation du " +
                                                                        (selectedConsultation?.date
                                                                                ?: ""),
                                                        style =
                                                                MaterialTheme.typography
                                                                        .subtitle1, // taille
                                                        // réduite
                                                        color = VetNutriColors.Primary
                                                )
                                                if (!selectedConsultation?.objectConsult
                                                                .isNullOrBlank()
                                                ) {
                                                        Text(
                                                                text =
                                                                        selectedConsultation
                                                                                ?.objectConsult
                                                                                ?: "",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                }
                                        }
                                }
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                // Section responsive dans une Card
                                BoxWithConstraints(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(AppSizes.paddingMedium)
                                ) {
                                        val isCompact = maxWidth < 600.dp // seuil abaissé à 600
                                        if (isCompact) {
                                                // Vue compacte : une seule colonne, scroll global
                                                Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingSmall
                                                                )
                                                ) {
                                                        // Section 1: Valeurs métaboliques
                                                        SectionValeursMetaboliques(
                                                                selectedConsultation =
                                                                        selectedConsultation,
                                                                poidsMetabolique = poidsMetabolique,
                                                                besoinEnergetiqueStandard =
                                                                        besoinEnergetiqueStandard,
                                                                besoinEnergetiqueTotal =
                                                                        besoinEnergetiqueTotal,
                                                                kObserve = kObserve,
                                                                kCalcule = kCalcule,
                                                               energieAdditionnelle = energieAdditionnelle,
                                                                referenceUtilisee = referenceUtilisee,
                                                                onExpand = {
                                                                        showMetabolicValuesDialog =
                                                                                true
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Divider()

                                                        // Section 2: Coefficients
                                                        SectionCoefficients(
                                                                selectedConsultation =
                                                                        selectedConsultation,
                                                                showCoefficientsDialog = {
                                                                        showCoefficientsDialog =
                                                                                true
                                                                },
                                                                viewModel = viewModel,
                                                                modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Divider()

                                                        // Section 3: Bilan énergétique
                                                        SectionBilanEnergetique(
                                                                energieApportee = energieApportee,
                                                                pourcentageCouverture =
                                                                        pourcentageCouverture,
                                                                kObserve = kObserve,
                                                                kCalcule = kCalcule,
                                                        
                                                                beFinal = besoinEnergetiqueTotal,
                                                                modifier = Modifier.fillMaxWidth()
                                                        )
                                                        Divider()

                                                        // Section 4: Liste des rations
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation =
                                                                        AppSizes.elevationMedium,
                                                                backgroundColor =
                                                                        MaterialTheme.colors.surface
                                                        ) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                AppSizes.paddingSmall
                                                                                        )
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Rations de la consultation",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle2,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                Icon(
                                                                                        imageVector =
                                                                                                Icons.Filled
                                                                                                        .Add,
                                                                                        contentDescription =
                                                                                                "Ajouter une ration",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Primary,
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                                AppSizes.iconSizeXSmall
                                                                                                        )
                                                                                                        .clickable(
                                                                                                                onClick = {
                                                                                                                        rationToEdit =
                                                                                                                                null
                                                                                                                        showRationEditDialog =
                                                                                                                                true
                                                                                                                }
                                                                                                        )
                                                                                )
                                                                        }
                                                                        Divider()
                                                                        if (selectedConsultation
                                                                                        ?.rations
                                                                                        .isNullOrEmpty()
                                                                        ) {
                                                                                CenteredMessage(
                                                                                        message =
                                                                                                "Aucune ration disponible",
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                )
                                                                        } else {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                8.dp
                                                                                                        )
                                                                                ) {
                                                                                        selectedConsultation
                                                                                                ?.rations
                                                                                                ?.forEach {
                                                                                                        ration
                                                                                                        ->
                                                                                                        RationItem(
                                                                                                                ration =
                                                                                                                        ration,
                                                                                                                isSelected =
                                                                                                                        ration.uuid ==
                                                                                                                                selectedRation
                                                                                                                                        ?.uuid,
                                                                                                                onClick = {
                                                                                                                        viewModel
                                                                                                                                .selectRation(
                                                                                                                                        ration
                                                                                                                                )
                                                                                                                },
                                                                                                                onEdit = {
                                                                                                                        rationToEdit =
                                                                                                                                ration
                                                                                                                        showRationEditDialog =
                                                                                                                                true
                                                                                                                },
                                                                                                                onDuplicate = {
                                                                                                                        viewModel
                                                                                                                                .duplicateRation(
                                                                                                                                        ration
                                                                                                                                )
                                                                                                                        showSnackbar(
                                                                                                                                "Ration '${ration.name}' dupliquée"
                                                                                                                        )
                                                                                                                },
                                                                                                                onDelete = {
                                                                                                                        rationToDelete =
                                                                                                                                ration
                                                                                                                        showDeleteRationDialog =
                                                                                                                                true
                                                                                                                }
                                                                                                        )
                                                                                                }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                        Divider()

                                                        // Section 5: Liste des aliments de la
                                                        // ration sélectionnée
                                                        if (selectedRation != null) {
                                                                SectionAlimentsRation(
                                                                        selectedRation = selectedRation,
                                                                        referenceUtilisee =
                                                                                referenceUtilisee,
                                                                        besoinEnergetiqueTotal =
                                                                                besoinEnergetiqueTotal,
                                                                        besoinEnergetiqueStandard =
                                                                                besoinEnergetiqueStandard,
                                                                        viewModel = viewModel,
                                                                        equationRepository = equationRepository,
                                                                        onAddAliment = {
                                                                                rationForAddAliment =
                                                                                        selectedRation
                                                                                showAddAlimentView =
                                                                                        true
                                                                        },
                                                                        onMultiNutrientAdjustment = {
                                                                                showMultiNutrientAdjustmentDialog =
                                                                                        true
                                                                        },
                                                                        onOpenRecipeDialog = {
                                                                                showRecipeDialog = true
                                                                        },
                                                                        onSaveRecipe = {
                                                                                selectedRation?.let { r ->
                                                                                        newRecipeName =
                                                                                                r.name
                                                                                        showSaveRecipeDialog =
                                                                                                true
                                                                                }
                                                                        },
                                                                        showSnackbar = showSnackbar,
                                                                        isCompact = isCompact,
                                                                        modifier = Modifier.fillMaxWidth()
                                                                )
                                                        } else {
                                                                Card(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        elevation = AppSizes.elevationMedium
                                                                ) {
                                                                        Box(
                                                                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                                                                contentAlignment = Alignment.Center
                                                                        ) {
                                                                                Text(
                                                                                        "Sélectionnez une ration pour voir les aliments",
                                                                                        style = MaterialTheme.typography.body1,
                                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                                )
                                                                        }
                                                                }
                                                        }

                                                        // Section 6: Analyse nutritionnelle (si une
                                                        // ration est sélectionnée)
                                                        if (selectedRation != null) {
                                                                Divider()
                                                                if (referenceUtilisee != null) {
                                                                        // Obtenir les nutriments
                                                                        // sélectionnés selon l'espèce avec
                                                                        // logs
                                                                        val nutrimentsSelectionnes =
                                                                                remember(
                                                                                        animal,
                                                                                        preferencesApplication
                                                                                ) {
                                                                                        val animalActuel =
                                                                                                animal
                                                                                        val prefsApp =
                                                                                                preferencesApplication
                                                                                        if (animalActuel !=
                                                                                                        null &&
                                                                                                        prefsApp !=
                                                                                                                null
                                                                                        ) {
                                                                                                val especeAnimal =
                                                                                                        animalActuel
                                                                                                                .getEspece()
                                                                                                val preferencesEspece =
                                                                                                        prefsApp.getPreferencesEspece(
                                                                                                                especeAnimal
                                                                                                        )
                                                                                                val nutrimentsLabels =
                                                                                                        convertirPreferencesVersLabelsNutriments(
                                                                                                                preferencesEspece
                                                                                                        )
                                                                                                if (nutrimentsLabels
                                                                                                                .isNotEmpty()
                                                                                                ) {
                                                                                                        nutrimentsLabels
                                                                                                } else {
                                                                                                        listOf(
                                                                                                                "PROTEINE",
                                                                                                                "LIPIDE",
                                                                                                                "ENA",
                                                                                                                "CELLULOSE",
                                                                                                                "CENDRE",
                                                                                                                "CAL",
                                                                                                                "PHOS"
                                                                                                        )
                                                                                                }
                                                                                        } else {
                                                                                                listOf(
                                                                                                        "PROTEINE",
                                                                                                        "LIPIDE",
                                                                                                        "ENA",
                                                                                                        "CELLULOSE",
                                                                                                        "CENDRE",
                                                                                                        "CAL",
                                                                                                        "PHOS",
                                                                                                        "FE",
                                                                                                        "ZN",
                                                                                                        "CU",
                                                                                                        "MN",
                                                                                                        "I",
                                                                                                        "SE",
                                                                                                        "NA",
                                                                                                        "K",
                                                                                                        "MG",
                                                                                                        "CHL",
                                                                                                        "VITA",
                                                                                                        "VITD",
                                                                                                        "VITE",
                                                                                                        "VITB1",
                                                                                                        "VITB2",
                                                                                                        "VITB3",
                                                                                                        "VITB5",
                                                                                                        "VITB6",
                                                                                                        "VITB8",
                                                                                                        "VITB9",
                                                                                                        "VITB12",
                                                                                                        "O3",
                                                                                                        "O6",
                                                                                                        "AG205",
                                                                                                        "AG226",
                                                                                                        "EPADHA",
                                                                                                        "AG60",
                                                                                                        "AG80",
                                                                                                        "AG100",
                                                                                                        "LYSINE",
                                                                                                        "METHIONINE",
                                                                                                        "TRYPTOPHANE",
                                                                                                        "CAP",
                                                                                                        "O6O3",
                                                                                                        "KNA",
                                                                                                        "ZNCU",
                                                                                                        "TAURINE",
                                                                                                        "CARNITINE"
                                                                                                )
                                                                                        }
                                                                                }

                                                                        // Obtenir le type d'expression
                                                                        // selon l'espèce
                                                                        val typeExpressionBesoin =
                                                                                remember(
                                                                                        animal,
                                                                                        preferencesApplication
                                                                                ) {
                                                                                        val animalActuel =
                                                                                                animal
                                                                                        val prefsApp =
                                                                                                preferencesApplication
                                                                                        if (animalActuel !=
                                                                                                        null &&
                                                                                                        prefsApp !=
                                                                                                                null
                                                                                        ) {
                                                                                                val especeAnimal =
                                                                                                        animalActuel
                                                                                                                .getEspece()
                                                                                                val preferencesEspece =
                                                                                                        prefsApp.getPreferencesEspece(
                                                                                                                especeAnimal
                                                                                                        )
                                                                                                preferencesEspece
                                                                                                        .typeExpressionBesoinId
                                                                                        } else {
                                                                                                0 // Par
                                                                                                // défaut
                                                                                        }
                                                                                }

                                                                        // Utiliser la version existante de
                                                                        // AnalyseNutritionnelleCard
                                                                        AnalyseNutritionnelleCard(
                                                                                ration = selectedRation!!,
                                                                                poidsMetabolique =
                                                                                        poidsMetabolique,
                                                                                referenceUtilisee =
                                                                                        referenceUtilisee,
                                                                                besoinEnergetiqueEntretien =
                                                                                        besoinEnergetiqueStandard,
                                                                                poidsAnimal =
                                                                                        selectedConsultation
                                                                                                ?.weight
                                                                                                ?.toDouble(),
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                nutrimentsSelectionnes =
                                                                                        nutrimentsSelectionnes,
                                                                                onNutrimentClick = {
                                                                                        nom,
                                                                                        valeurNutritionnelle
                                                                                        ->
                                                                                        selectedNutrimentData =
                                                                                                Triple(
                                                                                                        nom,
                                                                                                        valeurNutritionnelle,
                                                                                                        selectedRation!!
                                                                                                )
                                                                                        showNutrimentDetailDialog =
                                                                                                true
                                                                                },
                                                                                animal = animal,
                                                                                preferencesRepository =
                                                                                        preferencesRepository,
                                                                                equationRepository =
                                                                                        equationRepository,
                                                                                // Utiliser les préférences
                                                                                // pré-chargées du ViewModel
                                                                                typeExpressionBesoin =
                                                                                        viewModel
                                                                                                .typeExpressionBesoin
                                                                                                .collectAsState()
                                                                                                .value,
                                                                                isLargeView = !isCompact,
                                                                                referencesMaladies =
                                                                                        referencesMaladiesResolues
                                                                        )
                                                                } else {
                                                                        Card(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                elevation = AppSizes.elevationMedium
                                                                        ) {
                                                                                Box(
                                                                                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                                                                        contentAlignment = Alignment.Center
                                                                                ) {
                                                                                        Text(
                                                                                                "Sélectionner une référence dans la consultation pour permettre l'analyse",
                                                                                                style = MaterialTheme.typography.body1,
                                                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        } else {
                                                if (selectedConsultation == null) {
                                                        Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                        ) { 
                                                                Text("Sélectionnez une consultation pour voir les rations") 
                                                        }
                                                } else {
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        ) {
                                                                SectionValeursMetaboliques(
                                                                        selectedConsultation =
                                                                                selectedConsultation,
                                                                        poidsMetabolique = poidsMetabolique,
                                                                        besoinEnergetiqueStandard =
                                                                                besoinEnergetiqueStandard,
                                                                        besoinEnergetiqueTotal =
                                                                                besoinEnergetiqueTotal,
                                                                        kObserve = kObserve,
                                                                        kCalcule = kCalcule,
                                                                         energieAdditionnelle = energieAdditionnelle,
                                                                        referenceUtilisee = referenceUtilisee,
                                                                        onExpand = {
                                                                                showMetabolicValuesDialog =
                                                                                        true
                                                                        },
                                                                        modifier = Modifier.weight(1f)
                                                                )
                                                                Divider(
                                                                        modifier =
                                                                                Modifier.width(1.dp)
                                                                                        .fillMaxHeight()
                                                                )
                                                                SectionCoefficients(
                                                                        selectedConsultation =
                                                                                selectedConsultation,
                                                                        showCoefficientsDialog = {
                                                                                showCoefficientsDialog =
                                                                                        true
                                                                        },
                                                                        viewModel = viewModel,
                                                                        modifier = Modifier.weight(1f)
                                                                )
                                                                Divider(
                                                                        modifier =
                                                                                Modifier.width(1.dp)
                                                                                        .fillMaxHeight()
                                                                )
                                                                SectionBilanEnergetique(
                                                                        energieApportee = energieApportee,
                                                                        pourcentageCouverture =
                                                                                pourcentageCouverture,
                                                                        kObserve = kObserve,
                                                                        kCalcule = kCalcule,
                                                                       
                                                                        beFinal = besoinEnergetiqueTotal,
                                                                        modifier = Modifier.weight(1f)
                                                                )
                                                        }
                                                }
                                        }
                                }
                                // ... le reste du contenu de la vue ...
                        }

                        // Contenu principal - grille 2x2 de cartes (colonnes fixes 50/50)
                        Box(modifier = Modifier.weight(1f)) {
                                Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        // Colonne gauche (listes) - 50% de l'espace
                                        Column(
                                                modifier = Modifier.weight(0.5f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // Segment 2: Liste des rations de la
                                                // consultation
                                                Card(
                                                        modifier =
                                                                Modifier.weight(1f).fillMaxWidth(),
                                                        elevation = AppSizes.elevationMedium,
                                                        backgroundColor =
                                                                MaterialTheme.colors.surface
                                                ) {
                                                        Column(
                                                                modifier =
                                                                        Modifier.fillMaxSize()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                // En-tête avec titre et
                                                                // bouton
                                                                // d'ajout
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        horizontalArrangement =
                                                                                Arrangement
                                                                                        .SpaceBetween,
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        // Titre "Rations de la
                                                                        // consultation" (dans la
                                                                        // Card en-tête)
                                                                        Text(
                                                                                text =
                                                                                        "Rations de la consultation",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle2, // taille réduite
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour
                                                                        // ajouter une
                                                                        // nouvelle ration
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Filled
                                                                                                .Add,
                                                                                contentDescription =
                                                                                        "Ajouter une ration",
                                                                                tint =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                        AppSizes.iconSizeXSmall
                                                                                                )
                                                                                                .clickable(
                                                                                                        onClick = {
                                                                                                                rationToEdit =
                                                                                                                        null // Nouvelle ration
                                                                                                                showRationEditDialog =
                                                                                                                        true
                                                                                                        }
                                                                                                )
                                                                        )
                                                                }

                                                                Divider()

                                                                if (selectedConsultation?.rations
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucune ration disponible",
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )
                                                                } else {
                                                                        LazyColumn(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        ),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        8.dp
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedConsultation
                                                                                                ?.rations
                                                                                                ?: emptyList()
                                                                                ) { ration ->
                                                                                        RationItem(
                                                                                                ration =
                                                                                                        ration,
                                                                                                isSelected =
                                                                                                        ration.uuid ==
                                                                                                                selectedRation
                                                                                                                        ?.uuid,
                                                                                                onClick = {
                                                                                                        viewModel
                                                                                                                .selectRation(
                                                                                                                        ration
                                                                                                                )
                                                                                                },
                                                                                                onEdit = {
                                                                                                        rationToEdit =
                                                                                                                ration
                                                                                                        showRationEditDialog =
                                                                                                                true
                                                                                                },
                                                                                                onDuplicate = {
                                                                                                        viewModel
                                                                                                                .duplicateRation(
                                                                                                                        ration
                                                                                                                )
                                                                                                        showSnackbar(
                                                                                                                "Ration '${ration.name}' dupliquée"
                                                                                                        )
                                                                                                },
                                                                                                onDelete = {
                                                                                                        rationToDelete =
                                                                                                                ration
                                                                                                        showDeleteRationDialog =
                                                                                                                true
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Segment 3: Liste des aliments de la ration
                                                // sélectionnée
                                                if (selectedRation != null) {
                                                        SectionAlimentsRation(
                                                                selectedRation = selectedRation,
                                                                referenceUtilisee = referenceUtilisee,
                                                                besoinEnergetiqueTotal =
                                                                        besoinEnergetiqueTotal,
                                                                besoinEnergetiqueStandard =
                                                                        besoinEnergetiqueStandard,
                                                                viewModel = viewModel,
                                                                equationRepository = equationRepository,
                                                                onAddAliment = {
                                                                        rationForAddAliment =
                                                                                selectedRation
                                                                        showAddAlimentView = true
                                                                },
                                                                onMultiNutrientAdjustment = {
                                                                        showMultiNutrientAdjustmentDialog =
                                                                                true
                                                                },
                                                                onOpenRecipeDialog = {
                                                                        showRecipeDialog = true
                                                                },
                                                                onSaveRecipe = {
                                                                        selectedRation?.let { r ->
                                                                                newRecipeName = r.name
                                                                                showSaveRecipeDialog = true
                                                                        }
                                                                },
                                                                showSnackbar = showSnackbar,
                                                                modifier =
                                                                        Modifier.weight(1f).fillMaxWidth()
                                                        )
                                                } else {
                                                        Card(
                                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                                elevation = AppSizes.elevationMedium
                                                        ) {
                                                                Box(
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentAlignment = Alignment.Center
                                                                ) {
                                                                        Text(
                                                                                "Sélectionnez une ration pour voir les aliments",
                                                                                style = MaterialTheme.typography.body1,
                                                                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                        )
                                                                }
                                                        }
                                                }
                                        }

                                        // Colonne droite (analyses) - 50% de l'espace
                                        Column(
                                                modifier = Modifier.weight(0.5f),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {

                                                // Analyse nutritionnelle de la ration
                                                // sélectionnée
                                                if (selectedRation != null) {
                                                        if (referenceUtilisee != null) {
                                                                // Obtenir les nutriments
                                                                // sélectionnés selon
                                                                // l'espèce avec logs
                                                                val nutrimentsSelectionnes =
                                                                        remember(
                                                                                animal,
                                                                                preferencesApplication
                                                                        ) {
                                                                                val animalActuel = animal
                                                                                val prefsApp =
                                                                                        preferencesApplication

                                                                                if (animalActuel != null &&
                                                                                                prefsApp !=
                                                                                                        null
                                                                                ) {
                                                                                        val especeAnimal =
                                                                                                animalActuel
                                                                                                        .getEspece()

                                                                                        val preferencesEspece =
                                                                                                prefsApp.getPreferencesEspece(
                                                                                                        especeAnimal
                                                                                                )

                                                                                        val nutrimentsLabels =
                                                                                                convertirPreferencesVersLabelsNutriments(
                                                                                                        preferencesEspece
                                                                                                )

                                                                                        if (nutrimentsLabels
                                                                                                        .isNotEmpty()
                                                                                        ) {
                                                                                                nutrimentsLabels
                                                                                        } else {
                                                                                                listOf(
                                                                                                        "PROTEINE",
                                                                                                        "LIPIDE",
                                                                                                        "ENA",
                                                                                                        "CELLULOSE",
                                                                                                        "CENDRE",
                                                                                                        "CAL",
                                                                                                        "PHOS"
                                                                                                )
                                                                                        }
                                                                                } else {
                                                                                        listOf(
                                                                                                "PROTEINE",
                                                                                                "LIPIDE",
                                                                                                "ENA",
                                                                                                "CELLULOSE",
                                                                                                "CENDRE",
                                                                                                "CAL",
                                                                                                "PHOS",
                                                                                                "FE",
                                                                                                "ZN",
                                                                                                "CU",
                                                                                                "VITA",
                                                                                                "VITD",
                                                                                                "VITE",
                                                                                                "VITB1",
                                                                                                "VITB2"
                                                                                        )
                                                                                }
                                                                        }

                                                                AnalyseNutritionnelleCard(
                                                                        ration = selectedRation!!,
                                                                        poidsMetabolique = poidsMetabolique,
                                                                        referenceUtilisee =
                                                                                referenceUtilisee,
                                                                        besoinEnergetiqueEntretien =
                                                                                besoinEnergetiqueStandard,
                                                                        poidsAnimal =
                                                                                selectedConsultation
                                                                                        ?.effectiveWeight
                                                                                        ?.toDouble(),
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        nutrimentsSelectionnes =
                                                                                nutrimentsSelectionnes, // Utilisation des préférences
                                                                        onNutrimentClick = {
                                                                                nom,
                                                                                valeurNutritionnelle ->
                                                                                selectedNutrimentData =
                                                                                        Triple(
                                                                                                nom,
                                                                                                valeurNutritionnelle,
                                                                                                selectedRation!!
                                                                                        )
                                                                                showNutrimentDetailDialog =
                                                                                        true
                                                                        },
                                                                        // Nouveaux paramètres pour
                                                                        // les
                                                                        // préférences
                                                                        animal = animal,
                                                                        preferencesRepository =
                                                                                preferencesRepository,
                                                                        equationRepository =
                                                                                equationRepository,
                                                                        // Utiliser les préférences
                                                                        // pré-chargées du ViewModel
                                                                        typeExpressionBesoin =
                                                                                viewModel
                                                                                        .typeExpressionBesoin
                                                                                        .collectAsState()
                                                                                        .value,
                                                                        isLargeView = true,
                                                                        referencesMaladies =
                                                                                referencesMaladiesResolues
                                                                )
                                                        } else {
                                                                Card(
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        elevation = AppSizes.elevationMedium
                                                                ) {
                                                                        Box(
                                                                                modifier =
                                                                                        Modifier.fillMaxSize(),
                                                                                contentAlignment =
                                                                                        Alignment.Center
                                                                        ) {
                                                                                Text(
                                                                                        "Sélectionner une référence dans la consultation pour permettre l'analyse",
                                                                                        style = MaterialTheme.typography.body1,
                                                                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                } else {
                                                        Card(
                                                                modifier = Modifier.fillMaxSize(),
                                                                elevation = AppSizes.elevationMedium
                                                        ) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize(),
                                                                        contentAlignment =
                                                                                Alignment.Center
                                                                ) {
                                                                        Text(
                                                                                "Sélectionnez une ration pour voir l'analyse nutritionnelle"
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }

                        // TODO: Réimplémentez les dialogues d'édition ici quand nécessaire

                        // Afficher le dialogue d'édition de ration si nécessaire
                        if (showRationEditDialog) {
                                RationEditDialog(
                                        ration = rationToEdit,
                                        onDismiss = {
                                                showRationEditDialog = false
                                                rationToEdit = null
                                        },
                                        onSave = { updatedRation ->
                                                if (rationToEdit == null) {
                                                        // Création d'une nouvelle ration
                                                        // Assurer que la ration est liée à
                                                        // la
                                                        // consultation
                                                        val newRation =
                                                                updatedRation.copy(
                                                                        idConsult =
                                                                                selectedConsultation
                                                                                        ?.uuid
                                                                                        ?: ""
                                                                )

                                                        selectedConsultation?.let { consultation ->
                                                                // Créer une copie de la
                                                                // liste des
                                                                // rations et y ajouter la
                                                                // nouvelle
                                                                // ration
                                                                val updatedRations =
                                                                        consultation.rations
                                                                                .toMutableList()
                                                                updatedRations.add(newRation)

                                                                // Mettre à jour la
                                                                // consultation
                                                                // avec la nouvelle liste de
                                                                // rations
                                                                val updatedConsultation =
                                                                        consultation.copy(
                                                                                rations =
                                                                                        updatedRations
                                                                        )

                                                                // Sauvegarder la
                                                                // consultation mise
                                                                // à jour
                                                                viewModel.updateConsultation(
                                                                        updatedConsultation
                                                                )

                                                                // Sélectionner la nouvelle
                                                                // ration
                                                                viewModel.selectRation(newRation)

                                                                showSnackbar(
                                                                        "Ration '${newRation.name}' créée"
                                                                )
                                                        }
                                                } else {
                                                        // Mise à jour d'une ration
                                                        // existante
                                                        viewModel.updateRation(updatedRation)
                                                        showSnackbar(
                                                                "Ration '${updatedRation.name}' mise à jour"
                                                        )
                                                }

                                                showRationEditDialog = false
                                                rationToEdit = null
                                        }
                                )
                        }

                        // Dialogues d'agrandissement des sections
                        if (showMetabolicValuesDialog) {
                                MetabolicValuesDialog(
                                        selectedConsultation = selectedConsultation,
                                        poidsMetabolique = poidsMetabolique,
                                        besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                        besoinEnergetiqueTotal = besoinEnergetiqueTotal,
                                        kObserve = kObserve,
                                        kCalcule = kCalcule,
                                        referenceUtilisee = referenceUtilisee,
                                        onDismiss = { showMetabolicValuesDialog = false }
                                )
                        }

                        if (showCoefficientsDialog) {
                                CoefficientsDialog(
                                        selectedConsultation = selectedConsultation,
                                        viewModel = viewModel,
                                        onDismiss = { showCoefficientsDialog = false }
                                )
                        }
                }

                // Dialog détaillé de nutriment
                if (showNutrimentDetailDialog && selectedNutrimentData != null) {
                        val (nom, valeurNutritionnelle, ration) = selectedNutrimentData!!
                        NutrientDetailDialog(
                                nom = nom,
                                valeurNutritionnelle = valeurNutritionnelle,
                                ration = ration,
                                poidsMetabolique = poidsMetabolique,
                                referenceUtilisee = referenceUtilisee,
                                besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                espece = animal?.getEspece() ?: Espece.CHIEN,
                                preferencesStorage = preferencesStorage,
                                equationRepository = equationRepository,
                                referencesMaladies = referencesMaladiesResolues,
                                onDismiss = {
                                        showNutrimentDetailDialog = false
                                        selectedNutrimentData = null
                                }
                        )
                }

                // Dialog d'ajustement multi-nutriments
                if (showMultiNutrientAdjustmentDialog &&
                                selectedRation != null &&
                                referenceUtilisee != null &&
                                besoinEnergetiqueTotal != null
                ) {
                        val ration = selectedRation!!
                        val reference = referenceUtilisee!!
                        val besoinEnergetique = besoinEnergetiqueTotal!!

                        MultiNutrientAdjustmentView(
                                ration = ration,
                                referenceUtilisee = reference,
                                besoinEnergetiqueTotal = besoinEnergetique,
                                besoinEnergetiqueStandard = besoinEnergetiqueStandard!!,
                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                poidsMetabolique = poidsMetabolique,
                                equationRepository = equationRepository,
                                onConfirm = { result ->
                                        if (result.success) {
                                                // Appliquer les ajustements à la ration
                                                result.adjustedAliments?.let { adjustedAliments ->
                                                        coroutineScope.launch {
                                                                viewModel.updateRationAliments(
                                                                        ration,
                                                                        adjustedAliments
                                                                )
                                                                showSnackbar(result.message)
                                                        }
                                                }
                                        } else {
                                                showSnackbar("Erreur: ${result.message}")
                                        }
                                        showMultiNutrientAdjustmentDialog = false
                                },
                                onDismiss = { showMultiNutrientAdjustmentDialog = false }
                        )
                }

                // Dialog de confirmation de suppression de ration
                if (showDeleteRationDialog) {
                        val rationCible = rationToDelete
                        if (rationCible != null) {
                                AlertDialog(
                                        onDismissRequest = {
                                                showDeleteRationDialog = false
                                                rationToDelete = null
                                        },
                                        title = {
                                                Text(
                                                        "Confirmer la suppression",
                                                        style = MaterialTheme.typography.h6,
                                                        color = VetNutriColors.Error
                                                )
                                        },
                                        text = {
                                                Text(
                                                        "Êtes-vous sûr de vouloir supprimer la ration '${rationCible.name}' ?\n\nCette action est irréversible.",
                                                        style = MaterialTheme.typography.body1
                                                )
                                        },
                                        confirmButton = {
                                                Button(
                                                        onClick = {
                                                                viewModel.removeRationFromConsultation(
                                                                        rationCible
                                                                )
                                                                showSnackbar(
                                                                        "Ration '${rationCible.name}' supprimée"
                                                                )
                                                                showDeleteRationDialog = false
                                                                rationToDelete = null
                                                        },
                                                        colors =
                                                                ButtonDefaults.buttonColors(
                                                                        backgroundColor =
                                                                                VetNutriColors.Error,
                                                                        contentColor =
                                                                                VetNutriColors.OnError
                                                                )
                                                ) { Text("Supprimer") }
                                        },
                                        dismissButton = {
                                                TextButton(
                                                        onClick = {
                                                                showDeleteRationDialog = false
                                                                rationToDelete = null
                                                        }
                                                ) { Text("Annuler") }
                                        }
                                )
                        }
                }
        }
}

/**
 * Dialogue pour créer ou éditer une ration
 *
 * @param ration Ration à éditer, null pour une nouvelle ration
 * @param onDismiss Action à exécuter pour fermer le dialogue
 * @param onSave Action à exécuter pour sauvegarder la ration
 */
@Composable
fun RationEditDialog(ration: Ration?, onDismiss: () -> Unit, onSave: (Ration) -> Unit) {
        val isNewRation = ration == null
        val title = if (isNewRation) "Créer une ration" else "Modifier la ration"

        // État éditable de la ration
        var editedRation by remember {
                mutableStateOf(
                        ration?.copy()
                                ?: Ration(
                                        name = "Nouvelle ration",
                                        actual = false,
                                        alimentMutableList = mutableListOf()
                                )
                )
        }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title, style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = AppSizes.paddingSmall),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Nom de la ration
                                OutlinedTextField(
                                        value = editedRation.name,
                                        onValueChange = {
                                                editedRation = editedRation.copy(name = it)
                                        },
                                        label = { Text("Nom de la ration") },
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Type de ration (actuelle ou proposée)
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text("Type de ration:", modifier = Modifier.weight(1f))

                                        Switch(
                                                checked = editedRation.actual,
                                                onCheckedChange = { checked ->
                                                        editedRation =
                                                                editedRation.copy(actual = checked)
                                                },
                                                colors =
                                                        SwitchDefaults.colors(
                                                                checkedThumbColor =
                                                                        VetNutriColors.Primary,
                                                                checkedTrackColor =
                                                                        VetNutriColors.Primary.copy(
                                                                                alpha = 0.5f
                                                                        )
                                                        )
                                        )

                                        Text(
                                                text =
                                                        if (editedRation.actual) "Actuelle"
                                                        else "Proposée",
                                                style = MaterialTheme.typography.body2,
                                                modifier =
                                                        Modifier.padding(
                                                                start = AppSizes.paddingSmall
                                                        )
                                        )
                                }

                                // Description de la ration
                                OutlinedTextField(
                                        value = editedRation.description,
                                        onValueChange = {
                                                editedRation = editedRation.copy(description = it)
                                        },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 3,
                                        singleLine = false
                                )

                                // Coefficient de la ration
                                OutlinedTextField(
                                        value =
                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                        .formatDecimal(
                                                                editedRation.coef.toDouble(),
                                                                2
                                                        )
                                                        .replace('.', ','),
                                        onValueChange = { newValue ->
                                                // Accepter seulement les nombres positifs
                                                val normalizedValue = newValue.replace(',', '.')
                                                val coefficient = normalizedValue.toDoubleOrNull()
                                                if (coefficient != null && coefficient > 0) {
                                                        editedRation =
                                                                editedRation.copy(
                                                                        coef = coefficient
                                                                )
                                                }
                                        },
                                        label = { Text("Coefficient de la ration") },
                                        modifier = Modifier.fillMaxWidth(),
                                        keyboardOptions =
                                                KeyboardOptions(
                                                        keyboardType = KeyboardType.Decimal
                                                ),
                                        placeholder = { Text("Ex: 1,0 ou 1.0") }
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = { onSave(editedRation) },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text("Enregistrer") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
        )
}

/**
 * Composant compact pour afficher une ligne d'information avec label et valeur Format en colonne
 * pour économiser l'espace
 */
@Composable
private fun CompactLocalInfoRow(label: String, value: String) {
        Column {
                Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Text(
                        text = value,
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Medium
                )
        }
}

/** Dialogue agrandi pour les valeurs métaboliques */
@Composable
private fun MetabolicValuesDialog(
        selectedConsultation: ConsultationEv?,
        poidsMetabolique: Double?,
        besoinEnergetiqueStandard: Double?,
        besoinEnergetiqueTotal: Double?,
        kObserve: Double,
        kCalcule: Double,
        referenceUtilisee: ReferenceEv?,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                "Valeurs métaboliques détaillées",
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )
                },
                text = {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                                LocalInfoRow(
                                        label = "Poids actuel",
                                        value =
                                                selectedConsultation?.weight?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                                }
                                                        ?: "Non renseigné"
                                )

                                LocalInfoRow(
                                        label = "Poids idéal",
                                        value =
                                                selectedConsultation?.effectiveWeight?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                                }
                                                        ?: "Non calculé"
                                )

                                LocalInfoRow(
                                        label = "Poids métabolique",
                                        value =
                                                poidsMetabolique?.let {
                                                        TextUtils.formatKgAvecPuissanceDynamique(
                                                                it,
                                                                referenceUtilisee?.equationBW?.equationScript
                                                        )
                                                }
                                                        ?: "Non calculé"
                                )

                                LocalInfoRow(
                                        label = "Besoin énergétique standard (BEE)",
                                        value =
                                                besoinEnergetiqueStandard?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kcal/jour"
                                                }
                                                        ?: "Non calculé"
                                )

                                LocalInfoRow(
                                        label = "Besoin énergétique total",
                                        value =
                                                besoinEnergetiqueTotal?.let {
                                                        "${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(it.toDouble(), 1)} kcal/jour"
                                                }
                                                        ?: "Non calculé"
                                )

                                Divider()

                                LocalInfoRow(
                                        label = "K Observé",
                                        value =
                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                        .formatDecimal(kObserve, 2)
                                )

                                LocalInfoRow(
                                        label = "K Calculé",
                                        value =
                                                fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                        .formatDecimal(kCalcule, 2)
                                )

                                referenceUtilisee?.let { reference ->
                                        Divider()
                                        Text(
                                                "Référence utilisée: ${reference.nom}",
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Primary
                                        )
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
        )
}

/**
 * Composable pour éditer un coefficient avec une combobox de valeurs prédéfinies et édition directe
 * pour valeurs personnalisées
 */
@Composable
private fun CoefficientEditableRow(
        label: String,
        currentValue: Double?,
        currentDescription: String?,
        availableCoefficients: List<fr.vetbrain.vetnutri_mp.Data.CoefP>,
        onCoefficientSelected: (fr.vetbrain.vetnutri_mp.Data.CoefP) -> Unit
) {
        var showDropdown by remember { mutableStateOf(false) }

        // Vérifier si la valeur actuelle est personnalisée (pas dans la liste prédéfinie)
        val isCustomValue =
                remember(currentValue, currentDescription, availableCoefficients) {
                        currentDescription == "Valeur personnalisée" ||
                                availableCoefficients.none {
                                        it.coef == currentValue &&
                                                it.description == currentDescription
                                }
                }

        // État pour l'édition directe des valeurs personnalisées
        var editableValue by
                remember(currentValue) {
                        mutableStateOf(
                                fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal(
                                                (currentValue ?: 1.0).toDouble(),
                                                2
                                        )
                                        .replace('.', ',')
                        )
                }
        var isEditing by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxWidth()) {
                // Label du coefficient
                Text(
                        text = label,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )

                // Champ de sélection avec dropdown ou édition directe
                if (isEditing) {
                        // Mode édition : afficher le TextField
                        OutlinedTextField(
                                value = editableValue,
                                onValueChange = { newValue ->
                                        editableValue = newValue
                                        // Validation et mise à jour en temps réel
                                        val normalizedText = newValue.replace(',', '.')
                                        val value = normalizedText.toDoubleOrNull()
                                        if (value != null && value > 0) {
                                                val customCoef =
                                                        fr.vetbrain.vetnutri_mp.Data.CoefP(
                                                                description =
                                                                        "Valeur personnalisée",
                                                                coef = value,
                                                                groupUUID = null
                                                        )
                                                onCoefficientSelected(customCoef)
                                        }
                                },
                                label = { Text("Coefficient") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions =
                                        KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = { Text("Ex: 1,2 ou 1.2") },
                                trailingIcon = {
                                        Row {
                                                // Bouton de validation (uniquement en mode édition)
                                                if (isEditing) {
                                                        IconButton(
                                                                onClick = {
                                                                        // Valider et sortir du mode
                                                                        // édition
                                                                        val normalizedText =
                                                                                editableValue
                                                                                        .replace(
                                                                                                ',',
                                                                                                '.'
                                                                                        )
                                                                        val value =
                                                                                normalizedText
                                                                                        .toDoubleOrNull()
                                                                        if (value != null &&
                                                                                        value > 0
                                                                        ) {
                                                                                val customCoef =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Data
                                                                                                .CoefP(
                                                                                                        description =
                                                                                                                "Valeur personnalisée",
                                                                                                        coef =
                                                                                                                value,
                                                                                                        groupUUID =
                                                                                                                null
                                                                                                )
                                                                                onCoefficientSelected(
                                                                                        customCoef
                                                                                )
                                                                        }
                                                                        isEditing = false
                                                                }
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default.Check,
                                                                        contentDescription =
                                                                                "Valider",
                                                                        tint =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                        }
                                                }

                                                // Bouton dropdown (toujours présent)
                                                IconButton(
                                                        onClick = {
                                                                showDropdown = true
                                                                isEditing = false // Sortir du mode
                                                                // édition si
                                                                // on ouvre le dropdown
                                                        }
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        if (showDropdown)
                                                                                Icons.Default
                                                                                        .KeyboardArrowUp
                                                                        else
                                                                                Icons.Default
                                                                                        .KeyboardArrowDown,
                                                                contentDescription =
                                                                        "Sélectionner un coefficient"
                                                        )
                                                }
                                        }
                                }
                        )
                } else {
                        // Mode lecture : afficher le texte cliquable dans un Box
                        Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value =
                                                buildString {
                                                        if (currentDescription != null &&
                                                                        currentValue != null &&
                                                                        !isCustomValue
                                                        ) {
                                                                append(
                                                                        "$currentDescription (${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal((currentValue ?: 0.0).toDouble(), 2)})"
                                                                )
                                                        } else {
                                                                append(
                                                                        (fr.vetbrain.vetnutri_mp
                                                                                        .Utils
                                                                                        .TextUtils
                                                                                        .formatDecimal(
                                                                                                (currentValue
                                                                                                                ?: 1.0)
                                                                                                        .toDouble(),
                                                                                                2
                                                                                        ))
                                                                                .replace('.', ',')
                                                                )
                                                        }
                                                },
                                        onValueChange = { /* Pas d'édition en mode lecture */},
                                        label = { Text("Coefficient") },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                                // Bouton dropdown (toujours présent)
                                                IconButton(onClick = { showDropdown = true }) {
                                                        Icon(
                                                                imageVector =
                                                                        if (showDropdown)
                                                                                Icons.Default
                                                                                        .KeyboardArrowUp
                                                                        else
                                                                                Icons.Default
                                                                                        .KeyboardArrowDown,
                                                                contentDescription =
                                                                        "Sélectionner un coefficient"
                                                        )
                                                }
                                        }
                                )

                                // Zone cliquable invisible qui couvre le contenu du TextField
                                Box(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                56.dp
                                                        ) // Hauteur standard d'un OutlinedTextField
                                                        .clickable {
                                                                // Cliquer sur le texte lance
                                                                // directement l'édition
                                                                isEditing = true
                                                                // Si c'était un coefficient de
                                                                // référence, on initialise avec la
                                                                // valeur seule
                                                                if (!isCustomValue &&
                                                                                currentValue != null
                                                                ) {
                                                                        editableValue =
                                                                                fr.vetbrain
                                                                                        .vetnutri_mp
                                                                                        .Utils
                                                                                        .TextUtils
                                                                                        .formatDecimal(
                                                                                                (currentValue
                                                                                                                ?: 0.0)
                                                                                                        .toDouble(),
                                                                                                2
                                                                                        )
                                                                                        .replace(
                                                                                                '.',
                                                                                                ','
                                                                                        )
                                                                }
                                                        }
                                                        .padding(
                                                                end = 64.dp
                                                        ) // Eviter de chevaucher avec le bouton
                                        // dropdown (plus de marge)
                                        )
                        }
                }

                // DropdownMenu avec les coefficients disponibles
                DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                ) {
                        availableCoefficients.forEach { coef ->
                                DropdownMenuItem(
                                        onClick = {
                                                onCoefficientSelected(coef)
                                                showDropdown = false
                                        }
                                ) {
                                        Column {
                                                Text(
                                                        text = coef.description
                                                                        ?: "Sans description",
                                                        style = MaterialTheme.typography.body1
                                                )
                                                Text(
                                                        text =
                                                                "Coefficient: ${fr.vetbrain.vetnutri_mp.Utils.TextUtils.formatDecimal((coef.coef ?: 1.0).toDouble(), 2)}",
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray
                                                )
                                        }
                                }
                        }

                        // Option pour saisie manuelle
                        Divider()
                        DropdownMenuItem(
                                onClick = {
                                        // Lancer l'édition directe
                                        isEditing = true
                                        if (!isCustomValue && currentValue != null) {
                                                editableValue =
                                                        fr.vetbrain.vetnutri_mp.Utils.TextUtils
                                                                .formatDecimal(
                                                                        (currentValue ?: 0.0)
                                                                                .toDouble(),
                                                                        2
                                                                )
                                                                .replace('.', ',')
                                        }
                                        showDropdown = false
                                }
                        ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                                text = "Édition directe...",
                                                style = MaterialTheme.typography.body2,
                                                fontStyle = FontStyle.Italic
                                        )
                                }
                        }
                }
        }
}

/** Dialogue agrandi pour les coefficients */
@Composable
private fun CoefficientsDialog(
        selectedConsultation: ConsultationEv?,
        viewModel: AnimalDetailViewModel,
        onDismiss: () -> Unit
) {
        // Observer la référence utilisée pour récupérer les coefficients disponibles
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()

        // État pour l'édition du coefficient d'ajustement
        var isEditingCoefficient by remember { mutableStateOf(false) }
        var coefficientText by
                remember(selectedConsultation) {
                        mutableStateOf(
                                selectedConsultation?.coefficientAjustement?.toString() ?: "1.0"
                        )
                }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                "Coefficients détaillés",
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )
                },
                text = {
                        LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium),
                                modifier = Modifier.height(400.dp)
                        ) {
                                item {
                                        Text(
                                                "Coefficients K",
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary,
                                                fontWeight = FontWeight.Bold
                                        )
                                }

                                // K1 - Stade physiologique
                                item {
                                        CoefficientEditableRow(
                                                label = "K1",
                                                currentValue = selectedConsultation?.k1Value,
                                                currentDescription = selectedConsultation?.k1Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk1
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k1",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K2 - Activité
                                item {
                                        CoefficientEditableRow(
                                                label = "K2",
                                                currentValue = selectedConsultation?.k2Value,
                                                currentDescription = selectedConsultation?.k2Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk2
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k2",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K3 - Environnement
                                item {
                                        CoefficientEditableRow(
                                                label = "K3",
                                                currentValue = selectedConsultation?.k3Value,
                                                currentDescription = selectedConsultation?.k3Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk3
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k3",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K4 - État corporel
                                item {
                                        CoefficientEditableRow(
                                                label = "K4",
                                                currentValue = selectedConsultation?.k4Value,
                                                currentDescription = selectedConsultation?.k4Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk4
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k4",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                // K5 - Pathologie
                                item {
                                        CoefficientEditableRow(
                                                label = "K5",
                                                currentValue = selectedConsultation?.k5Value,
                                                currentDescription = selectedConsultation?.k5Id,
                                                availableCoefficients =
                                                        referenceUtilisee?.modk5
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k5",
                                                                        coef.coef ?: 1.0,
                                                                        coef.description
                                                                )
                                                        }
                                                }
                                        )
                                }

                                item { Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f)) }

                                // Coefficient d'ajustement éditable
                                item {
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Text(
                                                        "Coefficient d'ajustement",
                                                        style = MaterialTheme.typography.subtitle1
                                                )

                                                if (isEditingCoefficient) {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                OutlinedTextField(
                                                                        value = coefficientText,
                                                                        onValueChange = {
                                                                                coefficientText = it
                                                                        },
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        100.dp
                                                                                ),
                                                                        singleLine = true,
                                                                        keyboardOptions =
                                                                                KeyboardOptions(
                                                                                        keyboardType =
                                                                                                KeyboardType
                                                                                                        .Number
                                                                                )
                                                                )
                                                                IconButton(
                                                                        onClick = {
                                                                                coefficientText
                                                                                        .toDoubleOrNull()
                                                                                        ?.let {
                                                                                                newValue
                                                                                                ->
                                                                                                selectedConsultation
                                                                                                        ?.let {
                                                                                                                consultation
                                                                                                                ->
                                                                                                                viewModel
                                                                                                                        .updateCoefficientAjustement(
                                                                                                                                consultation
                                                                                                                                        .uuid,
                                                                                                                                newValue
                                                                                                                        )
                                                                                                        }
                                                                                        }
                                                                                isEditingCoefficient =
                                                                                        false
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                Icons.Filled.Check,
                                                                                contentDescription =
                                                                                        "Valider",
                                                                                tint = Color.Green
                                                                        )
                                                                }
                                                                IconButton(
                                                                        onClick = {
                                                                                coefficientText =
                                                                                        selectedConsultation
                                                                                                ?.coefficientAjustement
                                                                                                ?.toString()
                                                                                                ?: "1.0"
                                                                                isEditingCoefficient =
                                                                                        false
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                Icons.Filled.Close,
                                                                                contentDescription =
                                                                                        "Annuler",
                                                                                tint = Color.Red
                                                                        )
                                                                }
                                                        }
                                                } else {
                                                        Row(
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        selectedConsultation
                                                                                ?.coefficientAjustement
                                                                                ?.let {
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Utils
                                                                                                .TextUtils
                                                                                                .formatDecimal(
                                                                                                        it,
                                                                                                        2
                                                                                                )
                                                                                }
                                                                                ?: "1.00",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body1,
                                                                        fontWeight =
                                                                                FontWeight.Medium
                                                                )
                                                                IconButton(
                                                                        onClick = {
                                                                                coefficientText =
                                                                                        selectedConsultation
                                                                                                ?.coefficientAjustement
                                                                                                ?.toString()
                                                                                                ?: "1.0"
                                                                                isEditingCoefficient =
                                                                                        true
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                Icons.Filled.Edit,
                                                                                contentDescription =
                                                                                        "Éditer"
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
        )
}

/** Composable pour afficher une ligne d'information détaillée avec description */
@Composable
private fun DetailedLocalInfoRow(label: String, value: String, description: String? = null) {
        Column {
                Text(
                        text = label,
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                Text(
                        text = value,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Medium
                )
                description?.let {
                        Text(
                                text = it,
                                style = MaterialTheme.typography.caption,
                                color = VetNutriColors.Primary.copy(alpha = 0.7f)
                        )
                }
        }
}
