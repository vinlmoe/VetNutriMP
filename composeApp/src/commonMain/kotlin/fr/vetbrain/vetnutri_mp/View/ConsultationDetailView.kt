package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.AppDatePicker
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Components.NumberTextField
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AppConsultationDetailView(
        consultation: ConsultationEv?,
        availableReferences: List<ReferenceEv> = emptyList(),
        onDismiss: () -> Unit,
        onSave: (ConsultationEv) -> Unit
) {
        // Déterminer si c'est une nouvelle consultation (UUID vide ou null)
        val isNewConsultation = consultation == null || consultation.uuid.isEmpty()

        var editedConsultation by
                remember(consultation) { mutableStateOf(consultation ?: ConsultationEv()) }
        var weightText by
                remember(consultation) { mutableStateOf(consultation?.weight?.toString() ?: "") }
        var showDateError by remember(consultation) { mutableStateOf(false) }
        var showWeightError by remember(consultation) { mutableStateOf(false) }
        var dateErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }
        var weightErrorMessage by remember(consultation) { mutableStateOf<String?>(null) }

        Scaffold(
                floatingActionButton = {
                        if (isNewConsultation) {
                                FloatingActionButton(
                                        onClick = {
                                                if (!showDateError &&
                                                                !showWeightError &&
                                                                editedConsultation.date != null
                                                ) {
                                                        if (editedConsultation.uuid.isEmpty()) {
                                                                editedConsultation =
                                                                        editedConsultation.copy(
                                                                                uuid =
                                                                                        kotlin.uuid
                                                                                                .Uuid
                                                                                                .random()
                                                                                                .toString()
                                                                        )
                                                        }
                                                        onSave(editedConsultation)
                                                } else if (editedConsultation.date == null) {
                                                        showDateError = true
                                                        dateErrorMessage = "La date est obligatoire"
                                                }
                                        },
                                        backgroundColor = VetNutriColors.Primary
                                ) {
                                        Icon(
                                                imageVector = AppIcons.Check,
                                                contentDescription = "Valider la consultation",
                                                tint = VetNutriColors.OnPrimary
                                        )
                                }
                        }
                }
        ) { paddingValues ->
                Column(
                        modifier =
                                Modifier.padding(paddingValues)
                                        .padding(AppSizes.paddingMedium)
                                        .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Titre
                        Text(
                                text =
                                        if (isNewConsultation) General.ADD.translate()
                                        else "Détails de la consultation",
                                style = MaterialTheme.typography.h6
                        )

                        Divider(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                thickness = AppSizes.dividerHeight
                        )

                        if (isNewConsultation) {
                                // Mode édition pour nouvelle consultation
                                // Date
                                AppDatePicker(
                                        selectedDate = editedConsultation.date,
                                        onDateSelected = { date: LocalDate ->
                                                editedConsultation =
                                                        editedConsultation.copy(date = date)
                                                showDateError = false
                                                dateErrorMessage = null
                                        },
                                        label = Consultation.DATE.translate(),
                                        isError = showDateError,
                                        errorMessage = dateErrorMessage,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Poids
                                NumberTextField(
                                        value = weightText,
                                        onValueChange = { newValue: String ->
                                                weightText = newValue
                                                try {
                                                        if (newValue.isNotEmpty()) {
                                                                val weight = newValue.toDouble()
                                                                editedConsultation =
                                                                        editedConsultation.copy(
                                                                                weight = weight
                                                                        )
                                                                showWeightError = false
                                                                weightErrorMessage = null
                                                        } else {
                                                                editedConsultation =
                                                                        editedConsultation.copy(
                                                                                weight = null
                                                                        )
                                                                showWeightError = false
                                                                weightErrorMessage = null
                                                        }
                                                } catch (e: Exception) {
                                                        showWeightError = true
                                                        weightErrorMessage =
                                                                "Format de poids invalide (nombre décimal)"
                                                }
                                        },
                                        label = Animal.WEIGHT.translate(),
                                        leadingIcon = AppIcons.Weight,
                                        isError = showWeightError,
                                        errorMessage = weightErrorMessage,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Objectif
                                AppTextField(
                                        value = editedConsultation.objectConsult,
                                        onValueChange = { newValue: String ->
                                                editedConsultation =
                                                        editedConsultation.copy(
                                                                objectConsult = newValue
                                                        )
                                        },
                                        label = Consultation.OBJECTIVE.translate(),
                                        leadingIcon = AppIcons.Info,
                                        modifier = Modifier.fillMaxWidth()
                                )

                                // Observations
                                AppTextField(
                                        value = editedConsultation.observation,
                                        onValueChange = { newValue: String ->
                                                editedConsultation =
                                                        editedConsultation.copy(
                                                                observation = newValue
                                                        )
                                        },
                                        label = Consultation.OBSERVATION.translate(),
                                        leadingIcon = AppIcons.Info,
                                        modifier = Modifier.fillMaxWidth().weight(1.0),
                                        maxLines = 5,
                                        singleLine = false
                                )
                        } else {
                                // Mode affichage pour consultation existante
                                InfoRow(
                                        label = Consultation.DATE.translate(),
                                        value = consultation?.date?.toString() ?: "Non renseignée"
                                )

                                InfoRow(
                                        label = Animal.WEIGHT.translate(),
                                        value =
                                                if (consultation?.weight != null)
                                                        "${consultation.weight} kg"
                                                else "Non renseigné"
                                )

                                InfoRow(
                                        label = Consultation.OBJECTIVE.translate(),
                                        value =
                                                consultation?.objectConsult?.ifBlank {
                                                        "Non renseigné"
                                                }
                                                        ?: "Non renseigné"
                                )

                                // Observations avec plus d'espace
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                                text = "${Consultation.OBSERVATION.translate()} :",
                                                style = MaterialTheme.typography.subtitle1,
                                                modifier =
                                                        Modifier.padding(
                                                                vertical = AppSizes.paddingXSmall
                                                        )
                                        )
                                        Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                elevation = 2.dp,
                                                backgroundColor = MaterialTheme.colors.surface
                                        ) {
                                                Text(
                                                        text =
                                                                consultation?.observation?.ifBlank {
                                                                        "Aucune observation"
                                                                }
                                                                        ?: "Aucune observation",
                                                        style = MaterialTheme.typography.body1,
                                                        modifier =
                                                                Modifier.padding(
                                                                        AppSizes.paddingMedium
                                                                )
                                                )
                                        }
                                }
                        }

                        // Section Note d'État Corporel (BCS)
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall,
                                backgroundColor = VetNutriColors.Surface
                        ) {
                                Column(
                                        modifier = Modifier.padding(AppSizes.paddingMedium),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        Text(
                                                text = "Note d'État Corporel (BCS)",
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        val bcsValue =
                                                if (isNewConsultation) editedConsultation.BCS
                                                else consultation?.BCS
                                        val bcsDescription = getBCSDescription(bcsValue)

                                        InfoRow(
                                                label = "Note BCS",
                                                value =
                                                        if (bcsValue != null) "$bcsValue/9"
                                                        else "Non renseigné"
                                        )

                                        if (bcsValue != null) {
                                                Text(
                                                        text = bcsDescription,
                                                        style = MaterialTheme.typography.body2,
                                                        color = Color.Gray,
                                                        modifier =
                                                                Modifier.padding(
                                                                        start =
                                                                                AppSizes.paddingSmall
                                                                )
                                                )
                                        }
                                }
                        }

                        // Section Valeurs Métaboliques et Énergétiques
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall,
                                backgroundColor = VetNutriColors.Surface
                        ) {
                                Column(
                                        modifier = Modifier.padding(AppSizes.paddingMedium),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        Text(
                                                text = "Valeurs métaboliques et énergétiques",
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        if (isNewConsultation) {
                                                // Mode édition - champs éditables pour nouvelle
                                                // consultation
                                                InfoRow(
                                                        label = "Poids métabolique",
                                                        value = "Non calculé"
                                                )
                                                InfoRow(
                                                        label = "Besoin énergétique à l'entretien",
                                                        value = "Non calculé"
                                                )
                                                InfoRow(
                                                        label = "Besoin énergétique",
                                                        value = "Non calculé"
                                                )

                                                // Section Coefficients
                                                Text(
                                                        text = "Coefficients",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier =
                                                                Modifier.padding(
                                                                        top = AppSizes.paddingSmall
                                                                )
                                                )

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                InfoRow(
                                                                        label = "Coefficient 1",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 2",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 3",
                                                                        value = "Non défini"
                                                                )
                                                        }
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        )
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                InfoRow(
                                                                        label = "Coefficient 4",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 5",
                                                                        value = "Non défini"
                                                                )
                                                        }
                                                }
                                        } else {
                                                // Mode affichage - valeurs en lecture seule
                                                InfoRow(
                                                        label = "Poids métabolique",
                                                        value = "Non calculé" // TODO: Calculer avec
                                                        // formule
                                                        // appropriée
                                                        )
                                                InfoRow(
                                                        label = "Besoin énergétique à l'entretien",
                                                        value = "Non calculé" // TODO: Calculer avec
                                                        // formule
                                                        // appropriée
                                                        )
                                                InfoRow(
                                                        label = "Besoin énergétique",
                                                        value = "Non calculé" // TODO: Calculer avec
                                                        // formule
                                                        // appropriée
                                                        )

                                                // Section Coefficients
                                                Text(
                                                        text = "Coefficients",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier =
                                                                Modifier.padding(
                                                                        top = AppSizes.paddingSmall
                                                                )
                                                )

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                InfoRow(
                                                                        label = "Coefficient 1",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 2",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 3",
                                                                        value = "Non défini"
                                                                )
                                                        }
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        )
                                                        Column(modifier = Modifier.weight(1f)) {
                                                                InfoRow(
                                                                        label = "Coefficient 4",
                                                                        value = "Non défini"
                                                                )
                                                                InfoRow(
                                                                        label = "Coefficient 5",
                                                                        value = "Non défini"
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }

                        // Liste des rations
                        Text(text = "Rations", style = MaterialTheme.typography.subtitle1)

                        if (isNewConsultation) {
                                // Mode édition - affichage simple du nombre de rations
                                val rationCount = editedConsultation.rations.size
                                val rationCountActuelle =
                                        editedConsultation.rations.count { !it.actual }
                                val rationCountProposee =
                                        editedConsultation.rations.count { it.actual }

                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium)
                                        ) {
                                                InfoRow(
                                                        label = "Total",
                                                        value = "$rationCount ration(s)"
                                                )
                                                InfoRow(
                                                        label = "Actuelles",
                                                        value = "$rationCountActuelle ration(s)"
                                                )
                                                InfoRow(
                                                        label = "Proposées",
                                                        value = "$rationCountProposee ration(s)"
                                                )
                                        }
                                }
                        } else {
                                // Mode affichage - nombre de rations en lecture seule
                                val rationCount = consultation?.rations?.size ?: 0
                                val rationCountActuelle =
                                        consultation?.rations?.count { !it.actual } ?: 0
                                val rationCountProposee =
                                        consultation?.rations?.count { it.actual } ?: 0

                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = MaterialTheme.colors.surface
                                ) {
                                        Column(
                                                modifier = Modifier.padding(AppSizes.paddingMedium)
                                        ) {
                                                InfoRow(
                                                        label = "Total",
                                                        value = "$rationCount ration(s)"
                                                )
                                                InfoRow(
                                                        label = "Actuelles",
                                                        value = "$rationCountActuelle ration(s)"
                                                )
                                                InfoRow(
                                                        label = "Proposées",
                                                        value = "$rationCountProposee ration(s)"
                                                )
                                        }
                                }
                        }

                        Divider(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                thickness = AppSizes.dividerHeight
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        // Section Références Nutritionnelles
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = AppSizes.elevationSmall,
                                backgroundColor = VetNutriColors.Surface
                        ) {
                                Column(
                                        modifier = Modifier.padding(AppSizes.paddingMedium),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingSmall)
                                ) {
                                        Text(
                                                text = "Références nutritionnelles",
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        Divider(color = VetNutriColors.Primary.copy(alpha = 0.3f))

                                        if (isNewConsultation) {
                                                // Mode édition des références pour nouvelle
                                                // consultation
                                                // Référence générale
                                                Text(
                                                        text = "Référence générale",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold
                                                )

                                                OutlinedTextField(
                                                        value =
                                                                editedConsultation
                                                                        .referenceGeneraleId
                                                                        ?: "Aucune référence sélectionnée",
                                                        onValueChange = {},
                                                        label = { Text("Référence générale") },
                                                        readOnly = true,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        trailingIcon = {
                                                                IconButton(
                                                                        onClick = {
                                                                                // TODO: Ouvrir le
                                                                                // sélecteur
                                                                                // de
                                                                                // références
                                                                        }
                                                                ) {
                                                                        Icon(
                                                                                AppIcons.ArrowDropDown,
                                                                                contentDescription =
                                                                                        "Sélectionner une référence"
                                                                        )
                                                                }
                                                        }
                                                )

                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        AppSizes.paddingSmall
                                                                )
                                                )

                                                // Références de maladies
                                                Text(
                                                        text = "Références de maladies",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold
                                                )

                                                if (editedConsultation.referencesMaladies.isEmpty()
                                                ) {
                                                        Text(
                                                                text =
                                                                        "Aucune référence de maladie",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = Color.Gray,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        )
                                                } else {
                                                        editedConsultation.referencesMaladies
                                                                .forEach { referenceId ->
                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                elevation = 2.dp,
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.1f
                                                                                                )
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.padding(
                                                                                                        AppSizes.paddingSmall
                                                                                                ),
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .SpaceBetween,
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Text(
                                                                                                text =
                                                                                                        "Référence: $referenceId",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2,
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                1f
                                                                                                        )
                                                                                        )
                                                                                        IconButton(
                                                                                                onClick = {
                                                                                                        editedConsultation
                                                                                                                .supprimerReferenceMaladie(
                                                                                                                        referenceId
                                                                                                                )
                                                                                                }
                                                                                        ) {
                                                                                                Icon(
                                                                                                        AppIcons.Delete,
                                                                                                        contentDescription =
                                                                                                                "Supprimer la référence",
                                                                                                        tint =
                                                                                                                Color.Red
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                }

                                                // Bouton pour ajouter une référence de maladie
                                                OutlinedButton(
                                                        onClick = {
                                                                // TODO: Ouvrir le sélecteur de
                                                                // références
                                                                // de
                                                                // maladies
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors =
                                                                ButtonDefaults.outlinedButtonColors(
                                                                        contentColor =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                ) {
                                                        Icon(
                                                                AppIcons.Add,
                                                                contentDescription =
                                                                        "Ajouter une référence de maladie",
                                                                modifier =
                                                                        Modifier.size(
                                                                                AppSizes.iconSizeSmall
                                                                        )
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        )
                                                        Text("Ajouter une référence de maladie")
                                                }
                                        } else {
                                                // Mode affichage des références pour consultation
                                                // existante
                                                InfoRow(
                                                        label = "Référence générale",
                                                        value =
                                                                consultation?.referenceGeneraleId
                                                                        ?.let { id ->
                                                                                if (id.isBlank())
                                                                                        "Aucune"
                                                                                else
                                                                                        getReferenceNameById(
                                                                                                id,
                                                                                                availableReferences
                                                                                        )
                                                                        }
                                                                        ?: "Aucune"
                                                )

                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                        Text(
                                                                text = "Références de maladies :",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .subtitle2,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier =
                                                                        Modifier.padding(
                                                                                vertical =
                                                                                        AppSizes.paddingXSmall
                                                                        )
                                                        )

                                                        if (consultation?.referencesMaladies
                                                                        ?.isEmpty() == true
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Aucune référence de maladie",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .body2,
                                                                        color = Color.Gray,
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                        } else {
                                                                consultation?.referencesMaladies
                                                                        ?.forEach { referenceId ->
                                                                                Card(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                vertical =
                                                                                                                        2.dp
                                                                                                        ),
                                                                                        elevation =
                                                                                                1.dp,
                                                                                        backgroundColor =
                                                                                                VetNutriColors
                                                                                                        .Secondary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        )
                                                                                ) {
                                                                                        Text(
                                                                                                text =
                                                                                                        getReferenceNameById(
                                                                                                                referenceId,
                                                                                                                availableReferences
                                                                                                        ),
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2,
                                                                                                modifier =
                                                                                                        Modifier.padding(
                                                                                                                AppSizes.paddingSmall
                                                                                                        )
                                                                                        )
                                                                                }
                                                                        }
                                                        }
                                                }
                                        }
                                }
                        }

                        Spacer(modifier = Modifier.height(AppSizes.paddingLarge))

                        // Boutons d'action
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                Button(
                                        onClick = onDismiss,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                ),
                                        modifier = Modifier.weight(1f)
                                ) {
                                        Row(
                                                horizontalArrangement =
                                                        Arrangement.spacedBy(AppSizes.paddingSmall),
                                                verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                Icon(
                                                        if (isNewConsultation) AppIcons.Cancel
                                                        else AppIcons.ArrowBack,
                                                        contentDescription = null,
                                                        modifier =
                                                                Modifier.size(
                                                                        AppSizes.iconSizeSmall
                                                                )
                                                )
                                                Text(
                                                        if (isNewConsultation)
                                                                General.CANCEL.translate()
                                                        else "Fermer"
                                                )
                                        }
                                }
                                // Le FAB gère la validation/sauvegarde
                                Spacer(modifier = Modifier.weight(1f))
                        }
                }
        }
}

/** Fonction helper pour obtenir la description du BCS */
private fun getBCSDescription(bcs: Int?): String {
        return when (bcs) {
                1 -> "Très maigre - Côtes, vertèbres et os du bassin très saillants"
                2 -> "Maigre - Côtes facilement palpables, graisse corporelle minimale"
                3 -> "Mince - Côtes palpables avec une légère couche de graisse"
                4 -> "Sous-optimal - Côtes palpables avec un peu d'effort"
                5 -> "Idéal - Côtes palpables sans excès de graisse"
                6 -> "Légèrement en surpoids - Côtes difficilement palpables"
                7 -> "En surpoids - Côtes difficiles à palper, graisse visible"
                8 -> "Obèse - Côtes non palpables, accumulation de graisse"
                9 -> "Très obèse - Accumulation massive de graisse, problèmes de mobilité"
                else -> "Non renseigné"
        }
}

/** Fonction helper pour obtenir le nom d'une référence par son UUID */
private fun getReferenceNameById(
        referenceId: String,
        availableReferences: List<ReferenceEv>
): String {
        return availableReferences.find { it.uuid == referenceId }?.nom ?: "Référence inconnue"
}
