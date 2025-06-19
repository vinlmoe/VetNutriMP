package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.TypeExpressionBesoin
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

@Composable
fun SpeciesPreferencesView(
        species: Espece,
        preferencesRepository: PreferencesRepository,
        modifier: Modifier = Modifier
) {
    var currentPreferences by remember {
        mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
    }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Charger les préférences au démarrage
    LaunchedEffect(Unit) {
        try {
            preferencesRepository.loadPreferences()
            currentPreferences = preferencesRepository.preferences.value
        } catch (e: Exception) {
            println("Erreur lors du chargement des préférences: ${e.message}")
            currentPreferences = fr.vetbrain.vetnutri_mp.Data.PreferencesApplication()
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chargement des préférences...")
            }
        }
    } else {
        val speciesPrefs = currentPreferences?.getPreferencesEspece(species)
        val currentExpressionType =
                speciesPrefs?.getTypeExpressionBesoinEnum() ?: TypeExpressionBesoin.DEFAULT

        LazyColumn(
                modifier = modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // En-tête avec informations sur l'espèce
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = "Configuration pour ${species.label}",
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.Bold,
                                color = VetNutriColors.Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text =
                                        "Personnalisez l'expression des besoins et les nutriments à afficher pour cette espèce",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )
                    }
                }
            }

            item {
                // Section Expression des besoins
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Expression des besoins",
                                    tint = VetNutriColors.Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "Expression des besoins",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                text = "Sélection actuelle: ${currentExpressionType.displayName}",
                                style = MaterialTheme.typography.subtitle1,
                                color = VetNutriColors.Primary,
                                fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options de type d'expression
                        TypeExpressionBesoin.values().forEach { type ->
                            Row(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .selectable(
                                                            selected =
                                                                    currentExpressionType == type,
                                                            onClick = {
                                                                if (!isSaving &&
                                                                                currentExpressionType !=
                                                                                        type
                                                                ) {
                                                                    scope.launch {
                                                                        try {
                                                                            isSaving = true

                                                                            currentPreferences
                                                                                    ?.let { prefs ->
                                                                                        val updatedSpeciesPrefs =
                                                                                                prefs.getPreferencesEspece(
                                                                                                                species
                                                                                                        )
                                                                                                        .copy(
                                                                                                                typeExpressionBesoin =
                                                                                                                        type.id
                                                                                                        )
                                                                                        val updatedPrefs =
                                                                                                prefs.updatePreferencesEspece(
                                                                                                        updatedSpeciesPrefs
                                                                                                )

                                                                                        preferencesRepository
                                                                                                .savePreferences(
                                                                                                        updatedPrefs
                                                                                                )
                                                                                        currentPreferences =
                                                                                                updatedPrefs

                                                                                        println(
                                                                                                "DEBUG: Expression mise à jour pour ${species.label}: ${type.displayName}"
                                                                                        )
                                                                                    }
                                                                        } catch (e: Exception) {
                                                                            println(
                                                                                    "Erreur lors de la sauvegarde: ${e.message}"
                                                                            )
                                                                        } finally {
                                                                            isSaving = false
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                    )
                                                    .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                        selected = currentExpressionType == type,
                                        onClick = null,
                                        enabled = !isSaving
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                            text = type.displayName,
                                            style = MaterialTheme.typography.body1,
                                            fontWeight =
                                                    if (currentExpressionType == type)
                                                            FontWeight.Bold
                                                    else FontWeight.Normal,
                                            color = if (isSaving) Color.Gray else Color.Black
                                    )
                                    Text(
                                            text = type.description,
                                            style = MaterialTheme.typography.body2,
                                            color = Color.Gray
                                    )
                                    Text(
                                            text = "Unité: ${type.unite}",
                                            style = MaterialTheme.typography.caption,
                                            color = VetNutriColors.Primary
                                    )
                                }
                                if (isSaving && currentExpressionType == type) {
                                    CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = VetNutriColors.Primary,
                                            strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Section Nutriments à afficher (placeholder pour l'instant)
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White,
                        elevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Nutriments",
                                    tint = VetNutriColors.Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "Nutriments à afficher",
                                    style = MaterialTheme.typography.h6,
                                    fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                text = "Configuration des nutriments à venir...",
                                style = MaterialTheme.typography.body1,
                                color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                                text =
                                        "Cette section permettra de sélectionner quels nutriments afficher pour cette espèce dans les analyses nutritionnelles.",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )
                    }
                }
            }

            item {
                // Informations sur la persistance
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Persistance active",
                                    tint = VetNutriColors.Primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                    text = "Sauvegarde automatique",
                                    style = MaterialTheme.typography.subtitle2,
                                    fontWeight = FontWeight.Bold,
                                    color = VetNutriColors.Primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text =
                                        "Toutes les modifications sont automatiquement sauvegardées et seront restaurées au prochain démarrage.",
                                style = MaterialTheme.typography.body2,
                                color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
