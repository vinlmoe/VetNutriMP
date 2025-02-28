package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import fr.vetbrain.vetnutri_mp.Components.*
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Consultation
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.*

@Composable
fun ConsultationsView(
        viewModel: AnimalDetailViewModel,
        selectedConsultation: ConsultationEv?,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        modifier: Modifier = Modifier
) {
    val animal by viewModel.animal.collectAsState()
    val consultations = remember(animal) { animal?.consultations ?: emptyList() }

    Column(
            modifier = modifier.fillMaxSize().padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        ConsultationHeader(
                onAddClick = {
                    val currentMoment = Clock.System.now()
                    val localDateTime =
                            currentMoment.toLocalDateTime(TimeZone.currentSystemDefault())
                    val currentDate = localDateTime.date
                    viewModel.prepareNewConsultation(currentDate)
                    viewModel.startEditingConsultation()
                    onShowConsultationDetail(true)
                }
        )

        StandardDivider()

        LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSizes.cardSpacing),
                contentPadding = PaddingValues(vertical = AppSizes.paddingSmall)
        ) {
            items(consultations, key = { it.uuid }) { consultation ->
                ConsultationListItem(
                        consultation = consultation,
                        isSelected = consultation.uuid == selectedConsultation?.uuid,
                        onSelect = { viewModel.selectConsultation(consultation) },
                        onEdit = {
                            viewModel.selectConsultation(consultation)
                            viewModel.startEditingConsultation()
                            onShowConsultationDetail(true)
                        },
                        onDelete = {
                            if (consultations.size > 1) {
                                viewModel.deleteConsultation(consultation)
                            }
                        },
                        canDelete = consultations.size > 1
                )
            }
        }
    }

    // Dialog pour l'édition de consultation
    if (showConsultationDetail && selectedConsultation != null) {
        AlertDialog(
                onDismissRequest = {
                    onShowConsultationDetail(false)
                    viewModel.stopEditingConsultation()
                },
                title = null,
                text = {
                    ConsultationDetailView(
                            consultation = selectedConsultation,
                            onDismiss = {
                                onShowConsultationDetail(false)
                                viewModel.stopEditingConsultation()
                            },
                            onSave = { updatedConsultation ->
                                viewModel.updateConsultation(updatedConsultation)
                                onShowConsultationDetail(false)
                                viewModel.stopEditingConsultation()
                            }
                    )
                },
                buttons = {},
                backgroundColor = MaterialTheme.colors.surface,
                modifier = Modifier.padding(AppSizes.paddingMedium)
        )
    }
}

@Composable
fun ConsultationDetailView(
        consultation: ConsultationEv,
        onDismiss: () -> Unit,
        onSave: (ConsultationEv) -> Unit
) {
    var objectConsult by remember(consultation) { mutableStateOf(consultation.objectConsult) }
    var date by
            remember(consultation) {
                mutableStateOf(consultation.date ?: LocalDate.parse("2024-01-01"))
            }

    Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        SectionTitle(
                text =
                        if (consultation.uuid.isEmpty()) Consultation.NEW_CONSULTATION.translate()
                        else Consultation.EDIT_CONSULTATION.translate()
        )

        StandardDivider()

        Column {
            SectionSubtitle(text = Consultation.DATE.translate())
            Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { date = date.plus(1, DateTimeUnit.DAY) }) {
                        Icon(
                                AppIcons.ArrowDropDown,
                                Consultation.INCREASE_DAY.translate(),
                                modifier = Modifier.rotate(180f)
                        )
                    }
                    Text(
                            text = date.dayOfMonth.toString().padStart(2, '0'),
                            style = MaterialTheme.typography.h6.copy(fontSize = AppSizes.fontSizeH6)
                    )
                    IconButton(onClick = { date = date.plus(-1, DateTimeUnit.DAY) }) {
                        Icon(AppIcons.ArrowDropDown, Consultation.DECREASE_DAY.translate())
                    }
                }
            }
        }

        AppTextField(
                value = objectConsult,
                onValueChange = { objectConsult = it },
                label = Consultation.OBJECTIVE.translate(),
                leadingIcon = AppIcons.Info,
                singleLine = false,
                maxLines = 5,
                modifier = Modifier.height(AppSizes.textFieldHeight * 2.5f)
        )

        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            StandardButton(
                    onClick = onDismiss,
                    text = General.CANCEL.translate(),
                    modifier = Modifier.weight(1f)
            )
            StandardButton(
                    onClick = {
                        val updatedConsultation =
                                consultation.copy(objectConsult = objectConsult, date = date)
                        onSave(updatedConsultation)
                    },
                    text = General.SAVE.translate(),
                    modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ConsultationListItem(
        consultation: ConsultationEv,
        isSelected: Boolean,
        onSelect: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        canDelete: Boolean,
        modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        ConfirmDialog(
                title = Consultation.DELETE_CONSULTATION.translate(),
                message = Consultation.DELETE_CONSULTATION_CONFIRM.translate(),
                onConfirm = {
                    onDelete()
                    showDeleteConfirmation = false
                },
                onDismiss = { showDeleteConfirmation = false }
        )
    }

    StandardCard(onClick = onSelect, isSelected = isSelected, modifier = modifier) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Text(
                        text = consultation.date?.toString()
                                        ?: Consultation.UNSPECIFIED_DATE.translate(),
                        style = MaterialTheme.typography.h6.copy(fontSize = AppSizes.fontSizeH6)
                )
                Text(
                        text = consultation.objectConsult,
                        style =
                                MaterialTheme.typography.body1.copy(
                                        fontSize = AppSizes.fontSizeBody1
                                ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                            imageVector = AppIcons.Edit,
                            contentDescription = Consultation.EDIT_CONSULTATION.translate(),
                            modifier = Modifier.size(AppSizes.iconSizeMedium)
                    )
                }
                IconButton(
                        onClick = { if (canDelete) showDeleteConfirmation = true },
                        enabled = canDelete
                ) {
                    Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = Consultation.DELETE_CONSULTATION.translate(),
                            modifier = Modifier.size(AppSizes.iconSizeMedium),
                            tint =
                                    if (canDelete) MaterialTheme.colors.onSurface
                                    else MaterialTheme.colors.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}
