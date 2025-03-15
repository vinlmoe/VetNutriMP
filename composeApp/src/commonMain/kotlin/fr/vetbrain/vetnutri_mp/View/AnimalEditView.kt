package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.datetime.LocalDate

@Composable
fun AnimalEditView(
        animal: AnimalEv,
        onSave: (AnimalEv) -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
) {
        var nom by remember { mutableStateOf(animal.nom) }
        var race by remember { mutableStateOf(animal.race) }
        var ownerName by remember { mutableStateOf(animal.ownerName) }
        var summary by remember { mutableStateOf(animal.summary) }
        var selectedSex by remember { mutableStateOf(animal.getSex()) }
        var selectedEspece by remember { mutableStateOf(animal.getEspece()) }
        var birthDateText by remember { mutableStateOf(animal.birthdate?.toString() ?: "") }
        var isDateValid by remember { mutableStateOf(true) }
        var isDead by remember { mutableStateOf(animal.dead) }

        // État pour le dropdown du sexe
        var sexDropdownExpanded by remember { mutableStateOf(false) }
        // État pour le dropdown de l'espèce
        var especeDropdownExpanded by remember { mutableStateOf(false) }

        val scrollState = rememberScrollState()

        Card(
                modifier = modifier.fillMaxSize(),
                elevation = AppSizes.elevationMedium,
                backgroundColor = MaterialTheme.colors.surface
        ) {
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(AppSizes.paddingLarge)
                                        .verticalScroll(scrollState),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // Titre avec style amélioré
                        Text(
                                text = Animal.EDIT_ANIMAL.translate(),
                                style = MaterialTheme.typography.h5,
                                color = VetNutriColors.Primary
                        )

                        Divider(color = Color.LightGray, thickness = AppSizes.dividerHeight)

                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))

                        // Nom de l'animal
                        OutlinedTextField(
                                value = nom,
                                onValueChange = { nom = it },
                                label = { Text(Animal.NAME.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Nom",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        )

                        // Race
                        OutlinedTextField(
                                value = race,
                                onValueChange = { race = it },
                                label = { Text(Animal.BREED.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                        )

                        // Propriétaire
                        OutlinedTextField(
                                value = ownerName,
                                onValueChange = { ownerName = it },
                                label = { Text(Animal.OWNER.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Propriétaire",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        )

                        // Date de naissance
                        OutlinedTextField(
                                value = birthDateText,
                                onValueChange = {
                                        birthDateText = it
                                        isDateValid =
                                                try {
                                                        if (it.isNotEmpty()) LocalDate.parse(it)
                                                        true
                                                } catch (e: Exception) {
                                                        false
                                                }
                                },
                                label = { Text(Animal.BIRTH_DATE.translate()) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                isError = !isDateValid,
                                leadingIcon = {
                                        Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Date de naissance",
                                                tint = VetNutriColors.Primary
                                        )
                                }
                        )

                        if (!isDateValid) {
                                Text(
                                        text = "Format de date invalide (YYYY-MM-DD)",
                                        color = MaterialTheme.colors.error,
                                        style = MaterialTheme.typography.caption
                                )
                        }

                        // Sexe avec DropdownMenu
                        Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value = selectedSex.displayName,
                                        onValueChange = {},
                                        label = { Text(Animal.SEX.translate()) },
                                        modifier =
                                                Modifier.fillMaxWidth().onFocusChanged {
                                                        if (it.isFocused) sexDropdownExpanded = true
                                                },
                                        readOnly = true,
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = { sexDropdownExpanded = true }
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.ArrowDropDown,
                                                                contentDescription =
                                                                        "Sélectionner le sexe"
                                                        )
                                                }
                                        }
                                )

                                DropdownMenu(
                                        expanded = sexDropdownExpanded,
                                        onDismissRequest = { sexDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                        Sex.entries.forEach { sex ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedSex = sex
                                                                sexDropdownExpanded = false
                                                        }
                                                ) { Text(text = sex.displayName) }
                                        }
                                }
                        }

                        // Espèce avec DropdownMenu
                        Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                        value = selectedEspece.label,
                                        onValueChange = {},
                                        label = { Text(Animal.SPECIES.translate()) },
                                        modifier =
                                                Modifier.fillMaxWidth().onFocusChanged {
                                                        if (it.isFocused)
                                                                especeDropdownExpanded = true
                                                },
                                        readOnly = true,
                                        trailingIcon = {
                                                IconButton(
                                                        onClick = { especeDropdownExpanded = true }
                                                ) {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Default.ArrowDropDown,
                                                                contentDescription =
                                                                        "Sélectionner l'espèce"
                                                        )
                                                }
                                        }
                                )

                                DropdownMenu(
                                        expanded = especeDropdownExpanded,
                                        onDismissRequest = { especeDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                        Espece.valuesExcept(Espece.CH).forEach { espece ->
                                                DropdownMenuItem(
                                                        onClick = {
                                                                selectedEspece = espece
                                                                especeDropdownExpanded = false
                                                        }
                                                ) { Text(text = espece.label) }
                                        }
                                }
                        }

                        // Animal décédé
                        Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                        Modifier.padding(vertical = AppSizes.paddingSmall)
                                                .fillMaxWidth()
                                                .padding(horizontal = AppSizes.paddingSmall)
                        ) {
                                Checkbox(
                                        checked = isDead,
                                        onCheckedChange = { isDead = it },
                                        colors =
                                                CheckboxDefaults.colors(
                                                        checkedColor = VetNutriColors.Primary
                                                )
                                )
                                Text(
                                        text = Animal.DEAD.translate(),
                                        modifier = Modifier.padding(start = AppSizes.paddingSmall),
                                        style = MaterialTheme.typography.body1
                                )
                        }

                        // Résumé
                        OutlinedTextField(
                                value = summary,
                                onValueChange = { summary = it },
                                label = { Text(Animal.SUMMARY.translate()) },
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .height(AppSizes.inputMultilineHeight),
                                maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(AppSizes.paddingMedium))

                        Divider(color = Color.LightGray, thickness = AppSizes.dividerHeight)

                        // Boutons d'action
                        Row(
                                modifier =
                                        Modifier.fillMaxWidth()
                                                .padding(top = AppSizes.paddingMedium),
                                horizontalArrangement =
                                        Arrangement.spacedBy(AppSizes.paddingMedium, Alignment.End),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                OutlinedButton(
                                        onClick = onCancel,
                                        colors =
                                                ButtonDefaults.outlinedButtonColors(
                                                        contentColor = VetNutriColors.Secondary
                                                ),
                                        border =
                                                ButtonDefaults.outlinedBorder.copy(
                                                        brush = SolidColor(VetNutriColors.Secondary)
                                                )
                                ) {
                                        Text(
                                                General.CANCEL.translate(),
                                                style = MaterialTheme.typography.button
                                        )
                                }

                                Button(
                                        onClick = {
                                                val birthdate =
                                                        try {
                                                                if (birthDateText.isNotEmpty())
                                                                        LocalDate.parse(
                                                                                birthDateText
                                                                        )
                                                                else null
                                                        } catch (e: Exception) {
                                                                null
                                                        }

                                                val updatedAnimal =
                                                        animal.copy(
                                                                nom = nom,
                                                                race = race,
                                                                ownerName = ownerName,
                                                                summary = summary,
                                                                sexId = selectedSex.id,
                                                                birthdate = birthdate,
                                                                dead = isDead,
                                                                specieId = selectedEspece.label
                                                        )

                                                // Log de débogage pour vérifier les informations de
                                                // l'animal
                                                println(
                                                        "DEBUG_EDIT_VIEW: Animal mis à jour avec nom=${updatedAnimal.nom}, specieId=${updatedAnimal.specieId}, espece=${selectedEspece.label}"
                                                )

                                                onSave(updatedAnimal)
                                        },
                                        enabled = nom.isNotEmpty() && isDateValid,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary,
                                                        disabledBackgroundColor =
                                                                VetNutriColors.Primary.copy(
                                                                        alpha = 0.3f
                                                                )
                                                )
                                ) {
                                        Text(
                                                General.SAVE.translate(),
                                                style = MaterialTheme.typography.button
                                        )
                                }
                        }
                }
        }
}
