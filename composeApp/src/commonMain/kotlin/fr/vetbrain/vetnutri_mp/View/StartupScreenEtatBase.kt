package fr.vetbrain.vetnutri_mp.View

import kotlinx.coroutines.CoroutineScope
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.ViewModel.StartupViewModel
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Service.BackupService
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import fr.vetbrain.vetnutri_mp.Utils.DatabaseVersionManager
import fr.vetbrain.vetnutri_mp.Utils.AppLogo
import fr.vetbrain.vetnutri_mp.Utils.TermsAcceptanceStorage
import fr.vetbrain.vetnutri_mp.Utils.UpdateChecker
import fr.vetbrain.vetnutri_mp.Utils.isWindowsPlatform
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel.ImportResult
import fr.vetbrain.vetnutri_mp.Enumer.TextConstant
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.getPlatform
import fr.vetbrain.vetnutri_mp.performDatabaseFactoryReset
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.ConfirmationDialog
import kotlinx.coroutines.launch
import androidx.compose.runtime.MutableState

/** Écran de démarrage : état de la base, versions des données, sauvegardes, CGU et bouton Continuer */
@Composable
internal fun ColumnScope.EtatBaseDemarrage(
        launchDatabaseUpdateAutomatically: () -> Unit,
        coroutineScope: CoroutineScope,
        currentJsonVersion: String?,
        databaseStatus: DatabaseStatus?,
        databaseVersionManager: DatabaseVersionManager,
        embeddedJsonVersion: String?,
        hasAcceptedTerms: Boolean,
        isUpdatingDatabase: Boolean,
        jsonUpdateAvailable: Boolean,
        modifier: Modifier,
        onDatabaseReady: () -> Unit,
        onShowBackupDialog: () -> Unit,
        showExamDialogState: MutableState<Boolean>,
        showStartupScreenState: MutableState<Boolean>,
        showTermsDialogState: MutableState<Boolean>,
        showUpdateButtonByDefaultState: MutableState<Boolean>,
        showUpdateDialogState: MutableState<Boolean>
) {
        var showExamDialog by showExamDialogState
        var showStartupScreen by showStartupScreenState
        var showTermsDialog by showTermsDialogState
        var showUpdateButtonByDefault by showUpdateButtonByDefaultState
        var showUpdateDialog by showUpdateDialogState
        databaseStatus?.let { status ->
                DatabaseStatusCard(status = status)

                // Informations sur les versions JSON
                if (currentJsonVersion != null ||
                                embeddedJsonVersion != null
                ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp
                        ) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        16.dp
                                                )
                                ) {
                                        Text(
                                                text =
                                                                                                                                                 translate(LocalizationKeys.Startup.DATA_VERSIONS),
                                                style =
                                                        MaterialTheme
                                                                .typography
                                                                .subtitle1,
                                                fontWeight =
                                                        FontWeight
                                                                .Bold,
                                                color =
                                                        VetNutriColors
                                                                .Primary
                                        )

                                        Spacer(
                                                modifier =
                                                        Modifier.height(
                                                                8.dp
                                                        )
                                        )

                                        // Version actuelle importée
                                        if (currentJsonVersion !=
                                                        null
                                        ) {
                                                Text(
                                                        text =
                                                                                                                                                                 translate(LocalizationKeys.Startup.VERSION_CURRENT, databaseVersionManager.formatVersion(currentJsonVersion!!)),
                                                        style =
                                                                MaterialTheme
                                                                        .typography
                                                                        .body2
                                                )
                                        } else {
                                                Text(
                                                        text =
                                                                                                                                                                 translate(LocalizationKeys.Startup.VERSION_NONE),
                                                        style =
                                                                MaterialTheme
                                                                        .typography
                                                                        .body2,
                                                        color =
                                                                MaterialTheme
                                                                        .colors
                                                                        .onSurface
                                                                        .copy(
                                                                                alpha =
                                                                                        0.6f
                                                                        )
                                                )
                                        }

                                        // Version intégrée
                                        if (embeddedJsonVersion !=
                                                        null
                                        ) {
                                                Text(
                                                        text =
                                                                                                                                                                 translate(LocalizationKeys.Startup.VERSION_EMBEDDED, databaseVersionManager.formatVersion(embeddedJsonVersion!!)),
                                                        style =
                                                                MaterialTheme
                                                                        .typography
                                                                        .body2
                                                )
                                        }

                                        // Indicateur de mise à jour
                                        // disponible
                                        if (jsonUpdateAvailable) {
                                                Spacer(
                                                        modifier =
                                                                Modifier.height(
                                                                        8.dp
                                                                )
                                                )
                                                Row(
                                                        verticalAlignment =
                                                                Alignment
                                                                        .CenterVertically,
                                                        modifier =
                                                                Modifier.fillMaxWidth()
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default
                                                                                .Info,
                                                                contentDescription =
                                                                        null,
                                                                tint =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .secondary,
                                                                modifier =
                                                                        Modifier.size(
                                                                                16.dp
                                                                        )
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.width(
                                                                                8.dp
                                                                        )
                                                        )
                                                        Text(
                                                                text =
                                                                                                                                                                                 translate(LocalizationKeys.Startup.NEW_VERSION_AVAILABLE),
                                                                style =
                                                                        MaterialTheme
                                                                                .typography
                                                                                .body2,
                                                                color =
                                                                        MaterialTheme
                                                                                .colors
                                                                                .secondary,
                                                                fontWeight =
                                                                        FontWeight
                                                                                .Medium
                                                        )
                                                }
                                        }
                                }
                        }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Boutons d'action
                if (isUpdatingDatabase) {
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = VetNutriColors.Primary,
                                        strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                                                                                                 text = translate(LocalizationKeys.Startup.UPDATING_IN_PROGRESS),
                                        style = MaterialTheme.typography.body1,
                                        color = VetNutriColors.Primary,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                } else if (status.needsUpdate ||
                                jsonUpdateAvailable ||
                                showUpdateButtonByDefault
                ) {
                        Button(
                                onClick = {
                                        if (isWindowsPlatform) launchDatabaseUpdateAutomatically()
                                        else showUpdateDialog = true
                                        journaliserMiseAJour(
                                                "Ouverture du popup de mise à jour de la base (manuel)"
                                        )
                                        // Désactiver l'affichage
                                        // par défaut une fois que
                                        // l'utilisateur a cliqué
                                        showUpdateButtonByDefault =
                                                false
                                },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(56.dp),
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor =
                                                        VetNutriColors
                                                                .Primary
                                        )
                        ) {
                                Icon(
                                        imageVector =
                                                Icons.Default
                                                        .Refresh,
                                        contentDescription = null,
                                        modifier =
                                                Modifier.size(20.dp)
                                )
                                Spacer(
                                        modifier =
                                                Modifier.width(8.dp)
                                )
                                Text(
                                        text =
                                                                                                                                if (jsonUpdateAvailable) translate(LocalizationKeys.Startup.UPDATE_DATA) else translate(LocalizationKeys.Startup.UPDATE_DB),
                                        fontSize = 16.sp
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // ⚠️ IMPORTANT : Le bouton "Continuer"
                        // n'est affiché que si les CGU
                        // sont acceptées
                        if (hasAcceptedTerms) {
                                OutlinedButton(
                                        onClick = {
                                                coroutineScope.launch {
                                                    try { createPreferencesStorage().saveString("startup_complete", "true") } catch (ignored: Exception) { ignored.rethrowIfCancellation(); AppLog.d("StartupScreen", "Exception ignorée : ${ignored.message}") }
                                                }
                                                showStartupScreen =
                                                        false
                                                // Désactiver
                                                // l'affichage par
                                                // défaut quand
                                                // l'utilisateur
                                                // choisit de
                                                // continuer
                                                showUpdateButtonByDefault =
                                                        false
                                                onDatabaseReady()
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                48.dp
                                                        )
                                ) {
                                        Text(
                                                                                                                                 if (isUpdatingDatabase) translate(LocalizationKeys.Startup.CONTINUE_BG) else translate(LocalizationKeys.Startup.CONTINUE_NO_UPDATE)
                                        )
                                }

                                Spacer(
                                        modifier =
                                                Modifier.height(
                                                        8.dp
                                                )
                                )

                                // Bouton pour restaurer une
                                // sauvegarde
                                OutlinedButton(
                                        onClick =
                                                onShowBackupDialog,
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                48.dp
                                                        ),
                                        colors =
                                                ButtonDefaults
                                                        .outlinedButtonColors(
                                                                contentColor =
                                                                        VetNutriColors
                                                                                .Secondary
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default
                                                                .Download,
                                                contentDescription =
                                                        null,
                                                modifier =
                                                        Modifier.size(
                                                                18.dp
                                                        )
                                        )
                                        Spacer(
                                                modifier =
                                                        Modifier.width(
                                                                8.dp
                                                        )
                                        )
                                        Text(
                                                                                         translate(LocalizationKeys.Startup.RESTORE_BACKUP)
                                        )
                                }

                                Spacer(
                                        modifier =
                                                Modifier.height(
                                                        8.dp
                                                )
                                )

                                Button(
                                        onClick = {
                                                showExamDialog = true
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                48.dp
                                                        ),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor =
                                                                VetNutriColors.Secondary,
                                                        contentColor =
                                                                VetNutriColors.OnSecondary
                                                )
                                ) {
                                        Text(
                                                translate(LocalizationKeys.Startup.EXAM_MODE)
                                        )
                                }
                        }
                } else {
                        // Base complète : le bouton "Continuer"
                        // n'est affiché que si les CGU
                        // sont acceptées
                        if (hasAcceptedTerms) {
                                Button(
                                        onClick = {
                                                coroutineScope.launch {
                                                    try { createPreferencesStorage().saveString("startup_complete", "true") } catch (ignored: Exception) { ignored.rethrowIfCancellation(); AppLog.d("StartupScreen", "Exception ignorée : ${ignored.message}") }
                                                }
                                                showStartupScreen =
                                                        false
                                                // Désactiver
                                                // l'affichage par
                                                // défaut quand
                                                // l'utilisateur
                                                // choisit de
                                                // continuer
                                                showUpdateButtonByDefault =
                                                        false
                                                onDatabaseReady()
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                56.dp
                                                        ),
                                        colors =
                                                ButtonDefaults
                                                        .buttonColors(
                                                                backgroundColor =
                                                                        VetNutriColors
                                                                                .Primary
                                                        )
                                ) {
                                        Text(
                                                                                                                                 text = translate(LocalizationKeys.Startup.CONTINUE),
                                                fontSize = 16.sp
                                        )
                                }

                                Spacer(
                                        modifier =
                                                Modifier.height(
                                                        8.dp
                                                )
                                )

                                // Bouton pour restaurer une
                                // sauvegarde
                                OutlinedButton(
                                        onClick =
                                                onShowBackupDialog,
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                48.dp
                                                        ),
                                        colors =
                                                ButtonDefaults
                                                        .outlinedButtonColors(
                                                                contentColor =
                                                                        VetNutriColors
                                                                                .Secondary
                                                        )
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default
                                                                .Download,
                                                contentDescription =
                                                        null,
                                                modifier =
                                                        Modifier.size(
                                                                18.dp
                                                        )
                                        )
                                        Spacer(
                                                modifier =
                                                        Modifier.width(
                                                                8.dp
                                                        )
                                        )
                                        Text(
                                                translate(LocalizationKeys.Startup.RESTORE_BACKUP)
                                        )
                                }

                                Spacer(
                                        modifier =
                                                Modifier.height(
                                                        8.dp
                                                )
                                )

                                Button(
                                        onClick = {
                                                showExamDialog = true
                                        },
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .height(
                                                                48.dp
                                                        ),
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor =
                                                                VetNutriColors.Secondary,
                                                        contentColor =
                                                                VetNutriColors.OnSecondary
                                                )
                                ) {
                                        Text(
                                                translate(LocalizationKeys.Startup.EXAM_MODE)
                                        )
                                }
                        }
                }

                // ⚠️ IMPORTANT : Validation des CGU obligatoire à
                // chaque démarrage
                Spacer(modifier = Modifier.height(16.dp))

                if (!hasAcceptedTerms) {
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                backgroundColor =
                                        MaterialTheme.colors.error
                                                .copy(alpha = 0.1f)
                        ) {
                                Column(
                                        modifier =
                                                Modifier.padding(
                                                        16.dp
                                                ),
                                        horizontalAlignment =
                                                Alignment
                                                        .CenterHorizontally
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default
                                                                .Warning,
                                                contentDescription =
                                                        null,
                                                tint =
                                                        MaterialTheme
                                                                .colors
                                                                .error,
                                                modifier =
                                                        Modifier.size(
                                                                24.dp
                                                        )
                                        )

                                        Spacer(
                                                modifier =
                                                        Modifier.height(
                                                                8.dp
                                                        )
                                        )

                                        Text(
                                                text =
                                                        translate("terms.acceptance_required"),
                                                style =
                                                        MaterialTheme
                                                                .typography
                                                                .subtitle2,
                                                fontWeight =
                                                        FontWeight
                                                                .Bold,
                                                color =
                                                        MaterialTheme
                                                                .colors
                                                                .error,
                                                textAlign =
                                                        TextAlign
                                                                .Center
                                        )

                                        Text(
                                                text =
                                                        translate("terms.required_message"),
                                                style =
                                                        MaterialTheme
                                                                .typography
                                                                .caption,
                                                color =
                                                        MaterialTheme
                                                                .colors
                                                                .onSurface
                                                                .copy(
                                                                        alpha =
                                                                                0.7f
                                                                ),
                                                textAlign =
                                                        TextAlign
                                                                .Center
                                        )

                                        Spacer(
                                                modifier =
                                                        Modifier.height(
                                                                12.dp
                                                        )
                                        )

                                        Button(
                                                onClick = {
                                                        showTermsDialog =
                                                                true
                                                },
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .height(
                                                                        40.dp
                                                                ),
                                                colors =
                                                        ButtonDefaults
                                                                .buttonColors(
                                                                        backgroundColor =
                                                                                MaterialTheme
                                                                                        .colors
                                                                                        .error
                                                                )
                                        ) {
                                                Text(
                                                        translate("terms.read_accept")
                                                )
                                        }
                                }
                        }
                } else {
                        // CGU acceptées
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = 2.dp,
                                backgroundColor =
                                        MaterialTheme.colors.primary
                                                .copy(alpha = 0.1f)
                        ) {
                                Row(
                                        modifier =
                                                Modifier.padding(
                                                        16.dp
                                                ),
                                        verticalAlignment =
                                                Alignment
                                                        .CenterVertically,
                                        horizontalArrangement =
                                                Arrangement.Center
                                ) {
                                        Icon(
                                                imageVector =
                                                        Icons.Default
                                                                .Info,
                                                contentDescription =
                                                        null,
                                                tint =
                                                        MaterialTheme
                                                                .colors
                                                                .primary,
                                                modifier =
                                                        Modifier.size(
                                                                20.dp
                                                        )
                                        )

                                        Spacer(
                                                modifier =
                                                        Modifier.width(
                                                                8.dp
                                                        )
                                        )

                                        Text(
                                                text =
                                                        translate("terms.accepted"),
                                                style =
                                                        MaterialTheme
                                                                .typography
                                                                .body2,
                                                color =
                                                        MaterialTheme
                                                                .colors
                                                                .primary,
                                                fontWeight =
                                                        FontWeight
                                                                .Medium
                                        )
                                }
                        }
                }
        }
}
