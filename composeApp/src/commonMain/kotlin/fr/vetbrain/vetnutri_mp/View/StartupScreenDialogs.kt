package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import kotlinx.datetime.toLocalDateTime

// Écran de démarrage : état de la base, mise à jour des données, CGU, testeurs

/** Carte affichant l'état de la base de données */
@Composable
internal fun DatabaseStatusCard(status: DatabaseStatus, modifier: Modifier = Modifier) {
        Card(
                modifier = modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor =
                        if (status.needsUpdate) {
                                MaterialTheme.colors.error.copy(alpha = 0.1f)
                        } else {
                                MaterialTheme.colors.surface
                        }
        ) {
                Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector =
                                                if (status.needsUpdate) Icons.Default.Warning
                                                else Icons.Default.Info,
                                        contentDescription = null,
                                        tint =
                                                if (status.needsUpdate) MaterialTheme.colors.error
                                                else VetNutriColors.Primary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                        text =
                                                                                                if (status.needsUpdate) translate(LocalizationKeys.Database.INCOMPLETE_TITLE)
                                                else translate(LocalizationKeys.Database.COMPLETE_TITLE),
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                                if (status.needsUpdate) MaterialTheme.colors.error
                                                else VetNutriColors.Primary
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Statistiques de la base
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                                StatisticItem(
                                                                                 label = translate(LocalizationKeys.Database.STAT_FOODS),
                                        value = status.foodCount.toString(),
                                        color =
                                                if (status.foodCount > 0) VetNutriColors.Primary
                                                else MaterialTheme.colors.error
                                )

                                StatisticItem(
                                                                                 label = translate(LocalizationKeys.Database.STAT_REFERENCES),
                                        value = status.referenceCount.toString(),
                                        color =
                                                if (status.referenceCount > 0)
                                                        VetNutriColors.Primary
                                                else MaterialTheme.colors.error
                                )

                                StatisticItem(
                                                                                 label = translate(LocalizationKeys.Database.STAT_CONSEILS),
                                        value = status.conseilsCount.toString(),
                                        color = VetNutriColors.Primary
                                )
                        }

                        // Message d'erreur si présent
                        if (status.error != null) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text = "⚠️ ${status.error}",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.error,
                                        textAlign = TextAlign.Center
                                )
                        }

                        // Recommandation
                        if (status.needsUpdate) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text =
                                                                                                 translate(LocalizationKeys.Database.RECOMMENDED_UPDATE),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                )
                        }
                }
        }
}

/** Élément de statistique */
@Composable
private fun StatisticItem(
        label: String,
        value: String,
        color: Color,
        modifier: Modifier = Modifier
) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                        text = value,
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = color
                )

                Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
        }
}

/** Dialogue automatique de mise à jour des données JSON */
@Composable
internal fun JsonUpdateDialog(
        currentJsonVersion: String?,
        newJsonVersion: String?,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                                                                text = translate(LocalizationKeys.Update.DATA_CONFIRM_TITLE),
                                        style = MaterialTheme.typography.h6
                                )
                        }
                },
                text = {
                        Column {
                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Update.DETECTION_MSG),
                                        style = MaterialTheme.typography.body1,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Informations sur les versions
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = MaterialTheme.colors.surface,
                                        elevation = 2.dp
                                ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                                                                                text = translate(LocalizationKeys.Update.VERSIONS),
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                                                                                text = translate(LocalizationKeys.Update.ACTUAL),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2
                                                        )
                                                        Text(
                                                                text = currentJsonVersion
                                                                                                                                                                ?: translate(LocalizationKeys.General.NONE),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                                                                                text = translate(LocalizationKeys.Update.NEW),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = MaterialTheme.colors.primary
                                                        )
                                                        Text(
                                                                                                                                text = newJsonVersion ?: translate(LocalizationKeys.General.NONE),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colors.primary
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Update.INCLUDES_MSG),
                                        style = MaterialTheme.typography.body2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Update.WANT_TO_INSTALL),
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(translate(LocalizationKeys.Update.UPDATE_BUTTON))
                        }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text(translate(LocalizationKeys.Update.LATER_BUTTON)) } }
        )
}

