package fr.vetbrain.vetnutri_mp.Components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import fr.vetbrain.vetnutri_mp.Theme.AppColors.CodeColors

@Composable
fun CodeView(code: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        BasicText(
            text = highlightKotlinCode(code)
        )
    }
}

private fun highlightKotlinCode(code: String): AnnotatedString = buildAnnotatedString {
    // Liste des mots-clés Kotlin
    val keywords = setOf(
        "package", "import", "class", "object", "interface", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "try", "catch", "finally",
        "throw", "return", "continue", "break", "as", "is", "in", "!in", "by",
        "constructor", "delegate", "dynamic", "field", "file", "get", "init",
        "param", "property", "receiver", "set", "setparam", "where", "actual",
        "abstract", "annotation", "companion", "const", "crossinline", "data",
        "enum", "expect", "external", "final", "infix", "inline", "inner",
        "internal", "lateinit", "noinline", "open", "operator", "out", "override",
        "private", "protected", "public", "reified", "sealed", "suspend",
        "tailrec", "vararg", "null", "true", "false"
    )

    val lines = code.split("\n")
    lines.forEachIndexed { index, line ->
        if (line.trimStart().startsWith("//")) {
            // Commentaires
            withStyle(SpanStyle(color = CodeColors.Comment)) {
                append(line)
            }
        } else {
            var currentPosition = 0
            val tokens = line.split(Regex("(\\s+|[(){}\\[\\],.;:])|(?=[(){}\\[\\],.;:])|(?<=[(){}\\[\\],.;:])"))
            
            tokens.forEach { token ->
                when {
                    token.isBlank() -> append(token)
                    token.startsWith("@") -> withStyle(SpanStyle(color = CodeColors.Annotation)) {
                        append(token)
                    }
                    token.matches(Regex("\".*\"")) -> withStyle(SpanStyle(color = CodeColors.String)) {
                        append(token)
                    }
                    token.matches(Regex("-?\\d+(\\.\\d+)?")) -> withStyle(SpanStyle(color = CodeColors.Number)) {
                        append(token)
                    }
                    keywords.contains(token) -> withStyle(SpanStyle(color = CodeColors.Keyword)) {
                        append(token)
                    }
                    token.matches(Regex("[A-Z][A-Za-z]*")) -> withStyle(SpanStyle(color = CodeColors.Type)) {
                        append(token)
                    }
                    token.matches(Regex("[a-z][A-Za-z]*")) -> withStyle(SpanStyle(color = CodeColors.Property)) {
                        append(token)
                    }
                    else -> append(token)
                }
                currentPosition += token.length
            }
        }
        if (index < lines.size - 1) {
            append("\n")
        }
    }
} 