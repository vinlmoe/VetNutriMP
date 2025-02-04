package fr.vetbrain.vetnutri_mp.Example

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.vetbrain.vetnutri_mp.Components.CodeView

@Composable
fun CodeViewExample() {
    val sampleCode = """
        @Entity(tableName = "Biblio")
        @Serializable
        data class BiblioRef(
            @PrimaryKey val uuid: String = Uuid.random().toString(),
            @ColumnInfo(name = "fAuthor") var firstAuthor: String?,
            var year: String?,
            @ColumnInfo(name = "fullRef") var completeRef: String?,
            var comments: String?,
            var consistent: Int?
        )
    """.trimIndent()

    CodeView(
        code = sampleCode,
        modifier = Modifier
    )
} 