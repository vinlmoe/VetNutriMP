package fr.vetbrain.vetnutri_mp.View.AnalNut

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.BasicAppTextField
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel

/** Section compacte pour afficher les valeurs métaboliques d'une consultation */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionValeursMetaboliques(
        selectedConsultation: ConsultationEv?,
        poidsMetabolique: Double?,
        besoinEnergetiqueStandard: Double?,
        besoinEnergetiqueTotal: Double?,
        kObserve: Double,
        kCalcule: Double,
        onExpand: () -> Unit,
        modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isNarrow = maxWidth < 400.dp // Seuil pour largeur faible

        Column {
            Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Valeurs métaboliques",
                        style = MaterialTheme.typography.overline,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                IconButton(onClick = onExpand, modifier = Modifier.size(16.dp)) {
                    Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Agrandir les valeurs métaboliques",
                            tint = VetNutriColors.Primary,
                            modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Mode étroit : éléments qui se wrap automatiquement sur plusieurs lignes si
            // nécessaire
            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
            ) {
                LigneInfoLocaleCompacte(
                        label = "Poids actuel",
                        value =
                                selectedConsultation?.weight?.let {
                                    "${TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                }
                                        ?: "Non renseigné"
                )
                LigneInfoLocaleCompacte(
                        label = "Poids idéal",
                        value =
                                selectedConsultation?.effectiveWeight?.let {
                                    "${TextUtils.formatDecimal(it.toDouble(), 1)} kg"
                                }
                                        ?: "Non calculé"
                )
                LigneInfoLocaleCompacte(
                        label = "P. métabolique",
                        value = poidsMetabolique?.let { TextUtils.formatKgPuissance075(it) }
                                        ?: "Non calculé"
                )
                LigneInfoLocaleCompacte(
                        label = "BEE standard",
                        value =
                                besoinEnergetiqueStandard?.let {
                                    "${TextUtils.formatDecimal(it, 0)} kcal/j"
                                }
                                        ?: "Non calculé"
                )
                LigneInfoLocaleCompacte(
                        label = "BE",
                        value =
                                besoinEnergetiqueTotal?.let {
                                    "${TextUtils.formatDecimal(it, 0)} kcal/j"
                                }
                                        ?: "Non calculé"
                )
            }
        }
    }
}

