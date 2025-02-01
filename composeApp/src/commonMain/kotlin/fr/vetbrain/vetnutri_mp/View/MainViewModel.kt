package fr.vetbrain.vetnutri_mp.View

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import vetnutrimp.composeapp.generated.resources.Res
import vetnutrimp.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun DataModelAnimal(){
    var text by remember { mutableStateOf("Tester") }
    var showContent by remember { mutableStateOf(false) }
    Row (
        Modifier.fillMaxHeight() ,
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(

            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { showContent = !showContent }) {
                Text("Click me!")
            }
            TextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Label") }
            )
        }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting")
            }
        }
    }




}