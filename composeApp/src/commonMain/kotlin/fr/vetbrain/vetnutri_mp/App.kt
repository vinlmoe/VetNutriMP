package fr.vetbrain.vetnutri_mp

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.View.*

@Composable
@Preview
fun App() {
    MaterialTheme {

        var selectedItem by remember { mutableStateOf<NutrientMacro?>(null)}
        var text by remember { mutableStateOf("Tester") }
        var expanded by remember { mutableStateOf(false) }
        var showContent by remember { mutableStateOf(false) }




        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //preie

            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nom") }
            )
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Race") }
            )

            ComboBox(
                items = NutrientMacro.entries,
                init=null,
                label = "Choose an option",
                onItemSelected = {
                    selectedItem = NutrientMacro.getByLabel(it)?: NutrientMacro.CAL }
            )
            TextFieldNut(
                value= selectedItem,
                label = "Race"
            )

        }
    }


}



