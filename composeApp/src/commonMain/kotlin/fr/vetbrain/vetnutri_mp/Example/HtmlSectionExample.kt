package fr.vetbrain.vetnutri_mp.Example

import fr.vetbrain.vetnutri_mp.Export.*
import kotlinx.datetime.Clock

/**
 * Exemples d'utilisation du système de sections HTML réutilisables
 */
object HtmlSectionExample {

    /**
     * Crée des exemples de sections HTML pour démonstration
     */
    fun createSampleSections(): List<HtmlSection> {
        return listOf(
            // Section d'introduction
            HtmlSection(
                id = "intro_1",
                title = "Introduction Nutritionnelle",
                content = RichTextContent(
                    blocks = listOf(
                        TextBlock.Heading(
                            id = "heading_1",
                            level = 1,
                            text = "Conseils Nutritionnels Généraux"
                        ),
                        TextBlock.Paragraph(
                            id = "para_1",
                            text = "L'alimentation joue un rôle crucial dans la santé et le bien-être de votre animal. Une ration équilibrée doit prendre en compte l'âge, la race, l'activité physique et l'état de santé de l'animal.",
                            formatting = TextFormatting()
                        ),
                        TextBlock.ListBlock(
                            id = "list_1",
                            items = listOf(
                                "Respecter les proportions recommandées",
                                "Adapter la ration selon l'activité",
                                "Consulter régulièrement un vétérinaire"
                            ),
                            isOrdered = false
                        )
                    )
                ),
                category = SectionCategory.INTRODUCTION,
                tags = listOf("conseils", "général", "nutrition")
            ),

            // Section de conclusion
            HtmlSection(
                id = "conclusion_1",
                title = "Recommandations Finales",
                content = RichTextContent(
                    blocks = listOf(
                        TextBlock.Heading(
                            id = "heading_2",
                            level = 2,
                            text = "Suivi et Ajustements"
                        ),
                        TextBlock.Paragraph(
                            id = "para_2",
                            text = "Il est recommandé de surveiller régulièrement le poids et l'état général de votre animal. N'hésitez pas à consulter votre vétérinaire nutritionniste pour des ajustements personnalisés.",
                            formatting = TextFormatting(isItalic = true)
                        )
                    )
                ),
                category = SectionCategory.CONCLUSION,
                tags = listOf("suivi", "recommandations")
            ),

            // Section avec tableau
            HtmlSection(
                id = "table_example",
                title = "Tableau des Valeurs Nutritionnelles",
                content = RichTextContent(
                    blocks = listOf(
                        TextBlock.Heading(
                            id = "heading_3",
                            level = 2,
                            text = "Comparaison des Protéines"
                        ),
                        TextBlock.TableBlock(
                            id = "table_1",
                            headers = listOf("Aliment", "Protéines (%)", "Énergie (kcal/kg)"),
                            rows = listOf(
                                listOf("Viande de bœuf", "25.5", "1450"),
                                listOf("Poulet", "22.0", "1350"),
                                listOf("Poisson", "20.8", "1200")
                            )
                        )
                    )
                ),
                category = SectionCategory.RECOMMANDATIONS,
                tags = listOf("tableau", "valeurs", "comparaison")
            )
        )
    }

    /**
     * Exemple d'utilisation dans l'export PDF
     */
    fun createSampleExportData(): ExportData {
        val sampleSections = createSampleSections()

        return ExportData(
            animal = null,
            ration = null,
            reference = null,
            title = "Rapport Nutritionnel Complet",
            additionalText = "Ce rapport a été généré automatiquement le ${Clock.System.now()}.",
            htmlSections = sampleSections
        )
    }

    /**
     * Exemple de génération HTML complète
     */
    fun generateSampleHtml(): String {
        val exportData = createSampleExportData()
        return HtmlDocumentBuilder.buildHtml(DocumentType.RATION_ANALYSIS, exportData)
    }
}
