package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ComboBox
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun SecondView(onClose: () -> Unit = {}, modifier: Modifier = Modifier) {
        var selectedEspece by remember { mutableStateOf<Espece?>(null) }
        var textInput by remember { mutableStateOf("") }

        Column(
                modifier =
                        modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                Text(
                        General.APP_NAME.translate(),
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        label = {
                                Text(
                                        Animal.NAME.translate(),
                                        style = MaterialTheme.typography.body1
                                )
                        },
                        textStyle = MaterialTheme.typography.body1,
                        modifier = Modifier.fillMaxWidth()
                )

                ComboBox(
                        items = Espece.valuesExcept(),
                        init = null,
                        label = Animal.SPECIES.translate(),
                        onItemSelected = { selectedLabel ->
                                selectedEspece = Espece.values().find { it.label == selectedLabel }
                        },
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                ButtonDefaults.buttonColors(
                                        backgroundColor = VetNutriColors.Primary,
                                        contentColor = VetNutriColors.OnPrimary
                                )
                ) { Text(General.CANCEL.translate(), style = MaterialTheme.typography.button) }
        }
}
