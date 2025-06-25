package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.View.AnalNut.AnalyseNutritionnelleCard
import fr.vetbrain.vetnutri_mp.View.AnalNut.NutrimentDetailDialog
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

// Constante pour l'exposant formaté
private const val EXPOSANT_075 = "⁰·⁷⁵"

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

        // États pour les dialogues
        var showRationEditDialog by remember { mutableStateOf(false) }
        var showAddAlimentDialog by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        // États pour les dialogues de section agrandie
        var showMetabolicValuesDialog by remember { mutableStateOf(false) }
        var showCoefficientsDialog by remember { mutableStateOf(false) }

        // États pour le dialog détaillé de nutriment
        var showNutrimentDetailDialog by remember { mutableStateOf(false) }
        var selectedNutrimentData by remember {
                mutableStateOf<Triple<String, ValeurNutritionnelle, Ration>?>(null)
        }

        Column(
                modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                if (selectedConsultation == null) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) { Text("Sélectionnez une consultation pour voir les rations") }
                } else {
                        // En-tête compact avec informations de consultation et valeurs métaboliques
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall
                        ) {
                                Column(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                AppSizes.paddingSmall
                                                        ), // Réduction du padding
                                        verticalArrangement =
                                                Arrangement.spacedBy(
                                                        AppSizes.paddingXSmall
                                                ) // Réduction de l'espacement
                                ) {
                                        // Titre principal avec bouton d'édition
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                        selectedConsultation?.date?.let { date ->
                                                                Text(
                                                                        text =
                                                                                "Consultation du $date",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1, // Réduction de h6 à subtitle1
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                        }
                                                        animal?.let { anim ->
                                                                Text(
                                                                        text =
                                                                                "Animal: ${anim.nom}",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .caption // Réduction de subtitle2 à caption
                                                                )
                                                        }
                                                }

                                                // Bouton d'édition de la consultation
                                                IconButton(
                                                        onClick = {
                                                                selectedConsultation?.let {
                                                                        consultation ->
                                                                        viewModel
                                                                                .editConsultationFullScreen(
                                                                                        consultation
                                                                                )
                                                                }
                                                        },
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeMedium
                                                                )
                                                ) {
                                                        Icon(
                                                                imageVector = Icons.Filled.Edit,
                                                                contentDescription =
                                                                        "Éditer la consultation",
                                                                tint = VetNutriColors.Primary
                                                        )
                                                }
                                        }

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        // Section Valeurs Métaboliques et Énergétiques (réorganisée
                                        // sur 2 lignes)
                                        Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // Colonne gauche - Valeurs métaboliques (2 lignes)
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Valeurs métaboliques",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .overline,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                IconButton(
                                                                        onClick = {
                                                                                showMetabolicValuesDialog =
                                                                                        true
                                                                        },
                                                                        modifier =
                                                                                Modifier.size(16.dp)
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Filled
                                                                                                .Search,
                                                                                contentDescription =
                                                                                        "Agrandir les valeurs métaboliques",
                                                                                tint =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                12.dp
                                                                                        )
                                                                        )
                                                                }
                                                        }

                                                        // Première ligne
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                CompactLocalInfoRow(
                                                                        label = "Poids",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.weight
                                                                                        ?.let {
                                                                                                "${
                                                                                                    String.format(
                                                                                                            "%.1f",
                                                                                                            it
                                                                                                    )
                                                                                                } kg"
                                                                                        }
                                                                                        ?: "Non renseigné"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                CompactLocalInfoRow(
                                                                        label = "P. métabolique",
                                                                        value =
                                                                                poidsMetabolique
                                                                                        ?.let {
                                                                                                TextUtils
                                                                                                        .formatKgPuissance075(
                                                                                                                it
                                                                                                        )
                                                                                        }
                                                                                        ?: "Non calculé"
                                                                )
                                                        }

                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        )

                                                        // Deuxième ligne
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                CompactLocalInfoRow(
                                                                        label =
                                                                                "B. énergétique", // Label raccourci
                                                                        value =
                                                                                besoinEnergetiqueStandard
                                                                                        ?.let {
                                                                                                "${String.format("%.0f", it)} kcal/j"
                                                                                        }
                                                                                        ?: "Non calculé"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                CompactLocalInfoRow(
                                                                        label = "Besoin total",
                                                                        value =
                                                                                besoinEnergetiqueTotal
                                                                                        ?.let {
                                                                                                "${String.format("%.0f", it)} kcal/j"
                                                                                        }
                                                                                        ?: "Non calculé"
                                                                )
                                                        }
                                                }

                                                // Colonne droite - Coefficients (2 lignes)
                                                Column(modifier = Modifier.weight(1f)) {
                                                        Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        text = "Coefficients",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .overline,
                                                                        fontWeight =
                                                                                FontWeight.Bold,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                IconButton(
                                                                        onClick = {
                                                                                showCoefficientsDialog =
                                                                                        true
                                                                        },
                                                                        modifier =
                                                                                Modifier.size(16.dp)
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Filled
                                                                                                .Search,
                                                                                contentDescription =
                                                                                        "Agrandir les coefficients",
                                                                                tint =
                                                                                        VetNutriColors
                                                                                                .Primary,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                12.dp
                                                                                        )
                                                                        )
                                                                }
                                                        }

                                                        // Première ligne des coefficients (K1, K2,
                                                        // K3)
                                                        Row(modifier = Modifier.fillMaxWidth()) {
                                                                CompactLocalInfoRow(
                                                                        label =
                                                                                "K1 (Stade physiologique)",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.k1Value
                                                                                        ?.let {
                                                                                                String.format(
                                                                                                        "%.2f",
                                                                                                        it
                                                                                                )
                                                                                        }
                                                                                        ?: "1.00"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingXSmall
                                                                                )
                                                                )
                                                                CompactLocalInfoRow(
                                                                        label = "K2 (Activité)",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.k2Value
                                                                                        ?.let {
                                                                                                String.format(
                                                                                                        "%.2f",
                                                                                                        it
                                                                                                )
                                                                                        }
                                                                                        ?: "1.00"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingXSmall
                                                                                )
                                                                )
                                                                CompactLocalInfoRow(
                                                                        label =
                                                                                "K3 (Environnement)",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.k3Value
                                                                                        ?.let {
                                                                                                String.format(
                                                                                                        "%.2f",
                                                                                                        it
                                                                                                )
                                                                                        }
                                                                                        ?: "1.00"
                                                                )
                                                        }

                                                        // Deuxième ligne des coefficients (K4, K5,
                                                        // Coeff. ajust.)
                                                        Row(modifier = Modifier.fillMaxWidth()) {
                                                                CompactLocalInfoRow(
                                                                        label =
                                                                                "K4 (État corporel)",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.k4Value
                                                                                        ?.let {
                                                                                                String.format(
                                                                                                        "%.2f",
                                                                                                        it
                                                                                                )
                                                                                        }
                                                                                        ?: "1.00"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingXSmall
                                                                                )
                                                                )
                                                                CompactLocalInfoRow(
                                                                        label = "K5 (Pathologie)",
                                                                        value =
                                                                                selectedConsultation
                                                                                        ?.k5Value
                                                                                        ?.let {
                                                                                                String.format(
                                                                                                        "%.2f",
                                                                                                        it
                                                                                                )
                                                                                        }
                                                                                        ?: "1.00"
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingXSmall
                                                                                )
                                                                )

                                                                // Coefficient d'ajustement avec
                                                                // icône d'édition
                                                                var isEditingCoefficient by remember {
                                                                        mutableStateOf(false)
                                                                }
                                                                var coefficientText by
                                                                        remember(
                                                                                selectedConsultation
                                                                        ) {
                                                                                mutableStateOf(
                                                                                        selectedConsultation
                                                                                                ?.coefficientAjustement
                                                                                                ?.toString()
                                                                                                ?: "1.0"
                                                                                )
                                                                        }

                                                                if (isEditingCoefficient) {
                                                                        // Mode édition
                                                                        OutlinedTextField(
                                                                                value =
                                                                                        coefficientText,
                                                                                onValueChange = {
                                                                                        coefficientText =
                                                                                                it
                                                                                },
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                        80.dp
                                                                                                )
                                                                                                .height(
                                                                                                        50.dp
                                                                                                ),
                                                                                textStyle =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                singleLine = true,
                                                                                keyboardOptions =
                                                                                        KeyboardOptions(
                                                                                                keyboardType =
                                                                                                        KeyboardType
                                                                                                                .Number
                                                                                        ),
                                                                                trailingIcon = {
                                                                                        Row(
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .spacedBy(
                                                                                                                        2.dp
                                                                                                                )
                                                                                        ) {
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                // Valider
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
                                                                                                        },
                                                                                                        modifier =
                                                                                                                Modifier.size(
                                                                                                                        24.dp
                                                                                                                )
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Filled
                                                                                                                        .Check,
                                                                                                                contentDescription =
                                                                                                                        "Valider",
                                                                                                                tint =
                                                                                                                        Color.Green
                                                                                                        )
                                                                                                }
                                                                                                IconButton(
                                                                                                        onClick = {
                                                                                                                // Annuler
                                                                                                                coefficientText =
                                                                                                                        selectedConsultation
                                                                                                                                ?.coefficientAjustement
                                                                                                                                ?.toString()
                                                                                                                                ?: "1.0"
                                                                                                                isEditingCoefficient =
                                                                                                                        false
                                                                                                        },
                                                                                                        modifier =
                                                                                                                Modifier.size(
                                                                                                                        24.dp
                                                                                                                )
                                                                                                ) {
                                                                                                        Icon(
                                                                                                                Icons.Filled
                                                                                                                        .Close,
                                                                                                                contentDescription =
                                                                                                                        "Annuler",
                                                                                                                tint =
                                                                                                                        Color.Red
                                                                                                        )
                                                                                                }
                                                                                        }
                                                                                }
                                                                        )
                                                                } else {
                                                                        // Mode affichage
                                                                        Row(
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                CompactLocalInfoRow(
                                                                                        label =
                                                                                                "Coeff. ajust.",
                                                                                        value =
                                                                                                selectedConsultation
                                                                                                        ?.coefficientAjustement
                                                                                                        ?.let {
                                                                                                                String.format(
                                                                                                                        "%.2f",
                                                                                                                        it
                                                                                                                )
                                                                                                        }
                                                                                                        ?: "1.00"
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
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.size(
                                                                                                        20.dp
                                                                                                )
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Filled
                                                                                                        .Edit,
                                                                                                contentDescription =
                                                                                                        "Éditer",
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                16.dp
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
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
                                                // Segment 2: Liste des rations de la consultation
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
                                                                // En-tête avec titre et bouton
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
                                                                                        "Rations de la consultation",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour ajouter une
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

                                                // Segment 3: Liste des aliments de la ration
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
                                                                // En-tête avec titre et bouton
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
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        // Bouton pour ajouter un
                                                                        // aliment
                                                                        IconButton(
                                                                                onClick = {
                                                                                        // Vérifier
                                                                                        // que la
                                                                                        // ration
                                                                                        // existe
                                                                                        // avant
                                                                                        // d'afficher le dialogue
                                                                                        if (selectedRation !=
                                                                                                        null
                                                                                        ) {
                                                                                                showAddAlimentDialog =
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

                                                                Divider()

                                                                if (selectedRation
                                                                                ?.alimentMutableList
                                                                                .isNullOrEmpty()
                                                                ) {
                                                                        // Message plus explicite et
                                                                        // vérification que la liste
                                                                        // est vide
                                                                        CenteredMessage(
                                                                                message =
                                                                                        "Aucun aliment dans cette ration",
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
                                                // Analyse nutritionnelle de la ration sélectionnée
                                                if (selectedRation != null) {
                                                        // Obtenir les nutriments sélectionnés selon
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
                                                                }
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
                                                        // Assurer que la ration est liée à la
                                                        // consultation
                                                        val newRation =
                                                                updatedRation.copy(
                                                                        idConsult =
                                                                                selectedConsultation
                                                                                        ?.uuid
                                                                                        ?: ""
                                                                )

                                                        selectedConsultation?.let { consultation ->
                                                                // Créer une copie de la liste des
                                                                // rations et y ajouter la nouvelle
                                                                // ration
                                                                val updatedRations =
                                                                        consultation.rations
                                                                                .toMutableList()
                                                                updatedRations.add(newRation)

                                                                // Mettre à jour la consultation
                                                                // avec la nouvelle liste de rations
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

                                                                // Sélectionner la nouvelle ration
                                                                viewModel.selectRation(newRation)

                                                                showSnackbar(
                                                                        "Ration '${newRation.name}' créée"
                                                                )
                                                        }
                                                } else {
                                                        // Mise à jour d'une ration existante
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

                        // Afficher le dialogue d'ajout d'aliment si nécessaire
                        if (showAddAlimentDialog) {
                                selectedRation?.let { ration ->
                                        AddAlimentDialog(
                                                viewModel = viewModel,
                                                onDismiss = { showAddAlimentDialog = false },
                                                onAddAliment = { aliment, quantite ->
                                                        // Ajouter l'aliment à la ration
                                                        viewModel.addAlimentToRation(
                                                                ration,
                                                                aliment,
                                                                quantite
                                                        )
                                                        showSnackbar(
                                                                "Aliment '${aliment.nom}' ajouté à la ration (${quantite}g)"
                                                        )
                                                        showAddAlimentDialog = false
                                                }
                                        )
                                }
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
        }

        // Dialog détaillé de nutriment
        if (showNutrimentDetailDialog && selectedNutrimentData != null) {
                val (nom, valeurNutritionnelle, ration) = selectedNutrimentData!!
                NutrimentDetailDialog(
                        nom = nom,
                        valeurNutritionnelle = valeurNutritionnelle,
                        ration = ration,
                        poidsMetabolique = poidsMetabolique,
                        referenceUtilisee = referenceUtilisee,
                        besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                        poidsAnimal = selectedConsultation?.weight?.toDouble(),
                        onDismiss = {
                                showNutrimentDetailDialog = false
                                selectedNutrimentData = null
                        }
                )
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
 * Dialogue pour ajouter un aliment à une ration
 *
 * @param viewModel ViewModel contenant les données de l'animal et des aliments
 * @param onDismiss Action à exécuter pour fermer le dialogue
 * @param onAddAliment Action à exécuter pour ajouter un aliment
 */
@Composable
fun AddAlimentDialog(
        viewModel: AnimalDetailViewModel,
        onDismiss: () -> Unit,
        onAddAliment: (AlimentEv, Float) -> Unit
) {
        // État pour la recherche
        var searchQuery by remember { mutableStateOf("") }
        // État pour la quantité
        var quantite by remember { mutableStateOf("100") }
        // État pour l'aliment sélectionné
        var selectedAliment by remember { mutableStateOf<AlimentEv?>(null) }
        // État d'erreur pour la quantité
        var quantiteError by remember { mutableStateOf(false) }

        // Charger les aliments au premier affichage du dialogue
        LaunchedEffect(Unit) { viewModel.loadAvailableFoods() }

        // Filtrer les aliments selon la recherche
        val filteredFoods = viewModel.getFilteredFoods(searchQuery)

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Ajouter un aliment", style = MaterialTheme.typography.h6) },
                text = {
                        Column(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(vertical = AppSizes.paddingSmall),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // Champ de recherche
                                OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        label = { Text("Rechercher un aliment") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                )

                                // Liste des aliments filtrés
                                if (viewModel.isLoadingFoods) {
                                        Box(
                                                modifier = Modifier.height(200.dp).fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                        ) { CircularProgressIndicator() }
                                } else if (filteredFoods.isEmpty()) {
                                        Box(
                                                modifier = Modifier.height(200.dp).fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                        ) { Text("Aucun aliment trouvé") }
                                } else {
                                        LazyColumn(
                                                modifier = Modifier.height(200.dp).fillMaxWidth(),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall)
                                        ) {
                                                items(filteredFoods) { aliment ->
                                                        Card(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .clickable {
                                                                                        selectedAliment =
                                                                                                aliment
                                                                                },
                                                                elevation = AppSizes.elevationSmall,
                                                                backgroundColor =
                                                                        if (selectedAliment?.uuid ==
                                                                                        aliment.uuid
                                                                        )
                                                                                VetNutriColors
                                                                                        .Primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.1f
                                                                                        )
                                                                        else
                                                                                MaterialTheme.colors
                                                                                        .surface
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                AppSizes.paddingSmall
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                aliment.nom
                                                                                                        ?: "Sans nom",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Bold
                                                                                )
                                                                                aliment.brand
                                                                                        ?.let {
                                                                                                brand
                                                                                                ->
                                                                                                Text(
                                                                                                        text =
                                                                                                                brand,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .caption
                                                                                                )
                                                                                        }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }

                                // Champ de quantité
                                OutlinedTextField(
                                        value = quantite,
                                        onValueChange = {
                                                quantite = it
                                                // Vérifier que la valeur est un nombre valide
                                                quantiteError =
                                                        try {
                                                                it.toFloat() <= 0
                                                        } catch (e: NumberFormatException) {
                                                                true
                                                        }
                                        },
                                        label = { Text("Quantité (g)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = quantiteError,
                                        keyboardOptions =
                                                KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                )

                                if (quantiteError) {
                                        Text(
                                                text = "Veuillez entrer une quantité valide > 0",
                                                color = Color.Red,
                                                style = MaterialTheme.typography.caption
                                        )
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = {
                                        // Vérifier que tous les champs sont valides
                                        val aliment = selectedAliment
                                        if (aliment != null && !quantiteError) {
                                                try {
                                                        val quantiteValue = quantite.toFloat()
                                                        onAddAliment(aliment, quantiteValue)
                                                } catch (e: NumberFormatException) {
                                                        // Si la conversion échoue, ne rien faire
                                                }
                                        }
                                },
                                enabled = selectedAliment != null && !quantiteError,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        )
                        ) { Text("Ajouter") }
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

/** Dialogue agrandi pour les coefficients */
@Composable
private fun CoefficientsDialog(
        selectedConsultation: ConsultationEv?,
        viewModel: AnimalDetailViewModel,
        onDismiss: () -> Unit
) {
        // État pour l'édition du coefficient
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
                        Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                                // Coefficients K (lecture seule pour l'instant)
                                LocalInfoRow(
                                        label = "K1 (Stade physiologique)",
                                        value =
                                                selectedConsultation?.k1Value?.let {
                                                        String.format("%.2f", it)
                                                }
                                                        ?: "1.00"
                                )

                                LocalInfoRow(
                                        label = "K2 (Activité)",
                                        value =
                                                selectedConsultation?.k2Value?.let {
                                                        String.format("%.2f", it)
                                                }
                                                        ?: "1.00"
                                )

                                LocalInfoRow(
                                        label = "K3 (Environnement)",
                                        value =
                                                selectedConsultation?.k3Value?.let {
                                                        String.format("%.2f", it)
                                                }
                                                        ?: "1.00"
                                )

                                LocalInfoRow(
                                        label = "K4 (État corporel)",
                                        value =
                                                selectedConsultation?.k4Value?.let {
                                                        String.format("%.2f", it)
                                                }
                                                        ?: "1.00"
                                )

                                LocalInfoRow(
                                        label = "K5 (Pathologie)",
                                        value =
                                                selectedConsultation?.k5Value?.let {
                                                        String.format("%.2f", it)
                                                }
                                                        ?: "1.00"
                                )

                                Divider()

                                // Coefficient d'ajustement éditable
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
                                                                modifier = Modifier.width(100.dp),
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
                                                                                ?.let { newValue ->
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
                                                                        isEditingCoefficient = false
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
                                                                        isEditingCoefficient = false
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
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                        IconButton(
                                                                onClick = {
                                                                        coefficientText =
                                                                                selectedConsultation
                                                                                        ?.coefficientAjustement
                                                                                        ?.toString()
                                                                                        ?: "1.0"
                                                                        isEditingCoefficient = true
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
                },
                confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
        )
}

/** Composant pour afficher une ligne d'information détaillée avec description */
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

// TODO: Réimplémentation des fonctions de préférences à faire plus tard
