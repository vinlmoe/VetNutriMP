package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.AppTextField
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import kotlinx.datetime.LocalDate

@Composable
fun AnimalIdentityView(viewModel: AnimalDetailViewModel, modifier: Modifier = Modifier) {
    val animal by viewModel.animal.collectAsState()
    var nom by remember(animal) { mutableStateOf(animal?.nom ?: "") }
    var race by remember(animal) { mutableStateOf(animal?.race ?: "") }
    var ownerName by remember(animal) { mutableStateOf(animal?.ownerName ?: "") }
    var selectedSex by remember(animal) { mutableStateOf(animal?.getSex() ?: Sex.MALEE) }
    var selectedEspece by remember(animal) { mutableStateOf(animal?.getEspece() ?: Espece.CHIEN) }
    var birthdate by remember(animal) { mutableStateOf(animal?.birthdate) }
    var summary by remember(animal) { mutableStateOf(animal?.summary ?: "") }
    var id by remember(animal) { mutableStateOf(animal?.id ?: "") }
    var isDead by remember(animal) { mutableStateOf(animal?.dead ?: false) }

    Column(
            modifier =
                    modifier.fillMaxSize()
                            .padding(AppSizes.paddingMedium)
                            .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
    ) {
        AppTextField(
                value = id,
                onValueChange = { id = it },
                label = "Identifiant",
                leadingIcon = AppIcons.AccountBox
        )

        AppTextField(
                value = nom,
                onValueChange = { nom = it },
                label = "Nom",
                leadingIcon = AppIcons.Pets
        )

        AppTextField(
                value = race,
                onValueChange = { race = it },
                label = "Race",
                leadingIcon = AppIcons.List
        )

        AppTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = "Propriétaire",
                leadingIcon = AppIcons.Person
        )

        // État de l'animal (vivant/mort)
        Column {
            SectionSubtitle(text = "État de l'animal")
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = if (isDead) "Décédé" else "Vivant",
                        style =
                                MaterialTheme.typography.body2.copy(
                                        fontSize = AppSizes.fontSizeBody2,
                                        color =
                                                if (isDead) MaterialTheme.colors.error
                                                else MaterialTheme.colors.primary
                                )
                )
                Switch(
                        checked = isDead,
                        onCheckedChange = { isDead = it },
                        colors =
                                SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colors.error,
                                        checkedTrackColor =
                                                MaterialTheme.colors.error.copy(alpha = 0.5f),
                                        uncheckedThumbColor = MaterialTheme.colors.primary,
                                        uncheckedTrackColor =
                                                MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                )
            }
        }

        // Date de naissance
        Column {
            SectionSubtitle(text = "Date de naissance")
            AppTextField(
                    value = birthdate?.toString() ?: "",
                    onValueChange = { dateStr ->
                        try {
                            birthdate = LocalDate.parse(dateStr)
                        } catch (e: Exception) {
                            // Ignorer les dates invalides
                        }
                    },
                    label = "AAAA-MM-JJ",
                    leadingIcon = AppIcons.DateRange
            )
        }

        // Sélecteur de sexe
        SexSelector(selectedSex = selectedSex, onSexSelected = { selectedSex = it })

        AppTextField(
                value = summary,
                onValueChange = { summary = it },
                label = "Résumé",
                leadingIcon = AppIcons.Info,
                singleLine = false,
                maxLines = 5,
                modifier = Modifier.height(AppSizes.textFieldHeight * 2.5f)
        )

        StandardButton(
                onClick = {
                    val updatedAnimal =
                            animal?.copy(
                                    nom = nom,
                                    race = race,
                                    ownerName = ownerName,
                                    sexId = selectedSex.id,
                                    summary = summary,
                                    id = id,
                                    dead = isDead,
                                    birthdate = birthdate
                            )
                    updatedAnimal?.let { viewModel.updateAnimal(it) }
                },
                text = "Enregistrer",
                modifier = Modifier.fillMaxWidth()
        )
    }
}
