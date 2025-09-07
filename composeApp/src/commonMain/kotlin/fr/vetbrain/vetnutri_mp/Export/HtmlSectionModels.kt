package fr.vetbrain.vetnutri_mp.Export

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/** Modèle de données pour une section HTML réutilisable */
@Serializable
data class HtmlSection(
        val id: String,
        val title: String,
        val content: RichTextContent,
        val category: SectionCategory = SectionCategory.GENERAL,
        val tags: List<String> = emptyList(),
        val createdAt: Instant = Clock.System.now(),
        val updatedAt: Instant = Clock.System.now(),
        val isTemplate: Boolean = false
)

/** Catégories de sections pour une meilleure organisation */
@Serializable
enum class SectionCategory {
    GENERAL,
    INTRODUCTION,
    CONCLUSION,
    CONSEILS,
    RECOMMANDATIONS,
    NOTES,
    CUSTOM
}

/** Contenu de texte enrichi avec support du formatage */
@Serializable data class RichTextContent(val blocks: List<TextBlock> = emptyList())

/** Bloc de texte avec son formatage */
@Serializable
sealed class TextBlock {
    abstract val id: String

    @Serializable
    data class Paragraph(
            override val id: String,
            val text: String,
            val formatting: TextFormatting = TextFormatting()
    ) : TextBlock()

    @Serializable
    data class Heading(
            override val id: String,
            val level: Int, // 1-6 pour h1-h6
            val text: String
    ) : TextBlock()

    @Serializable
    data class ListBlock(
            override val id: String,
            val items: List<String>,
            val isOrdered: Boolean = false
    ) : TextBlock()

    @Serializable
    data class TableBlock(
            override val id: String,
            val headers: List<String>,
            val rows: List<List<String>>
    ) : TextBlock()
}

/** Formatage de texte pour les paragraphes */
@Serializable
data class TextFormatting(
        val isBold: Boolean = false,
        val isItalic: Boolean = false,
        val isUnderline: Boolean = false,
        val isStrikethrough: Boolean = false,
        val fontSize: Int? = null, // Taille en points, null = taille par défaut
        val color: String? = null, // Couleur hex, null = couleur par défaut
        val alignment: TextAlignment = TextAlignment.LEFT
)

@Serializable
enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

/** Modèle pour une bibliothèque de sections */
@Serializable
data class HtmlSectionLibrary(
        val sections: List<HtmlSection> = emptyList(),
        val categories: List<SectionCategory> = SectionCategory.entries
)



