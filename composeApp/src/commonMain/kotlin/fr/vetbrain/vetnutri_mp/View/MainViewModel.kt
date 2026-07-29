package fr.vetbrain.vetnutri_mp.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Greeting
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Minerals
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.ViewModel.MainViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun MainViewModel() {
    MaterialTheme {
        var text by remember { mutableStateOf("aa") }
        var showContent by remember { mutableStateOf(false) }
        var name by remember { mutableStateOf("aa") }
        var greetingText by remember { mutableStateOf("aa") }
        Row(Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    text = "Hello, $name!"
                    greetingText = text
                }) { Text(General.VALIDATE.translate()) }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(General.SEARCH.translate()) }
                )
            }
            AnimatedVisibility(showContent) {
                val greeting = remember { Greeting().greet() }
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(Minerals.CALCIUM.translate())
                }
            }
        }
    }
}
