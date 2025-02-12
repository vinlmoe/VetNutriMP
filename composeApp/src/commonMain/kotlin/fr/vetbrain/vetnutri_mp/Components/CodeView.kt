package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors

@Composable
fun CodeView(code: String) {
    Box(modifier = Modifier.fillMaxWidth().background(VetNutriColors.Surface).padding(8.dp)) {
        Text(text = highlightSyntax(code), modifier = Modifier.fillMaxWidth())
    }
}

private fun highlightSyntax(code: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = code.split("\n")
        lines.forEachIndexed { index, line ->
            if (index > 0) append("\n")

            // Mots-clés
            val keywords =
                    listOf(
                            "class",
                            "fun",
                            "val",
                            "var",
                            "if",
                            "else",
                            "when",
                            "for",
                            "while",
                            "return",
                            "private",
                            "public",
                            "protected",
                            "internal",
                            "object",
                            "interface",
                            "enum"
                    )
            var currentPosition = 0

            val words = line.split(" ", "(", ")", "{", "}", "[", "]", ",", ".", ";")
            words.forEach { word ->
                val start = line.indexOf(word, currentPosition)
                if (start >= 0) {
                    val end = start + word.length

                    when {
                        keywords.contains(word) -> {
                            addStyle(
                                    SpanStyle(color = VetNutriColors.CodeColors.Keyword),
                                    start,
                                    end
                            )
                        }
                        word.startsWith("\"") && word.endsWith("\"") -> {
                            addStyle(
                                    SpanStyle(color = VetNutriColors.CodeColors.String),
                                    start,
                                    end
                            )
                        }
                        word.all { it.isDigit() } -> {
                            addStyle(
                                    SpanStyle(color = VetNutriColors.CodeColors.Number),
                                    start,
                                    end
                            )
                        }
                        word.startsWith("//") -> {
                            addStyle(
                                    SpanStyle(color = VetNutriColors.CodeColors.Comment),
                                    start,
                                    line.length
                            )
                            currentPosition = line.length
                            return@forEach
                        }
                        word.startsWith("@") -> {
                            addStyle(
                                    SpanStyle(color = VetNutriColors.CodeColors.Annotation),
                                    start,
                                    end
                            )
                        }
                        word.matches(Regex("[A-Z][a-zA-Z]*")) -> {
                            addStyle(SpanStyle(color = VetNutriColors.CodeColors.Type), start, end)
                        }
                    }
                    currentPosition = end
                }
            }
            append(line)
        }
    }
}
