package fr.vetbrain.vetnutri_mp.View.SettingsSections

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.InfoSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.SettingsSection
import fr.vetbrain.vetnutri_mp.View.SettingsComponents.WarningSection
import fr.vetbrain.vetnutri_mp.Utils.NasTestResult
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.Utils.testNasDbPath
import fr.vetbrain.vetnutri_mp.browseNasDbPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class UiNasStatus { NONE, OK, VERSION_TOO_HIGH, INACCESSIBLE, TESTING }

@Composable
fun DatabaseSettings(modifier: Modifier = Modifier) {
    val preferencesStorage = remember {
        try { createPreferencesStorage() } catch (_: Exception) { null }
    }
    val preferencesRepository = remember(preferencesStorage) {
        preferencesStorage?.let { PreferencesRepository(it) }
    } ?: return

    val coroutineScope = rememberCoroutineScope()

    var currentPath by remember { mutableStateOf("") }
    var editedPath by remember { mutableStateOf("") }
    var uiStatus by remember { mutableStateOf(UiNasStatus.NONE) }
    var versionFound by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        preferencesRepository.loadPreferences()
        val loaded = preferencesRepository.preferences.nasDbPath
        currentPath = loaded
        editedPath = loaded
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SettingsSection(
            title = "Base de données partagée (NAS / Réseau)",
            subtitle = "Chemin vers un fichier .db SQLite sur un partage réseau ou NAS",
            icon = Icons.Default.Storage,
            content = {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Champ + bouton Parcourir
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editedPath,
                            onValueChange = {
                                editedPath = it
                                uiStatus = UiNasStatus.NONE
                            },
                            label = { Text("Chemin du fichier .db") },
                            placeholder = { Text("/mnt/nas/vetnutri/vetnutri.db") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = if (editedPath.isNotBlank()) {
                                {
                                    IconButton(onClick = {
                                        editedPath = ""
                                        uiStatus = UiNasStatus.NONE
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            } else null
                        )

                        OutlinedButton(
                            onClick = {
                                val selected = browseNasDbPath()
                                if (selected != null) {
                                    editedPath = selected
                                    uiStatus = UiNasStatus.NONE
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Parcourir")
                        }
                    }

                    // Boutons Tester + Enregistrer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    uiStatus = UiNasStatus.TESTING
                                    val result = withContext(Dispatchers.Default) {
                                        testNasDbPath(editedPath.trim())
                                    }
                                    when (result) {
                                        is NasTestResult.Ok -> uiStatus = UiNasStatus.OK
                                        is NasTestResult.VersionTooHigh -> {
                                            versionFound = result.fileVersion
                                            uiStatus = UiNasStatus.VERSION_TOO_HIGH
                                        }
                                        is NasTestResult.Inaccessible -> uiStatus = UiNasStatus.INACCESSIBLE
                                        is NasTestResult.EmptyPath -> uiStatus = UiNasStatus.NONE
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tester")
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val updated = preferencesRepository.preferences
                                        .copy(nasDbPath = editedPath.trim())
                                    preferencesRepository.savePreferences(updated)
                                    currentPath = editedPath.trim()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = VetNutriColors.Primary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enregistrer")
                        }
                    }

                    // Badge de statut
                    when (uiStatus) {
                        UiNasStatus.TESTING -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = VetNutriColors.Primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Test en cours…",
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                        UiNasStatus.OK -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Fichier accessible — version OK",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        UiNasStatus.VERSION_TOO_HIGH -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Version incompatible (base : v$versionFound) — mettez à jour VetNutriMP",
                                    color = Color(0xFFE65100),
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        UiNasStatus.INACCESSIBLE -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = VetNutriColors.Error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Fichier ou répertoire inaccessible",
                                    color = VetNutriColors.Error,
                                    style = MaterialTheme.typography.body2,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        UiNasStatus.NONE -> {
                            val caption = when {
                                currentPath.isBlank() ->
                                    "Aucun chemin NAS configuré — la base de données locale est utilisée."
                                editedPath == currentPath ->
                                    "Chemin configuré : $currentPath"
                                else -> null
                            }
                            if (caption != null) {
                                Text(
                                    caption,
                                    style = MaterialTheme.typography.caption,
                                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    InfoSection(
                        title = "Redémarrage requis",
                        message = "Le changement de chemin prend effet au prochain démarrage de VetNutriMP."
                    )

                    WarningSection(
                        title = "Accès concurrent non supporté",
                        message = "SQLite n'est pas conçu pour les accès simultanés depuis plusieurs " +
                                  "postes. N'ouvrez pas la même base depuis plusieurs instances en même temps."
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
