package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Export.*

/**
 * Exemple d'utilisation du ConseilRepository
 *
 * Ce fichier montre comment utiliser le nouveau système de gestion des conseils personnalisés avec
 * persistance Room multiplatform.
 */
class ConseilRepositoryExample(private val database: AppDatabase) {

    private val conseilRepository = ConseilRepository(database.htmlSectionDao())

    /** Exemple : Créer un conseil nutritionnel pour les chiens */
    suspend fun creerConseilNutritionnelChien(): Result<Unit> {
        val contenu =
                RichTextContent(
                        blocks =
                                listOf(
                                        TextBlock.Heading(
                                                "h1",
                                                2,
                                                "Conseils nutritionnels pour chiens"
                                        ),
                                        TextBlock.Paragraph(
                                                "p1",
                                                "Une alimentation équilibrée est essentielle pour la santé de votre chien. Voici quelques recommandations importantes :",
                                                TextFormatting(isBold = true)
                                        ),
                                        TextBlock.ListBlock(
                                                "list1",
                                                items =
                                                        listOf(
                                                                "Respectez les portions recommandées selon l'âge et le poids",
                                                                "Privilégiez les aliments de qualité premium",
                                                                "Assurez-vous d'un apport suffisant en protéines",
                                                                "Évitez les aliments toxiques (chocolat, oignons, etc.)"
                                                        ),
                                                isOrdered = false
                                        )
                                )
                )

        val conseil =
                HtmlSection(
                        id = "conseil_nutrition_chien_001",
                        title = "Conseils nutritionnels pour chiens",
                        content = contenu,
                        category = SectionCategory.CONSEIL_NUTRITIONNEL,
                        tags = listOf("nutrition", "chien", "alimentation", "santé"),
                        priority = 5,
                        isActive = true,
                        targetSpecies = listOf("chien"),
                        targetAgeGroups = listOf("adulte", "senior")
                )

        return conseilRepository.sauvegarderConseil(conseil)
    }

    /** Exemple : Créer un template de conseil d'hygiène */
    suspend fun creerTemplateConseilHygiene(): Result<Unit> {
        val contenu =
                RichTextContent(
                        blocks =
                                listOf(
                                        TextBlock.Heading(
                                                "h1",
                                                2,
                                                "Conseils d'hygiène pour {{ESPECE}}"
                                        ),
                                        TextBlock.Paragraph(
                                                "p1",
                                                "L'hygiène est fondamentale pour maintenir la santé de votre {{ESPECE}}. Voici les points essentiels :",
                                                TextFormatting(isBold = true)
                                        ),
                                        TextBlock.ListBlock(
                                                "list1",
                                                items =
                                                        listOf(
                                                                "Brossage régulier du pelage",
                                                                "Nettoyage des oreilles",
                                                                "Coupe des griffes si nécessaire",
                                                                "Bain selon les besoins de l'espèce"
                                                        ),
                                                isOrdered = false
                                        )
                                )
                )

        val template =
                HtmlSection(
                        id = "template_hygiene_generique",
                        title = "Template conseils d'hygiène",
                        content = contenu,
                        category = SectionCategory.CONSEIL_HYGIENE,
                        tags = listOf("hygiène", "template", "soins"),
                        priority = 3,
                        isActive = true,
                        isTemplate = true,
                        targetSpecies = emptyList(), // Template générique
                        targetAgeGroups = emptyList()
                )

        return conseilRepository.sauvegarderConseil(template)
    }

    /** Exemple : Utiliser un template pour créer un conseil personnalisé */
    suspend fun creerConseilHygieneChatDepuisTemplate(): Result<HtmlSection> {
        return conseilRepository.creerConseilDepuisTemplate(
                templateId = "template_hygiene_generique",
                titre = "Conseils d'hygiène pour chats",
                modifications = mapOf("{{ESPECE}}" to "chat")
        )
    }

    /** Exemple : Rechercher des conseils */
    suspend fun rechercherConseilsNutrition(): Result<List<HtmlSection>> {
        return conseilRepository.rechercherConseils("nutrition", limit = 10)
    }

    /** Exemple : Récupérer les conseils populaires */
    suspend fun obtenirConseilsPopulaires(): Result<List<HtmlSection>> {
        return conseilRepository.getConseilsPopulaires(limit = 5)
    }

    /** Exemple : Récupérer les conseils pour une espèce spécifique */
    suspend fun obtenirConseilsPourChiens(): Result<List<HtmlSection>> {
        return conseilRepository.getConseilsParEspece("chien")
    }

    /** Exemple : Marquer un conseil comme utilisé */
    suspend fun marquerConseilUtilise(conseilId: String): Result<Unit> {
        return conseilRepository.incrementerUsage(conseilId)
    }

    /** Exemple : Récupérer les conseils par catégorie */
    suspend fun obtenirConseilsSante(): Result<List<HtmlSection>> {
        return conseilRepository.getConseilsByCategory(SectionCategory.CONSEIL_SANTE)
    }

    /** Exemple : Récupérer les templates disponibles */
    suspend fun obtenirTemplatesConseils(): Result<List<HtmlSection>> {
        return conseilRepository.getTemplatesConseils()
    }

    /** Exemple : Sauvegarder un conseil existant comme template */
    suspend fun sauvegarderConseilCommeTemplate(
            conseilId: String,
            nomTemplate: String
    ): Result<HtmlSection> {
        return conseilRepository.sauvegarderCommeTemplate(conseilId, nomTemplate)
    }
}
