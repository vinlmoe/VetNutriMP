package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun AnimalDetailView(
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onNavigateBack: () -> Unit,
        modifier: Modifier = Modifier
) {
    val animal by viewModel.animal.collectAsState()
    val selectedConsultation by viewModel.selectedConsultation.collectAsState()
    var showConsultationDetail by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Surface(
                        modifier = Modifier.width(AppSizes.drawerWidth),
                        elevation = AppSizes.elevationMedium,
                        color = MaterialTheme.colors.surface
                ) {
                    Column(
                            modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                    ) {
                        Text(
                                text = "Détails de l'animal",
                                style =
                                        MaterialTheme.typography.h6.copy(
                                                fontSize = AppSizes.fontSizeH6
                                        )
                        )
                        Divider()
                        animal?.let { animalDetails ->
                            Text(
                                    text = "Nom: ${animalDetails.nom}",
                                    style =
                                            MaterialTheme.typography.body1.copy(
                                                    fontSize = AppSizes.fontSizeBody1
                                            )
                            )
                            Text(
                                    text = "Espèce: ${animalDetails.getEspece().label}",
                                    style =
                                            MaterialTheme.typography.body1.copy(
                                                    fontSize = AppSizes.fontSizeBody1
                                            )
                            )
                            Text(
                                    text = "Race: ${animalDetails.race}",
                                    style =
                                            MaterialTheme.typography.body1.copy(
                                                    fontSize = AppSizes.fontSizeBody1
                                            )
                            )
                            Text(
                                    text = "Sexe: ${animalDetails.getSex().label}",
                                    style =
                                            MaterialTheme.typography.body1.copy(
                                                    fontSize = AppSizes.fontSizeBody1
                                            )
                            )
                            animalDetails.birthdate?.let { birthdate ->
                                val today =
                                        Clock.System.now()
                                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                                .date
                                val age =
                                        today.year -
                                                birthdate.year -
                                                if (today.monthNumber < birthdate.monthNumber ||
                                                                (today.monthNumber ==
                                                                        birthdate.monthNumber &&
                                                                        today.dayOfMonth <
                                                                                birthdate
                                                                                        .dayOfMonth)
                                                )
                                                        1
                                                else 0
                                Text(
                                        text = "Date de naissance: ${birthdate}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                                Text(
                                        text = "Âge: $age ans",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                            }
                            if (animalDetails.ownerName.isNotEmpty()) {
                                Text(
                                        text = "Propriétaire: ${animalDetails.ownerName}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                            }
                            if (animalDetails.summary.isNotEmpty()) {
                                Text(
                                        text = "Résumé: ${animalDetails.summary}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                            }
                            if (animalDetails.weightHistory.isNotEmpty()) {
                                val lastWeight = animalDetails.weightHistory.maxByOrNull { it.date }
                                lastWeight?.let {
                                    Text(
                                            text = "Dernier poids: ${it.value} kg (${it.date})",
                                            style =
                                                    MaterialTheme.typography.body1.copy(
                                                            fontSize = AppSizes.fontSizeBody1
                                                    )
                                    )
                                }
                            }
                        }
                    }
                }

                // Contenu principal
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                                modifier =
                                        Modifier.weight(0.4f)
                                                .fillMaxHeight()
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            Button(
                                    onClick = {
                                        val currentMoment = Clock.System.now()
                                        val localDateTime =
                                                currentMoment.toLocalDateTime(
                                                        TimeZone.currentSystemDefault()
                                                )
                                        val currentDate = localDateTime.date
                                        viewModel.prepareNewConsultation(currentDate)
                                        viewModel.startEditingConsultation()
                                        showConsultationDetail = true
                                    },
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Primary,
                                                    contentColor = VetNutriColors.OnPrimary
                                            ),
                                    modifier = Modifier.fillMaxWidth().height(AppSizes.buttonHeight)
                            ) {
                                Text(
                                        General.ADD.translate(),
                                        style =
                                                MaterialTheme.typography.button.copy(
                                                        fontSize = AppSizes.fontSizeBody2
                                                )
                                )
                            }

                            LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(AppSizes.cardSpacing)
                            ) {
                                animal?.consultations?.let { consultations ->
                                    items(consultations) { consultation ->
                                        ConsultationCard(
                                                consultation = consultation,
                                                isSelected =
                                                        consultation.uuid ==
                                                                selectedConsultation?.uuid,
                                                onClick = {
                                                    viewModel.selectConsultation(consultation)
                                                    showConsultationDetail = true
                                                },
                                                onEdit = {
                                                    viewModel.selectConsultation(consultation)
                                                    viewModel.startEditingConsultation()
                                                    showConsultationDetail = true
                                                },
                                                onDelete = {
                                                    viewModel.deleteConsultation(consultation)
                                                }
                                        )
                                    }
                                }
                            }

                            Button(
                                    onClick = onNavigateBack,
                                    colors =
                                            ButtonDefaults.buttonColors(
                                                    backgroundColor = VetNutriColors.Secondary,
                                                    contentColor = VetNutriColors.OnSecondary
                                            ),
                                    modifier = Modifier.fillMaxWidth().height(AppSizes.buttonHeight)
                            ) {
                                Text(
                                        General.CANCEL.translate(),
                                        style =
                                                MaterialTheme.typography.button.copy(
                                                        fontSize = AppSizes.fontSizeBody2
                                                )
                                )
                            }
                        }

                        Box(
                                modifier =
                                        Modifier.weight(0.6f)
                                                .fillMaxHeight()
                                                .padding(AppSizes.paddingMedium)
                        ) {
                            if (showConsultationDetail && selectedConsultation != null) {
                                ConsultationDetailView(
                                        consultation = selectedConsultation,
                                        onDismiss = {
                                            showConsultationDetail = false
                                            viewModel.stopEditingConsultation()
                                        },
                                        onSave = { updatedConsultation ->
                                            viewModel.updateConsultation(updatedConsultation)
                                            showConsultationDetail = false
                                            viewModel.stopEditingConsultation()
                                        }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            ModalDrawer(
                    drawerContent = {
                        Column(
                                modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                            Text(
                                    text = "Détails de l'animal",
                                    style =
                                            MaterialTheme.typography.h6.copy(
                                                    fontSize = AppSizes.fontSizeH6
                                            )
                            )
                            Divider()
                            animal?.let { animalDetails ->
                                Text(
                                        text = "Nom: ${animalDetails.nom}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                                Text(
                                        text = "Espèce: ${animalDetails.getEspece().label}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                                Text(
                                        text = "Race: ${animalDetails.race}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                                Text(
                                        text = "Sexe: ${animalDetails.getSex().label}",
                                        style =
                                                MaterialTheme.typography.body1.copy(
                                                        fontSize = AppSizes.fontSizeBody1
                                                )
                                )
                                animalDetails.birthdate?.let { birthdate ->
                                    val today =
                                            Clock.System.now()
                                                    .toLocalDateTime(
                                                            TimeZone.currentSystemDefault()
                                                    )
                                                    .date
                                    val age =
                                            today.year -
                                                    birthdate.year -
                                                    if (today.monthNumber < birthdate.monthNumber ||
                                                                    (today.monthNumber ==
                                                                            birthdate.monthNumber &&
                                                                            today.dayOfMonth <
                                                                                    birthdate
                                                                                            .dayOfMonth)
                                                    )
                                                            1
                                                    else 0
                                    Text(
                                            text = "Date de naissance: ${birthdate}",
                                            style =
                                                    MaterialTheme.typography.body1.copy(
                                                            fontSize = AppSizes.fontSizeBody1
                                                    )
                                    )
                                    Text(
                                            text = "Âge: $age ans",
                                            style =
                                                    MaterialTheme.typography.body1.copy(
                                                            fontSize = AppSizes.fontSizeBody1
                                                    )
                                    )
                                }
                                if (animalDetails.ownerName.isNotEmpty()) {
                                    Text(
                                            text = "Propriétaire: ${animalDetails.ownerName}",
                                            style =
                                                    MaterialTheme.typography.body1.copy(
                                                            fontSize = AppSizes.fontSizeBody1
                                                    )
                                    )
                                }
                                if (animalDetails.summary.isNotEmpty()) {
                                    Text(
                                            text = "Résumé: ${animalDetails.summary}",
                                            style =
                                                    MaterialTheme.typography.body1.copy(
                                                            fontSize = AppSizes.fontSizeBody1
                                                    )
                                    )
                                }
                                if (animalDetails.weightHistory.isNotEmpty()) {
                                    val lastWeight =
                                            animalDetails.weightHistory.maxByOrNull { it.date }
                                    lastWeight?.let {
                                        Text(
                                                text = "Dernier poids: ${it.value} kg (${it.date})",
                                                style =
                                                        MaterialTheme.typography.body1.copy(
                                                                fontSize = AppSizes.fontSizeBody1
                                                        )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    drawerState = drawerState,
                    gesturesEnabled = true,
                    drawerBackgroundColor = MaterialTheme.colors.surface,
                    content = {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                    modifier =
                                            Modifier.fillMaxWidth().padding(AppSizes.paddingMedium),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                        onClick = {
                                            scope.launch {
                                                if (drawerState.isClosed) drawerState.open()
                                                else drawerState.close()
                                            }
                                        },
                                        modifier = Modifier.size(AppSizes.iconSizeLarge)
                                ) {
                                    Icon(
                                            Icons.Default.Menu,
                                            contentDescription = "Menu",
                                            modifier = Modifier.size(AppSizes.iconSizeMedium)
                                    )
                                }
                            }

                            if (showConsultationDetail && selectedConsultation != null) {
                                ConsultationDetailView(
                                        consultation = selectedConsultation,
                                        onDismiss = {
                                            showConsultationDetail = false
                                            viewModel.stopEditingConsultation()
                                        },
                                        onSave = { updatedConsultation ->
                                            viewModel.updateConsultation(updatedConsultation)
                                            showConsultationDetail = false
                                            viewModel.stopEditingConsultation()
                                        }
                                )
                            } else {
                                Column(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .padding(AppSizes.paddingMedium),
                                        verticalArrangement =
                                                Arrangement.spacedBy(AppSizes.paddingMedium)
                                ) {
                                    Button(
                                            onClick = {
                                                val currentMoment = Clock.System.now()
                                                val localDateTime =
                                                        currentMoment.toLocalDateTime(
                                                                TimeZone.currentSystemDefault()
                                                        )
                                                val currentDate = localDateTime.date
                                                viewModel.prepareNewConsultation(currentDate)
                                                viewModel.startEditingConsultation()
                                                showConsultationDetail = true
                                            },
                                            colors =
                                                    ButtonDefaults.buttonColors(
                                                            backgroundColor =
                                                                    VetNutriColors.Primary,
                                                            contentColor = VetNutriColors.OnPrimary
                                                    ),
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .height(AppSizes.buttonHeight)
                                    ) {
                                        Text(
                                                General.ADD.translate(),
                                                style =
                                                        MaterialTheme.typography.button.copy(
                                                                fontSize = AppSizes.fontSizeBody2
                                                        )
                                        )
                                    }

                                    LazyColumn(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement =
                                                    Arrangement.spacedBy(AppSizes.cardSpacing)
                                    ) {
                                        animal?.consultations?.let { consultations ->
                                            items(consultations) { consultation ->
                                                ConsultationCard(
                                                        consultation = consultation,
                                                        isSelected =
                                                                consultation.uuid ==
                                                                        selectedConsultation?.uuid,
                                                        onClick = {
                                                            viewModel.selectConsultation(
                                                                    consultation
                                                            )
                                                            showConsultationDetail = true
                                                        },
                                                        onEdit = {
                                                            viewModel.selectConsultation(
                                                                    consultation
                                                            )
                                                            viewModel.startEditingConsultation()
                                                            showConsultationDetail = true
                                                        },
                                                        onDelete = {
                                                            viewModel.deleteConsultation(
                                                                    consultation
                                                            )
                                                        }
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                            onClick = onNavigateBack,
                                            colors =
                                                    ButtonDefaults.buttonColors(
                                                            backgroundColor =
                                                                    VetNutriColors.Secondary,
                                                            contentColor =
                                                                    VetNutriColors.OnSecondary
                                                    ),
                                            modifier =
                                                    Modifier.fillMaxWidth()
                                                            .height(AppSizes.buttonHeight)
                                    ) {
                                        Text(
                                                General.CANCEL.translate(),
                                                style =
                                                        MaterialTheme.typography.button.copy(
                                                                fontSize = AppSizes.fontSizeBody2
                                                        )
                                        )
                                    }
                                }
                            }
                        }
                    }
            )
        }
    }
}

@Composable
private fun ConsultationCard(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
            modifier = modifier.fillMaxWidth(),
            elevation =
                    if (isSelected) AppSizes.cardElevationSelected
                    else AppSizes.cardElevationNormal,
            backgroundColor =
                    if (isSelected) VetNutriColors.Secondary else MaterialTheme.colors.surface
    ) {
        Column(
                modifier = Modifier.clickable(onClick = onEdit).padding(AppSizes.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXXSmall)
        ) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = consultation.date?.toString() ?: "",
                        style =
                                MaterialTheme.typography.subtitle1.copy(
                                        fontSize = AppSizes.fontSizeSubtitle1
                                )
                )
                IconButton(
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.size(AppSizes.iconSizeLarge)
                ) {
                    Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            modifier = Modifier.size(AppSizes.iconSizeSmall)
                    )
                }
            }
            Text(
                    text = consultation.objectConsult,
                    style = MaterialTheme.typography.body2.copy(fontSize = AppSizes.fontSizeBody2),
                    maxLines = 2
            )
            if (consultation.rations.isNotEmpty()) {
                Text(
                        text = "Rations: ${consultation.rations.size}",
                        style =
                                MaterialTheme.typography.caption.copy(
                                        fontSize = AppSizes.fontSizeCaption
                                )
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        ConfirmDialog(
                title = "Supprimer la consultation",
                message = "Êtes-vous sûr de vouloir supprimer cette consultation ?",
                onConfirm = {
                    onDelete()
                    showDeleteConfirmation = false
                },
                onDismiss = { showDeleteConfirmation = false }
        )
    }
}
