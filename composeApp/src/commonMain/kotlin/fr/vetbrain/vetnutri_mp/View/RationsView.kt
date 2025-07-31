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
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AlimentItem
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.ValeurNutritionnelle
import fr.vetbrain.vetnutri_mp.Data.convertirPreferencesVersLabelsNutriments
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrientDetailDialog
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionBilanEnergetique
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionCoefficients
import fr.vetbrain.vetnutri_mp.View.AnalNut.SectionValeursMetaboliques
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.coroutines.launch

// Constante pour l'exposant formaté
private const val EXPOSANT_075 = "⁰·⁷⁵"

/**
 * Calcule la densité énergétique d'un aliment selon les formules de la référence
 * @param aliment L'aliment pour lequel calculer la densité énergétique
 * @param reference La référence contenant les équations DE commerciale et brute
 * @return La densité énergétique en kcal/100g
 */
private fun calculerDensiteEnergetique(
        alimentRation: AlimentRation,
        reference: ReferenceEv
): Double {
        try {
                val aliment = alimentRation.aliment ?: return 0.0

                // Déterminer si l'aliment est commercial (complet/complémentaire) ou brut
                val estCommercial =
                        aliment.indicat.any { indication ->
                                indication.name == "COMP" || indication.name == "COMPL"
                        }

                // Choisir l'équation appropriée
                val equation =
                        if (estCommercial) {
                                reference.equationDEcom
                        } else {
                                reference.equationDEraw
                        }

                if (equation == null || equation.equationScript.isEmpty()) {
                        println(
                                "DEBUG: Aucune équation DE ${if (estCommercial) "commerciale" else "brute"} disponible"
                        )
                        return 0.0
                }

                // Créer les variables pour l'évaluation
                val variables = mutableMapOf<String, Double>()

                // Ajouter les nutriments principaux nécessaires aux formules
                aliment.valMap.forEach { (nutrient, quantity) ->
                        when (nutrient.label.uppercase()) {
                                "PROTEINE", "PB" -> {
                                        variables["PB"] = quantity.value.toDouble()
                                        variables["PROT"] =
                                                quantity.value
                                                        .toDouble() // Alias pour compatibilité
                                        variables["PROTEINE"] = quantity.value.toDouble()
                                        // équations
                                }
                                "LIPIDE" -> {
                                        variables["LIPIDE"] = quantity.value.toDouble()
                                        variables["LIP"] =
                                                quantity.value
                                                        .toDouble() // Alias pour compatibilité
                                        // équations
                                }
                                "ENA" -> variables["ENA"] = quantity.value.toDouble()
                                "CENDRE" -> variables["CENDRE"] = quantity.value.toDouble()
                        }
                }

                // Vérifier que les nutriments essentiels sont présents

                println(
                        "DEBUG: Calcul DE pour ${aliment.nom} - Type: ${if (estCommercial) "commercial" else "brut"}"
                )
                println("DEBUG: Équation: ${equation.equationScript}")
                println(
                        "DEBUG: Variables: PB=${variables["PB"]}, MG=${variables["MG"]}, ENA=${variables["ENA"]}"
                )

                // Évaluer l'équation
                val resultat =
                        fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique.evaluer(
                                equation.equationScript,
                                variables
                        )

                println("DEBUG: Densité énergétique calculée: $resultat kcal/100g")
                return resultat ?: 0.0
        } catch (e: Exception) {
                println(
                        "ERREUR: Calcul de densité énergétique pour ${alimentRation.aliment?.nom}: ${e.message}"
                )
                return 0.0
        }
}

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
        modifier: Modifier = Modifier
) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // Récupération des valeurs métaboliques calculées
        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()
        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
        val besoinEnergetiqueTotal by viewModel.besoinEnergetiqueTotal.collectAsState()
        val referenceUtilisee by viewModel.referenceUtilisee.collectAsState()

        // Calcul de l'énergie totale apportée par la ration sélectionnée
        val energieApportee =
                remember(selectedRation, referenceUtilisee) {
                        selectedRation?.let { ration ->
                                referenceUtilisee?.let { reference ->
                                        ration.alimentMutableList.sumOf { aliment ->
                                                val densiteEnergetique =
                                                        calculerDensiteEnergetique(
                                                                aliment,
                                                                reference
                                                        )
                                                (densiteEnergetique * aliment.quantite) / 100.0
                                        }
                                }
                                        ?: 0.0
                        }
                                ?: 0.0
                }

        // Calcul du pourcentage de couverture avec le besoin énergétique total
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

        // Système de préférences pour le filtrage des nutriments avec logs de debug
        val preferencesStorage: PreferencesStorage = remember { createPreferencesStorage() }
        val preferencesRepository: PreferencesRepository = remember {
                PreferencesRepository(preferencesStorage)
        }
        var preferencesApplication by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }

        // Charger les préférences au démarrage avec logs
        LaunchedEffect(Unit) {
                println("DEBUG PREFERENCES: Début du chargement des préférences...")
                preferencesRepository.loadPreferences()
                preferencesApplication = preferencesRepository.preferences
                println(
                        "DEBUG PREFERENCES: Préférences chargées - ${preferencesApplication?.preferencesParEspece?.size ?: 0} espèces trouvées"
                )

                // Log des préférences par espèce
                preferencesApplication?.preferencesParEspece?.forEach { (espece, prefs) ->
                        println(
                                "DEBUG PREFERENCES: Espèce $espece - ${prefs.getTotalSelectedNutrients()} nutriments sélectionnés"
                        )
                        prefs.nutrimentsSelectionnes.forEach { (categorie, nutriments) ->
                                println(
                                        "DEBUG PREFERENCES:   - Catégorie $categorie: ${nutriments.size} nutriments"
                                )
                        }
                }
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

        // Afficher la vue d'ajout d'aliment si nécessaire
        if (showAddAlimentView && rationForAddAliment != null) {
                AddAlimentView(
                        viewModel = viewModel,
                        ration = rationForAddAliment!!,
                        onNavigateBack = {
                                showAddAlimentView = false
                                rationForAddAliment = null
                        },
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
                                        // Fermer la vue d'ajout et revenir à la vue des rations
                                        showAddAlimentView = false
                                        rationForAddAliment = null
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
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                rationToEdit =
                                                                                                        null
                                                                                                showRationEditDialog =
                                                                                                        true
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        AppSizes.iconSizeMedium
                                                                                                )
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Filled
                                                                                                        .Add,
                                                                                                contentDescription =
                                                                                                        "Ajouter une ration",
                                                                                                tint =
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                        )
                                                                                }
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
                                                                                                                        viewModel
                                                                                                                                .removeRationFromConsultation(
                                                                                                                                        ration
                                                                                                                                )
                                                                                                                        showSnackbar(
                                                                                                                                "Ration supprimée"
                                                                                                                        )
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
                                                                                                "Aliments de la ration",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle2,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                Row(
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically,
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                AppSizes.paddingXSmall
                                                                                                        )
                                                                                ) {
                                                                                        // Bouton
                                                                                        // pour
                                                                                        // ajuster
                                                                                        // la ration
                                                                                        val coroutineScope =
                                                                                                rememberCoroutineScope()
                                                                                        val ration =
                                                                                                selectedRation
                                                                                        val beTotal =
                                                                                                besoinEnergetiqueTotal
                                                                                        IconButton(
                                                                                                onClick = {
                                                                                                        if (ration !=
                                                                                                                        null &&
                                                                                                                        beTotal !=
                                                                                                                                null &&
                                                                                                                        beTotal >
                                                                                                                                0
                                                                                                        ) {
                                                                                                                val energieApportee =
                                                                                                                        ration.alimentMutableList
                                                                                                                                .sumOf {
                                                                                                                                        alimentRation
                                                                                                                                        ->
                                                                                                                                        val densiteEnergetique =
                                                                                                                                                referenceUtilisee
                                                                                                                                                        ?.let {
                                                                                                                                                                ref
                                                                                                                                                                ->
                                                                                                                                                                calculerDensiteEnergetique(
                                                                                                                                                                        alimentRation,
                                                                                                                                                                        ref
                                                                                                                                                                )
                                                                                                                                                        }
                                                                                                                                                        ?: 0.0
                                                                                                                                        (densiteEnergetique *
                                                                                                                                                alimentRation
                                                                                                                                                        .quantite) /
                                                                                                                                                100.0
                                                                                                                                }
                                                                                                                if (energieApportee >
                                                                                                                                0
                                                                                                                ) {
                                                                                                                        val ratio =
                                                                                                                                beTotal /
                                                                                                                                        energieApportee
                                                                                                                        val alimentsAjustes =
                                                                                                                                ration.alimentMutableList
                                                                                                                                        .map {
                                                                                                                                                alimentRation
                                                                                                                                                ->
                                                                                                                                                alimentRation
                                                                                                                                                        .copy(
                                                                                                                                                                quantite =
                                                                                                                                                                        (alimentRation
                                                                                                                                                                                        .quantite *
                                                                                                                                                                                        ratio)
                                                                                                                                                                                .toFloat()
                                                                                                                                                        )
                                                                                                                                        }
                                                                                                                        coroutineScope
                                                                                                                                .launch {
                                                                                                                                        viewModel
                                                                                                                                                .updateRationAliments(
                                                                                                                                                        ration,
                                                                                                                                                        alimentsAjustes
                                                                                                                                                )
                                                                                                                                        showSnackbar(
                                                                                                                                                "Ration ajustée pour couvrir 100% du besoin énergétique total"
                                                                                                                                        )
                                                                                                                                }
                                                                                                                } else {
                                                                                                                        showSnackbar(
                                                                                                                                "Impossible d'ajuster : apport énergétique nul"
                                                                                                                        )
                                                                                                                }
                                                                                                        }
                                                                                                },
                                                                                                enabled =
                                                                                                        ration !=
                                                                                                                null &&
                                                                                                                beTotal !=
                                                                                                                        null &&
                                                                                                                beTotal >
                                                                                                                        0 &&
                                                                                                                (ration.alimentMutableList
                                                                                                                        .isNotEmpty())
                                                                                        ) {
                                                                                                Icon(
                                                                                                        imageVector =
                                                                                                                Icons.Filled
                                                                                                                        .Tune,
                                                                                                        contentDescription =
                                                                                                                "Ajuster la ration",
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                        .Primary
                                                                                                )
                                                                                        }
                                                                                        // Bouton
                                                                                        // pour
                                                                                        // ajouter
                                                                                        // un
                                                                                        // aliment
                                                                                        IconButton(
                                                                                                onClick = {
                                                                                                        if (selectedRation !=
                                                                                                                        null
                                                                                                        ) {
                                                                                                                rationForAddAliment =
                                                                                                                        selectedRation
                                                                                                                showAddAlimentView =
                                                                                                                        true
                                                                                                        } else {
                                                                                                                showSnackbar(
                                                                                                                        "Sélectionnez d'abord une ration"
                                                                                                                )
                                                                                                        }
                                                                                                },
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                AppSizes.iconSizeMedium
                                                                                                        )
                                                                                        ) {
                                                                                                Icon(
                                                                                                        imageVector =
                                                                                                                Icons.Filled
                                                                                                                        .Add,
                                                                                                        contentDescription =
                                                                                                                "Ajouter un aliment",
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                        .Primary
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                        Divider()
                                                                        if (selectedRation
                                                                                        ?.alimentMutableList
                                                                                        .isNullOrEmpty()
                                                                        ) {
                                                                                CenteredMessage(
                                                                                        message =
                                                                                                "Aucun aliment dans cette ration",
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
                                                                                                                AppSizes.paddingSmall
                                                                                                        )
                                                                                ) {
                                                                                        selectedRation
                                                                                                ?.alimentMutableList
                                                                                                ?.forEach {
                                                                                                        aliment
                                                                                                        ->
                                                                                                        AlimentItem(
                                                                                                                aliment =
                                                                                                                        aliment,
                                                                                                                isEditing =
                                                                                                                        editingAlimentId ==
                                                                                                                                aliment.uuid,
                                                                                                                onStartEditing = {
                                                                                                                        if (editingAlimentId !=
                                                                                                                                        null &&
                                                                                                                                        editingAlimentId !=
                                                                                                                                                aliment.uuid
                                                                                                                        ) {
                                                                                                                                editingAlimentId =
                                                                                                                                        null
                                                                                                                        }
                                                                                                                        editingAlimentId =
                                                                                                                                aliment.uuid
                                                                                                                },
                                                                                                                onQuantityChange = {
                                                                                                                        newQuantity
                                                                                                                        ->
                                                                                                                        viewModel
                                                                                                                                .updateAlimentQuantity(
                                                                                                                                        aliment.uuid,
                                                                                                                                        newQuantity
                                                                                                                                )
                                                                                                                },
                                                                                                                onFinishEditing = {
                                                                                                                        editingAlimentId =
                                                                                                                                null
                                                                                                                },
                                                                                                                onDelete = {
                                                                                                                        viewModel
                                                                                                                                .removeAlimentFromRation(
                                                                                                                                        aliment.uuid
                                                                                                                                )
                                                                                                                }
                                                                                                        )
                                                                                                }
                                                                                }
                                                                        }
                                                                }
                                                        }

                                                        // Section 6: Analyse nutritionnelle (si une
                                                        // ration est sélectionnée)
                                                        if (selectedRation != null) {
                                                                Divider()
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
                                                                                println(
                                                                                        "DEBUG FILTRAGE: Animal=$animalActuel, Préférences=$prefsApp"
                                                                                )
                                                                                if (animalActuel !=
                                                                                                null &&
                                                                                                prefsApp !=
                                                                                                        null
                                                                                ) {
                                                                                        val especeAnimal =
                                                                                                animalActuel
                                                                                                        .getEspece()
                                                                                        println(
                                                                                                "DEBUG FILTRAGE: Espèce de l'animal: ${especeAnimal.name} (${especeAnimal.label})"
                                                                                        )
                                                                                        val preferencesEspece =
                                                                                                prefsApp.getPreferencesEspece(
                                                                                                        especeAnimal
                                                                                                )
                                                                                        println(
                                                                                                "DEBUG FILTRAGE: Préférences trouvées pour cette espèce: ${preferencesEspece.getTotalSelectedNutrients()} nutriments"
                                                                                        )
                                                                                        val nutrimentsLabels =
                                                                                                convertirPreferencesVersLabelsNutriments(
                                                                                                        preferencesEspece
                                                                                                )
                                                                                        println(
                                                                                                "DEBUG FILTRAGE: Labels de nutriments extraits: $nutrimentsLabels"
                                                                                        )
                                                                                        if (nutrimentsLabels
                                                                                                        .isNotEmpty()
                                                                                        ) {
                                                                                                nutrimentsLabels
                                                                                        } else {
                                                                                                println(
                                                                                                        "DEBUG FILTRAGE: Aucun nutriment trouvé dans les préférences, utilisation de la liste par défaut"
                                                                                                )
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
                                                                                        println(
                                                                                                "DEBUG FILTRAGE: Animal ou préférences null, utilisation de la liste par défaut"
                                                                                        )
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

                                                                println(
                                                                        "DEBUG EXPRESSION: Type d'expression trouvé pour ${animal?.getEspece()?.label}: ${when (typeExpressionBesoin) {
                                                                        0 -> "par kg de poids métabolique"
                                                                        1 -> "par 1000 kcal"
                                                                        else -> "par kg de poids métabolique"
                                                                }}"
                                                                )

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
                                                                        isLargeView = !isCompact
                                                                )
                                                        }
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
                                                                modifier = Modifier.weight(1f)
                                                        )
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
                                                                        IconButton(
                                                                                onClick = {
                                                                                        rationToEdit =
                                                                                                null // Nouvelle ration
                                                                                        showRationEditDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                AppSizes.iconSizeMedium
                                                                                        )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Filled
                                                                                                .Add,
                                                                                        contentDescription =
                                                                                                "Ajouter une ration",
                                                                                        tint =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        }
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
                                                                                                        // Temporairement commenté jusqu'à l'implémentation de cette méthode
                                                                                                        showSnackbar(
                                                                                                                "Suppression de ration non implémentée"
                                                                                                        )
                                                                                                        // viewModel.deleteRation(ration)
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                // Segment 3: Liste des aliments de la
                                                // ration
                                                // sélectionnée
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
                                                                        Text(
                                                                                text =
                                                                                        "Aliments de la ration",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle2, // taille réduite
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        Row(
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically,
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingXSmall
                                                                                                )
                                                                        ) {
                                                                                // Bouton
                                                                                // pour
                                                                                // ajuster
                                                                                // la ration
                                                                                // (icône)
                                                                                val coroutineScope =
                                                                                        rememberCoroutineScope()
                                                                                val ration =
                                                                                        selectedRation
                                                                                val beTotal =
                                                                                        besoinEnergetiqueTotal
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                if (ration !=
                                                                                                                null &&
                                                                                                                beTotal !=
                                                                                                                        null &&
                                                                                                                beTotal >
                                                                                                                        0
                                                                                                ) {
                                                                                                        val energieApportee =
                                                                                                                ration.alimentMutableList
                                                                                                                        .sumOf {
                                                                                                                                alimentRation
                                                                                                                                ->
                                                                                                                                val densiteEnergetique =
                                                                                                                                        referenceUtilisee
                                                                                                                                                ?.let {
                                                                                                                                                        ref
                                                                                                                                                        ->
                                                                                                                                                        calculerDensiteEnergetique(
                                                                                                                                                                alimentRation,
                                                                                                                                                                ref
                                                                                                                                                        )
                                                                                                                                                }
                                                                                                                                                ?: 0.0
                                                                                                                                (densiteEnergetique *
                                                                                                                                        alimentRation
                                                                                                                                                .quantite) /
                                                                                                                                        100.0
                                                                                                                        }
                                                                                                        if (energieApportee >
                                                                                                                        0
                                                                                                        ) {
                                                                                                                val ratio =
                                                                                                                        beTotal /
                                                                                                                                energieApportee
                                                                                                                val alimentsAjustes =
                                                                                                                        ration.alimentMutableList
                                                                                                                                .map {
                                                                                                                                        alimentRation
                                                                                                                                        ->
                                                                                                                                        alimentRation
                                                                                                                                                .copy(
                                                                                                                                                        quantite =
                                                                                                                                                                (alimentRation
                                                                                                                                                                                .quantite *
                                                                                                                                                                                ratio)
                                                                                                                                                                        .toFloat()
                                                                                                                                                )
                                                                                                                                }
                                                                                                                coroutineScope
                                                                                                                        .launch {
                                                                                                                                viewModel
                                                                                                                                        .updateRationAliments(
                                                                                                                                                ration,
                                                                                                                                                alimentsAjustes
                                                                                                                                        )
                                                                                                                                showSnackbar(
                                                                                                                                        "Ration ajustée pour couvrir 100% du besoin énergétique total"
                                                                                                                                )
                                                                                                                        }
                                                                                                        } else {
                                                                                                                showSnackbar(
                                                                                                                        "Impossible d'ajuster : apport énergétique nul"
                                                                                                                )
                                                                                                        }
                                                                                                }
                                                                                        },
                                                                                        enabled =
                                                                                                ration !=
                                                                                                        null &&
                                                                                                        beTotal !=
                                                                                                                null &&
                                                                                                        beTotal >
                                                                                                                0 &&
                                                                                                        (ration.alimentMutableList
                                                                                                                .isNotEmpty()),
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector =
                                                                                                        Icons.Filled
                                                                                                                .Tune,
                                                                                                contentDescription =
                                                                                                        "Ajuster la ration",
                                                                                                tint =
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                        )
                                                                                }
                                                                                // Bouton
                                                                                // pour
                                                                                // ajouter
                                                                                // un
                                                                                // aliment
                                                                                IconButton(
                                                                                        onClick = {
                                                                                                // Vérifier que la ration existe avant d'afficher le dialogue
                                                                                                if (selectedRation !=
                                                                                                                null
                                                                                                ) {
                                                                                                        rationForAddAliment =
                                                                                                                selectedRation
                                                                                                        showAddAlimentView =
                                                                                                                true
                                                                                                } else {
                                                                                                        showSnackbar(
                                                                                                                "Sélectionnez d'abord une ration"
                                                                                                        )
                                                                                                }
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        AppSizes.iconSizeMedium
                                                                                                )
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector =
                                                                                                        Icons.Filled
                                                                                                                .Add,
                                                                                                contentDescription =
                                                                                                        "Ajouter un aliment",
                                                                                                tint =
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                        )
                                                                                }
                                                                        }
                                                                }

                                                                Divider()

                                                                if (selectedRation
                                                                                ?.alimentMutableList
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        // Message plus
                                                                        // explicite et
                                                                        // vérification que
                                                                        // la liste
                                                                        // est vide
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucun aliment dans cette ration",
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
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
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                items(
                                                                                        selectedRation
                                                                                                ?.alimentMutableList
                                                                                                ?: emptyList()
                                                                                ) { aliment ->
                                                                                        AlimentItem(
                                                                                                aliment =
                                                                                                        aliment,
                                                                                                isEditing =
                                                                                                        editingAlimentId ==
                                                                                                                aliment.uuid,
                                                                                                onStartEditing = {
                                                                                                        // Si une autre édition est en cours, valider cette édition d'abord
                                                                                                        if (editingAlimentId !=
                                                                                                                        null &&
                                                                                                                        editingAlimentId !=
                                                                                                                                aliment.uuid
                                                                                                        ) {
                                                                                                                editingAlimentId =
                                                                                                                        null
                                                                                                        }
                                                                                                        editingAlimentId =
                                                                                                                aliment.uuid
                                                                                                },
                                                                                                onQuantityChange = {
                                                                                                        newQuantity
                                                                                                        ->
                                                                                                        viewModel
                                                                                                                .updateAlimentQuantity(
                                                                                                                        aliment.uuid,
                                                                                                                        newQuantity
                                                                                                                )
                                                                                                },
                                                                                                onFinishEditing = {
                                                                                                        editingAlimentId =
                                                                                                                null
                                                                                                },
                                                                                                onDelete = {
                                                                                                        // Utilisation de l'UUID au lieu de l'objet
                                                                                                        viewModel
                                                                                                                .removeAlimentFromRation(
                                                                                                                        aliment.uuid
                                                                                                                )
                                                                                                }
                                                                                        )
                                                                                }
                                                                        }
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

                                                                        println(
                                                                                "DEBUG FILTRAGE: Animal=$animalActuel, Préférences=$prefsApp"
                                                                        )

                                                                        if (animalActuel != null &&
                                                                                        prefsApp !=
                                                                                                null
                                                                        ) {
                                                                                val especeAnimal =
                                                                                        animalActuel
                                                                                                .getEspece()
                                                                                println(
                                                                                        "DEBUG FILTRAGE: Espèce de l'animal: ${especeAnimal.name} (${especeAnimal.label})"
                                                                                )

                                                                                val preferencesEspece =
                                                                                        prefsApp.getPreferencesEspece(
                                                                                                especeAnimal
                                                                                        )
                                                                                println(
                                                                                        "DEBUG FILTRAGE: Préférences trouvées pour cette espèce: ${preferencesEspece.getTotalSelectedNutrients()} nutriments"
                                                                                )

                                                                                val nutrimentsLabels =
                                                                                        convertirPreferencesVersLabelsNutriments(
                                                                                                preferencesEspece
                                                                                        )
                                                                                println(
                                                                                        "DEBUG FILTRAGE: Labels de nutriments extraits: $nutrimentsLabels"
                                                                                )

                                                                                if (nutrimentsLabels
                                                                                                .isNotEmpty()
                                                                                ) {
                                                                                        nutrimentsLabels
                                                                                } else {
                                                                                        println(
                                                                                                "DEBUG FILTRAGE: Aucun nutriment trouvé dans les préférences, utilisation de la liste par défaut"
                                                                                        )
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
                                                                                println(
                                                                                        "DEBUG FILTRAGE: Animal ou préférences null, utilisation de la liste par défaut"
                                                                                )
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
                                                                        selectedConsultation?.weight
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
                                                                isLargeView = true
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
                                poidsAnimal = selectedConsultation?.weight?.toDouble(),
                                espece = animal?.getEspece() ?: Espece.CHIEN,
                                preferencesStorage = preferencesStorage,
                                onDismiss = {
                                        showNutrimentDetailDialog = false
                                        selectedNutrimentData = null
                                }
                        )
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
                                        label = "Poids corporel",
                                        value =
                                                selectedConsultation?.weight?.let {
                                                        "${String.format("%.1f", it)} kg"
                                                }
                                                        ?: "Non renseigné"
                                )

                                LocalInfoRow(
                                        label = "Poids métabolique",
                                        value =
                                                poidsMetabolique?.let {
                                                        TextUtils.formatKgPuissance075(it)
                                                }
                                                        ?: "Non calculé"
                                )

                                LocalInfoRow(
                                        label = "Besoin énergétique standard (BEE)",
                                        value =
                                                besoinEnergetiqueStandard?.let {
                                                        "${String.format("%.1f", it)} kcal/jour"
                                                }
                                                        ?: "Non calculé"
                                )

                                LocalInfoRow(
                                        label = "Besoin énergétique total",
                                        value =
                                                besoinEnergetiqueTotal?.let {
                                                        "${String.format("%.1f", it)} kcal/jour"
                                                }
                                                        ?: "Non calculé"
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
        currentValue: Float?,
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
                                String.format("%.2f", currentValue ?: 1.0f).replace('.', ',')
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
                                        val value = normalizedText.toFloatOrNull()
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
                                                                                        .toFloatOrNull()
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
                                                                        "$currentDescription (${String.format("%.2f", currentValue)})"
                                                                )
                                                        } else {
                                                                append(
                                                                        String.format(
                                                                                        "%.2f",
                                                                                        currentValue
                                                                                                ?: 1.0f
                                                                                )
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
                                                                                String.format(
                                                                                                "%.2f",
                                                                                                currentValue
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
                                                                "Coefficient: ${String.format("%.2f", coef.coef ?: 1.0f)}",
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
                                                        String.format("%.2f", currentValue)
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
                                                        referenceUtilisee?.getModk1()
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k1",
                                                                        coef.coef ?: 1.0f,
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
                                                        referenceUtilisee?.getModk2()
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k2",
                                                                        coef.coef ?: 1.0f,
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
                                                        referenceUtilisee?.getModk3()
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k3",
                                                                        coef.coef ?: 1.0f,
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
                                                        referenceUtilisee?.getModk4()
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k4",
                                                                        coef.coef ?: 1.0f,
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
                                                        referenceUtilisee?.getModk5()
                                                                ?: emptyList(),
                                                onCoefficientSelected = { coef ->
                                                        selectedConsultation?.let { consultation ->
                                                                viewModel.updateCoefficient(
                                                                        consultation.uuid,
                                                                        "k5",
                                                                        coef.coef ?: 1.0f,
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
                                                                                        String.format(
                                                                                                "%.2f",
                                                                                                it
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
