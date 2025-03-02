package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Vue pour afficher les informations d'identification d'un animal
 *
 * @param animal L'animal dont les informations sont affichées
 * @param onEdit Action à exécuter lors du clic sur le bouton d'édition
 * @param onDelete Action à exécuter lors du clic sur le bouton de suppression
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AnimalIdentificationView(
        animal: AnimalEv,
        onEdit: () -> Unit,
        onDelete: () -> Unit,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // En-tête avec boutons d'action
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Identification de l'animal", style = MaterialTheme.typography.h6)
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = VetNutriColors.Primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = Color.Red
                    )
                }
            }
        }

        Divider()

        // Informations de l'animal
        Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Nom et race
            InfoRow(label = Animal.NAME.translate(), value = animal.nom)
            InfoRow(label = Animal.BREED.translate(), value = animal.race)

            // Sexe
            InfoRow(label = Animal.SEX.translate(), value = Sex.getSimpleSex(animal.sexId))

            // Date de naissance et âge
            val birthdate = animal.birthdate
            if (birthdate != null) {
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val age =
                        today.year -
                                birthdate.year -
                                (if (today.month < birthdate.month ||
                                                (today.month == birthdate.month &&
                                                        today.dayOfMonth < birthdate.dayOfMonth)
                                )
                                        1
                                else 0)

                InfoRow(label = Animal.BIRTH_DATE.translate(), value = birthdate.toString())
                InfoRow(label = Animal.AGE.translate(), value = "$age ans")
            }

            // Propriétaire
            if (animal.ownerName.isNotEmpty()) {
                InfoRow(label = Animal.OWNER.translate(), value = animal.ownerName)
            }

            // Statut (vivant/décédé)
            InfoRow(label = "Statut", value = if (animal.dead) "Décédé" else "Vivant")

            // Résumé
            if (animal.summary.isNotEmpty()) {
                Text(text = Animal.SUMMARY.translate(), style = MaterialTheme.typography.subtitle1)
                Text(text = animal.summary, style = MaterialTheme.typography.body1)
            }

            // Historique de poids
            if (animal.weightHistory.isNotEmpty()) {
                val lastWeight = animal.weightHistory.maxByOrNull { it.date }
                lastWeight?.let { weight ->
                    InfoRow(
                            label = Animal.WEIGHT.translate(),
                            value = "${weight.value} kg (${weight.date})"
                    )
                }
            }
        }
    }
}
