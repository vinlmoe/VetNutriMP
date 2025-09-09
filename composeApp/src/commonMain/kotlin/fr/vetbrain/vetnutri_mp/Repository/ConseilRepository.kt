package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.DataBase.HtmlSectionDao
import fr.vetbrain.vetnutri_mp.DataBase.HtmlSectionEntity
import fr.vetbrain.vetnutri_mp.Export.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Repository spécialisé pour la gestion des conseils personnalisés */
class ConseilRepository(private val dao: HtmlSectionDao) {

    private val json = Json { prettyPrint = true }

    /** Récupère tous les conseils actifs */
    suspend fun getConseilsActifs(): Result<List<HtmlSection>> {
        return try {
            // Récupérer toutes les sections qui commencent par "CONSEIL"
            val allEntities = dao.getAllSections()
            val conseilEntities =
                    allEntities.filter { entity -> entity.category.startsWith("CONSEIL") }
            val conseils = conseilEntities.map { it.toDomain() }.filter { it.isActive }
            Result.success(conseils)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les conseils par espèce */
    suspend fun getConseilsParEspece(espece: String): Result<List<HtmlSection>> {
        return try {
            val allConseils = getConseilsActifs().getOrThrow()
            val conseilsFiltres =
                    allConseils.filter {
                        it.targetSpecies.isEmpty() || it.targetSpecies.contains(espece)
                    }
            Result.success(conseilsFiltres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Incrémente le compteur d'utilisation d'un conseil */
    suspend fun incrementerUsage(conseilId: String): Result<Unit> {
        return try {
            val entity =
                    dao.getSectionById(conseilId)
                            ?: return Result.failure(Exception("Conseil non trouvé"))
            val conseil = entity.toDomain()
            val conseilMisAJour =
                    conseil.copy(usageCount = conseil.usageCount + 1, lastUsed = Clock.System.now())
            dao.updateSection(conseilMisAJour.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les conseils les plus utilisés */
    suspend fun getConseilsPopulaires(limit: Int = 10): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getConseilsPopulaires(limit)
            val conseils = entities.map { it.toDomain() }
            Result.success(conseils)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les conseils récemment utilisés */
    suspend fun getConseilsRecents(limit: Int = 20): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getConseilsRecents(limit)
            val conseils = entities.map { it.toDomain() }
            Result.success(conseils)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Recherche de conseils par mot-clé */
    suspend fun rechercherConseils(query: String, limit: Int = 50): Result<List<HtmlSection>> {
        return try {
            val entities = dao.searchConseils(query, limit)
            val conseils = entities.map { it.toDomain() }
            Result.success(conseils)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère les conseils par catégorie */
    suspend fun getConseilsByCategory(category: SectionCategory): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getSectionsByCategory(category.name)
            val conseils = entities.map { it.toDomain() }
            Result.success(conseils)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Crée un conseil à partir d'un template */
    suspend fun creerConseilDepuisTemplate(
            templateId: String,
            titre: String,
            modifications: Map<String, String> = emptyMap()
    ): Result<HtmlSection> {
        return try {
            val template =
                    dao.getSectionById(templateId)?.toDomain()
                            ?: return Result.failure(Exception("Template non trouvé"))

            val nouveauConseil =
                    template.copy(
                            id = generateConseilId(),
                            title = titre,
                            isTemplate = false,
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now(),
                            usageCount = 0,
                            lastUsed = null
                    )

            // Appliquer les modifications personnalisées
            val conseilFinal = appliquerModifications(nouveauConseil, modifications)

            dao.insertSection(conseilFinal.toEntity())
            Result.success(conseilFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère le nombre total de conseils actifs */
    suspend fun getConseilsCount(): Result<Int> {
        return try {
            val count = dao.getConseilsCount()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sauvegarde un conseil (insert ou update) */
    suspend fun saveConseil(conseil: fr.vetbrain.vetnutri_mp.Export.HtmlSection): Result<Unit> {
        return try {
            dao.insertSection(conseil.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Récupère tous les templates de conseils */
    suspend fun getTemplatesConseils(): Result<List<HtmlSection>> {
        return try {
            val entities = dao.getTemplateSections()
            val templates =
                    entities.map { it.toDomain() }.filter { it.category.name.startsWith("CONSEIL") }
            Result.success(templates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sauvegarde un conseil comme template */
    suspend fun sauvegarderCommeTemplate(
            conseilId: String,
            nomTemplate: String
    ): Result<HtmlSection> {
        return try {
            val conseil =
                    dao.getSectionById(conseilId)?.toDomain()
                            ?: return Result.failure(Exception("Conseil non trouvé"))

            val template =
                    conseil.copy(
                            id = generateTemplateId(),
                            title = nomTemplate,
                            isTemplate = true,
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now()
                    )

            dao.insertSection(template.toEntity())
            Result.success(template)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Sauvegarde un nouveau conseil */
    suspend fun sauvegarderConseil(conseil: HtmlSection): Result<Unit> {
        return try {
            dao.insertSection(conseil.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Met à jour un conseil existant */
    suspend fun mettreAJourConseil(conseil: HtmlSection): Result<Unit> {
        return try {
            val conseilMisAJour = conseil.copy(updatedAt = Clock.System.now())
            dao.updateSection(conseilMisAJour.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Supprime un conseil */
    suspend fun supprimerConseil(conseilId: String): Result<Unit> {
        return try {
            val entity =
                    dao.getSectionById(conseilId)
                            ?: return Result.failure(Exception("Conseil non trouvé"))
            dao.deleteSection(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Méthodes utilitaires de conversion

    private fun HtmlSection.toEntity(): HtmlSectionEntity {
        return HtmlSectionEntity(
                id = this.id,
                title = this.title,
                contentJson = json.encodeToString(this.content),
                category = this.category.name,
                tagsJson = json.encodeToString(this.tags),
                createdAt = this.createdAt.toEpochMilliseconds(),
                updatedAt = this.updatedAt.toEpochMilliseconds(),
                isTemplate = this.isTemplate,
                priority = this.priority,
                isActive = this.isActive,
                targetSpeciesJson = json.encodeToString(this.targetSpecies),
                targetAgeGroupsJson = json.encodeToString(this.targetAgeGroups),
                usageCount = this.usageCount,
                lastUsed = this.lastUsed?.toEpochMilliseconds()
        )
    }

    private fun HtmlSectionEntity.toDomain(): HtmlSection {
        return HtmlSection(
                id = this.id,
                title = this.title,
                content = json.decodeFromString(this.contentJson),
                category = SectionCategory.valueOf(this.category),
                tags = json.decodeFromString(this.tagsJson),
                createdAt = Instant.fromEpochMilliseconds(this.createdAt),
                updatedAt = Instant.fromEpochMilliseconds(this.updatedAt),
                isTemplate = this.isTemplate,
                priority = this.priority,
                isActive = this.isActive,
                targetSpecies = json.decodeFromString(this.targetSpeciesJson),
                targetAgeGroups = json.decodeFromString(this.targetAgeGroupsJson),
                usageCount = this.usageCount,
                lastUsed = this.lastUsed?.let { Instant.fromEpochMilliseconds(it) }
        )
    }

    private fun appliquerModifications(
            conseil: HtmlSection,
            modifications: Map<String, String>
    ): HtmlSection {
        // Logique pour appliquer les modifications personnalisées
        // Par exemple, remplacer des placeholders dans le contenu
        return conseil
    }

    private fun generateConseilId(): String =
            "conseil_${Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}"
    private fun generateTemplateId(): String =
            "template_${Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt(1000)}"
}
