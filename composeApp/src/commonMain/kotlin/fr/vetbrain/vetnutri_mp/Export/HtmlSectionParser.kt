package fr.vetbrain.vetnutri_mp.Export

/** Parseur pour convertir les sections HTML en HTML propre */
object HtmlSectionParser {

    /** Convertit une section HTML en HTML */
    fun parseSectionToHtml(section: HtmlSection): String {
        val contentHtml = parseContentToHtml(section.content)
        return """
            <div class='html-section' data-section-id='${section.id}' data-category='${section.category}'>
                <div class='section-title'>${escapeHtml(section.title)}</div>
                <div class='section-content'>
                    $contentHtml
                </div>
                ${if (section.tags.isNotEmpty()) "<div class='section-tags'>${section.tags.joinToString(", ") { "#$it" }}</div>" else ""}
            </div>
        """.trimIndent()
    }

    /** Convertit une section HTML en HTML pour l'export (sans titre) */
    fun parseSectionToHtmlForExport(section: HtmlSection): String {
        val contentHtml = parseContentToHtml(section.content)
        return """
            <div class='html-section' data-section-id='${section.id}' data-category='${section.category}'>
                <div class='section-content'>
                    $contentHtml
                </div>
                ${if (section.tags.isNotEmpty()) "<div class='section-tags'>${section.tags.joinToString(", ") { "#$it" }}</div>" else ""}
            </div>
        """.trimIndent()
    }

    /** Convertit le contenu riche en HTML */
    fun parseContentToHtml(content: RichTextContent): String {
        return content.blocks.joinToString("\n") { parseBlockToHtml(it) }
    }

    /** Convertit un bloc de texte en HTML */
    private fun parseBlockToHtml(block: TextBlock): String {
        return when (block) {
            is TextBlock.Paragraph -> parseParagraphToHtml(block)
            is TextBlock.Heading -> parseHeadingToHtml(block)
            is TextBlock.ListBlock -> parseListToHtml(block)
            is TextBlock.TableBlock -> parseTableToHtml(block)
        }
    }

    /** Convertit un paragraphe en HTML avec formatage */
    private fun parseParagraphToHtml(paragraph: TextBlock.Paragraph): String {
        val formattedText = applyTextFormatting(paragraph.text, paragraph.formatting)
        return "<p>$formattedText</p>"
    }

    /** Convertit un titre en HTML */
    private fun parseHeadingToHtml(heading: TextBlock.Heading): String {
        val level = heading.level.coerceIn(1, 6)
        val escapedText = escapeHtml(heading.text)
        return "<h$level>$escapedText</h$level>"
    }

    /** Convertit une liste en HTML */
    private fun parseListToHtml(listBlock: TextBlock.ListBlock): String {
        val listItems =
                listBlock.items.joinToString("\n") { item -> "<li>${escapeHtml(item)}</li>" }

        return if (listBlock.isOrdered) {
            "<ol>$listItems</ol>"
        } else {
            "<ul>$listItems</ul>"
        }
    }

    /** Convertit un tableau en HTML */
    private fun parseTableToHtml(tableBlock: TextBlock.TableBlock): String {
        val headers =
                tableBlock.headers.joinToString("") { header -> "<th>${escapeHtml(header)}</th>" }

        val rows =
                tableBlock.rows.joinToString("\n") { row ->
                    val cells = row.joinToString("") { cell -> "<td>${escapeHtml(cell)}</td>" }
                    "<tr>$cells</tr>"
                }

        return """
            <table>
                <thead><tr>$headers</tr></thead>
                <tbody>$rows</tbody>
            </table>
        """.trimIndent()
    }

    /** Applique le formatage de texte à une chaîne */
    private fun applyTextFormatting(text: String, formatting: TextFormatting): String {
        var result = escapeHtml(text)

        // Gestion des styles de base
        if (formatting.isBold) {
            result = "<strong>$result</strong>"
        }
        if (formatting.isItalic) {
            result = "<em>$result</em>"
        }
        if (formatting.isUnderline) {
            result = "<u>$result</u>"
        }
        if (formatting.isStrikethrough) {
            result = "<del>$result</del>"
        }

        // Gestion de la couleur
        if (formatting.color != null) {
            result = "<span style='color: ${formatting.color}'>$result</span>"
        }

        // Gestion de la taille de police
        if (formatting.fontSize != null) {
            val sizeStyle =
                    if (formatting.color != null) {
                        "font-size: ${formatting.fontSize}pt; color: ${formatting.color}"
                    } else {
                        "font-size: ${formatting.fontSize}pt"
                    }
            result = "<span style='$sizeStyle'>$result</span>"
        }

        return result
    }

    /** Échappe les caractères HTML spéciaux */
    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br/>")
    }

    /** Génère le CSS pour les sections HTML */
    fun generateSectionCss(): String {
        return """
            .html-section {
                margin-bottom: 20px;
                padding: 15px;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                background: #fafafa;
            }

            .section-title {
                font-size: 18px;
                font-weight: bold;
                margin-bottom: 10px;
                color: #333;
                border-bottom: 2px solid #007acc;
                padding-bottom: 5px;
            }

            .section-content {
                line-height: 1.6;
            }

            .section-content p {
                margin-bottom: 10px;
            }

            .section-content h1,
            .section-content h2,
            .section-content h3,
            .section-content h4,
            .section-content h5,
            .section-content h6 {
                margin-top: 20px;
                margin-bottom: 10px;
                color: #333;
            }

            .section-content h1 { font-size: 24px; }
            .section-content h2 { font-size: 20px; }
            .section-content h3 { font-size: 18px; }
            .section-content h4 { font-size: 16px; }
            .section-content h5 { font-size: 14px; }
            .section-content h6 { font-size: 12px; }

            .section-content ul,
            .section-content ol {
                margin-left: 20px;
                margin-bottom: 10px;
            }

            .section-content li {
                margin-bottom: 5px;
            }

            .section-content table {
                width: 100%;
                border-collapse: collapse;
                margin-bottom: 10px;
                background: white;
            }

            .section-content th,
            .section-content td {
                border: 1px solid #ddd;
                padding: 8px 12px;
                text-align: left;
            }

            .section-content th {
                background: #f5f5f5;
                font-weight: bold;
            }

            .section-tags {
                margin-top: 10px;
                font-size: 12px;
                color: #666;
            }

            .section-tags::before {
                content: "Tags: ";
                font-weight: bold;
            }
        """.trimIndent()
    }
}



