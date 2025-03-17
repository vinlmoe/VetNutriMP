package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AlimentItem
import fr.vetbrain.vetnutri_mp.Components.CenteredMessage
import fr.vetbrain.vetnutri_mp.Components.RationItem
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

/**
 * Vue pour afficher les rations d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param showSnackbar Action à exécuter pour afficher un message snackbar
 */
@Composable
fun RationsView(viewModel: AnimalDetailViewModel, showSnackbar: (String) -> Unit) {
        val animal by viewModel.animal.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        val selectedRation by viewModel.selectedRation.collectAsState()

        // État pour la répartition de l'espace entre les colonnes (valeur entre 0.2f et 0.8f)
        var repartitionColonnes by remember { mutableStateOf(0.5f) }

        // États pour les dialogues
        var showRationEditDialog by remember { mutableStateOf(false) }
        var showAddAlimentDialog by remember { mutableStateOf(false) }
        var rationToEdit by remember { mutableStateOf<Ration?>(null) }
        var editingAlimentId by remember { mutableStateOf<String?>(null) }

        Column(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
                Text(
                        text = "Rations",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = AppSizes.paddingSmall)
                )

                if (selectedConsultation == null) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) { Text("Sélectionnez une consultation pour voir les rations") }
                } else {
                        // En-tête avec informations de consultation et ration
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall
                        ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(AppSizes.paddingMedium),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                                selectedConsultation?.date?.let { date ->
                                                        Text(
                                                                text = "Consultation du $date",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle1
                                                        )
                                                }
                                                selectedRation?.let { ration ->
                                                        Text(
                                                                text =
                                                                        "${ration.name} (${if (ration.actual) "Proposée" else "Actuelle"})",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                fontWeight = FontWeight.Bold
                                                        )
                                                }
                                        }

                                        // Contrôle de répartition de l'espace
                                        Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier =
                                                        Modifier.width(
                                                                AppSizes.iconSizeXLarge.times(3f)
                                                        )
                                        ) {
                                                Text(
                                                        text = "Répartition",
                                                        style = MaterialTheme.typography.caption,
                                                        color = Color.Gray
                                                )
                                                Slider(
                                                        value = repartitionColonnes,
                                                        onValueChange = {
                                                                repartitionColonnes = it
                                                        },
                                                        valueRange = 0.2f..0.8f,
                                                        steps = 5,
                                                        colors =
                                                                SliderDefaults.colors(
                                                                        thumbColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        activeTrackColor =
                                                                                VetNutriColors
                                                                                        .Primary,
                                                                        inactiveTrackColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.3f
                                                                                        )
                                                                )
                                                )
                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                text = "Listes",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                        Text(
                                                                text = "Analyse",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .caption,
                                                                color = Color.Gray
                                                        )
                                                }
                                        }
                                }
                        }

                        // Contenu principal - grille 2x2 de cartes
                        Box(modifier = Modifier.weight(1f)) {
                                Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                        // Colonne gauche (listes) - poids dynamique basé sur
                                        // repartitionColonnes
                                        Column(
                                                modifier = Modifier.weight(repartitionColonnes),
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

                                        // Colonne droite (analyses) - poids dynamique basé sur (1 -
                                        // repartitionColonnes)
                                        Column(
                                                modifier = Modifier.weight(1 - repartitionColonnes),
                                                verticalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingMedium)
                                        ) {
                                                // TODO: Ajouter les composants d'analyse ici quand
                                                // nécessaire
                                                // Pour l'instant, ajoutons juste un placeholder
                                                Card(
                                                        modifier = Modifier.fillMaxSize(),
                                                        elevation = AppSizes.elevationMedium
                                                ) {
                                                        Box(
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentAlignment = Alignment.Center
                                                        ) { Text("Fonctionnalités d'analyse") }
                                                }
                                        }
                                }
                        }

                        // TODO: Réimplémentez les dialogues d'édition ici quand nécessaire
                }
        }
}