/** Dialogue de confirmation de mise à jour */
@Composable
internal fun UpdateConfirmationDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        isJsonUpdate: Boolean = false,
        currentJsonVersion: String? = null,
        newJsonVersion: String? = null
) {
                val title =
                if (isJsonUpdate) translate(LocalizationKeys.Update.DATA_CONFIRM_TITLE) else translate(LocalizationKeys.Update.CONFIRM_TITLE)
        val message =
                if (isJsonUpdate) {
                        translate(LocalizationKeys.Update.DATA_CONFIRM_MSG, currentJsonVersion ?: translate(LocalizationKeys.General.NONE), newJsonVersion ?: translate(LocalizationKeys.General.NONE))
                } else {
                        translate(LocalizationKeys.Update.DB_CONFIRM_MSG)
                }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = title, style = MaterialTheme.typography.h6) },
                text = { Text(text = message, style = MaterialTheme.typography.body1) },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) { Text(translate(LocalizationKeys.General.CONFIRM)) }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CANCEL)) } }
        )
}

/** Dialogue des conditions générales d'utilisation */
@Composable
internal fun TermsAndConditionsDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                                                text = translate(LocalizationKeys.Terms.FULL_TITLE),
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                        )
                },
                text = {
                        Column(
                                modifier =
                                        Modifier.heightIn(max = 420.dp)
                                                .verticalScroll(rememberScrollState())
                        ) {
                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Terms.SUBTITLE),
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Terms.WARNING),
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.error,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Terms.ACCEPT_HEADER),
                                        style = MaterialTheme.typography.body1,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                        text =
                                                                                                translate(LocalizationKeys.Terms.POINT_VETS),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_INFORMATIVE),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_NO_MEDICAL),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_RESPONSIBILITY),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_CONFIDENTIAL),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_DATA_EXPORT),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.POINT_PRIVACY_POLICY),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )
 
                                Text(
                                        text =
                                                translate(LocalizationKeys.Terms.AUTHOR_FOOTER),
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Secondary,
                                        textAlign = TextAlign.Center
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onAccept,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) { Text(translate(LocalizationKeys.Terms.ACCEPT_BUTTON)) }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text(translate(LocalizationKeys.General.CLOSE)) } }  
        )
}

/** Dialogue affichant la liste des testeurs */
@Composable
internal fun TestersDialog(onDismiss: () -> Unit) {
        val testers = listOf(
                "Virginie Durand Lefebvre",
                "Tiphaine Blanchard",
                "Mathilde Thierry",
                "Antoine Rached"
        ).sorted()

        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                                                text = translate(LocalizationKeys.Testers.TITLE),
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                        )
                },
                text = {
                        Column(
                                modifier = Modifier
                                        .heightIn(max = 400.dp)
                                        .verticalScroll(rememberScrollState())
                        ) {
                                Text(
                                                                                text = translate(LocalizationKeys.Testers.THANKS_MESSAGE),
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                testers.forEach { tester ->
                                        Text(
                                                text = "• $tester",
                                                style = MaterialTheme.typography.body2,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                }
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary
                                )
                        ) {
                                                                Text(translate(LocalizationKeys.General.CLOSE))
                        }
                }
        )
}

/** Date d'une sauvegarde au format jj/mm/aaaa hh:mm (fuseau local) */
internal fun formatBackupDate(timestamp: Long): String =
        try {
                val dateTime =
                        kotlinx.datetime.Instant.fromEpochMilliseconds(timestamp)
                                .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                "${dateTime.dayOfMonth.toString().padStart(2, '0')}/" +
                        "${dateTime.monthNumber.toString().padStart(2, '0')}/${dateTime.year} " +
                        "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        } catch (ignored: Exception) {
                ignored.rethrowIfCancellation()
                "?"
        }
