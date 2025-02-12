package fr.vetbrain.vetnutri_mp.Example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.CodeView

@Composable
fun CodeViewExample() {
    Column(modifier = Modifier.padding(16.dp)) {
        CodeView(
                code =
                        """
                fun example() {
                    // Ceci est un commentaire
                    val number = 42
                    val text = "Hello, World!"
                    
                    @Composable
                    fun MyComponent() {
                        // Code du composant
                    }
                }
            """.trimIndent()
        )
    }
}