/** Section compacte pour afficher les coefficients d'une consultation */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionCoefficients(
        selectedConsultation: ConsultationEv?,
        showCoefficientsDialog: () -> Unit,
        viewModel: AnimalDetailViewModel,
        modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isNarrow = maxWidth < 400.dp

        Column {
            Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Coefficients",
                        style = MaterialTheme.typography.overline,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
                IconButton(onClick = showCoefficientsDialog, modifier = Modifier.size(16.dp)) {
                    Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Agrandir les coefficients",
                            tint = VetNutriColors.Primary,
                            modifier = Modifier.size(12.dp)
                    )
                }
            }

            var isEditingCoefficient by remember { mutableStateOf(false) }
            var coefficientText by
                    remember(selectedConsultation) {
                        mutableStateOf(
                                selectedConsultation?.coefficientAjustement?.toString() ?: "1.0"
                        )
                    }

            if (isNarrow) {
                // Mode étroit : tous les coefficients sur une seule FlowRow
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    LigneInfoLocaleCompacte(
                            label = "K1",
                            value =
                                    selectedConsultation?.k1Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    LigneInfoLocaleCompacte(
                            label = "K2",
                            value =
                                    selectedConsultation?.k2Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    LigneInfoLocaleCompacte(
                            label = "K3",
                            value =
                                    selectedConsultation?.k3Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    LigneInfoLocaleCompacte(
                            label = "K4",
                            value =
                                    selectedConsultation?.k4Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    LigneInfoLocaleCompacte(
                            label = "K5",
                            value =
                                    selectedConsultation?.k5Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )

                    // Coefficient d'ajustement
                    if (isEditingCoefficient) {
                        BasicAppTextField(
                                value = coefficientText,
                                onValueChange = { coefficientText = it },
                                placeholder = "Coeff. ajust.",
                                modifier = Modifier.width(100.dp).height(50.dp),
                                trailingIcon = Icons.Filled.Check,
                                onTrailingIconClick = {
                                    coefficientText.toDoubleOrNull()?.let { newValue ->
                                        selectedConsultation?.let { consultation ->
                                            viewModel.updateCoefficientAjustement(
                                                    consultation.uuid,
                                                    newValue
                                            )
                                        }
                                    }
                                    isEditingCoefficient = false
                                }
                        )
                        IconButton(
                                onClick = {
                                    coefficientText =
                                            selectedConsultation?.coefficientAjustement?.toString()
                                                    ?: "1.0"
                                    isEditingCoefficient = false
                                },
                                modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Annuler",
                                    tint = Color.Red
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LigneInfoLocaleCompacte(
                                    label = "Coeff. ajust.",
                                    value =
                                            selectedConsultation?.coefficientAjustement?.let {
                                                TextUtils.formatDecimal(it.toDouble(), 2)
                                            }
                                                    ?: "1.00"
                            )
                            IconButton(
                                    onClick = {
                                        coefficientText =
                                                selectedConsultation?.coefficientAjustement
                                                        ?.toString()
                                                        ?: "1.0"
                                        isEditingCoefficient = true
                                    },
                                    modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Éditer",
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Mode large : coefficients sur deux lignes
                Row() {
                    LigneInfoLocaleCompacte(
                            label = "K1",
                            value =
                                    selectedConsultation?.k1Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                    LigneInfoLocaleCompacte(
                            label = "K2",
                            value =
                                    selectedConsultation?.k2Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                    LigneInfoLocaleCompacte(
                            label = "K3",
                            value =
                                    selectedConsultation?.k3Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                }
                Row() {
                    LigneInfoLocaleCompacte(
                            label = "K4",
                            value =
                                    selectedConsultation?.k4Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                    LigneInfoLocaleCompacte(
                            label = "K5",
                            value =
                                    selectedConsultation?.k5Value?.let {
                                        TextUtils.formatDecimal(it.toDouble(), 2)
                                    }
                                            ?: "1.00"
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))

                    // Coefficient d'ajustement
                    if (isEditingCoefficient) {
                        BasicAppTextField(
                                value = coefficientText,
                                onValueChange = { coefficientText = it },
                                placeholder = "Coeff. ajust.",
                                modifier = Modifier.width(80.dp).height(50.dp),
                                trailingIcon = Icons.Filled.Check,
                                onTrailingIconClick = {
                                    coefficientText.toDoubleOrNull()?.let { newValue ->
                                        selectedConsultation?.let { consultation ->
                                            viewModel.updateCoefficientAjustement(
                                                    consultation.uuid,
                                                    newValue
                                            )
                                        }
                                    }
                                    isEditingCoefficient = false
                                }
                        )
                        IconButton(
                                onClick = {
                                    coefficientText =
                                            selectedConsultation?.coefficientAjustement?.toString()
                                                    ?: "1.0"
                                    isEditingCoefficient = false
                                },
                                modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Annuler",
                                    tint = Color.Red
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LigneInfoLocaleCompacte(
                                    label = "Coeff. ajust.",
                                    value =
                                            selectedConsultation?.coefficientAjustement?.let {
                                                TextUtils.formatDecimal(it.toDouble(), 2)
                                            }
                                                    ?: "1.00"
                            )
                            IconButton(
                                    onClick = {
                                        coefficientText =
                                                selectedConsultation?.coefficientAjustement
                                                        ?.toString()
                                                        ?: "1.0"
                                        isEditingCoefficient = true
                                    },
                                    modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Éditer",
                                        modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Section compacte pour afficher le bilan énergétique */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SectionBilanEnergetique(
        energieApportee: Double,
        pourcentageCouverture: Double,
        kObserve: Double,
        kCalcule: Double,
        modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isNarrow = maxWidth < 400.dp

        Column {
            Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = "Bilan énergétique",
                        style = MaterialTheme.typography.overline,
                        fontWeight = FontWeight.Bold,
                        color = VetNutriColors.Primary
                )
            }

            if (isNarrow) {
                // Mode étroit : éléments qui se wrap automatiquement
                FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)
                ) {
                    // Énergie apportée
                    LigneInfoLocaleCompacte(
                            label = "Énergie apportée",
                            value = "${TextUtils.formatDecimal(energieApportee, 0)} kcal/j"
                    )

                    // Couverture
                    Column {
                        Text(
                                text = "Couverture",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = "${TextUtils.formatDecimal(pourcentageCouverture, 0)}%",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color =
                                        when {
                                            pourcentageCouverture >= 90 &&
                                                    pourcentageCouverture <= 110 ->
                                                    Color(0xFF4CAF50)
                                            pourcentageCouverture >= 80 &&
                                                    pourcentageCouverture <= 120 ->
                                                    Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                        )
                    }

                    // K Observé
                    Column {
                        Text(
                                text = "K Observé",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = TextUtils.formatDecimal(kObserve, 2),
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color =
                                        when {
                                            // Vert si proche du K calculé (±10%)
                                            kObserve >= kCalcule * 0.9 &&
                                                    kObserve <= kCalcule * 1.1 -> Color(0xFF4CAF50)
                                            // Orange si moyennement proche (±20%)
                                            kObserve >= kCalcule * 0.8 &&
                                                    kObserve <= kCalcule * 1.2 -> Color(0xFFFF9800)
                                            // Rouge sinon
                                            else -> Color(0xFFF44336)
                                        }
                        )
                    }

                    // K Calculé
                    Column {
                        Text(
                                text = "K Calculé",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = TextUtils.formatDecimal(kCalcule, 2),
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color = VetNutriColors.Primary
                        )
                    }
                }
            } else {
                // Mode large : structure originale
                Row() {
                    LigneInfoLocaleCompacte(
                            label = "Énergie apportée",
                            value = "${TextUtils.formatDecimal(energieApportee, 0)} kcal/j"
                    )
                    Spacer(modifier = Modifier.width(AppSizes.paddingXSmall))
                    Column {
                        Text(
                                text = "Couverture",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = "${TextUtils.formatDecimal(pourcentageCouverture, 0)}%",
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color =
                                        when {
                                            pourcentageCouverture >= 90 &&
                                                    pourcentageCouverture <= 110 ->
                                                    Color(0xFF4CAF50)
                                            pourcentageCouverture >= 80 &&
                                                    pourcentageCouverture <= 120 ->
                                                    Color(0xFFFF9800)
                                            else -> Color(0xFFF44336)
                                        }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingXSmall)) {
                    Column {
                        Text(
                                text = "K Observé",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = TextUtils.formatDecimal(kObserve, 2),
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color =
                                        when {
                                            // Vert si proche du K calculé (±10%)
                                            kObserve >= kCalcule * 0.9 &&
                                                    kObserve <= kCalcule * 1.1 -> Color(0xFF4CAF50)
                                            // Orange si moyennement proche (±20%)
                                            kObserve >= kCalcule * 0.8 &&
                                                    kObserve <= kCalcule * 1.2 -> Color(0xFFFF9800)
                                            // Rouge sinon
                                            else -> Color(0xFFF44336)
                                        }
                        )
                    }
                    Column {
                        Text(
                                text = "K Calculé",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                                text = TextUtils.formatDecimal(kCalcule, 2),
                                style = MaterialTheme.typography.caption,
                                fontWeight = FontWeight.Medium,
                                color = VetNutriColors.Primary
                        )
                    }
                }
            }
        }
    }
}

/** Ligne d'information compacte pour affichage label/valeur */
@Composable
private fun LigneInfoLocaleCompacte(label: String, value: String) {
    Column {
        Text(
                text = label,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        )
        Text(text = value, style = MaterialTheme.typography.caption, fontWeight = FontWeight.Medium)
    }
}
